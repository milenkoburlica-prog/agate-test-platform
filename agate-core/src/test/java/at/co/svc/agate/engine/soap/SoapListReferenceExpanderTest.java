package at.co.svc.agate.engine.soap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SoapListReferenceExpanderTest {
    @TempDir Path references;

    private String template() {
        return "<xml><!-- AGATE:LIST anamneseRisikogruppen --><!-- AGATE:ITEM -->"
                + "<group><!-- AGATE:LIST risikofaktorCode --><!-- AGATE:ITEM -->"
                + "<code>{B[risikofaktorCode]}</code><!-- AGATE:ENDITEM -->"
                + "<!-- AGATE:ENDLIST --><type>{B[risikogruppeCode]}</type></group>"
                + "<!-- AGATE:ENDITEM --><!-- AGATE:ENDLIST --></xml>";
    }

    private void reference() throws Exception {
        Files.writeString(references.resolve("PROP_3_AnamneseRisikogruppe.csv"),
                "Variable;1;12\nxlsRisikofaktorCode;RF_LEI;RF_ASA\nxlsRisikogruppeCode;RG_EIN;RG_ZWEI\n");
    }

    @Test void expandsOneRecordWithNestedCode() throws Exception {
        reference();
        String result = SoapListReferenceExpander.expand(template(),
                Map.of("anamneseRisikogruppen", "@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[1]"), references);
        assertTrue(result.contains("<code>RF_LEI</code>"));
        assertTrue(result.contains("<type>RG_EIN</type>"));
        assertFalse(result.contains("AGATE:LIST"));
        assertFalse(result.contains("{B["));
    }

    @Test void expandsMultipleRecords() throws Exception {
        reference();
        String result = SoapListReferenceExpander.expand(template(),
                Map.of("anamneseRisikogruppen", "@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[1;12]"), references);
        assertEquals(2, result.split("<group>", -1).length - 1);
        assertTrue(result.contains("<code>RF_ASA</code>"));
    }

    @Test void mergesSameGroupAsNestedFactorElements() throws Exception {
        Files.writeString(references.resolve("PROP_3_AnamneseRisikogruppe.csv"),
                "Variable;1;4\nxlsRisikofaktorCode;RF_LEI;RF_SEI\nxlsRisikogruppeCode;RG_EIN;RG_EIN\n");
        String result = SoapListReferenceExpander.expand(template(),
                Map.of("anamneseRisikogruppen", "@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[1;4]"), references);
        assertEquals(1, result.split("<group>", -1).length - 1);
        assertEquals(2, result.split("<code>", -1).length - 1);
        assertEquals(1, result.split("<type>RG_EIN</type>", -1).length - 1);
        assertTrue(result.indexOf("RF_LEI") < result.indexOf("RF_SEI"));
    }

    @Test void explicitNonePreservesDuplicateGroupsForNegativeCases() throws Exception {
        Files.writeString(references.resolve("PROP_3_AnamneseRisikogruppe.csv"),
                "Variable;1;4\nxlsRisikofaktorCode;RF_LEI;RF_SEI\nxlsRisikogruppeCode;RG_EIN;RG_EIN\n");
        String result = SoapListReferenceExpander.expand(template(),
                Map.of("anamneseRisikogruppen", "@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[1;4]",
                       "anamneseRisikogruppen.grouping", "NONE"), references);
        assertEquals(2, result.split("<group>", -1).length - 1);
    }

    @Test void unchangedMissingParameter() throws Exception {
        assertEquals(template(), SoapListReferenceExpander.expand(template(), Map.of("other", "value"), references));
    }

    @Test void nullListIsRemovedByExistingPreprocessor() throws Exception {
        String without = SoapNullListPreprocessor.removeNullLists(template(), Map.of("anamneseRisikogruppen", "{NULL}"));
        assertFalse(without.contains("<group>"));
    }

    @Test void missingCsvIsErrorNotMissingBuffer() {
        assertThrows(java.io.IOException.class, () -> SoapListReferenceExpander.expand(template(),
                Map.of("anamneseRisikogruppen", "@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[1]"), references));
    }
}
