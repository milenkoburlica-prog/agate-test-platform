package at.co.svc.agate.core.dsl.register;

import at.co.svc.agate.core.dsl.utils.SemicolonCsvParser;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SemicolonCsvInstantiationTest {
    @Test void multiLineTestCaseNamesDoNotBecomeVariables() throws IOException {
        String csv = "Variable;A;\"FF: mit e-card SV Signature\n - ZS-00168\";C\n"
                + "testcaseIndex;1;2;3\n"
                + "cardTokenID.art;;baseContact;\n"
                + "xlsSvNummer;1748012321;1749012331;\n";
        List<List<String>> rows = SemicolonCsvParser.parse(csv, "test.csv");
        assertEquals(4, rows.size());
        assertEquals("FF: mit e-card SV Signature\n - ZS-00168", rows.get(0).get(2));
        assertEquals("cardTokenID.art", rows.get(2).get(0));
        assertEquals("", rows.get(2).get(1));
        assertEquals("", rows.get(2).get(3));
    }

    @Test void escapedQuotesSemicolonsAndLeadingZerosRemainIntact() throws IOException {
        List<List<String>> rows = SemicolonCsvParser.parse(
                "Variable;\"FF: \"\"Text\"\"; mit Semikolon\"\ncode;007\n", "test.csv");
        assertEquals("FF: \"Text\"; mit Semikolon", rows.get(0).get(1));
        assertEquals("007", rows.get(1).get(1));
    }

    @Test void unmatchedQuoteProducesClearError() {
        assertThrows(IOException.class, () -> SemicolonCsvParser.parse("Variable;\"open\n", "test.csv"));
    }

    @Test void nullLiteralAndEmptyCellAreNotRewrittenByParser() throws IOException {
        var rows = SemicolonCsvParser.parse("Variable;A;B\nfield;NULL;\n", "test.csv");
        assertEquals("NULL", rows.get(1).get(1));
        assertEquals("", rows.get(1).get(2));
    }
}
