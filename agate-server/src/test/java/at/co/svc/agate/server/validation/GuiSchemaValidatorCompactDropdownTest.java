package at.co.svc.agate.server.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

class GuiSchemaValidatorCompactDropdownTest {
    private static final ObjectMapper JSON = new ObjectMapper();

    private void validate(String json) throws Exception {
        JsonNode step = JSON.readTree(json);
        Method check = GuiSchemaValidator.class.getDeclaredMethod("check", JsonNode.class, JsonNode.class);
        check.setAccessible(true);
        try {
            check.invoke(null, step, JSON.readTree("{}"));
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IllegalArgumentException ex) throw ex;
            throw e;
        }
    }

    @Test void acceptsCompactDropdownWithExactId() {
        assertDoesNotThrow(() -> validate("""
            {"type":"GUI","op":"EXEC","action":"SELECT",
             "dropdown":{"id":"DmpForm:dmpProgramm","value":"Diabetes mellitus Typ 2"}}
            """));
    }
    @Test void acceptsCompactDropdownWithIdContains() {
        assertDoesNotThrow(() -> validate("""
            {"type":"GUI","op":"EXEC","action":"SELECT",
             "dropdown":{"idContains":"cardReader","value":"SIMU_6666901100"}}
            """));
    }
    @Test void rejectsConflictingLegacySelection() {
        assertThrows(IllegalArgumentException.class, () -> validate("""
            {"type":"GUI","op":"EXEC","action":"SELECT",
             "dropdown":{"id":"DmpForm:dmpProgramm","value":"Diabetes mellitus Typ 2"},
             "value":{"label":"Diabetes mellitus Typ 2"}}
            """));
    }
    @Test void rejectsEmptyOption() {
        assertThrows(IllegalArgumentException.class, () -> validate("""
            {"type":"GUI","op":"EXEC","action":"SELECT",
             "dropdown":{"id":"DmpForm:dmpProgramm","value":""}}
            """));
    }
    @Test void rejectsUnsupportedLocatorProperty() {
        assertThrows(IllegalArgumentException.class, () -> validate("""
            {"type":"GUI","op":"EXEC","action":"SELECT",
             "dropdown":{"id":"DmpForm:dmpProgramm","unknown":"bad","value":"DM2"}}
            """));
    }
}
