package at.co.svc.agate.engine.gui;

import java.nio.file.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class GuiPdfFile {
    private GuiPdfFile() {}
    static void write(Path output, byte[] bytes) throws IOException {
        String prefix = new String(bytes, 0, Math.min(bytes.length, 1024), StandardCharsets.ISO_8859_1);
        if (!prefix.contains("%PDF-")) throw new IllegalArgumentException("Response is not a PDF; existing output was not replaced");
        Path parent = output.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temporary = Files.createTempFile(parent, "agate-pdf-", ".tmp");
        try {
            Files.write(temporary, bytes);
            try { Files.move(temporary, output, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, output, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}
