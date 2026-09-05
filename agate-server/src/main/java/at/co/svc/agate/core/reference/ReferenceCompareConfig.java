package at.co.svc.agate.core.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReferenceCompareConfig {

    private final List<String> ignore = new ArrayList<>();

    private final List<UnorderedRule> unordered = new ArrayList<>();

    public ReferenceCompareConfig() {
    }

    public List<String> getIgnore() {
        return Collections.unmodifiableList(ignore);
    }

    public List<UnorderedRule> getUnordered() {
        return Collections.unmodifiableList(unordered);
    }

    public ReferenceCompareConfig addIgnore(String path) {
        if (path != null && !path.isBlank()) {
            ignore.add(path);
        }
        return this;
    }

    public ReferenceCompareConfig addIgnore(List<String> paths) {
        if (paths != null) {
            paths.forEach(this::addIgnore);
        }
        return this;
    }

    public ReferenceCompareConfig addUnordered(UnorderedRule rule) {
        if (rule != null
                && rule.getPath() != null
                && !rule.getPath().isBlank()) {

            unordered.add(rule);
        }

        return this;
    }

    public ReferenceCompareConfig addUnordered(
            List<UnorderedRule> rules) {

        if (rules != null) {
            rules.forEach(this::addUnordered);
        }

        return this;
    }

    public boolean hasIgnoredPaths() {
        return !ignore.isEmpty();
    }

    public boolean hasUnorderedRules() {
        return !unordered.isEmpty();
    }

    public static ReferenceCompareConfig empty() {
        return new ReferenceCompareConfig();
    }
}