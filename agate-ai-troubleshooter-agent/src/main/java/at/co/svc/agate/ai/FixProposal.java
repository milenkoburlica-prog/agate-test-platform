package at.co.svc.agate.ai;
import java.util.List;
public record FixProposal(FixType type, String target, String originalValue,
        String proposedValue, String explanation, List<String> evidence,
        List<String> verificationSteps) { }
