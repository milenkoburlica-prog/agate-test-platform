package at.co.svc.agate.core.dsl.register;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ConditionEvaluatorUnicodeTest {
    @Test
    void shouldSkipBufferWhenAndConditionContainsSharpS() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("Dialogdaten.Ordination.Straße_kurz", "");
        variables.put("Dialogdaten.Vertragspartner.Hat_mehrere_Ordinationen", "1");
        variables.put("Dialogdaten.Ordination.Straße", "Waldstadion 20");
        String condition = "(Dialogdaten.Ordination.Straße_kurz != NULL) AND "
            + "(Dialogdaten.Vertragspartner.Hat_mehrere_Ordinationen != \"1\") AND "
            + "(Dialogdaten.Ordination.Straße != NULL) AND "
            + "(Dialogdaten.Ordination.Straße != NULL)";
        assertFalse(new ConditionEvaluator().evaluate(condition, variables));
    }

    @Test
    void shouldPreserveAndOrLogicWhenIdentifiersContainSharpS() {
        Map<String, Object> vars = Map.of("Straße", "Waldstadion 20", "count", "1");
        ConditionEvaluator evaluator = new ConditionEvaluator();
        assertFalse(evaluator.evaluate("(Straße != NULL) AND (count != \"1\")", vars));
        assertTrue(evaluator.evaluate("(Straße == NULL) OR (count == \"1\")", vars));
        assertTrue(evaluator.evaluate("(Straße != NULL) AND (count == \"1\")", vars));
        assertFalse(evaluator.evaluate("(Straße != NULL) and (count != \"1\")", vars));
    }
    @Test
    void missingUnicodeVariableIsNullButEmptyStringIsNot() {
        var vars = Map.<String, Object>of("Dialogdaten.Vertragspartner.Geschlecht", "");
        var evaluator = new ConditionEvaluator();
        assertFalse(evaluator.evaluate("Dialogdaten.Vertragspartner.Kürzung_Name_Dialogaufbau != NULL", vars));
        assertTrue(evaluator.evaluate("Dialogdaten.Vertragspartner.Kürzung_Name_Dialogaufbau == NULL", vars));
        assertFalse(evaluator.evaluate("(Dialogdaten.Vertragspartner.Geschlecht!=\"Sächlich\" AND "
                + "Dialogdaten.Vertragspartner.Druckname.Gesamt!=NULL AND "
                + "Dialogdaten.Vertragspartner.Kürzung_Name_Dialogaufbau==NULL)", vars));
        assertFalse(evaluator.evaluate("(Dialogdaten.Vertragspartner.Geschlecht!=\"Sächlich\" && "
                + "Dialogdaten.Vertragspartner.Druckname.Gesamt!=NULL && "
                + "Dialogdaten.Vertragspartner.Kürzung_Name_Dialogaufbau==NULL)", vars));
        assertTrue(evaluator.evaluate("Dialogdaten.Vertragspartner.Geschlecht != NULL", vars));
        assertFalse(evaluator.evaluate("Dialogdaten.Vertragspartner.Geschlecht == NULL", vars));
        assertTrue(evaluator.evaluate("Straße == NULL", vars));
    }
}
