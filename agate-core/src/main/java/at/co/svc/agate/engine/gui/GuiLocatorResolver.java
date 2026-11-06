package at.co.svc.agate.engine.gui;

import java.util.*;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;

/** Strict Playwright locators: ambiguous targets fail rather than clicking first(). */
public final class GuiLocatorResolver {
    private GuiLocatorResolver() {}
    public static Locator resolve(Page page, Map<String, Object> spec) {
        Locator root;
        Object frames = spec.get("frames");
        if (frames instanceof List<?> list && !list.isEmpty()) {
            FrameLocator frame = page.frameLocator(list.get(0).toString());
            for (int i=1;i<list.size();i++) frame = frame.frameLocator(list.get(i).toString());
            root = frame.locator(":root");
        } else root = page.locator(":root");
        return locate(root, spec);
    }
    private static Locator locate(Locator root, Map<String, Object> spec) {
        if (spec.get("scope") instanceof Map) root = locate(root, GuiStepConfig.map(spec.get("scope")));
        long count = List.of("testId", "role", "label", "text", "placeholder", "css", "linkText").stream().filter(spec::containsKey).count();
        if (count != 1) throw new IllegalArgumentException("GUI target requires exactly one locator strategy");
        boolean exact = !Boolean.FALSE.equals(spec.get("exact"));
        Locator result;
        if (spec.containsKey("linkText")) {
            result = root.getByRole(AriaRole.LINK,
                    new Locator.GetByRoleOptions().setName(text(spec,"linkText")).setExact(exact));
        }
        else if (spec.containsKey("testId")) result = root.getByTestId(text(spec,"testId"));
        else if (spec.containsKey("role")) {
            AriaRole role = AriaRole.valueOf(text(spec,"role").toUpperCase(Locale.ROOT));
            Locator.GetByRoleOptions options = new Locator.GetByRoleOptions().setExact(exact);
            if (spec.containsKey("name")) options.setName(text(spec,"name"));
            result = root.getByRole(role, options);
        } else if (spec.containsKey("label")) result = root.getByLabel(text(spec,"label"),new Locator.GetByLabelOptions().setExact(exact));
        else if (spec.containsKey("text")) result = root.getByText(text(spec,"text"),new Locator.GetByTextOptions().setExact(exact));
        else if (spec.containsKey("placeholder")) result = root.getByPlaceholder(text(spec,"placeholder"),new Locator.GetByPlaceholderOptions().setExact(exact));
        else result = root.locator(text(spec,"css"));
        if (spec.containsKey("nth")) {
            int nth = GuiStepConfig.integer(spec.get("nth"));
            if (nth < 0) throw new IllegalArgumentException("GUI nth must be nonnegative");
            result = result.nth(nth);
        }
        if (spec.containsKey("tableCell")) {
            if (!(spec.get("tableCell") instanceof Map<?, ?> coordinates))
                throw new IllegalArgumentException("GUI tableCell requires row and cell coordinates");
            String cell = position(coordinates.get("cell"), "cell");
            if (coordinates.keySet().equals(Set.of("row", "cell"))) {
                String row = position(coordinates.get("row"), "row");
                result = result.locator("xpath=(./tr | ./thead/tr | ./tbody/tr | ./tfoot/tr)[" + row + "]");
            } else if (coordinates.keySet().equals(Set.of("rowCellsAll", "cell"))) {
                Object raw = coordinates.get("rowCellsAll");
                if (!(raw instanceof List<?> criteria) || criteria.isEmpty())
                    throw new IllegalArgumentException("rowCellsAll requires a nonempty list");
                result = result.locator("xpath=./tr | ./thead/tr | ./tbody/tr | ./tfoot/tr");
                for (Object criterion : criteria) {
                    java.util.regex.Pattern pattern = GuiRowCriterion.pattern(criterion);
                    // Each criterion must match a direct cell in the same row.
                    // Returning to its parent retains live locator retry behavior and frame context.
                    result = result.locator("xpath=./*[self::td or self::th]")
                            .filter(new Locator.FilterOptions().setHasText(pattern))
                            .locator("xpath=..");
                }
            } else throw new IllegalArgumentException("tableCell requires row/cell or rowCellsAll/cell");
            result = result.locator("xpath=./*[self::td or self::th][" + cell + "]");
        }
        return result;
    }
    private static String position(Object value, String coordinate) {
        if ("last".equals(value)) return "last()";
        String text = Objects.toString(value, "");
        if (!text.matches("[1-9][0-9]*"))
            throw new IllegalArgumentException("GUI tableCell " + coordinate + " must be a positive one-based integer or last");
        return text;
    }
    private static String text(Map<String,Object> spec,String key) { return Objects.requireNonNull(spec.get(key), key).toString(); }
}
