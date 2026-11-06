package at.co.svc.agate.server.dto;

import java.util.List;

public class ExecutionContextConfig {
    public List<ConfigItem> environments;
    public List<ConfigItem> users;

    public ExecutionContextConfig(List<ConfigItem> environments, List<ConfigItem> users) {
        this.environments = environments;
        this.users = users;
    }
}
