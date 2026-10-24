package at.co.svc.agate.core.dsl.register;

import at.co.svc.agate.core.dsl.model.TestCase;
import at.co.svc.agate.core.dsl.runtime.ExecutionContext;

/**
 * Test-case variable declarations are literal data. Placeholder evaluation
 * belongs to the individual DSL step that consumes a value, not test setup.
 */
final class TestVariableInitializer {
    private TestVariableInitializer() { }

    static void copyLiteralValues(TestCase testCase, ExecutionContext context) {
        if (testCase == null || testCase.getVariables() == null) {
            return;
        }
        testCase.getVariables().forEach((name, value) -> {
            if (name != null && value != null) {
                String literal = String.valueOf(value);
                // A deferred reference in variables is a declaration, not
                // an executable assignment. In particular, do not put
                // dialogId={B[dialogId]} into ExecutionContext: it would
                // override the actual buffer when reusable R parameters
                // are resolved later.
                if (!containsDeferredRuntimePlaceholder(literal)) {
                    context.setVar(name, literal);
                }
            }
        });
    }

    static boolean containsDeferredRuntimePlaceholder(String literal) {
        return literal.contains("{B[")
                || literal.contains("{R[")
                || literal.contains("{XL[")
                || literal.contains("{T[")
                || literal.contains("{E[");
    }
}
