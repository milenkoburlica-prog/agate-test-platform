package at.co.svc.aga.transformator.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LegacyEdgeRestoreWorkaroundTest {
    @Test void removesOnlyCompleteLegacyEdgeRestoreBlock() {
        String prefix="agate_cf_d3e3fd9636573ed386ecd51679c8ba60_";
        List<Map<String,Object>> steps=new ArrayList<>();
        steps.add(buffer(prefix+"parent_init",prefix+"parent","false",null));
        steps.add(buffer(prefix+"parent_set",prefix+"parent","true","'{E[env.B_TestkonfigBrowser]}' == 'Edge'"));
        steps.add(buffer(prefix+"p0_init",prefix+"p0","false",null));
        steps.add(buffer(prefix+"p1_init",prefix+"p1","false",null));
        steps.add(find(prefix+"p0_find","PANEL","Seiten wiederherstellen",prefix+"response0"));
        steps.add(exists(prefix+"p0_buffer",prefix+"p0",prefix+"response0"));
        steps.add(find(prefix+"p1_find","RESTORE","Wiederherstellen",prefix+"response1"));
        steps.add(exists(prefix+"p1_buffer",prefix+"p1",prefix+"response1"));
        steps.add(buffer(prefix+"decision_init",prefix+"decision","false",null));
        steps.add(buffer(prefix+"decision_set",prefix+"decision","true",prefix+"parent "+prefix+"p0 "+prefix+"p1"));
        Map<String,Object> close=find("close-id","CLOSE","Schließen","");
        close.put("op","EXEC");close.put("action","CLICK");close.remove("response");
        close.put("condition",prefix+"parent "+prefix+"decision");
        steps.add(close);
        assertTrue(LegacyEdgeRestoreWorkaround.matches(steps));
        var changed=new ArrayList<>(steps);
        changed.get(6).put("caption","*Other application");
        assertFalse(LegacyEdgeRestoreWorkaround.matches(changed));
        changed=new ArrayList<>(steps);
        changed.add(new LinkedHashMap<>(Map.of("type","WINDOW","op","EXEC")));
        assertFalse(LegacyEdgeRestoreWorkaround.matches(changed));
        changed=new ArrayList<>(steps);
        changed.get(10).put("action","CLOSE_BROWSER");
        assertFalse(LegacyEdgeRestoreWorkaround.matches(changed));
    }
    private static Map<String,Object> buffer(String id,String name,String value,String condition) {
        Map<String,Object> m=new LinkedHashMap<>(Map.of("id",id,"name",name,"value",value,"type","BUFFER","op","EXEC"));
        if(condition!=null)m.put("condition",condition);
        return m;
    }
    private static Map<String,Object> find(String id,String role,String business,String response) {
        Map<String,Object> m=new LinkedHashMap<>(Map.of("id",id,"type","WINDOW","op","EXEC","action","FIND",
                "target",Map.of("edgeRestore",role),"caption","*Microsoft Edge",
                "source",Map.of("businessName",business,"properties",Map.of("Engine","AnyUI"))));
        if(!response.isEmpty())m.put("response",response);
        m.put("condition","agate_cf_d3e3fd9636573ed386ecd51679c8ba60_parent");
        return m;
    }
    private static Map<String,Object> exists(String id,String name,String response) {
        return new LinkedHashMap<>(Map.of("id",id,"type","WINDOW","op","BUFFER","action","EXISTS","name",name,"response",response));
    }
}
