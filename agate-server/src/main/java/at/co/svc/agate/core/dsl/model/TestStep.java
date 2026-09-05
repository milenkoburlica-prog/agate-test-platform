package at.co.svc.agate.core.dsl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import at.co.svc.agate.core.reference.UnorderedRule;

/**
 * Represents a single test step.
 */
public class TestStep {

    // --- Reference response assertion ---
    private List<String> ignore = new ArrayList<>();
    private List<UnorderedRule> unordered = new ArrayList<>();

    private String id;
    private StepType type;
    private String op;
    private String action;
    private String name;

    // --- Hierarchy ---
    private List<TestStep> subSteps = new ArrayList<>();
    private Map<String, Object> parameters;

    private List<Constraint> constraints = new ArrayList<>();

    private SoapAuth auth;

    private List<DownloadConfig> download = new ArrayList<>();
    private List<UploadConfig> upload = new ArrayList<>();

    // --- PDF Engine specific fields ---
    private String targetPDF;
    private String pdfPassword;
    private List<PdfAssertion> pdfAssertions = new ArrayList<>();

    private String command;
    private String response;
    private String source;
    private String selector;
    private String row;
    private String column;
    private String expected;
    private String value;
    private String pod;
    private String namespace;
    private String from;
    private String to;
    private String path;
    private String required;
    private String url;
    private String method;
    private String body;
    private Map<String, String> headers;
    private String field;
    private String assertType;
    private String condition;
    private String endpoint;

    private String textYaml;

    private int startLine;
    private int endLine;

    private String file;

    public List<Constraint> getConstraints() { return constraints; }
    public void setConstraints(List<Constraint> constraints) {
        this.constraints = constraints != null ? constraints : new ArrayList<>();
    }

    public SoapAuth getAuth() { return auth; }
    public void setAuth(SoapAuth auth) { this.auth = auth; }

    public List<DownloadConfig> getDownload() { return download; }
    public void setDownload(List<DownloadConfig> download) {
        this.download = download != null ? download : new ArrayList<>();
    }

    public List<UploadConfig> getUpload() { return upload; }
    public void setUpload(List<UploadConfig> upload) {
        this.upload = upload != null ? upload : new ArrayList<>();
    }

    public String getTargetPDF() { return targetPDF; }
    public void setTargetPDF(String targetPDF) { this.targetPDF = targetPDF; }

    public String getPdfPassword() { return pdfPassword; }
    public void setPdfPassword(String pdfPassword) { this.pdfPassword = pdfPassword; }

    public List<PdfAssertion> getPdfAssertions() { return pdfAssertions; }
    public void setPdfAssertions(List<PdfAssertion> pdfAssertions) {
        this.pdfAssertions = pdfAssertions != null ? pdfAssertions : new ArrayList<>();
    }

    public String getTextYaml() { return textYaml; }
    public void setTextYaml(String textYaml) { this.textYaml = textYaml; }

    public int getStartLine() { return startLine; }
    public void setStartLine(int startLine) { this.startLine = startLine; }

    public int getEndLine() { return endLine; }
    public void setEndLine(int endLine) { this.endLine = endLine; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<TestStep> getSubSteps() { return subSteps; }
    public void setSubSteps(List<TestStep> subSteps) {
        this.subSteps = subSteps != null ? subSteps : new ArrayList<>();
    }
    public void addSubStep(TestStep step) { this.subSteps.add(step); }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public StepType getType() { return type; }
    public void setType(StepType type) { this.type = type; }

    public String getOp() { return op; }
    public void setOp(String op) { this.op = op; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getExpected() { return expected; }
    public void setExpected(String expected) { this.expected = expected; }

    public String getAssertType() { return assertType; }
    public void setAssertType(String assertType) { this.assertType = assertType; }

    public String getPod() { return pod; }
    public void setPod(String pod) { this.pod = pod; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public String getTo() { return to; }
    public void setTo(String to) { this.to = to; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSelector() { return selector; }
    public void setSelector(String selector) { this.selector = selector; }

    public String getRow() { return row; }
    public void setRow(String row) { this.row = row; }

    public String getColumn() { return column; }
    public void setColumn(String column) { this.column = column; }

    public String getNamespace() { return namespace; }
    public void setNamespace(String namespace) { this.namespace = namespace; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getRequired() { return required; }
    public void setRequired(String required) { this.required = required; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }

    public List<String> getIgnore() { return ignore; }
    public void setIgnore(List<String> ignore) {
        this.ignore = ignore != null ? ignore : new ArrayList<>();
    }

    public List<UnorderedRule> getUnordered() { return unordered; }
    public void setUnordered(List<UnorderedRule> unordered) {
        this.unordered = unordered != null ? unordered : new ArrayList<>();
    }
}
