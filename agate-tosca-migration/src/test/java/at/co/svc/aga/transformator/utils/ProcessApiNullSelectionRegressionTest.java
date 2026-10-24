package at.co.svc.aga.transformator.utils;

import at.co.svc.aga.transformator.dto.Constraint;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessApiNullSelectionRegressionTest {
    private Constraint filter(String value) {
        Constraint c = new Constraint();
        c.setPath("/*[local-name()='svNummer']");
        c.setAction("EQUALS");
        c.setExpected(value);
        return c;
    }
    @Test void emptyStructuralSelectionIsNotAnAssertionFilter() {
        assertFalse(ProcessAPILogic.hasMeaningfulApiConstraint(filter("")));
        assertFalse(ProcessAPILogic.hasMeaningfulApiConstraint(filter("{NULL}")));
        assertFalse(ProcessAPILogic.hasMeaningfulApiConstraint(filter("NULL")));
    }
    @Test void explicitNonNullSelectionRemainsAFilter() {
        assertTrue(ProcessAPILogic.hasMeaningfulApiConstraint(filter("4561047149")));
    }
}
