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

/** Lokale, schreibgeschützte XML-Prüfung. Kein AGATE-Testlauf. */
@ApplicationScoped
public class XpathFixVerifier {
    private static final Pattern PATH = Pattern.compile("(?m)^\\s*>>> DSL\\s+(?:- )?path:\\s*(.+?)\\s*$");
    private static final Pattern EXPECTED = Pattern.compile("(?m)^\\s*>>> DSL\\s+expected:\\s*(.+?)\\s*$");

    public record CaseContext(String xml, String failedBlock, String mainPath, String mainExpected,
                              String constraintPath, String constraintExpected) {}
    public record CheckResult(boolean accepted, String status, String detail) {}

    public CaseContext readCase(String log) {
        int failed = log.lastIndexOf("SOAP STEP FINISHED | Status: FAILED");
        if (failed < 0) return null;
        int start = log.lastIndexOf(">>> DSL      - type: SOAP", failed);
        if (start < 0) return null;
        String block = log.substring(start, failed);
        List<String> paths = new ArrayList<>();
        Matcher p = PATH.matcher(block);
        while (p.find()) paths.add(p.group(1).strip());
        if (paths.isEmpty()) return null;
        List<String> expected = new ArrayList<>();
        Matcher e = EXPECTED.matcher(block);
        while (e.find()) expected.add(stripQuotes(e.group(1).strip()));
        String xml = extractXml(log.substring(0, failed));
        if (xml == null) return null;
        return new CaseContext(xml, block, paths.get(0), expected.isEmpty() ? null : expected.get(0),
                paths.size() > 1 ? paths.get(1) : null, expected.size() > 1 ? expected.get(1) : null);
    }

    public CheckResult verify(CaseContext c, String target, String proposed, String expected) {
        if (proposed == null || proposed.isBlank() || proposed.length() > 2000) return reject("Ungültige Vorschlagslänge");
        if (!target.equals("CONSTRAINT_XPATH") && !target.equals("HAUPT_XPATH")) return reject("Unbekanntes Korrekturziel");
        String original = target.equals("CONSTRAINT_XPATH") ? c.constraintPath() : c.mainPath();
        String originalExpected = target.equals("CONSTRAINT_XPATH") ? c.constraintExpected() : c.mainExpected();
        if (original == null) return reject("Zielpfad fehlt im fehlgeschlagenen ASSERT");
        if (originalExpected == null || !originalExpected.equals(expected))
            return reject("Sollwert wurde geändert oder fehlt; nur der XPath darf verändert werden");
        if (proposed.equals(original)) return reject("Unveränderter XPath ist keine Korrektur");
        try {
            Document doc = parse(c.xml());
            NodeList before = evaluate(doc, original);
            NodeList after = evaluate(doc, proposed);
            if (before.getLength() > 0) return reject("Originalpfad trifft bereits " + before.getLength() + " Knoten; Ursache nicht belegt");
            if (after.getLength() == 0) return reject("Vorgeschlagener XPath liefert 0 Treffer");
            boolean matched = false;
            for (int i = 0; i < after.getLength(); i++) {
                if (expected.equals(after.item(i).getTextContent().strip())) matched = true;
            }
            if (!matched) return reject("XPath findet Knoten, aber kein Wert entspricht dem erwarteten Sollwert");
            // Zusätzlich garantieren: Haupt-XPath bleibt unverändert und trifft weiterhin seinen Sollwert.
            if (target.equals("CONSTRAINT_XPATH")) {
                NodeList main = evaluate(doc, c.mainPath());
                boolean mainOk = false;
                for (int i = 0; i < main.getLength(); i++)
                    if (c.mainExpected() != null && c.mainExpected().equals(main.item(i).getTextContent().strip())) mainOk = true;
                if (!mainOk) return reject("Haupt-XPath oder dessen Sollwert wird nicht bestätigt");
            }
            return new CheckResult(true, "XPATH_LOKAL_BESTÄTIGT",
                    "Alter XPath: 0 Treffer; neuer XPath: " + after.getLength() +
                    " Treffer; Sollwert=" + expected + " korrekt. Kein AGATE-Testlauf.");
        } catch (Exception ex) {
            return reject("XPath/Antwort konnte nicht sicher ausgewertet werden (Syntax oder XML prüfen)");
        }
    }

    private CheckResult reject(String detail) { return new CheckResult(false, "ABGELEHNT", detail); }
    private NodeList evaluate(Document d, String path) throws Exception {
        return (NodeList) XPathFactory.newInstance().newXPath().evaluate(path, d, XPathConstants.NODESET);
    }
    private Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        f.setFeature("http://xml.org/sax/features/external-general-entities", false);
        f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setXIncludeAware(false); f.setExpandEntityReferences(false);
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }
    private String extractXml(String log) {
        StringBuilder b = new StringBuilder(); boolean started = false;
        for (String line : log.split("\\R")) {
            int idx = line.indexOf("<<< BODY");
            if (idx < 0) continue;
            int colon = line.indexOf(':', idx);
            if (colon < 0) continue;
            String text = line.substring(colon + 1).strip();
            if (!started && text.matches("<(?:(?:[A-Za-z0-9_]+):)?Envelope(?:\\s[^>]*)?>.*")) started = true;
            if (started) b.append(text).append('\n');
            if (started && text.matches(".*</(?:(?:[A-Za-z0-9_]+):)?Envelope\\s*>.*")) return b.toString();
        }
        Matcher m = Pattern.compile("(?s)<(?:(?:[A-Za-z0-9_]+):)?Envelope\\b.*?</(?:(?:[A-Za-z0-9_]+):)?Envelope>").matcher(log);
        return m.find() ? m.group() : null;
    }
    private String stripQuotes(String s) {
        if (s.length() >= 2 && ((s.startsWith("'") && s.endsWith("'")) || (s.startsWith("\"") && s.endsWith("\""))))
            return s.substring(1, s.length()-1);
        return s;
    }
}
