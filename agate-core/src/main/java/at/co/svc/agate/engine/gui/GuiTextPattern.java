package at.co.svc.agate.engine.gui;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/** Joins literal and runtime-resolved pieces of a Tosca text pattern. */
final class GuiTextPattern {
    private GuiTextPattern() {}

    static Pattern pattern(Object expected) {
        if (!(expected instanceof Map<?, ?> map) || map.size() != 1 || !(map.get("parts") instanceof List<?> parts) || parts.isEmpty()) {
            throw new IllegalArgumentException("TEXT_PATTERN requires expected.parts as a nonempty list");
        }
        StringBuilder glob = new StringBuilder();
        for (Object part : parts) {
            // GuiStepConfig.resolveTree already evaluates currentDate maps at runtime.
            if (!(part instanceof String)) {
                throw new IllegalArgumentException("TEXT_PATTERN part must resolve to text: " + Objects.toString(part));
            }
            glob.append(part);
        }
        return GuiTextGlob.pattern(glob.toString());
    }
}
