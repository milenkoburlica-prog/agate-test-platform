package at.co.svc.agate.core.reference;

public class UnorderedRule {

    private String path;

    private String matchBy;

    public UnorderedRule() {
    }

    public UnorderedRule(String path) {
        this.path = path;
    }

    public UnorderedRule(String path, String matchBy) {
        this.path = path;
        this.matchBy = matchBy;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMatchBy() {
        return matchBy;
    }

    public void setMatchBy(String matchBy) {
        this.matchBy = matchBy;
    }

    public boolean hasMatchBy() {
        return matchBy != null && !matchBy.isBlank();
    }

    @Override
    public String toString() {
        return "UnorderedRule{" +
                "path='" + path + '\'' +
                ", matchBy='" + matchBy + '\'' +
                '}';
    }
}