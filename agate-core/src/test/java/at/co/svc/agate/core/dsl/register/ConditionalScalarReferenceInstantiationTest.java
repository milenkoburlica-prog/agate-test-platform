package at.co.svc.agate.core.dsl.register;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/** Regression: filter conditional steps before resolving !File[ID].field. */
class ConditionalScalarReferenceInstantiationTest {
    @TempDir Path root;

    @SuppressWarnings("unchecked")
    private Map<String, Object> filterAndResolve(String index) throws Exception {
        Path dir = Files.createDirectories(root.resolve("template/references"));
        Files.writeString(dir.resolve("PROP_3_KonsultationList.csv"),
                "Variable;7;FOO\nleistungsSvtCode;07;17\n");

        Map<String, Object> conditionalCall = new LinkedHashMap<>();
        conditionalCall.put("type", "CALL");
        conditionalCall.put("command", "reusable.KSE_20_doKonsultation");
        conditionalCall.put("condition", index.isBlank() ? "{NULL} != NULL" : index + " != NULL");
        conditionalCall.put("parameters", new LinkedHashMap<>(Map.of("svtCode",
                "!PROP_3_KonsultationList[" + (index.isBlank() ? "{NULL}" : index)
                        + "].leistungsSvtCode")));

        Map<String, Object> unconditionalCall = new LinkedHashMap<>();
        unconditionalCall.put("type", "CALL");
        unconditionalCall.put("command", "reusable.ru_dialog_aufbau_mit_ordno");
        List<Map<String, Object>> steps = new ArrayList<>(List.of(conditionalCall, unconditionalCall));
        Map<String, Object> testCase = new LinkedHashMap<>();
        testCase.put("id", "FF DialogId not existing");
        testCase.put("steps", steps);

        Class<?> tcClass = Class.forName(YamlTestInstantiator.class.getName() + "$TCObject");
        Constructor<?> ctor = tcClass.getDeclaredConstructor(String.class);
        ctor.setAccessible(true);
        Object tc = ctor.newInstance("FF DialogId not existing");
        YamlTestInstantiator inst = new YamlTestInstantiator();
        Method filter = YamlTestInstantiator.class.getDeclaredMethod(
                "processStepsAfterInstanciation", Map.class, tcClass, Set.class);
        filter.setAccessible(true);
        filter.invoke(inst, testCase, tc, new TreeSet<String>());
        new ReferenceValueResolver(root).resolveTree(testCase, "FF DialogId not existing");
        return testCase;
    }

    @Test void emptyCsvIndexDoesNotResolveReferenceInSkippedCall() throws Exception {
        var result = filterAndResolve("");
        var steps = (List<?>) result.get("steps");
        assertEquals(1, steps.size());
        assertEquals("reusable.ru_dialog_aufbau_mit_ordno",
                ((Map<?, ?>) steps.get(0)).get("command"));
    }

    @Test void populatedCsvIndexResolvesReferenceInRetainedCall() throws Exception {
        var result = filterAndResolve("7");
        var steps = (List<?>) result.get("steps");
        assertEquals(2, steps.size());
        var retained = (Map<?, ?>) steps.get(0);
        assertFalse(retained.containsKey("condition"));
        assertEquals("07", ((Map<?, ?>) retained.get("parameters")).get("svtCode"));
    }
}
