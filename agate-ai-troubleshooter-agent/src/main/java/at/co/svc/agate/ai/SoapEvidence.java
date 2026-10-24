package at.co.svc.agate.ai;

import jakarta.enterprise.context.ApplicationScoped;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Read-only, deterministic evidence extracted from an AGATE SOAP execution log. */
@ApplicationScoped
public class SoapEvidence {
    private static final Pattern DSL_PATH = Pattern.compile("(?m)^\\s*>>> DSL\\s+(?:- )?path:\\s*(.+?)\\s*$");
    private static final Pattern COMPACT_PATH = Pattern.compile("(?m)^>>> SOAP ASSERT path:\\s*(.+?)\\s*$");
    private static final Pattern COMPACT_CONSTRAINT = Pattern.compile("(?m)^>>> constraints: path:\\s*(.+?)(?:;|$)");

    public String inspect(String log) {
        String xml = extractSoapResponse(log);
        if (xml == null) return "SOAP_PRÜFUNG: NICHT_VERFÜGBAR (vollständige SOAP-Antwort fehlt). XPath-Ausführung nicht behaupten.";
        try {
            var f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            Document doc = f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            var paths = collectPaths(log);
            StringBuilder sb = new StringBuilder("SOAP_PRÜFUNG: XML_GELESEN; XPath-Auswertung ab DOKUMENTWURZEL (nicht ab SOAP-Body).\n");
            if (paths.isEmpty()) sb.append("Keine XPath-Ausdrücke aus dem fehlgeschlagenen ASSERT erkannt.\n");
            var xpath = XPathFactory.newInstance().newXPath();
            for (PathEvidence p : paths) {
                sb.append(p.kind).append(": ").append(p.path).append("\n");
                try {
                    NodeList nodes = (NodeList) xpath.evaluate(p.path, doc, XPathConstants.NODESET);
                    sb.append("  treffer=").append(nodes.getLength());
                    if (nodes.getLength() > 0) {
                        String value = nodes.item(0).getTextContent();
                        // Do not echo source text or secrets to the LLM a second time.
                        sb.append("; textlaenge_erster_treffer=").append(value == null ? 0 : value.length());
                    }
                    sb.append("\n");
                } catch (Exception ex) {
                    sb.append("  UNGÜLTIGER_XPATH (Auswertung fehlgeschlagen)\n");
                }
            }
            if (log.contains("SOAP ASSERT SUCCESS") && log.contains("local-name()='Envelope'")) {
                sb.append("GEGENBEWEIS: Ein vorheriger Envelope/Body-XPath-ASSERT war im selben Lauf erfolgreich. ")
                  .append("Aus source=BODY allein folgt kein Verbot des Envelope/Body-Präfixes.\n");
            }
            sb.append("EINSCHRÄNKUNG: XPath-Treffer belegen weder die AGATE-Constraint-Semantik noch die Fehlerursache.\n");
            return sb.toString();
        } catch (Exception ex) {
            return "SOAP_PRÜFUNG: NICHT_VERFÜGBAR (XML konnte nicht gelesen werden). Keine XPath-Verifizierung behaupten.";
        }
    }

    private record PathEvidence(String kind, String path) {}
    private List<PathEvidence> collectPaths(String log) {
        List<PathEvidence> result = new ArrayList<>();
        // Detailed execution logs: restrict extraction to final failed SOAP ASSERT block.
        int failed = log.lastIndexOf("SOAP STEP FINISHED | Status: FAILED");
        if (failed >= 0) {
            int start = log.lastIndexOf(">>> DSL      - type: SOAP", failed);
            if (start >= 0) {
                String block = log.substring(start, failed);
                Matcher m = DSL_PATH.matcher(block);
                int i = 0;
                while (m.find()) result.add(new PathEvidence(i++ == 0 ? "HAUPT_XPATH" : "CONSTRAINT_XPATH", m.group(1).trim()));
            }
        }
        if (result.isEmpty()) {
            Matcher m = COMPACT_PATH.matcher(log);
            if (m.find()) result.add(new PathEvidence("HAUPT_XPATH", m.group(1).trim()));
            Matcher c = COMPACT_CONSTRAINT.matcher(log);
            while (c.find()) result.add(new PathEvidence("CONSTRAINT_XPATH", c.group(1).trim()));
        }
        return result;
    }

    private String extractSoapResponse(String log) {
        // Detailed AGATE logs have <<< BODY : prefixes on each line.
        StringBuilder sb = new StringBuilder();
        boolean inXml = false;
        for (String line : log.split("\\R")) {
            int idx = line.indexOf("<<< BODY");
            if (idx < 0) continue;
            int colon = line.indexOf(':', idx);
            if (colon < 0) continue;
            String part = line.substring(colon + 1).trim();
            if (!inXml && part.matches("<[^>]*Envelope(?:\\s[^>]*)?>.*")) inXml = true;
            if (inXml) sb.append(part).append('\n');
            if (inXml && part.matches(".*</(?:\\w+:)?Envelope\\s*>.*")) return sb.toString();
        }
        // Compact synthetic example: XML is on one line after "<<< SOAP response:".
        Matcher m = Pattern.compile("(?m)^<(?:(?:[A-Za-z0-9_]+):)?Envelope\\b[^\\r\\n]*</(?:(?:[A-Za-z0-9_]+):)?Envelope>").matcher(log);
        return m.find() ? m.group() : null;
    }
}
