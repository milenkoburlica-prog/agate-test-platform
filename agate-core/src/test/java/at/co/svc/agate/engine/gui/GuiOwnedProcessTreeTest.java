package at.co.svc.agate.engine.gui;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Regression: never leave launch-owned children alive, never stop unrelated processes. */
class GuiOwnedProcessTreeTest {
    private static String javaBin() {
        boolean windows = System.getProperty("os.name").toLowerCase().contains("win");
        return Path.of(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java")
                .toAbsolutePath().toString();
    }
    private static Process run(String mode) throws IOException {
        return new ProcessBuilder(javaBin(), "-cp", System.getProperty("java.class.path"),
                GuiOwnedProcessTreeTest.class.getName(), mode).start();
    }

    /** Run via JUnit, using real OS child processes, not Playwright mocks. */
    @Test void cleansOnlyDriverDescendants() throws Exception {
        Process owner = run("parent");
        Process unrelated = run("sleep");
        try {
            List<GuiOwnedProcessTree.Owned> snapshot = List.of();
            for (int i = 0; i < 80 && snapshot.isEmpty(); i++) {
                Thread.sleep(50);
                snapshot = GuiOwnedProcessTree.capture(owner);
            }
            assertEquals(false, snapshot.isEmpty(), "test parent must spawn a living child");
            long childPid = snapshot.get(0).handle().pid();
            GuiOwnedProcessTree.Result result = GuiOwnedProcessTree.stopDescendants(snapshot, "unit", 1000);
            assertEquals(true, result.clean(), "AGATE-owned child should be stopped: " + result.survivors());
            assertEquals(false, ProcessHandle.of(childPid).map(ProcessHandle::isAlive).orElse(false));
            assertEquals(true, unrelated.isAlive(), "an unrelated process must never be stopped");
        } finally {
            owner.descendants().forEach(ProcessHandle::destroyForcibly);
            owner.destroyForcibly();
            unrelated.destroyForcibly();
            owner.waitFor(2, TimeUnit.SECONDS);
            unrelated.waitFor(2, TimeUnit.SECONDS);
        }
    }

    /** Small helper subprocess used exclusively by this test. */
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "parent".equals(args[0])) {
            Process child = run("sleep");
            child.waitFor();
        } else if (args.length > 0 && "sleep".equals(args[0])) {
            Thread.sleep(30_000);
        }
    }
}
