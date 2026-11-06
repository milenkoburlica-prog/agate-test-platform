package at.co.svc.agate.core.dsl.register;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MigratedSoapExceptionConditionTest {
    @Test
    void bkf015KeepsOnlyFailureAssertions() {
        var v = Map.of("exceptionTyp", "BkfInvalidParameterException", "exceptionReview", "NULL");
        assertEquals(Boolean.TRUE, MigratedSoapExceptionCondition.evaluate(
                "'BkfInvalidParameterException' != '{NULL}' && '{NULL}' == '{NULL}'", v));
        assertEquals(Boolean.FALSE, MigratedSoapExceptionCondition.evaluate(
                "'BkfInvalidParameterException' == '{NULL}' && '{NULL}' == '{NULL}'", v));
    }

    @Test
    void successCaseKeepsOnlySuccessAssertions() {
        var v = Map.of("exceptionTyp", "NULL", "exceptionReview", "NULL");
        assertEquals(Boolean.TRUE, MigratedSoapExceptionCondition.evaluate(
                "'{NULL}' == '{NULL}' && '{NULL}' == '{NULL}'", v));
        assertEquals(Boolean.FALSE, MigratedSoapExceptionCondition.evaluate(
                "'{NULL}' != '{NULL}' && '{NULL}' == '{NULL}'", v));
    }

    @Test
    void reviewCaseSkipsNormalAndFailureAssertions() {
        var v = Map.of("exceptionTyp", "BkfInvalidParameterException", "exceptionReview", "REVIEW");
        assertEquals(Boolean.FALSE, MigratedSoapExceptionCondition.evaluate(
                "'BkfInvalidParameterException' != '{NULL}' && 'REVIEW' == '{NULL}'", v));
    }

    @Test
    void unrelatedConditionsAreUntouched() {
        var v = Map.of("exceptionTyp", "BkfInvalidParameterException", "exceptionReview", "NULL");
        assertNull(MigratedSoapExceptionCondition.evaluate("CardToken.Tokentyp == 'SVSV'", v));
        assertNull(MigratedSoapExceptionCondition.evaluate(
                "'wrong exception' != '{NULL}' && '{NULL}' == '{NULL}'", v));
    }
    @Test
    void newSingleClauseBkfFaultWithoutReview() {
        var values = Map.of("exceptionTyp", "BkfInvalidParameterException");
        assertEquals(Boolean.TRUE, MigratedSoapExceptionCondition.evaluate(
                "'BkfInvalidParameterException' != '{NULL}'", values));
        assertEquals(Boolean.FALSE, MigratedSoapExceptionCondition.evaluate(
                "'BkfInvalidParameterException' == '{NULL}'", values));
        assertNull(MigratedSoapExceptionCondition.evaluate("'some other exception' != '{NULL}'", values));
    }

    @Test
    void newSingleClauseSuccessfulResponseWithoutReview() {
        var values = Map.of("exceptionTyp", "NULL");
        assertEquals(Boolean.TRUE, MigratedSoapExceptionCondition.evaluate("'{NULL}' == '{NULL}'", values));
        assertEquals(Boolean.FALSE, MigratedSoapExceptionCondition.evaluate("'{NULL}' != '{NULL}'", values));
    }
}
