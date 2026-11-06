package at.co.svc.agate.core.runner;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class VerboseCliRegressionTest {
    @AfterEach
    void cleanUp() {
        System.clearProperty("agate.call.verbose");
    }

    @Test
    void verboseIsRemovedFromPositionalArguments() throws Exception {
        System.clearProperty("agate.call.verbose");
        Method method = Main.class.getDeclaredMethod("parseProjectArguments", String[].class);
        method.setAccessible(true);
        Object parsed = method.invoke(null, (Object) new String[]{"Milenko", "ECS_SYST_AUT1", "DMP.11.all", "suite.yaml", "--verbose"});
        Method argsMethod = parsed.getClass().getDeclaredMethod("remainingArgs");
        argsMethod.setAccessible(true);
        String[] remaining = (String[]) argsMethod.invoke(parsed);
        assertArrayEquals(new String[]{"Milenko", "ECS_SYST_AUT1", "DMP.11.all", "suite.yaml"}, remaining);
        assertEquals("true", System.getProperty("agate.call.verbose"));
    }
}
