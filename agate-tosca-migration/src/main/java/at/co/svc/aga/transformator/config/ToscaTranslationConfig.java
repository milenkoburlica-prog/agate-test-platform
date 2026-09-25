package at.co.svc.aga.transformator.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ToscaTranslationConfig {

    private String customer;

    private Map<String, String> bufferMappings =
            new LinkedHashMap<>();

    private Map<String, String> settingMappings =
            new LinkedHashMap<>();

    private Map<String, String> literalMappings =
            new LinkedHashMap<>();


    public String getCustomer() {
        return customer;
    }

    public void setCustomer(
            String customer) {

        this.customer = customer;
    }


    public Map<String, String> getBufferMappings() {
        return bufferMappings;
    }

    public void setBufferMappings(
            Map<String, String> bufferMappings) {

        this.bufferMappings =
                bufferMappings != null
                        ? bufferMappings
                        : new LinkedHashMap<>();
    }


    public Map<String, String> getSettingMappings() {
        return settingMappings;
    }

    public void setSettingMappings(
            Map<String, String> settingMappings) {

        this.settingMappings =
                settingMappings != null
                        ? settingMappings
                        : new LinkedHashMap<>();
    }


    public Map<String, String> getLiteralMappings() {
        return literalMappings;
    }

    public void setLiteralMappings(
            Map<String, String> literalMappings) {

        this.literalMappings =
                literalMappings != null
                        ? literalMappings
                        : new LinkedHashMap<>();
    }
}