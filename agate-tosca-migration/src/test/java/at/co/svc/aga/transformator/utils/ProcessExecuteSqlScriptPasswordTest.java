package at.co.svc.aga.transformator.utils;

import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class ProcessExecuteSqlScriptPasswordTest {
    private static StepValueDetails input(String name, String value) {
        StepValueDetails parameter = new StepValueDetails();
        parameter.setName(name);
        parameter.setValue(value);
        parameter.setActionMode("Input");
        return parameter;
    }

    private static CleanStep sqlStep(String password) {
        CleanStep step = new CleanStep();
        step.setModule("SET - ExecuteSQL Scripte");
        step.setSurrogate("01KZECEDS4CV37VEK6PS0NNHFG");
        step.setValues(new ArrayList<>(List.of(
                input("Server Endpoint", "{B[EC_DB_TNS]}"),
                input("User Name", "{B[EC_DB_SCHEMA]}"),
                input("Password", password),
                input("SQL Plus Executeable", "%ORACLE_HOME%\\bin\\sqlplus.exe"),
                input("SQL Script Path", "{B[SVC_Projekt_root_path]}\\Tosca\\Daten\\SQLScripts\\clearDMP.sql"),
                input("Arguments", "'{PL[SVNR]}'"),
                input("To File", "C:\\temp\\SQL-Log.txt")
        )));
        return step;
    }

    @Test
    void masksEncryptedPasswordInGeneratedCommand() {
        String secret = "some-opaque-tosca-password";
        var result = ProcessExecuteSqlScript.translate(sqlStep(secret));
        assertTrue(result.review().isEmpty(), result.review().toString());
        assertEquals(1, result.steps().size());
        Map<String,Object> output = result.steps().get(0);
        String command = (String) output.get("command");
        assertTrue(command.contains("{E[env.database.password]}"));
        assertFalse(command.contains(secret));
        assertEquals("CMD", output.get("type"));
        assertEquals("EXEC", output.get("op"));
        assertEquals("C:\\temp\\SQL-Log.txt", output.get("outputFile"));
        assertEquals(true, output.get("ignoreExitCode"));
    }

    @Test
    void acceptsMaskedOrEmptyToscaPassword() {
        for (String password : new String[]{"*****", "", "{NULL}"}) {
            var result = ProcessExecuteSqlScript.translate(sqlStep(password));
            assertTrue(result.review().isEmpty(), result.review().toString());
            assertTrue(((String)result.steps().get(0).get("command"))
                    .contains("{E[env.database.password]}"));
        }
    }
}
