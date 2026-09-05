package at.co.svc.agate.core.reference;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ReferenceFileStore {

    public boolean exists(Path path) {
        return path != null && Files.isRegularFile(path);
    }

    public String read(Path path) throws IOException {
        if (!exists(path)) {
            throw new IOException(
                    "Reference file does not exist: "
                            + (path != null ? path.toAbsolutePath() : "<null>"));
        }

        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Creates a new reference file exactly once.
     *
     * CREATE_NEW is intentional: an existing approved reference must never be
     * overwritten implicitly, even if two tests run in parallel.
     */
    public void create(Path path, String content) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Reference path must not be null");
        }

        Path parent = path.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try {
            Files.writeString(
                    path,
                    content != null ? content : "",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE);
        } catch (FileAlreadyExistsException e) {
            throw new IOException(
                    "Reference file already exists and will not be overwritten: "
                            + path.toAbsolutePath(),
                    e);
        }
    }
}
