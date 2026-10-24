package at.co.svc.aga.transformator.utils;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

class ProcessTBoxEvaluationEqualityTest {
    private Object parsed(String value) throws Exception {
        Method method = ProcessTBoxEvaluationRuntime.class.getDeclaredMethod("parseEquality", String.class);
        method.setAccessible(true);
        return method.invoke(null, value);
    }
    private String part(Object parsed, String part) throws Exception {
        Method method = parsed.getClass().getDeclaredMethod(part);
        method.setAccessible(true);
        return (String) method.invoke(parsed);
    }
    @Test void acceptsToscaSingleEquals() throws Exception {
        Object p = parsed("'{B[BatchStatus]}'='0 STARTED'");
        assertNotNull(p);
        assertEquals("{B[BatchStatus]}", part(p, "left"));
        assertEquals("0 STARTED", part(p, "right"));
    }
    @Test void acceptsAgateDoubleEquals() throws Exception {
        assertNotNull(parsed("'{B[BatchStatus]}'=='0 STARTED'"));
    }
    @Test void ignoresOperatorInsideQuotes() throws Exception {
        Object p = parsed("'{B[A=B]}'='0 STARTED'");
        assertNotNull(p);
        assertEquals("{B[A=B]}", part(p,"left"));
    }
    @Test void doesNotAcceptInequalityAsEquality() throws Exception {
        assertNull(parsed("'{B[BatchStatus]}'!='0 STARTED'"));
    }
}
