package at.co.svc.tosca.tsu.del;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.io.StringWriter;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class DecodeTCProperties {

    public static void main(String[] args) throws Exception {
        
        // 1. TCProperties (GZIP + Base64 -> XML)
        String rawTCProperties = "H4sIAAAAAAAACsyVTW7CMBCFrxJ5H8hfaSKRsqAURSqoSiL2TjyAJcdO/YPI2brokXqFmhYCi3ZNNpb8NPPe57Fkf318TmfHhjkHkIoKniJ/5CEHeC0I5bsUGb11YzR7mpbzNylakJqCmgvGoNa2/kbvnDVuIEWZyuHdgNLI2WBmrFJKA3ZDFa1Yv80BE8FZl6IXzJQVCtNKUNa7vWo/BmXX2q5CSwt0lgo45y+lMG2KkLM4tkLqUhSmUqAvIeM/+OZCSmD41J2RnhH7OADiVe42qWI3epjEbuLZBQeYAH5MEh+C4Zxh83tbPX0QjgZEtwK9F9fRLhflcNiKeg8Nvg7OCyd+GEVRfIN4jr8bYw5KGFlf3FM03p1M1HCmmBHgmm4pyJ5xOHArG4J3oJbAQWINZKAPUcaxbT3AWhBQPWMOjSqMuPKVKm2zTuU943PHcUPru0GO//mMvgEAAP//AwBVFCy3zgYAAA==";
        String decodedTCProperties = decodeTCProperties(rawTCProperties);
        
        System.out.println("========== TC PROPERTIES (XML) ==========");
        System.out.println(prettyPrintXml(decodedTCProperties));
        System.out.println();

        // 2. ExplicitConnection (Samo Base64 -> JSON)
        String rawExplicitConnection = "IFt7IktleSI6Ik5hbWUiLCJWYWx1ZSI6Ilx1MDAzQ2V4cGxpY2l0XHUwMDNFIn0seyJLZXkiOiJUcmFuc3BvcnRUeXBlIiwiVmFsdWUiOiJIdHRwIn0seyJLZXkiOiJFbmRwb2ludCIsIlZhbHVlIjoiaHR0cDovL2xvY2FsaG9zdCJ9XQ==";
        String decodedExplicitConnection = decodePlainBase64(rawExplicitConnection);
        
        System.out.println("========== EXPLICIT CONNECTION (JSON) ==========");
        System.out.println(decodedExplicitConnection);
        System.out.println();

        // 3. Headers (Samo Base64 -> JSON)
        String rawHeaders = "IFtbeyJLZXkiOiJLZXkiLCJWYWx1ZSI6IlgtU1ZDLUNMSUVOVC1JUCJ9LHsiS2V5IjoiVmFsdWUiLCJWYWx1ZSI6IiJ9XV0=";
        String decodedHeaders = decodePlainBase64(rawHeaders);
        
        System.out.println("========== HEADERS (JSON) ==========");
        System.out.println(decodedHeaders);
        System.out.println();
    }

    // Za TCProperties koji je kompresovan sa GZIP-om
    private static String decodeTCProperties(String value) {
        try {
            byte[] compressed = Base64.getDecoder().decode(value.trim());
            StringBuilder sb = new StringBuilder();

            try (GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                 InputStreamReader reader = new InputStreamReader(gis, StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(reader)) {

                String line;
                // Čitamo liniju po liniju unutar try bloka
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } catch (java.util.zip.ZipException ze) {
                // Ako je string skraćen, hvatamo ZipException ovde
                System.err.println("[UPOZORENJE] Base64 string je verovatno odsečen/nepotpun! Prikazujem delimične podatke...");
            }
            
            return sb.toString();
        } catch (Exception e) {
            return "Greška pri dekodovanju: " + e.getMessage();
        }
    }
    
    // Za Headers i ExplicitConnection koji su običan Base64 string (JSON tekst)
    private static String decodePlainBase64(String value) {
        byte[] decodedBytes = Base64.getDecoder().decode(value.trim());
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    private static String prettyPrintXml(String xml) {
        try {
            xml = xml.replace("\uFEFF", "").trim();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xml)));

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");

            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            return xml;
        }
    }
}