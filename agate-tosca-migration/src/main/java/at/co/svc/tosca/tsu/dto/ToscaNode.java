package at.co.svc.tosca.tsu.dto;

import java.util.*;
import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToscaNode {

    @JsonProperty("surrogate")
    public String surrogate;

    @JsonProperty("objectClass")
    public String objectClass;

    @JsonIgnore
    public String tempResolvedCondition;

    @JsonProperty("attributes")
    public Map<String, Object> attributes = new HashMap<>();

    @JsonProperty("childrenIds")
    public List<String> childrenIds;

    @JsonProperty("extraAssocs")
    public Map<String, List<String>> extraAssocs = new HashMap<>();

    @JsonProperty("children")
    public List<ToscaNode> children = new ArrayList<>();
    
    // -------------------------------------------------
    // SAFE NAME ACCESS (FIX)
    // -------------------------------------------------
    @JsonIgnore
    public String getName() {
        return attr("Name");
    }

    // -------------------------------------------------
    // SAFE ATTRIBUTE ACCESS (NEW - IMPORTANT)
    // -------------------------------------------------
    @JsonIgnore
    public String attr(String key) {
        if (attributes == null) {
            return "";
        }
        Object val = attributes.get(key);
        return val == null ? "" : String.valueOf(val);
    }

    // -------------------------------------------------
    // SAFE BOOLEAN HELPERS (optional, ali korisno)
    // -------------------------------------------------
    @JsonIgnore
    public boolean hasAttr(String key) {
        return attributes != null && attributes.containsKey(key);
    }

    @JsonIgnore
    public boolean hasAssoc(String key) {
        return extraAssocs != null
                && extraAssocs.containsKey(key)
                && extraAssocs.get(key) != null
                && !extraAssocs.get(key).isEmpty();
    }

    @JsonIgnore
    public List<String> assoc(String key) {
        if (extraAssocs == null) return Collections.emptyList();
        return extraAssocs.getOrDefault(key, Collections.emptyList());
    }
}