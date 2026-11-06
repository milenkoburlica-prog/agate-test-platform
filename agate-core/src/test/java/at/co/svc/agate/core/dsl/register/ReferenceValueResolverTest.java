package at.co.svc.agate.core.dsl.register;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ReferenceValueResolverTest {
    @TempDir Path root;

    private ReferenceValueResolver resolver() throws Exception {
        Path dir = Files.createDirectories(root.resolve("template/references"));
        Files.writeString(dir.resolve("PROP_3_KonsultationList.csv"),
                "Variable;2;FOO;0002\nleistungsSvtCode;01;07;14\ninfo;Hallo;\"Text; mit Semikolon\";Test\n");
        return new ReferenceValueResolver(root);
    }

    @Test void fixedNumericAndStringId() throws Exception {
        var r = resolver();
        assertEquals("01", r.resolve("!PROP_3_KonsultationList[2].leistungsSvtCode", "TCF001"));
        assertEquals("07", r.resolve("!PROP_3_KonsultationList[FOO].leistungsSvtCode", "TCF002"));
        assertEquals("14", r.resolve("!PROP_3_KonsultationList[0002].leistungsSvtCode", "TCF003"));
        assertEquals("Text; mit Semikolon", r.resolve("!PROP_3_KonsultationList[FOO].info", "TCF004"));
    }

    @Test void dynamicIdAfterXlReplacement() throws Exception {
        var r = resolver();
        Map<String, Object> step = new LinkedHashMap<>();
        step.put("value", "!PROP_3_KonsultationList[FOO].leistungsSvtCode");
        step.put("other", "{B[token]}");
        var test = new LinkedHashMap<String, Object>();
        test.put("steps", List.of(step));
        r.resolveTree(test, "TCF005");
        assertEquals("07", step.get("value"));
        assertEquals("{B[token]}", step.get("other"));
    }

    @Test void missingAndDuplicateAreErrors() throws Exception {
        var r = resolver();
        assertThrows(IllegalArgumentException.class,
                () -> r.resolve("!PROP_3_KonsultationList[ABSENT].leistungsSvtCode", "TCF006"));
        assertThrows(IllegalArgumentException.class,
                () -> r.resolve("!PROP_3_KonsultationList[FOO].absent", "TCF006"));
        assertThrows(IllegalArgumentException.class,
                () -> r.resolve("!PROP_3_KonsultationList[{XL[id]}].leistungsSvtCode", "TCF006"));
        Files.writeString(root.resolve("template/references/Duplicate.csv"), "Variable;FOO;FOO\ncode;01;07\n");
        assertThrows(IllegalArgumentException.class,
                () -> r.resolve("!Duplicate[FOO].code", "TCF006"));
    }

    @Test void existingListSyntaxUnchanged() throws Exception {
        var r = resolver();
        assertEquals("@PROP_3_KonsultationList.konsultationen[2]",
                r.resolve("@PROP_3_KonsultationList.konsultationen[2]", "TCF007"));
    }
}
