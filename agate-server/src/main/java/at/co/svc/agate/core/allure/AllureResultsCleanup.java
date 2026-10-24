package at.co.svc.agate.core.allure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.FileVisitResult;

/** Cleans legacy Allure data only when standard AGATE execution disables Allure. */
public final class AllureResultsCleanup {

    private AllureResultsCleanup() { }

    public static void installForDisabledAllure() {
        if (!"false".equalsIgnoreCase(System.getProperty("allure.enabled"))) {
            return;
        }

        // Capture the start directory; do not derive a cleanup target later
        // from a potentially changed working directory or project setting.
        final Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        final Path resultsDirectory = workingDirectory.resolve("allure-results");

        // Never operate through a symlink (or a replaced working directory).
        // Existing files are deleted at startup; newly created files at exit.
        cleanup(resultsDirectory);
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> cleanup(resultsDirectory), "agate-allure-results-cleanup"));
    }

    private static void cleanup(Path directory) {
        try {
            if (!"allure-results".equals(directory.getFileName().toString())
                    || !Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) {
                return;
            }
            if (Files.isSymbolicLink(directory)) {
                System.err.println("[WARN] Skipping symbolic link: " + directory);
                return;
            }
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException error)
                        throws IOException {
                    if (error != null) {
                        throw error;
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException | SecurityException e) {
            System.err.println("[WARN] Could not clean legacy Allure results at "
                    + directory + ": " + e.getMessage());
        }
    }
}
