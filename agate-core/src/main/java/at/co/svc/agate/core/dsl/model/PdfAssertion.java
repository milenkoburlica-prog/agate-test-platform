package at.co.svc.agate.core.dsl.model;

public class PdfAssertion {
    private String value;
    private String action;
    private Integer expected;

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Integer getExpected() { return expected; }
    public void setExpected(Integer expected) { this.expected = expected; }
}
