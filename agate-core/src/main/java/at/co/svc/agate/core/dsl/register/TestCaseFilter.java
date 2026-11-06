package at.co.svc.agate.core.dsl.register;

import java.util.*;
import java.util.stream.Collectors;

import at.co.svc.agate.core.dsl.model.TestCase;

public class TestCaseFilter {

    private static final Map<String, Integer> PRIORITY_ORDER = Map.of(
            "LOW", 1,
            "MEDIUM", 2,
            "HIGH", 3,
            "CRITICAL", 4
    );

    public static List<TestCase> filter(List<TestCase> testCases) {

        String stageParam = System.getProperty("STAGE");
        String prioParam = System.getProperty("PRIO");

        return testCases.stream()
                .filter(tc -> matchesStage(tc, stageParam))
                .filter(tc -> matchesPriority(tc, prioParam))
                .collect(Collectors.toList());
    }

    private static boolean matchesStage(TestCase tc, String stageParam) {

        if (stageParam == null || stageParam.isBlank()) {
            return true;
        }

        if (tc.getStage() == null) {
            return false;
        }

        return stageParam.equalsIgnoreCase(tc.getStage());
    }

    private static boolean matchesPriority(TestCase tc, String prioParam) {

        if (prioParam == null || prioParam.isBlank()) {
            return true;
        }

        Integer requiredLevel = PRIORITY_ORDER.get(prioParam.toUpperCase());
        if (requiredLevel == null) {
            return true;
        }

        String tcPriority = tc.getPriority() != null
                ? tc.getPriority().toUpperCase()
                : "LOW";

        Integer testLevel = PRIORITY_ORDER.get(tcPriority);

        if (testLevel == null) {
            return true;
        }

        return testLevel >= requiredLevel;
    }
}