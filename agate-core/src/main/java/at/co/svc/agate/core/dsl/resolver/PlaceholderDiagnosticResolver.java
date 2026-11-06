package at.co.svc.agate.core.dsl.resolver;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.model.TestStep;
import at.co.svc.agate.core.env.EnvironmentManager;

/**
 * Best-effort placeholder resolver used only for diagnostics.
 *
 * Important:
 * - It never changes normal AGATE runtime resolution.
 * - It never throws because a placeholder is missing.
 * - Known placeholders are replaced with their current runtime value.
 * - Unknown placeholders remain unchanged and are reported separately.
 * - Sensitive values are masked in diagnostic output.
 */
public final class PlaceholderDiagnosticResolver {

    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{(B|R|E)\\[([^\\]]+)]}");

    private static final int MAX_PASSES = 5;

    private PlaceholderDiagnosticResolver() {
    }

    public static DiagnosticResolution resolve(
            TestCase tc,
            TestStep step,
            Map<String, Object> runtimeVariables,
            String input) {

        if (input == null) {
            return new DiagnosticResolution(
                    null,
                    Map.of(),
                    Set.of());
        }

        Map<String, String> knownValues =
                new LinkedHashMap<>();

        Set<String> unresolved =
                new LinkedHashSet<>();

        String resolved = input;

        for (int pass = 0; pass < MAX_PASSES; pass++) {

            ResolutionPass result =
                    resolveOnePass(
                            tc,
                            step,
                            runtimeVariables,
                            resolved,
                            knownValues,
                            unresolved);

            if (result.value().equals(resolved)) {
                break;
            }

            resolved = result.value();
        }

        // Re-scan the final value so placeholders that became resolvable
        // during an earlier pass are not incorrectly kept as unresolved.
        unresolved.clear();

        Matcher finalMatcher =
                PLACEHOLDER_PATTERN.matcher(resolved);

        while (finalMatcher.find()) {
            unresolved.add(finalMatcher.group(0));
        }

        return new DiagnosticResolution(
                resolved,
                knownValues,
                unresolved);
    }

    private static ResolutionPass resolveOnePass(
            TestCase tc,
            TestStep step,
            Map<String, Object> runtimeVariables,
            String input,
            Map<String, String> knownValues,
            Set<String> unresolved) {

        Matcher matcher =
                PLACEHOLDER_PATTERN.matcher(input);

        StringBuffer out =
                new StringBuffer();

        while (matcher.find()) {

            String type =
                    matcher.group(1);

            String name =
                    matcher.group(2);

            Object rawValue =
                    lookup(
                            type,
                            name,
                            tc,
                            step,
                            runtimeVariables);

            if (rawValue == null) {
                unresolved.add(matcher.group(0));
                matcher.appendReplacement(
                        out,
                        Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            String displayValue =
                    isSensitive(name)
                            ? "***MASKED***"
                            : String.valueOf(rawValue);

            knownValues.put(
                    displayName(type, name),
                    displayValue);

            matcher.appendReplacement(
                    out,
                    Matcher.quoteReplacement(displayValue));
        }

        matcher.appendTail(out);

        return new ResolutionPass(
                out.toString());
    }

    private static Object lookup(
            String type,
            String name,
            TestCase tc,
            TestStep step,
            Map<String, Object> runtimeVariables) {

        if ("B".equals(type)) {

            if (runtimeVariables != null
                    && runtimeVariables.containsKey(name)
                    && runtimeVariables.get(name) != null) {

                return runtimeVariables.get(name);
            }

            if (tc != null
                    && tc.getVariables() != null
                    && tc.getVariables().containsKey(name)
                    && tc.getVariables().get(name) != null) {

                return tc.getVariables().get(name);
            }

            return null;
        }

        if ("R".equals(type)) {

            if (step != null
                    && step.getParameters() != null
                    && step.getParameters().containsKey(name)
                    && step.getParameters().get(name) != null) {

                return step.getParameters().get(name);
            }

            return null;
        }

        if ("E".equals(type)) {
            return lookupEnvironment(name);
        }

        return null;
    }

    private static Object lookupEnvironment(
            String name) {

        try {
            if (name.startsWith("env.")) {
                return EnvironmentManager.getEnvValue(
                        name.substring("env.".length()));
            }

            if (name.startsWith("users.")) {
                return EnvironmentManager.getReaderValue(
                        name.substring("users.".length()));
            }

        } catch (Exception ignored) {
            // Diagnostic resolution must never replace the real runtime error.
        }

        return null;
    }

    private static String displayName(
            String type,
            String name) {

        return type + "[" + name + "]";
    }

    private static boolean isSensitive(
            String name) {

        if (name == null) {
            return false;
        }

        String lower =
                name.toLowerCase(Locale.ROOT);

        return lower.contains("password")
                || lower.contains("passwd")
                || lower.contains("pwd")
                || lower.contains("secret")
                || lower.contains("token")
                || lower.contains("credential")
                || lower.contains("api_key")
                || lower.contains("apikey")
                || lower.contains("privatekey")
                || lower.contains("private_key");
    }

    public record DiagnosticResolution(
            String resolved,
            Map<String, String> knownValues,
            Set<String> unresolved) {

        public DiagnosticResolution {
            knownValues = Map.copyOf(knownValues);
            unresolved = Set.copyOf(unresolved);
        }
    }

    private record ResolutionPass(
            String value) {
    }
}
