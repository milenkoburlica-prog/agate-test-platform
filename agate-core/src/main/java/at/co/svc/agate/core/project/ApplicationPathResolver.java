package at.co.svc.agate.core.project;

import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

/** Maps hierarchical AppIDs such as DMP.10.DMP_GUI_DEL to dmp/10/dmp_gui_del. */
public final class ApplicationPathResolver {
    private static final Pattern SEGMENT = Pattern.compile("[a-z0-9_-]+");

    private ApplicationPathResolver() { }

    public static Path relativePath(String appId) {
        if (appId == null || appId.isBlank()) {
            throw new IllegalArgumentException("AppID must not be blank");
        }
        Path result = Path.of("");
        for (String part : appId.trim().toLowerCase(Locale.ROOT).split("\\.", -1)) {
            if (!SEGMENT.matcher(part).matches()) {
                throw new IllegalArgumentException("Invalid AppID segment in: " + appId);
            }
            result = result.resolve(part);
        }
        return result;
    }

    public static Path resolve(Path root, String appId) {
        if (root == null) throw new IllegalArgumentException("Root must not be null");
        Path normalizedRoot = root.toAbsolutePath().normalize();
        Path result = normalizedRoot.resolve(relativePath(appId)).normalize();
        if (!result.startsWith(normalizedRoot)) {
            throw new IllegalArgumentException("AppID escapes root: " + appId);
        }
        return result;
    }
}
