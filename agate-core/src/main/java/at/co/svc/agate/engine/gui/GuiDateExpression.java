package at.co.svc.agate.engine.gui;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

/** Evaluates explicit date expressions while retaining other configuration maps. */
public final class GuiDateExpression {
    private GuiDateExpression() {}
    public static Object resolve(Map<String,Object> expression, LocalDate today) {
        if(!expression.containsKey("currentDate"))return expression;
        if(!Set.of("currentDate","offsetDays","prefix","suffix").containsAll(expression.keySet()))
            throw new IllegalArgumentException("GUI date expression needs an explicit format; unsupported fields: " + expression.keySet());
        if(!(expression.get("currentDate") instanceof String format) || format.isBlank())
            throw new IllegalArgumentException("GUI currentDate requires an explicit date format");
        long offset=expression.containsKey("offsetDays")?Long.parseLong(expression.get("offsetDays").toString()):0;
        return expression.getOrDefault("prefix", "").toString()
                + today.plusDays(offset).format(DateTimeFormatter.ofPattern(format))
                + expression.getOrDefault("suffix", "").toString();
    }
}
