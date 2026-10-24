package at.co.svc.agate.ai;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class XpathFixVerifierTest {
    private final XpathFixVerifier verifier = new XpathFixVerifier();
    private static final String XML = "<soap:Envelope xmlns:soap=\"urn:soap\"><soap:Body><return><anspruchsDaten><kostenanteilbefreit>false</kostenanteilbefreit><svtCode>11</svtCode></anspruchsDaten><versichertenDaten><svNummer>123</svNummer></versichertenDaten></return></soap:Body></soap:Envelope>";
    private static final String MAIN = "/*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='return']/*[local-name()='anspruchsDaten']/*[local-name()='kostenanteilbefreit']";
    private static final String WRONG = "/*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='return']/*[local-name()='versichertenDaten']/*[local-name()='svtCode']";
    private static final String RIGHT = "/*[local-name()='Envelope']/*[local-name()='Body']/*[local-name()='return']/*[local-name()='anspruchsDaten']/*[local-name()='svtCode']";
    private XpathFixVerifier.CaseContext context() {return new XpathFixVerifier.CaseContext(XML,"failed",MAIN,"false",WRONG,"11");}
    @Test void korrigierterConstraintBesteht() { assertTrue(verifier.verify(context(),"CONSTRAINT_XPATH",RIGHT,"11").accepted()); }
    @Test void falscheKorrekturWirdAbgelehnt() { assertFalse(verifier.verify(context(),"CONSTRAINT_XPATH",WRONG,"11").accepted()); }
    @Test void hauptXPathOhneFehlerWirdNichtErsetzt() { assertFalse(verifier.verify(context(),"HAUPT_XPATH",RIGHT,"11").accepted()); }
    @Test void erwarteterWertDarfNichtGeaendertWerden() { assertFalse(verifier.verify(context(),"CONSTRAINT_XPATH",RIGHT,"12").accepted()); }
    @Test void nichtGefundenerVorschlagWirdAbgelehnt() { assertFalse(verifier.verify(context(),"CONSTRAINT_XPATH",RIGHT.replace("svtCode","xyz"),"11").accepted()); }
}
