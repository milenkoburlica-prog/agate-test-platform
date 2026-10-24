package at.co.svc.aga.transformator.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProcessAPILogicConditionalQuoteTest {
    @Test void removesSingleWrapperForLeistungserbringer() {
        assertEquals("{XL[Leistungserbringer]}",
                ProcessAPILogic.normalizeConditionalApiPlaceholderValue("\"{XL[Leistungserbringer]}\""));
    }

    @Test void removesDoubleWrapperForPlz() {
        assertEquals("{XL[Zustelladresse.PLZ]}",
                ProcessAPILogic.normalizeConditionalApiPlaceholderValue("\"\"{XL[Zustelladresse.PLZ]}\"\""));
    }

    @Test void leavesPlainPlaceholderUntouched() {
        assertEquals("{XL[Leistungserbringer]}",
                ProcessAPILogic.normalizeConditionalApiPlaceholderValue("{XL[Leistungserbringer]}"));
    }

    @Test void doesNotChangeLiteralOrMixedExpression() {
        assertEquals("\"actual literal\"", ProcessAPILogic.normalizeConditionalApiPlaceholderValue("\"actual literal\""));
        assertEquals("\"prefix-{XL[Name]}\"", ProcessAPILogic.normalizeConditionalApiPlaceholderValue("\"prefix-{XL[Name]}\""));
        assertEquals("\"{XL[Name]}\" suffix", ProcessAPILogic.normalizeConditionalApiPlaceholderValue("\"{XL[Name]}\" suffix"));
        assertEquals("\"{XL[Name]}\"\"", ProcessAPILogic.normalizeConditionalApiPlaceholderValue("\"{XL[Name]}\"\""));
        assertEquals("\"{B[token]}\"", ProcessAPILogic.normalizeConditionalApiPlaceholderValue("\"{B[token]}\""));
        assertNull(ProcessAPILogic.normalizeConditionalApiPlaceholderValue(null));
    }
}
