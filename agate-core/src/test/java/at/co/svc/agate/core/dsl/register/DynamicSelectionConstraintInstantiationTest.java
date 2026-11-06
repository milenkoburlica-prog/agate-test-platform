package at.co.svc.agate.core.dsl.register;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.*;

class DynamicSelectionConstraintInstantiationTest {
    private static void invoke(YamlTestInstantiator inst, String name, Object yaml,
            Map<Map<String,Object>, Boolean> tracked) throws Exception {
        Method m = YamlTestInstantiator.class.getDeclaredMethod(name, Object.class, Map.class);
        m.setAccessible(true);
        m.invoke(inst, yaml, tracked);
    }

    @Test void preservesLiteralNullButOmitsNullFromDynamicCsvSelection() throws Exception {
        Map<String,Object> optional = new LinkedHashMap<>();
        optional.put("path", "//svNummer"); optional.put("action", "EQUALS");
        optional.put("expected", "{XL[SVPerson_Mitversicherter_response.SVNR2]}");
        Map<String,Object> explicit = new LinkedHashMap<>();
        explicit.put("path", "//other"); explicit.put("action", "EQUALS");
        explicit.put("expected", "{NULL}");
        List<Object> constraints = new ArrayList<>(List.of(optional, explicit));
        Map<String,Object> step = new LinkedHashMap<>();
        step.put("type", "SOAP"); step.put("op", "ASSERT"); step.put("constraints", constraints);
        Map<Map<String,Object>,Boolean> tracked = new IdentityHashMap<>();
        YamlTestInstantiator inst = new YamlTestInstantiator();
        invoke(inst, "collectDynamicSelectionConstraints", step, tracked);
        assertEquals(1, tracked.size());
        optional.put("expected", "{NULL}");
        invoke(inst, "dropUnspecifiedSelectionConstraints", step, tracked);
        assertEquals(1, constraints.size());
        assertSame(explicit, constraints.get(0));
    }

    @Test void preservesConcreteCsvSelection() throws Exception {
        Map<String,Object> optional = new LinkedHashMap<>();
        optional.put("expected", "{XL[SVNR2]}");
        Map<String,Object> step = new LinkedHashMap<>();
        step.put("type", "SOAP"); step.put("op", "ASSERT");
        step.put("constraints", new ArrayList<>(List.of(optional)));
        Map<Map<String,Object>,Boolean> tracked = new IdentityHashMap<>();
        YamlTestInstantiator inst = new YamlTestInstantiator();
        invoke(inst, "collectDynamicSelectionConstraints", step, tracked);
        optional.put("expected", "4561047149");
        invoke(inst, "dropUnspecifiedSelectionConstraints", step, tracked);
        assertEquals(1, ((List<?>)step.get("constraints")).size());
    }
}
