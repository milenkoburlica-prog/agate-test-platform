package at.co.svc.aga.transformator.utils;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class YamlConditionLineNormalizerTest {
    @Test void preservesConditionAndProducesValidYaml() {
        String raw = "        condition: \"(Antwort.EingabeStatus != \"\")\"";
        Map<?,?> parsed = new Yaml().load(YamlConditionLineNormalizer.normalize(raw));
        assertEquals("(Antwort.EingabeStatus != \"\")", parsed.get("condition"));
    }
    @Test void doesNotDoubleEscapeValidYaml() {
        String valid = "        condition: \"(Antwort.EingabeStatus != \\\"\\\")\"";
        assertEquals(valid, YamlConditionLineNormalizer.normalize(valid));
        new Yaml().load(valid);
    }
    @Test void leavesOtherFieldsAlone() {
        String value = "        expected: \"abc\"";
        assertEquals(value, YamlConditionLineNormalizer.normalize(value));
    }
    @Test void fixesCapturedReusableDocumentWithMultipleConditions() {
        String raw = "steps:\n  - type: SOAP\n    condition: \"(Antwort.EingabeStatus != \"\")\"\n"
                + "  - type: SOAP\n    condition: \"(EmailAdresse != \"\")\"\n";
        String corrected = YamlConditionLineNormalizer.normalizeDocument(raw);
        Map<?,?> parsed = new Yaml().load(corrected);
        assertEquals(2, ((java.util.List<?>) parsed.get("steps")).size());
        assertEquals(corrected, YamlConditionLineNormalizer.normalizeDocument(corrected));
    }
}
