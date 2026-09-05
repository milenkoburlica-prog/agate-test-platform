package at.co.svc.agate.core.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComparisonResult {

    private final List<ComparisonDifference> differences = new ArrayList<>();

    public ComparisonResult() {
    }

    public ComparisonResult(List<ComparisonDifference> differences) {
        if (differences != null) {
            this.differences.addAll(differences);
        }
    }
    
    public void addDifference(
            DifferenceType type,
            String path,
            String expected,
            String actual) {

        differences.add(
                new ComparisonDifference(
                        type,
                        path,
                        expected,
                        actual));
    }

    public void addDifference(ComparisonDifference difference) {
        if (difference != null) {
            differences.add(difference);
        }
    }

    public boolean isEqual() {
        return differences.isEmpty();
    }

    public boolean hasDifferences() {
        return !differences.isEmpty();
    }

    public List<ComparisonDifference> getDifferences() {
        return Collections.unmodifiableList(differences);
    }

    public int getDifferenceCount() {
        return differences.size();
    }

    public static ComparisonResult equal() {
        return new ComparisonResult();
    }
}