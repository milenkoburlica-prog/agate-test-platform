package at.co.svc.aga.transformator.utils;

import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProcessGuiStepReaderNameTest {
    private static final Map<String,Object> READER = Map.of("name", "Kartenlesergerät");

    @Test
    void cardReaderNumberedItemUsesModuleInnerTextWithoutTrailingWildcard() {
        Map<String,Object> gui = Map.of("properties", Map.of("InnerText", "{B[B_Kartenlesername]}*"));
        assertEquals("{B[B_Kartenlesername]}",
                ProcessGuiStep.readerNameFromInnerText("ListItem", gui, READER, "#1"));
    }

    @Test
    void actualToscaModuleAttributeNameWithMojibakeUsesInnerText() {
        Map<String,Object> gui = Map.of("properties", Map.of("InnerText", "{B[B_Kartenlesername]}*"));
        assertEquals("{B[B_Kartenlesername]}",
                ProcessGuiStep.readerNameFromInnerText("ListItem", gui,
                        Map.of("name", "KartenlesegerÃ¤t"), "#1"));
    }

    @Test
    void actualCorrectlyEncodedToscaModuleAttributeNameUsesInnerText() {
        Map<String,Object> gui = Map.of("properties", Map.of("InnerText", "{B[B_Kartenlesername]}*"));
        assertEquals("{B[B_Kartenlesername]}",
                ProcessGuiStep.readerNameFromInnerText("ListItem", gui,
                        Map.of("name", "Kartenlesegerät"), "#1"));
    }

    @Test
    void readerCssLocatorBecomesHumanReadableIdContains() {
        assertEquals("cardReader", ProcessGuiStep.readerSelectIdContains(
                Map.of("css", "select[id*=\"cardReader\"]")));
        assertThrows(IllegalArgumentException.class, () -> ProcessGuiStep.readerSelectIdContains(
                Map.of("css", "input[id*=\"cardReader\"]")));
    }

    @Test
    void regularNumberedListItemKeepsIndexSelection() {
        Map<String,Object> gui = Map.of("properties", Map.of("InnerText", "Something*"));
        assertNull(ProcessGuiStep.readerNameFromInnerText("ListItem", gui, Map.of("name", "Country"), "#1"));
    }

    @Test
    void cardReaderWithoutInnerTextKeepsOriginalIndexSelection() {
        assertNull(ProcessGuiStep.readerNameFromInnerText("ListItem",
                Map.of("properties", Map.of()), READER, "#1"));
    }

    @Test
    void explicitReaderSelectionDoesNotUseSpecialCase() {
        assertNull(ProcessGuiStep.readerNameFromInnerText("ListItem",
                Map.of("properties", Map.of("InnerText", "{B[B_Kartenlesername]}*")),
                READER, "SIMU_6666901100"));
    }
}
