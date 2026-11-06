package at.co.svc.agate.core.dsl.register;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NullListReferenceNormalizerTest {
    @Test void nullIndicesAreNullLists() {
        assertEquals("{NULL}", NullListReferenceNormalizer.normalize(
                "@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[{NULL}]"));
        assertEquals("{NULL}", NullListReferenceNormalizer.normalize(
                "@PROP_3_GruppenKommentar.gruppenkommentar[NULL]"));
    }

    @Test void realIndicesAndOtherValuesStayUntouched() {
        assertEquals("@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[2]",
                NullListReferenceNormalizer.normalize(
                        "@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[2]"));
        assertEquals("!PROP_3_KonsultationList[FOO].leistungsSvtCode",
                NullListReferenceNormalizer.normalize(
                        "!PROP_3_KonsultationList[FOO].leistungsSvtCode"));
    }

    @Test void nestedYamlParameterValuesAreNormalized() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("anamneseRisikogruppen", "@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[{NULL}]");
        params.put("gruppenkommentar", "@PROP_3_GruppenKommentar.gruppenkommentar[{NULL}]");
        params.put("valid", "@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[2]");
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("parameters", params);
        Map<String, Object> tc = new LinkedHashMap<>();
        tc.put("steps", List.of(step));
        NullListReferenceNormalizer.normalizeTree(tc);
        assertEquals("{NULL}", params.get("anamneseRisikogruppen"));
        assertEquals("{NULL}", params.get("gruppenkommentar"));
        assertEquals("@PROP_3_AnamneseRisikogruppe.anamneseRisikogruppen[2]", params.get("valid"));
    }
}
