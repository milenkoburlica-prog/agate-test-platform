package at.co.svc.agate.ai;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.*;
import java.util.regex.*;

/** Correlates the failed AGATE SOAP ASSERT with its actual response, never a previous unrelated XML response. */
public final class SoapResponseCorrelation {
    private static final Pattern FAILED = Pattern.compile("(?m)^\\s*>>> SOAP STEP FINISHED \\| Status: FAILED.*$");
    private static final Pattern PATH = Pattern.compile("(?m)^\\s*>>> DSL\\s+path:\\s*(.+)$");
    private static final Pattern RESPONSE = Pattern.compile("(?m)^\\s*>>> DSL\\s+response:\\s*(.+)$");
    private static final Pattern BODY = Pattern.compile("(?m)^\\s*<<< BODY\\s*:\\s?(.*)$");
    private static final Pattern REASON = Pattern.compile("(?m)^\\s*Reason\\s*:\\s*(.+)$");
    public record Finding(String path, String response, String reason, int hits, String sample,
                          String explanation, String action, String status) {}

    public Optional<Finding> inspect(String log) {
        if (log == null || !log.contains("SOAP XPath was not found")) return Optional.empty();
        Matcher fail=FAILED.matcher(log);
        int at=-1;
        while (fail.find()) {
            int dsl=log.lastIndexOf(">>> DSL      - type: SOAP",fail.start());
            if(dsl>=0 && log.substring(dsl,fail.start()).contains("op: ASSERT")) {at=fail.start();break;}
        }
        if(at<0) return Optional.empty();
        int start=log.lastIndexOf(">>> DSL      - type: SOAP",at);
        String failedBlock=log.substring(start,at);
        if(!failedBlock.contains("source: BODY")) return Optional.empty();
        Matcher path=PATH.matcher(failedBlock), response=RESPONSE.matcher(failedBlock);
        if(!path.find() || !response.find()) return Optional.empty();
        String xpath=path.group(1).trim(), responseName=response.group(1).trim();
        Matcher reason=REASON.matcher(log.substring(at,Math.min(log.length(),at+1200)));
        String error=reason.find()?reason.group(1).trim():"SOAP XPath was not found";
        // Match only the most recent SOAP EXEC producing this response, before the failing assertion.
        String prefix=log.substring(0,start);
        Matcher exec=Pattern.compile("(?m)^\\s*>>> DSL\\s+- type: SOAP\\s*$").matcher(prefix);
        int producer=-1;
        while(exec.find()) {
            int end=prefix.indexOf("SOAP STEP FINISHED",exec.start());
            if(end<0)continue;
            String block=prefix.substring(exec.start(),end);
            if(block.contains("op: EXEC") && block.contains("response: "+responseName))producer=exec.start();
        }
        if(producer<0)return Optional.of(new Finding(xpath,responseName,error,-1,"",
            "Die fehlgeschlagene SOAP-Assertion ist bekannt, aber der zugehörige SOAP-EXEC mit derselben Response konnte nicht eindeutig zugeordnet werden.",
            "SOAP EXEC und den Namen der Response im Protokoll prüfen.","SOAP_RESPONSE_FEHLT"));
        int execEnd=prefix.indexOf("SOAP STEP FINISHED",producer);
        String execBlock=prefix.substring(producer,execEnd);
        String xml=extractLastXml(execBlock);
        if(xml==null)return Optional.of(new Finding(xpath,responseName,error,-1,"",
           "Die Response wurde zugeordnet, enthält aber keine vollständig lesbare SOAP-XML-Antwort im Protokoll.",
           "Mit --verbose erneut protokollieren und den vollständigen SOAP-Response überprüfen.","SOAP_RESPONSE_FEHLT"));
        try {
            DocumentBuilderFactory f=DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
            f.setFeature("http://xml.org/sax/features/external-general-entities",false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING,true);
            f.setXIncludeAware(false);f.setExpandEntityReferences(false);
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD,"");f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA,"");
            Document doc=f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            NodeList nodes=(NodeList)XPathFactory.newInstance().newXPath().evaluate(xpath,doc,XPathConstants.NODESET);
            int hits=nodes.getLength();
            String sample=hits>0?nodes.item(0).getTextContent().trim():"";
            if(hits>0) return Optional.of(new Finding(xpath,responseName,error,hits,sample,
                "Der angegebene XPath findet im tatsächlich zugeordneten SOAP-Response "+hits+" Knoten. Die AGATE-Meldung 'SOAP XPath was not found' ist deshalb nicht durch einen fehlenden XML-Knoten erklärt. Mögliche Ursachen sind die interne AGATE-Auswertung, Filter-/Constraint-Semantik oder die Verarbeitung des Response-Kontexts.",
                "AGATE SoapEngine, XPath-/Constraint-Auswertung und die ursprüngliche Fehlermeldung prüfen; XPath nicht blind ändern.","URSACHE_EINGEGRENZT"));
            return Optional.of(new Finding(xpath,responseName,error,0,"",
                "Im zugeordneten SOAP-Response findet dieser XPath keinen Knoten (0 Treffer). Prüfen, ob fotoInformation fehlt, der Response einen SOAP-Fault enthält oder die XML-Struktur abweicht.",
                "Originalen SOAP-Response mit dem XPath vergleichen, Service-Testdaten prüfen und nur einen durch XML belegten Pfad vorschlagen.","URSACHE_EINGEGRENZT"));
        }catch(Exception ex){return Optional.of(new Finding(xpath,responseName,error,-1,"",
             "SOAP-Response oder XPath konnte lokal nicht ausgewertet werden; der Grund ist noch offen.",
             "Originale SOAP-Antwort und XPath-Syntax prüfen.","NICHT_PRÜFBAR"));}
    }
    private static String extractLastXml(String block) {
        Matcher m=BODY.matcher(block);StringBuilder xml=new StringBuilder();boolean collecting=false;
        while(m.find()) {
            String line=m.group(1).trim();
            if(!collecting && line.matches("<(?:[\\w-]+:)?Envelope(?:\\s[^>]*)?>.*")){ collecting=true;xml.setLength(0); }
            if(collecting)xml.append(line).append('\n');
            if(collecting && line.matches(".*</(?:[\\w-]+:)?Envelope\\s*>.*"))return xml.toString();
        }
        return null;
    }
    public DiagnosticResult diagnose(Finding f) {
        String report="ENGINE: SOAP\nPROBLEMTYP: SOAP_XPATH_NOT_FOUND\nFEHLERSTELLE: SOAP ASSERT (Response: "+f.response()+")\n"+
            "AGATE-FEHLER: "+f.reason()+"\nXPATH: "+f.path()+"\nLOKALE SOAP-RESPONSE-PRÜFUNG: "+
            (f.hits()<0?"Nicht möglich":f.hits()+" Treffer")+"\n"+
            (f.hits()>0?"ELEMENT VORHANDEN (Wert im lokalen XML gefunden).\n":"")+
            "BEFUND: "+f.explanation()+"\nKONKRETER NÄCHSTER SCHRITT: "+f.action()+"\n"+
            "VERIFIKATION: Keine Korrektur durch AGATE-Testlauf bestätigt.";
        return new DiagnosticResult("JAVA",report,false,f.status());
    }
}
