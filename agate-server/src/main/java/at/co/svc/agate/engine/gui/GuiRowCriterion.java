package at.co.svc.agate.engine.gui;
import java.util.*;
import java.util.regex.Pattern;
final class GuiRowCriterion {
    private GuiRowCriterion() {}
    static Pattern pattern(Object raw) {
        if (!(raw instanceof Map<?,?> m) || !m.containsKey("expected"))
            throw new IllegalArgumentException("rowCellsAll criterion requires expected");
        if (!Set.of("sourceMode","operator","type","expected").containsAll(m.keySet()))
            throw new IllegalArgumentException("Unsupported rowCellsAll criterion fields");
        String mode = Objects.toString(m.get("sourceMode"), "519");
        String op = Objects.toString(m.get("operator"), "1");
        String type = Objects.toString(m.get("type"), "TEXT_GLOB");
        if (!mode.equals("519") || !op.equals("1") || !type.equals("TEXT_GLOB"))
            throw new IllegalArgumentException("rowCellsAll supports sourceMode 519, operator 1 and TEXT_GLOB");
        return GuiTextGlob.pattern(Objects.requireNonNull(m.get("expected")).toString());
    }
}
