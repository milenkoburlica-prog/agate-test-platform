package at.co.svc.tosca.testcases;

import java.util.*;

/** GUI provenance carried unchanged through compressed JSON and parser phases. */
public final class GuiMetadata {
    private GuiMetadata() {}
    @SuppressWarnings("unchecked")
    private static Map<String,Object> map(Object value) {
        return value instanceof Map ? (Map<String,Object>) value : Map.of();
    }
    private static String text(Object value) { return Objects.toString(value, ""); }
    private static List<?> list(Object value) { return value instanceof List<?> l ? l : List.of(); }
    public static Map<String,Object> fromValue(Map<String,Object> value,
            Map<String,Map<String,Object>> values, Map<String,Map<String,Object>> tsu) {
        List<?> ids=list(map(value.get("Assocs")).get("ModuleAttribute"));
        if(ids.isEmpty())return Map.of();
        Map<String,Object> attr=tsu.get(text(ids.get(0)));
        if(attr==null)return Map.of();
        Map<String,Object> gui=attribute(attr,tsu);
        if(gui.isEmpty())return gui;
        gui.put("valueSurrogate",value.get("Surrogate"));
        gui.put("rawValue",map(value.get("Attributes")).get("Value"));
        gui.put("explicitName",map(value.get("Attributes")).get("ExplicitName"));
        gui.put("rawActionMode",map(value.get("Attributes")).get("ActionMode"));
        gui.put("disabledDescription",map(value.get("Attributes")).get("DisabledDescription"));
        List<Map<String,Object>> parents=new ArrayList<>();
        Set<String> seen=new HashSet<>();
        Map<String,Object> current=value;
        while(true) {
            List<?> parentIds=list(map(current.get("Assocs")).get("ParentValue"));
            if(parentIds.isEmpty()||!seen.add(text(parentIds.get(0))))break;
            current=values.get(text(parentIds.get(0)));
            if(current==null)current=tsu.get(text(parentIds.get(0)));
            if(current==null)break;
            List<?> aid=list(map(current.get("Assocs")).get("ModuleAttribute"));
            if(aid.isEmpty())break;
            Map<String,Object> pa=tsu.get(text(aid.get(0)));
            if(pa==null)break;
            Map<String,Object> entry=attribute(pa,tsu);
            entry.put("explicitName",map(current.get("Attributes")).get("ExplicitName"));
            entry.put("value",map(current.get("Attributes")).get("Value"));
            entry.put("actionMode",map(current.get("Attributes")).get("ActionMode"));
            entry.put("actionProperty",map(current.get("Attributes")).get("ActionProperty"));
            parents.add(entry);
        }
        Collections.reverse(parents);gui.put("valueParents",parents);
        return gui;
    }
    public static Map<String,Object> attribute(Map<String,Object> attr,Map<String,Map<String,Object>> tsu) {
        Map<String,Object> a=map(attr.get("Attributes"));
        Map<String,Object> properties=new LinkedHashMap<>();
        for(Object pid:list(map(attr.get("Assocs")).get("Properties"))) {
            Map<String,Object> prop=tsu.get(text(pid));if(prop==null)continue;
            Map<String,Object> def=map(prop.get("Attributes"));
            properties.put(text(def.get("Name")),def.get("Value"));
        }
        if(!Set.of("html","anyui","uia","pdf","winx").contains(text(properties.get("Engine")).toLowerCase(Locale.ROOT)))return new LinkedHashMap<>();
        Map<String,Object> out=new LinkedHashMap<>();
        out.put("moduleAttributeSurrogate",attr.get("Surrogate"));
        out.put("name",a.get("Name"));out.put("businessType",a.get("BusinessType"));
        out.put("properties",properties);
        // AnyUI needs its owning native window, not an HTML page title.
        if(Set.of("anyui","uia","winx","pdf").contains(text(properties.get("Engine")).toLowerCase(Locale.ROOT))) {
            List<?> moduleIds=list(map(attr.get("Assocs")).get("Module"));
            if(moduleIds.isEmpty())moduleIds=list(map(attr.get("Assocs")).get("ParentModule"));
            if(!moduleIds.isEmpty()) {
                Map<String,Object> module=tsu.get(text(moduleIds.get(0)));
                if(module!=null) {
                    Map<String,Object> window=new LinkedHashMap<>();
                    for(Object pid:list(map(module.get("Assocs")).get("Properties"))) {
                        Map<String,Object> prop=tsu.get(text(pid));if(prop==null)continue;
                        Map<String,Object> pd=map(prop.get("Attributes"));window.put(text(pd.get("Name")),pd.get("Value"));
                    }
                    out.put("windowProperties",window);
                }
            }
        }

        // Preserve the complete attribute ancestry, including frames not present as values.
        List<Map<String,Object>> ancestors=new ArrayList<>();Set<String> seen=new HashSet<>();
        Map<String,Object> current=attr;
        while(true) {
            List<?> ids=list(map(current.get("Assocs")).get("ParentAttribute"));
            if(ids.isEmpty()||!seen.add(text(ids.get(0))))break;
            current=tsu.get(text(ids.get(0)));if(current==null)break;
            Map<String,Object> ca=map(current.get("Attributes"));
            Map<String,Object> cp=new LinkedHashMap<>();
            for(Object pid:list(map(current.get("Assocs")).get("Properties"))) {
                Map<String,Object> po=tsu.get(text(pid));if(po!=null){Map<String,Object> pd=map(po.get("Attributes"));cp.put(text(pd.get("Name")),pd.get("Value"));}
            }
            Map<String,Object> parent=new LinkedHashMap<>();parent.put("moduleAttributeSurrogate",current.get("Surrogate"));
            parent.put("name",ca.get("Name"));parent.put("businessType",ca.get("BusinessType"));parent.put("properties",cp);
            ancestors.add(parent);
        }
        Collections.reverse(ancestors);out.put("attributeParents",ancestors);return out;
    }
}
