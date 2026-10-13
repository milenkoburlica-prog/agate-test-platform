package at.co.svc.aga.transformator.utils;

import java.util.*;
import org.yaml.snakeyaml.*;
import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.*;

/** Migration contracts for test-case-name buffering and Tosca PDF comparison. */
public final class ProcessPdfWorkflow {
    private ProcessPdfWorkflow() {}
    public record Translation(List<Map<String,Object>> steps,List<String> review) {}
    public static boolean supports(CleanStep step) {
        return step != null && ("TBox Name To Buffer".equals(step.getModule())
                || "1:1 Compare".equals(step.getModule()) || isPdfDocument(step));
    }
    private static boolean isPdfDocument(CleanStep step) {
        return step.getValues()!=null && step.getValues().stream().anyMatch(v->v.getGui()!=null
                && "pdf".equalsIgnoreCase(Objects.toString(((Map<?,?>)v.getGui().getOrDefault("properties",Map.of())).get("Engine"),"")));
    }
    public static Translation translate(CleanStep step) {
        try {
            if(isPdfDocument(step)) {
                PdfTextMigration.Result result=PdfTextMigration.translate(step);
                return new Translation(result.steps(),result.review());
            }
            Map<String,String> parameters=new LinkedHashMap<>();
            for(StepValueDetails v:Objects.requireNonNull(step.getValues(),"values")) {
                String value=Objects.toString(v.getValue(),"");if(value.isBlank()||value.equals("{NULL}"))continue;
                if(!Set.of("37","Input","Insert").contains(v.getActionMode()))throw new IllegalArgumentException("unsupported parameter mode");
                if(v.getxCondition()!=null&&!v.getxCondition().isBlank())throw new IllegalArgumentException("parameter condition requires mapping");
                String name=v.getName();if(name==null||name.isBlank())name=v.getToscaPath();
                if(name==null||parameters.putIfAbsent(name,value)!=null)throw new IllegalArgumentException("missing or duplicate parameter name");
            }
            Map<String,Object> out=new LinkedHashMap<>();out.put("id",step.getSurrogate());
            if(step.getModule().equals("TBox Name To Buffer")) {
                if(!parameters.keySet().equals(Set.of("Buffer")))throw new IllegalArgumentException("Name To Buffer requires Buffer parameter only");
                out.put("type","BUFFER");out.put("op","EXEC");out.put("name",parameters.get("Buffer"));
                out.put("value",Map.of("currentTestCaseName",true));
            } else {
                if(!parameters.keySet().equals(Set.of("Reference PDF","Target PDF","Accuracy [%]")))throw new IllegalArgumentException("PDF comparison parameters require explicit mapping");
                String accuracy=parameters.get("Accuracy [%]");
                if(!accuracy.matches("(?:100|[0-9]{1,2})(?:\\.[0-9]+)?")||Double.parseDouble(accuracy)>100)throw new IllegalArgumentException("PDF accuracy must be between 0 and 100");
                out.put("type","FILE");out.put("op","COMPARE");
                out.put("reference",ToscaValueTranslator.translateToscaValues(parameters.get("Reference PDF")));
                out.put("target",ToscaValueTranslator.translateToscaValues(parameters.get("Target PDF")));
                out.put("comparison",Map.of("type","PDF","algorithmRef","tosca:CompareTwoPdfFiles","accuracyPercent",Double.parseDouble(accuracy)));
            }
            if(step.getCondition()!=null&&!step.getCondition().isBlank())out.put("condition",ToscaValueTranslator.translateToscaValues(step.getCondition()));
            return new Translation(List.of(out),List.of());
        } catch(IllegalArgumentException|NullPointerException ex) {return new Translation(List.of(),List.of(Objects.toString(ex.getMessage(),"missing parameters")));}
    }
    private static final Map<String,List<Map<String,Object>>> PDF_DIAGNOSTICS=new LinkedHashMap<>();
    private static synchronized void writePdfDiagnostics(CleanStep step,String file,List<String> review) throws java.io.IOException {
        java.nio.file.Path path=java.nio.file.Path.of(file+".pdf-migration.json").toAbsolutePath().normalize();
        Map<String,Object> entry=new LinkedHashMap<>();entry.put("stepId",step.getSurrogate());
        entry.put("module",step.getModule());entry.put("textOnly",Boolean.getBoolean("agate.migration.pdf.textOnly"));
        entry.put("review",review);entry.put("values",step.getValues());
        List<Map<String,Object>> entries=PDF_DIAGNOSTICS.computeIfAbsent(path.toString(),k->new ArrayList<>());
        entries.removeIf(e->Objects.equals(e.get("stepId"),step.getSurrogate()));entries.add(entry);
        if(path.getParent()!=null)java.nio.file.Files.createDirectories(path.getParent());
        new com.fasterxml.jackson.databind.ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(path.toFile(),entries);
    }
    public static boolean process(CleanStep step,String indent,String file) {
        if(!supports(step))return false;
        Translation result=translate(step);
        if(isPdfDocument(step)) {
            try { writePdfDiagnostics(step,file,result.review()); }
            catch(java.io.IOException e) {
                result=new Translation(List.of(),List.of("Could not save PDF diagnostics: "+e.getMessage()));
            }
        }
        for(String issue:result.review()) {
            ToscaToAgaPhase1.writeLine(indent+"      # [Pending] PDF workflow review: "+issue);
            MigrationReport.registerUnprocessedModule(step.getModule(),step.getName()+" | "+issue,file);
        }
        if(!isPdfDocument(step) && !result.steps().isEmpty())ToscaToAgaPhase1.writeLine(indent+"      # [Runtime] Testfallname/PDF-Vergleichsvertrag in agate-server erforderlich.");
        DumperOptions options=new DumperOptions();options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);options.setSplitLines(false);
        Yaml yaml=new Yaml(options);
        for(Map<String,Object> output:result.steps()) {
            ToscaToAgaPhase1.writeLine("");
            for(String line:AgateYamlFormatting.dump(yaml,List.of(output)).split("\\R"))ToscaToAgaPhase1.writeLine(indent+"      "+line);
        }
        return true;
    }
}
