package at.co.svc.agate.engine.soap;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SoapNullListPreprocessorTest {
    private static final String XML = "<root>"
            + "<!-- AGATE:LIST anamneseRisikogruppen -->"
            + "<!-- AGATE:ITEM --><outer>"
            + "<!-- AGATE:LIST risikofaktorCode -->"
            + "<!-- AGATE:ITEM --><inner>{B[risikofaktorCode]}</inner>"
            + "<!-- AGATE:ENDITEM --><!-- AGATE:ENDLIST -->"
            + "<code>{B[risikogruppeCode]}</code></outer>"
            + "<!-- AGATE:ENDITEM --><!-- AGATE:ENDLIST -->"
            + "<version>{B[version]}</version></root>";

    @Test void nullOuterRemovesWholeNestedBlock() {
        String result = SoapNullListPreprocessor.removeNullLists(XML,
                Map.of("anamneseRisikogruppen", "{NULL}", "version", "5"));
        assertEquals("<root><version>{B[version]}</version></root>", result);
    }
    @Test void nullInnerRemovesOnlyInnerBlock() {
        String result = SoapNullListPreprocessor.removeNullLists(XML,
                Map.of("anamneseRisikogruppen", "USED", "risikofaktorCode", "{NULL}"));
        assertFalse(result.contains("{B[risikofaktorCode]}"));
        assertTrue(result.contains("{B[risikogruppeCode]}"));
    }
    @Test void nonNullListRemainsUntouched() {
        assertEquals(XML, SoapNullListPreprocessor.removeNullLists(XML,
                Map.of("anamneseRisikogruppen", "2")));
    }
    @Test void unclosedMarkersFailClearly() {
        assertThrows(IllegalArgumentException.class, () ->
                SoapNullListPreprocessor.removeNullLists("<!-- AGATE:LIST a -->",
                        Map.of("a", "{NULL}")));
    }

    @Test void actualCreateBefundMarkersPreserveVersionAndRemoveRiskBuffers() {
        String template = "<prop:anamneseData>\n"
                + "<!-- AGATE:LIST anamneseRisikogruppen -->\n"
                + "<!-- AGATE:ITEM -->\n"
                + "<prop:anamneseRisikogruppen>\n"
                + "<!-- AGATE:LIST risikofaktorCode -->\n"
                + "<!-- AGATE:ITEM -->\n"
                + "<prop:risikofaktorCode>{B[risikofaktorCode]}</prop:risikofaktorCode>\n"
                + "<!-- AGATE:ENDITEM -->\n"
                + "<!-- AGATE:ENDLIST -->\n"
                + "<prop:risikogruppeCode>{B[risikogruppeCode]}</prop:risikogruppeCode>\n"
                + "</prop:anamneseRisikogruppen>\n"
                + "<!-- AGATE:ENDITEM -->\n"
                + "<!-- AGATE:ENDLIST -->\n"
                + "<prop:version>{B[version]}</prop:version>\n"
                + "</prop:anamneseData>";
        String actual = SoapNullListPreprocessor.removeNullLists(template,
                Map.of("anamneseRisikogruppen", "{NULL}", "version", "5"));
        assertFalse(actual.contains("risikofaktorCode"));
        assertFalse(actual.contains("risikogruppeCode"));
        assertFalse(actual.contains("AGATE:LIST"));
        assertTrue(actual.contains("<prop:version>{B[version]}</prop:version>"));
    }
}
