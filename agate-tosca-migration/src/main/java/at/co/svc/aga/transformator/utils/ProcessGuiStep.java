package at.co.svc.aga.transformator.utils;

import java.util.*;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import at.co.svc.aga.transformator.ToscaToAgaPhase1;
import at.co.svc.aga.transformator.dto.CleanStep;
import at.co.svc.aga.transformator.dto.StepValueDetails;

/** One shared GUI translator for testcase and reusable steps. No guessing of locators. */
public final class ProcessGuiStep {
    private ProcessGuiStep() {}
    public record Translation(List<Map<String,Object>> steps, List<String> review) {}
    public static boolean process(CleanStep step, String indent, String yamlFile) {
        if (!isGui(step)) return false;
        Translation result = translate(step);
        if(isEmptyHtmlStep(step)) {
            ToscaToAgaPhase1.writeLine(indent + "      # [Info] Empty HTML step: no source actions | "
                    + Objects.toString(step.getSurrogate(),""));
            GuiMigrationTrace.emit("EMPTY_HTML_STEP",step,step.getValues().get(0),"explicit TestStepValues is empty; no inherited actions");
        }
        for (String issue : result.review()) {
            String message = "GUI review: " + issue;
            ToscaToAgaPhase1.writeLine(indent + "      # [Pending] " + message.replace('\n',' '));
            // Reusable generation captures stdout as YAML; diagnostics must use stderr.
            // Preserve the review in the final dependency-scoped report.
            // Per-step regex locator diagnostics are too noisy in normal mode.
            if (issue.contains("regex locator requires mapping beyond literal alternatives")) {
                MigrationLog.debug(message + " | step=" + step.getName());
            } else {
                MigrationLog.warn(message + " | step=" + step.getName());
            }
            MigrationReport.registerUnprocessedModule(step.getModule(), step.getName() + " | " + issue, yamlFile);
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setSplitLines(false);
        Yaml yaml = new Yaml(options);
        for (Map<String,Object> output : result.steps()) {
            adaptKnownHelpTabClose(output, yamlFile);
            adaptBrowserTabsClose(output, yamlFile);
            humanReadable(output);
            ToscaToAgaPhase1.writeLine("");
            for (String line : AgateYamlFormatting.dump(yaml,List.of(output)).split("\\R"))
                ToscaToAgaPhase1.writeLine(indent + "      " + line);
        }
        return true;
    }
    /**
     * The Tosca online-help reusable closes the popup opened by HelpAnchor,
     * not the e-card browser context. Scope this rule to the known reusable and
     * the non-TSV branch; never reinterpret ordinary CloseBrowser steps.
     */
    static void adaptKnownHelpTabClose(Map<String,Object> output, String yamlFile) {
        if (yamlFile == null || !yamlFile.replace('\\', '/').toLowerCase(Locale.ROOT)
                .contains("online_hilfe_aufrufen_de")) return;
        if (!"GUI".equals(text(output.get("type")))
                || !"CLOSE_BROWSER".equals(text(output.get("action")))) return;
        String condition = text(output.get("condition"));
        if (!condition.contains("ONLINE_HILFE_APPL") || !condition.contains("NOT")
                || !condition.contains("TSV")) return;
        output.put("action", "CLOSE_TAB");
        output.put("urlGlob", "*chipkarte.at*");
    }

    /**
     * This Tosca reusable closes pre-existing desktop Edge windows by caption.
     * The generic CloseBrowser translator must remain a Playwright GUI action.
     */
    static void adaptBrowserTabsClose(Map<String,Object> output, String yamlFile) {
        if (yamlFile == null) return;
        String normalized = yamlFile.replace('\\', '/').toLowerCase(Locale.ROOT);
        String fileName = normalized.substring(normalized.lastIndexOf('/') + 1);
        if (!"browser_tabs_schließen.yaml".equals(fileName)) return;
        if (!"GUI".equals(text(output.get("type")))
                || !"CLOSE_BROWSER".equals(text(output.get("action")))) return;

        output.put("type", "WINDOW");
        output.put("action", "CLOSE");
        output.put("caption", "e-card*");
    }

    public static boolean isGui(CleanStep step) {
        if ("CloseBrowser".equalsIgnoreCase(step.getModule()) || browserUrl(step)!=null) return true;
        return step.getValues()!=null && step.getValues().stream().anyMatch(v -> v.getGui()!=null && !v.getGui().isEmpty());
    }
    public static Translation translate(CleanStep step) {
        List<Map<String,Object>> output=new ArrayList<>();List<String> issues=new ArrayList<>();
        if(isEmptyHtmlStep(step))return new Translation(output,issues);
        if ("CloseBrowser".equalsIgnoreCase(step.getModule())) {
            Map<String,Object> close=base(step,"close","EXEC");close.remove("page");close.put("action","CLOSE_BROWSER");condition(close,step.getCondition(),null);
            output.add(close);return new Translation(output,issues);
        }
        String url=browserUrl(step);
        if(url!=null) {
            Map<String,Object> open=base(step,"open","OPEN");
            open.remove("page");open.put("url",ToscaValueTranslator.translateToscaValues(url));
            condition(open,step.getCondition(),null);output.add(open);return new Translation(output,issues);
        }
        if(step.getValues()==null)return new Translation(output,List.of("GUI values missing"));
        Map<StepValueDetails,List<Map<String,Object>>> rowCriteria=collectRowCriteria(step.getValues());
        for(StepValueDetails value:step.getValues()) {
            Map<String,Object> gui=value.getGui();if(gui==null||gui.isEmpty())continue;
            GuiMigrationTrace.emit("INPUT",step,value,"metadata received by YAML translator");
            gui=new LinkedHashMap<>(gui);
            if(rowCriteria.containsKey(value))gui.put("rowCriteria",rowCriteria.get(value));
            String type=text(gui.get("businessType"));String mode=text(value.getActionMode());
            String raw=gui.containsKey("rawValue")?text(gui.get("rawValue")):text(value.getValue());
            // Structural selection is a locator context, not a SELECT operation on a dropdown.
            if (mode.equalsIgnoreCase("Select")||mode.equals("517")||mode.equalsIgnoreCase("Constraint")||mode.equals("1")||mode.equals("519")||mode.equals("Unknown_519")) {GuiMigrationTrace.emit("STRUCTURAL",step,value,"locator context; no standalone action");continue;}
            boolean listItemIndex=type.equalsIgnoreCase("ListItem") && text(gui.get("explicitName")).matches("#[1-9][0-9]*");
            // An explicit NULL on a textbox Insert means clear the input, not skip the step.
            boolean clearTextbox = type.equalsIgnoreCase("TextBox")
                    && Set.of("Insert", "37", "515").contains(mode)
                    && isNullToken(raw);
            if (!clearTextbox && (isNullToken(raw) || (raw.isEmpty() && !listItemIndex))) {
                GuiMigrationTrace.emit("SKIP",step,value,"NULL or empty non-index value");continue;
            }
            String name=text(gui.get("name"));
            try {
                if(!text(gui.get("disabledDescription")).isEmpty())throw new IllegalArgumentException("disabled value requires review");
                Map<String,Object> target;
                Map<String,Object> comboParent=null;
                if(type.equalsIgnoreCase("ListItem")) {
                    for(Map<String,Object> parent:maps(gui.get("valueParents")))
                        if(text(parent.get("businessType")).equalsIgnoreCase("ComboBox"))comboParent=parent;
                    if(comboParent==null)throw new IllegalArgumentException("ListItem has no ComboBox parent");
                    target=target(comboParent);
                } else if(type.equalsIgnoreCase("Row") && text(value.getActionProperty()).equalsIgnoreCase("rowcount")) {
                    List<Map<String,Object>> parents=maps(gui.get("valueParents"));
                    if(parents.size()!=1 || !text(parents.get(0).get("businessType")).equalsIgnoreCase("Table"))throw new IllegalArgumentException("rowcount Row requires one Table parent");
                    target=target(parents.get(0));
                } else target=target(gui);
                Map<String,Object> runtime=runtimeExpression(raw);
                boolean currentDate=runtime!=null;
                String translated=currentDate?(raw.endsWith("*")?"*":"")
                        : (raw.equals("{EMPTY}") || clearTextbox) ? "" : ToscaValueTranslator.translateToscaValues(raw);
                if(translated.contains("{DATE[")||translated.contains("{REGEX[")||translated.contains("{CP["))
                    throw new IllegalArgumentException("unresolved Tosca expression");
                if(text(value.getActionProperty()).equalsIgnoreCase("rowcount") && translated.startsWith(".rowcount="))translated=translated.substring(".rowcount=".length());
                if(runtime!=null && runtime.containsKey("parts") && (!Set.of("Verify","69","WaitOn","101").contains(mode)
                        || !Set.of("","InnerText","Text","Value").contains(text(value.getActionProperty()))))
                    throw new IllegalArgumentException("composite runtime pattern requires text/value verification");
                String op;
                Map<String,Object> extras=new LinkedHashMap<>();
                if(mode.equalsIgnoreCase("Insert")||mode.equals("37")||mode.equals("515")) {
                    if(type.equalsIgnoreCase("Button")||type.equalsIgnoreCase("Link")||type.equalsIgnoreCase("Cell")) {
                        if(!Set.of("X","{CLICK}","{JUMPTO[CLICK]}","{MOUSE[CLICK][JUMP]}").contains(raw.toUpperCase(Locale.ROOT)))
                            throw new IllegalArgumentException("unsupported button/link input");
                        op="CLICK";
                    } else if(type.equalsIgnoreCase("TextBox")) { op="FILL";extras.put("value",currentDate?runtime:translated); }
                    else if(type.equalsIgnoreCase("ComboBox") || type.equalsIgnoreCase("ListItem")) {
                        op="SELECT";
                        String item=raw.isEmpty()?text(gui.get("explicitName")):translated;
                        String readerName = readerNameFromInnerText(type, gui, comboParent, item);
                        if(readerName != null) {
                            String idContains = readerSelectIdContains(target);
                            extras.put("select", Map.of("idContains", idContains, "label", readerName));
                        }
                        else if(item.matches("#[1-9][0-9]*")) extras.put("value",Map.of("index",Integer.parseInt(item.substring(1))-1));
                        else if(item.contains("*")) extras.put("value",Map.of("labelGlob",item));
                        else extras.put("value",Map.of("label",item));
                    } else if(type.equalsIgnoreCase("CheckBox")) {
                        if(Set.of("{CLICK}","{JUMPTO[CLICK]}","{MOUSE[CLICK][JUMP]}").contains(raw.toUpperCase(Locale.ROOT)))op="CLICK";
                        else {if(!isBoolean(raw))throw new IllegalArgumentException("unsupported checkbox input");
                            op=truth(raw)?"CHECK":"UNCHECK";}
                    } else if(type.equalsIgnoreCase("RadioButton")) {
                        if(raw.matches("\\{(?:PL|P|B|E|R|XL)\\[[^\\[\\]{}]+\\]\\}")) {
                            extras.put("selection",Map.of("value",translated,"syntax","tosca-radio","empty","skip"));
                        } else if(!Set.of("X","{CLICK}","{JUMPTO[CLICK]}","{MOUSE[CLICK][JUMP]}").contains(raw.toUpperCase(Locale.ROOT)))
                            throw new IllegalArgumentException("unsupported radio input");
                        op="CHECK";
                    } else throw new IllegalArgumentException("unsupported input type " + type);
                } else if(mode.equalsIgnoreCase("Verify")||mode.equals("69")||mode.equalsIgnoreCase("WaitOn")||mode.equals("101")) {
                    Map<String,Object> assertion=assertion(value,translated,type.equalsIgnoreCase("Row") && text(value.getActionProperty()).equalsIgnoreCase("rowcount")?"Table":type);
                    if(currentDate) {
                        assertion.put("expected",runtime);
                        if(runtime.containsKey("parts"))assertion.put("type",type.equalsIgnoreCase("TextBox")?"VALUE_PATTERN":"TEXT_PATTERN");
                    }
                    op="ASSERT";extras.put("assertion",assertion);
                } else if(mode.equalsIgnoreCase("Buffer")||mode.equals("165")) {
                    op="EXTRACT";extras.put("buffer",translated);
                    String property=text(value.getActionProperty());
                    if(property.isEmpty()||property.equalsIgnoreCase("InnerText"))extras.put("source","text");
                    else if(property.equalsIgnoreCase("Value"))extras.put("source","value");
                    else throw new IllegalArgumentException("unsupported buffer property " + property);
                } else throw new IllegalArgumentException("unsupported action mode " + mode);
                Map<String,Object> converted=base(step,text(gui.get("valueSurrogate")),op);
                if (!extras.containsKey("select")) converted.put("target",target);
                converted.putAll(extras);
                DbasMigrationOverrides.reader(converted);

                condition(converted,step.getCondition(),value.getxCondition());
                if(name.toLowerCase(Locale.ROOT).matches(".*(pin|password|passwd|secret).*"))converted.put("sensitive",true);
                output.add(converted);
                GuiMigrationTrace.emit("GENERATED",step,value,converted);
            } catch(IllegalArgumentException e) {
                // Never include raw values (PINs, tokens, etc.) in diagnostics.
                issues.add(name + ": " + e.getMessage());
                GuiMigrationTrace.emit("REJECTED",step,value,e.getMessage());
            }
        }
        return new Translation(output,issues);
    }
    /**
     * Tosca HTML ComboBox special case: a numbered ListItem (#1) may represent
     * a reader identified by its ModuleAttribute.InnerText, not the first entry
     * in the live dropdown. Only Kartenlesergeraet supports this override.
     * Other ListItems keep their existing index/label semantics.
     */
    static String readerNameFromInnerText(String type, Map<String,Object> gui,
                                          Map<String,Object> comboParent, String item) {
        if (!"ListItem".equalsIgnoreCase(type) || comboParent == null
                || !item.matches("#[1-9][0-9]*")
                || !isKartenlesegeraet(text(comboParent.get("name"))))
            return null;
        String inner = text(map(gui.get("properties")).get("InnerText"));
        if (inner.isBlank()) return null;
        // Tosca's trailing '*' means a prefix match; intentionally discard it
        // only for this reader-name override, as agreed for this special case.
        if (inner.endsWith("*")) inner = inner.substring(0, inner.length() - 1);
        if (inner.isBlank() || inner.contains("*"))
            throw new IllegalArgumentException("unsupported Kartenlesergeraet InnerText selection");
        return ToscaValueTranslator.translateToscaValues(inner);
    }

    // Only this specific reader case uses shorthand; the module locator must
    // be a CSS selector of the form select[id*="..."] (no guessed defaults).
    static String readerSelectIdContains(Map<String,Object> target) {
        String css = text(target.get("css"));
        var matcher = Pattern.compile("^select\\[id\\*=\"([^\"]+)\"\\]$", Pattern.CASE_INSENSITIVE).matcher(css);
        if (!matcher.matches())
            throw new IllegalArgumentException("Kartenlesegerät requires select[id*=\"...\"] module locator; got " + css);
        return matcher.group(1);
    }

    private static boolean isKartenlesegeraet(String name) {
        // Tosca exports may contain the correctly encoded German umlaut or
        // mojibake (UTF-8 read as Latin-1). Never rely on "Kartenlesergeraet"
        // (extra 'r'), which does not match the Tosca ModuleAttribute name.
        String n = name.trim().toLowerCase(Locale.ROOT);
        return n.equals("kartenlesegerät") || n.equals("kartenlesegeraet")
                || n.equals("kartenlesegerÃ¤t");
    }

    private static boolean isEmptyHtmlStep(CleanStep step) {
        return step.getValues()!=null && step.getValues().size()==1
                && step.getValues().get(0).getGui()!=null
                && Boolean.TRUE.equals(step.getValues().get(0).getGui().get("emptyHtmlStep"));
    }
    /** Composite text pattern: dates stay dynamic, XB captures stay captures. */
    private static Map<String,Object> runtimeExpression(String raw) {
        Map<String,Object> date=dateExpression(raw);if(date!=null)return date;
        if(!raw.contains("{DATE[") && !raw.contains("{XB["))return null;
        var token=Pattern.compile("\\{DATE\\[\\]\\[[+-]?[0-9]*d?\\]\\[(?:dd\\.MM\\.yyyy)?\\]\\}|\\{XB\\[([^\\]{}]+)\\]\\}").matcher(raw);
        List<Object> parts=new ArrayList<>();int end=0;
        while(token.find()) {
            if(token.start()>end)parts.add(ToscaValueTranslator.translateToscaValues(raw.substring(end,token.start())));
            if(token.group(1)!=null)parts.add(Map.of("capture",token.group(1)));
            else {Map<String,Object> part=dateExpression(token.group());if(part==null)throw new IllegalArgumentException("unsupported DATE expression");parts.add(part);}
            end=token.end();
        }
        if(end<raw.length())parts.add(ToscaValueTranslator.translateToscaValues(raw.substring(end)));
        for(Object part:parts)if(part instanceof String text && (text.contains("{DATE[")||text.contains("{XB[")||text.contains("{REGEX[")||text.contains("{CP[")))
            throw new IllegalArgumentException("unsupported composite runtime expression");
        if(parts.isEmpty())throw new IllegalArgumentException("unsupported composite runtime expression");
        return Map.of("parts",parts);
    }
    private static Map<String,Object> dateExpression(String raw) {
        var m=Pattern.compile("^(\\*?)\\{DATE\\[\\]\\[([+-]?[0-9]+d)?\\]\\[(dd\\.MM\\.yyyy)?\\]\\}(\\*?)$").matcher(raw);
        if(!m.matches())return null;
        Map<String,Object> date=new LinkedHashMap<>();
        if(m.group(3)==null) {
            date.put("currentDate",true);
            date.put("formatRef","tosca.targetDateFormat");
        } else date.put("currentDate",m.group(3));
        if(m.group(2)!=null)date.put("offsetDays",Integer.parseInt(m.group(2).substring(0,m.group(2).length()-1)));
        if(!m.group(1).isEmpty())date.put("prefix","*");
        if(!m.group(4).isEmpty())date.put("suffix","*");return date;
    }
    private static Map<String,Object> dateValue(String raw) {
        Map<String,Object> date=dateExpression(raw);
        if(date==null)throw new IllegalArgumentException("unsupported DATE expression");
        return date;
    }
    /** Tosca 519 is a cell constraint, not a standalone assertion. All criteria
     * remain attached to the same physical row; unspecified cells are existential. */
    private static Map<StepValueDetails,List<Map<String,Object>>> collectRowCriteria(List<StepValueDetails> values) {
        Map<StepValueDetails,List<Map<String,Object>>> result=new IdentityHashMap<>();
        List<Map<String,Object>> criteria=null;
        for(StepValueDetails value:values) {
            Map<String,Object> gui=value.getGui();if(gui==null)continue;
            String type=text(gui.get("businessType"));
            if(type.equalsIgnoreCase("Table")){criteria=null;continue;}
            if(type.equalsIgnoreCase("Row")){criteria=new ArrayList<>();result.put(value,criteria);continue;}
            if(!type.equalsIgnoreCase("Cell")||criteria==null)continue;
            result.put(value,criteria);
            String mode=text(value.getActionMode());
            if(!Set.of("519","Unknown_519","Constraint","1").contains(mode))continue;
            String raw=text(gui.get("rawValue"));if(Set.of("","{NULL}").contains(raw))continue;
            String operator=text(value.getOperator());
            Map<String,Object> criterion=new LinkedHashMap<>();
            criterion.put("sourceMode",mode);criterion.put("operator",operator);
            String index=text(gui.get("explicitName"));
            if(!index.isEmpty())criterion.put("cell",tableIndex(index,"cell"));
            criterion.put("type",raw.contains("*")?"TEXT_GLOB":"TEXT");
            criterion.put("expected",dateExpression(raw)!=null?dateValue(raw):ToscaValueTranslator.translateToscaValues(raw));
            criteria.add(criterion);
        }
        return result;
    }
    private static String browserUrl(CleanStep step) {
        if(!"TBox Start Program".equalsIgnoreCase(step.getModule())||step.getValues()==null)return null;
        String path="";StringBuilder args=new StringBuilder();
        for(StepValueDetails v:step.getValues()) {
            String name=text(v.getName());String tosca=text(v.getToscaPath());
            if(name.equalsIgnoreCase("Path")||tosca.equals("Path"))path=text(v.getValue());
            if(name.equalsIgnoreCase("arguments")||name.equalsIgnoreCase("Argument")||tosca.startsWith("Arguments"))
                args.append(text(v.getValue())).append(' ');
        }
        String lower=path.toLowerCase(Locale.ROOT);
        boolean browser=lower.matches(".*(chrome|chromium|msedge|firefox)(\\.exe)?.*") ||
                args.toString().contains("--silent-debugger-extension-api") || args.toString().contains("--restore-last-session");
        if(!browser)return null;
        var m=Pattern.compile("https?://[^\\s\"]+").matcher(args);
        if(!m.find())return null;String url=m.group();
        if(m.find())return null;return url;
    }
    private static Map<String,Object> assertion(StepValueDetails value,String expected,String businessType) {
        String property=text(value.getActionProperty());String operator=text(value.getOperator());
        boolean negative=operator.equals("2");
        if(!operator.isEmpty()&&!Set.of("0","1","2").contains(operator))throw new IllegalArgumentException("unsupported assertion operator " + operator);
        Map<String,Object> a=new LinkedHashMap<>();
        if(property.equalsIgnoreCase("rowcount")) {
            if(!businessType.equalsIgnoreCase("Table") || negative)
                throw new IllegalArgumentException("rowcount requires Table and equality operator");
            if(expected.isBlank())throw new IllegalArgumentException("rowcount expectation missing");
            a.put("type","ROW_COUNT");a.put("expected",expected);
        } else if(property.equalsIgnoreCase("Exists")||property.equalsIgnoreCase("Visible")||property.equalsIgnoreCase("Enabled")) {
            if(isBooleanReference(expected)) {
                a.put("type",property.equalsIgnoreCase("Exists")?"EXISTS":property.equalsIgnoreCase("Visible")?"VISIBLE":"ENABLED");
                a.put("expected",expected);
                if(negative)a.put("negate",true);
                return a;
            }
            if(expected.startsWith("=")&&isBoolean(expected.substring(1)))expected=expected.substring(1);
            if(!isBoolean(expected))throw new IllegalArgumentException("non-boolean " + property + " expectation");
            boolean wanted=truth(expected)^negative;
            if(property.equalsIgnoreCase("Exists")) {
                a.put("type",wanted?"EXISTS":"NOT_EXISTS");
            } else a.put("type",property.equalsIgnoreCase("Enabled")?(wanted?"ENABLED":"DISABLED"):(wanted?"VISIBLE":"HIDDEN"));
        } else {
            if(negative)throw new IllegalArgumentException("negative text/value assertion requires engine support");
            if(!property.isEmpty()&&!Set.of("innertext","innerhtml","value","text").contains(property.toLowerCase(Locale.ROOT)))
                throw new IllegalArgumentException("unsupported assertion property " + property);
            boolean input=property.equalsIgnoreCase("Value") || (property.isEmpty()&&businessType.equalsIgnoreCase("TextBox"));
            if(expected.contains("*")) {
                a.put("type",property.equalsIgnoreCase("InnerHtml")?"HTML_GLOB":input?"VALUE_GLOB":"TEXT_GLOB");a.put("expected",expected);
            } else {a.put("type",property.equalsIgnoreCase("InnerHtml")?"HTML":input?"VALUE":"TEXT");a.put("expected",expected);}
        }
        return a;
    }
    public static Map<String,Object> target(Map<String,Object> gui) {
        if(text(gui.get("businessType")).equalsIgnoreCase("Cell"))return indexedTableCell(gui);
        if(text(gui.get("businessType")).equalsIgnoreCase("Row"))return indexedTableRow(gui);
        List<Map<String,Object>> rowParents=maps(gui.get("valueParents"));
        if(rowParents.size()==2 && text(rowParents.get(1).get("businessType")).equalsIgnoreCase("Row")) {
            Map<String,Object> row=new LinkedHashMap<>(rowParents.get(1));
            String rowMode=text(row.get("actionMode")),rowValue=text(row.get("value"));
            boolean rowCheck=Set.of("69","Verify","101","WaitOn").contains(rowMode)
                    && Set.of("","InnerText","Text").contains(text(row.get("actionProperty")));
            boolean rowSelection=Set.of("517","Select").contains(rowMode) && Set.of("","{NULL}").contains(rowValue);
            if(!rowCheck && !rowSelection)throw new IllegalArgumentException("unsupported row context for descendant action");
            row.put("valueParents",List.of(rowParents.get(0)));
            Map<String,Object> resolved=indexedTableRow(row);
            List<Map<String,Object>> ancestry=maps(gui.get("attributeParents"));
            List<Map<String,Object>> expected=new ArrayList<>(maps(row.get("attributeParents")));expected.add(row);
            if(ancestry.size()!=expected.size())throw new IllegalArgumentException("row descendant attribute/value ancestry differs");
            for(int i=0;i<ancestry.size();i++)if(!Objects.equals(ancestry.get(i).get("moduleAttributeSurrogate"),expected.get(i).get("moduleAttributeSurrogate")))
                throw new IllegalArgumentException("row descendant attribute/value ancestry differs");
            Map<String,Object> child=new LinkedHashMap<>(gui);child.put("attributeParents",List.of());child.put("valueParents",List.of());
            Map<String,Object> props=new LinkedHashMap<>(map(child.get("properties")));
            Map<String,Object> date=dateExpression(text(props.get("Value")));
            if(date!=null && text(child.get("businessType")).equalsIgnoreCase("Button")) {
                props.remove("Value");child.put("properties",props);
                // Dynamic technical Value remains a runtime locator constraint, never a frozen date.
                String selector=css(child,true);
                resolved.put("descendant",Map.of("css",selector,"attributes",Map.of("value",date)));
            } else resolved.put("descendant",target(child));
            return resolved;
        }
        List<Map<String,Object>> ancestors=maps(gui.get("attributeParents"));
        List<Map<String,Object>> valueParents=maps(gui.get("valueParents"));
        List<String> frames=new ArrayList<>();String scope="";
        Set<String> added=new HashSet<>();
        for(Map<String,Object> parent:ancestors) {
            String type=text(parent.get("businessType"));
            if(type.equalsIgnoreCase("HtmlFrame")) {frames.add(css(parent));added.add(text(parent.get("moduleAttributeSurrogate")));}
            else if(type.equalsIgnoreCase("Table")) {
                Map<String,Object> context=valueParents.stream().filter(v->Objects.equals(v.get("moduleAttributeSurrogate"),parent.get("moduleAttributeSurrogate"))).findFirst()
                        .orElse(null);
                // A scanned Table can be a pure layout ancestor of a directly populated control.
                // Validate explicit selection context when present; preserve technical scope either way.
                if(context!=null)requirePlainTableScope(context);
                scope+=css(parent)+" ";added.add(text(parent.get("moduleAttributeSurrogate")));
            } else if(Set.of("Row","Column","Cell").contains(type))
                throw new IllegalArgumentException("table ancestry requires row/column constraint mapping");
            else if(!type.equalsIgnoreCase("HtmlDocument")) {scope+=css(parent)+" ";added.add(text(parent.get("moduleAttributeSurrogate")));}
        }
        for(Map<String,Object> parent:valueParents) {
            String type=text(parent.get("businessType"));
            if(type.equalsIgnoreCase("Table")) {
                requirePlainTableScope(parent);
                if(!added.contains(text(parent.get("moduleAttributeSurrogate"))))throw new IllegalArgumentException("Table selection absent from attribute ancestry");
                continue;
            }
            if(Set.of("Row","Column","Cell").contains(type))throw new IllegalArgumentException("table parent requires row/column constraint mapping");
            if(!added.add(text(parent.get("moduleAttributeSurrogate"))))continue;
            if(type.equalsIgnoreCase("HtmlFrame"))frames.add(css(parent));
            else if(!type.equalsIgnoreCase("HtmlDocument"))scope+=css(parent)+" ";
        }
        Map<String,Object> target=new LinkedHashMap<>();target.put("css",scope+css(gui));
        Map<String,Object> properties=map(gui.get("properties"));
        String inner=text(properties.get("InnerText"));
        if(inner.isEmpty())inner=text(properties.get("VisibleInnerText"));
        inner=ToscaValueTranslator.translateToscaValues(inner);
        boolean plainLink=text(gui.get("businessType")).equalsIgnoreCase("Link")
                && text(properties.get("Tag")).equalsIgnoreCase("a") && scope.isEmpty() && frames.isEmpty()
                && List.of("Id","Name","Title","ClassName","Type","AssociatedLabel").stream()
                   .allMatch(key->text(properties.get(key)).isEmpty());
        String buttonText=text(properties.get("Value"));
        boolean plainButton=text(gui.get("businessType")).equalsIgnoreCase("Button")
                && text(properties.get("Tag")).equalsIgnoreCase("INPUT") && scope.isEmpty() && frames.isEmpty()
                && List.of("Id","Name","Title","ClassName","Type","AssociatedLabel").stream()
                   .allMatch(key->text(properties.get(key)).isEmpty());
        if(plainButton && !buttonText.isEmpty() && !buttonText.contains("*") && !buttonText.contains("{REGEX[")) {
            target.clear();target.put("role","button");
            target.put("name",ToscaValueTranslator.translateToscaValues(buttonText));target.put("exact",true);
        }
        String linkName = TextLinkMigration.exactName(inner);
        if(plainLink && linkName != null) {
            target.clear();target.put("linkText",linkName);target.put("exact",true);
        }
        if(!frames.isEmpty())target.put("frames",frames);return target;
    }
    /** A plain Table selection scopes a technical descendant; it selects no row/cell. */
    private static void requirePlainTableScope(Map<String,Object> context) {
        if(!Set.of("517","Select","1","Constraint").contains(text(context.get("actionMode")))
                || !Set.of("","{NULL}").contains(text(context.get("value")))
                || !text(context.get("explicitName")).isEmpty()
                || !text(context.get("actionProperty")).isEmpty()
                || !text(context.get("disabledDescription")).isEmpty())
            throw new IllegalArgumentException("Table scope has selection constraints requiring mapping");
    }
    /** A row assertion and its child action retain the same one-based table row. */
    private static Map<String,Object> indexedTableRow(Map<String,Object> gui) {
        List<Map<String,Object>> parents=maps(gui.get("valueParents"));
        if(parents.size()!=1 || !text(parents.get(0).get("businessType")).equalsIgnoreCase("Table"))
            throw new IllegalArgumentException("row requires one Table value parent");
        Map<String,Object> table=parents.get(0);
        if(!Set.of("517","Select","1","Constraint").contains(text(table.get("actionMode"))) || !Set.of("","{NULL}").contains(text(table.get("value"))))
            throw new IllegalArgumentException("unsupported Table selection context");
        List<Map<String,Object>> expected=new ArrayList<>(maps(table.get("attributeParents")));expected.add(table);
        List<Map<String,Object>> actual=maps(gui.get("attributeParents"));
        if(actual.size()!=expected.size())throw new IllegalArgumentException("row attribute/value ancestry differs");
        for(int i=0;i<actual.size();i++)if(!Objects.equals(actual.get(i).get("moduleAttributeSurrogate"),expected.get(i).get("moduleAttributeSurrogate")))
            throw new IllegalArgumentException("row attribute/value ancestry differs");
        Map<String,Object> resolved=new LinkedHashMap<>(target(table));
        if(text(gui.get("explicitName")).isEmpty()&&!maps(gui.get("rowCriteria")).isEmpty())
            resolved.put("tableRow",Map.of("rowCellsAll",maps(gui.get("rowCriteria"))));
        else resolved.put("tableRow",Map.of("row",tableRowPosition(text(gui.get("explicitName")))));
        return resolved;
    }
    /** Numeric Tosca table coordinates are one-based physical row/cell positions. */
    private static Map<String,Object> indexedTableCell(Map<String,Object> gui) {
        List<Map<String,Object>> parents=maps(gui.get("valueParents"));
        if(parents.size()!=2 || !text(parents.get(0).get("businessType")).equalsIgnoreCase("Table")
                || !text(parents.get(1).get("businessType")).equalsIgnoreCase("Row"))
            throw new IllegalArgumentException("numeric table cell requires Table -> Row value context; named/column/constraint contexts need mapping");
        Map<String,Object> table=parents.get(0),row=parents.get(1);
        int cellIndex=tableIndex(text(gui.get("explicitName")),"cell");
        String rowName=text(row.get("explicitName"));
        String rowValue=text(row.get("value"));
        boolean lastRow=rowName.equals("$last");
        boolean textRow=(rowName.isEmpty() || lastRow) && !Set.of("","{NULL}").contains(rowValue);
        Map<String,Object> coordinates=new LinkedHashMap<>();
        if(textRow) {
            if(!Set.of("517","Select","1","Constraint","519","Unknown_519").contains(text(row.get("actionMode"))))
                throw new IllegalArgumentException("text row selection requires Select action mode");
            String translated=ToscaValueTranslator.translateToscaValues(rowValue);
            coordinates.put(translated.contains("*")?"rowTextGlob":"rowText",translated);
            if(lastRow)coordinates.put("row","last");
        } else if(rowName.isEmpty() && !maps(gui.get("rowCriteria")).isEmpty()) {
            coordinates.put("rowCellsAll",maps(gui.get("rowCriteria")));
        } else coordinates.put("row",tableRowPosition(rowName));
        coordinates.put("cell",cellIndex);
        for(Map<String,Object> context:parents) {
            String mode=text(context.get("actionMode")),raw=text(context.get("value"));
            boolean allowedMode=Set.of("517","Select").contains(mode)
                    || Set.of("1","Constraint").contains(mode);
            boolean tableVerification=context==table && Set.of("69","Verify","101","WaitOn").contains(mode)
                    && text(context.get("actionProperty")).equalsIgnoreCase("rowcount");
            if(!tableVerification && (!allowedMode || (!Set.of("","{NULL}").contains(raw) && !(context==row && textRow))))
                throw new IllegalArgumentException("table context " + text(context.get("businessType"))
                        + " has unsupported action mode or non-empty selection value requiring mapping");
        }
        List<Map<String,Object>> expected=new ArrayList<>(maps(table.get("attributeParents")));expected.add(table);expected.add(row);
        List<Map<String,Object>> ancestors=maps(gui.get("attributeParents"));
        if(ancestors.size()!=expected.size())throw new IllegalArgumentException("table attribute/value ancestry differs");
        for(int i=0;i<expected.size();i++)if(!Objects.equals(ancestors.get(i).get("moduleAttributeSurrogate"),expected.get(i).get("moduleAttributeSurrogate")))
            throw new IllegalArgumentException("table attribute/value ancestry differs");
        Map<String,Object> resolved=new LinkedHashMap<>(target(table));
        resolved.put("tableCell",coordinates);
        return resolved;
    }
    /** Keep relative last-row selection dynamic; numeric positions remain one-based. */
    private static Object tableRowPosition(String position) {
        return "$last".equals(position)?"last":tableIndex(position,"row");
    }
    private static int tableIndex(String index,String axis) {
        if(!index.matches("#[1-9][0-9]*"))throw new IllegalArgumentException("unsupported "+axis+" selection '"+index+"'; expected positive numeric #index");
        try{return Integer.parseInt(index.substring(1));}
        catch(NumberFormatException e){throw new IllegalArgumentException(axis+" index exceeds supported integer range");}
    }
    private static String css(Map<String,Object> gui) { return css(gui,false); }
    private static String css(Map<String,Object> gui, boolean dynamicValueConstraint) {
        Map<String,Object> props=map(gui.get("properties"));String tag=text(props.get("Tag"));
        String selector="";
        if(tag.equalsIgnoreCase("*FRAME"))selector=":is(frame,iframe)";
        else if(tag.startsWith("{REGEX[") && tag.endsWith("]}")) {
            String alternatives=tag.substring(7,tag.length()-2);
            if(alternatives.startsWith("\"") && alternatives.endsWith("\""))alternatives=alternatives.substring(1,alternatives.length()-1);
            if(!alternatives.matches("[A-Za-z][A-Za-z0-9]*(\\|[A-Za-z][A-Za-z0-9]*)*"))throw new IllegalArgumentException("unsupported Tag pattern");
            selector=":is("+alternatives.toLowerCase(Locale.ROOT).replace('|',',')+")";
        }
        else if(tag.matches("[a-zA-Z][a-zA-Z0-9]*"))selector=tag.toLowerCase(Locale.ROOT);
        else if(!tag.isEmpty())throw new IllegalArgumentException("unsupported Tag pattern");
        String id=text(props.get("Id"));String name=text(props.get("Name"));
        String buttonValue=text(gui.get("businessType")).equalsIgnoreCase("Button")
                && tag.equalsIgnoreCase("INPUT")?text(props.get("Value")):"";
        if(!id.isEmpty())selector+=attributeSelector("id",id);
        else if(!name.isEmpty())selector+=attributeSelector("name",name);
        else if(!dynamicValueConstraint && buttonValue.isEmpty() && text(props.get("InnerText")).isEmpty() && text(props.get("VisibleInnerText")).isEmpty())
            throw new IllegalArgumentException("no active Id/Name/text locator; scanned self-healing data is not authoritative");
        if(!id.isEmpty()&&!name.isEmpty()&&(id.startsWith("{REGEX[")||name.startsWith("{REGEX[")))selector+=attributeSelector("name",name);
        if(!buttonValue.isEmpty())selector+=attributeSelector("value",ToscaValueTranslator.translateToscaValues(buttonValue));
        String inner=text(props.get("InnerText"));
        if(inner.isEmpty())inner=text(props.get("VisibleInnerText"));
        if(!inner.isEmpty()) {
            // Playwright CSS supports text pseudo-classes; use only active Tosca identification properties.
            inner=ToscaValueTranslator.translateToscaValues(inner);
            if(inner.contains("{REGEX["))throw new IllegalArgumentException("regex text locator requires explicit mapping");
            if(inner.contains("*") && inner.contains("{"))throw new IllegalArgumentException("dynamic wildcard text locator requires explicit mapping");
            if(inner.contains("*")) {
                String regex="^"+String.join(".*",Arrays.stream(inner.split("\\*",-1)).map(ProcessGuiStep::jsQuote).toList())+"$";
                selector+=":text-matches("+yamlString(regex)+", \"s\")";
            } else selector+=":text-is("+yamlString(inner)+")";
        }
        // Other active technical identification properties must not be dropped silently.
        for(String key:List.of("Title","ClassName","Type","AssociatedLabel"))
            if(!text(props.get(key)).isEmpty())throw new IllegalArgumentException("additional locator property " + key + " requires mapping");
        if(selector.isEmpty())throw new IllegalArgumentException("regex attributes provide no restriction and no active Tag/text locator is available");
        return selector;
    }
    public static String attributeSelector(String attribute,String pattern) {
        if(pattern.startsWith("{REGEX["))return literalRegexAttribute(attribute,pattern);
        if(pattern.contains("{")||pattern.contains("}"))throw new IllegalArgumentException("dynamic locator requires explicit mapping");
        String operator="=";String literal=pattern;
        boolean start=literal.startsWith("*"),end=literal.endsWith("*");
        if(start)literal=literal.substring(1);if(end&&!literal.isEmpty())literal=literal.substring(0,literal.length()-1);
        if(literal.isEmpty()||literal.contains("*")||literal.contains("?"))throw new IllegalArgumentException("unsupported locator wildcard");
        if(start&&end)operator="*=";else if(start)operator="$=";else if(end)operator="^=";
        return "["+attribute+operator+"\""+literal.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\a ").replace("\r","\\d ")+"\"]";
    }
    /** Substring regex semantics, restricted to literal alternatives (not arbitrary .NET regex). */
    private static String literalRegexAttribute(String attribute,String expression) {
        if(!expression.endsWith("]}"))throw new IllegalArgumentException("malformed REGEX locator expression");
        String body=expression.substring(7,expression.length()-2);
        if(body.startsWith("\"")&&body.endsWith("\"")&&body.length()>=2)body=body.substring(1,body.length()-1);
        List<String> alternatives=new ArrayList<>();StringBuilder literal=new StringBuilder();
        for(int i=0;i<body.length();i++) {
            char c=body.charAt(i);
            if(c=='\\') {
                if(++i>=body.length())throw new IllegalArgumentException("incomplete escape in REGEX locator");
                char escaped=body.charAt(i);
                if("\\.^$|?*+()[]{}-".indexOf(escaped)<0)throw new IllegalArgumentException("regex locator requires mapping beyond literal alternatives");
                literal.append(escaped);
            } else if(c=='|') {alternatives.add(literal.toString());literal.setLength(0);}
            else if(".^$?*+()[]{}".indexOf(c)>=0)throw new IllegalArgumentException("regex locator requires mapping beyond literal alternatives");
            else literal.append(c);
        }
        alternatives.add(literal.toString());
        // An empty alternative matches the empty substring in every attribute value.
        if(alternatives.contains(""))return "";
        List<String> selectors=alternatives.stream().distinct().map(v->"["+attribute+"*="+yamlString(v)+"]").toList();
        return selectors.size()==1?selectors.get(0):":is("+String.join(",",selectors)+")";
    }
    private static Map<String,Object> base(CleanStep step,String suffix,String op) {
        Map<String,Object> out=new LinkedHashMap<>();
        String id=text(step.getSurrogate());if(id.isEmpty())id="gui_"+step.getIndex();
        out.put("id",id+"_"+(suffix.isEmpty()?op.toLowerCase(Locale.ROOT):suffix));out.put("type","GUI");out.put("op",op);
        out.put("page",step.getModule());return out;
    }
    private static void condition(Map<String,Object> output,String step,String value) {
        List<String> parts=new ArrayList<>();
        for(String p:Arrays.asList(step,value))if(p!=null&&!p.isBlank()&&!parts.contains(p))parts.add(p);
        if(!parts.isEmpty())output.put("condition",ToscaValueTranslator.translateToscaValues(String.join(" AND ",parts.stream().map(p->"("+p+")").toList())));
    }
    private static String yamlString(String value) {
        return "\""+value.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r")+"\"";
    }
    private static String jsQuote(String value) {
        StringBuilder out=new StringBuilder();
        for(char c:value.toCharArray()) {
            if("\\.^$|?*+()[]{}".indexOf(c)>=0)out.append('\\');
            out.append(c);
        }
        return out.toString();
    }
    private static String wildcard(String value) {
        // Java and JavaScript compatible source: Playwright assertions execute regex in the browser.
        return "^" + String.join(".*",Arrays.stream(value.split("\\*",-1)).map(ProcessGuiStep::jsQuote).toList()) + "$";
    }
    /** A complete runtime reference is resolved and validated as boolean by the engine. */
    /** Presentation lowering only: internal translate() remains compatible with IF processing. */
    public static void humanReadable(Map<String,Object> step) {
        String op=text(step.get("op"));
        if ("ASSERT".equals(op) && step.get("assertion") instanceof Map<?,?> a) {
            // Preserve advanced assertions with properties/negation; do not drop constraints.
            if (a.keySet().stream().allMatch(k->Set.of("type","expected").contains(k))) {
                step.remove("assertion"); step.put("action", a.get("type"));
                if (a.containsKey("expected")) step.put("expected", a.get("expected"));
            }
        } else if (Set.of("OPEN","BACK","CLICK","FILL","SELECT","CHECK","UNCHECK","HOVER","PRESS","UPLOAD","DOWNLOAD","WAIT","SCREENSHOT").contains(op)) {
            step.put("op", "EXEC"); step.put("action", op);
        }
        // Human-readable SELECT: use one dropdown block, without duplicating target/value.
        if ("SELECT".equals(text(step.get("action")))) {
            if (step.get("select") instanceof Map<?,?> select) {
                Map<String,Object> dropdown = new LinkedHashMap<>(map(select));
                Object label = dropdown.remove("label");
                if (label != null) {
                    dropdown.put("value", label);
                    step.remove("select");
                    step.put("dropdown", dropdown);
                }
            } else if (step.get("value") instanceof Map<?,?> value && map(value).containsKey("label")) {
                Map<String,Object> originalTarget = map(step.get("target"));
                Map<String,Object> dropdown = new LinkedHashMap<>();
                String css = text(originalTarget.get("css"));
                java.util.regex.Matcher match=java.util.regex.Pattern.compile("^select\\[id\\*=\"([^\"]+)\"\\]$").matcher(css);
                if(match.matches()) {
                    dropdown.put("idContains",match.group(1));
                    dropdown.put("value",map(value).get("label"));
                    step.remove("target"); step.remove("value"); step.put("dropdown",dropdown);
                }
            }
        }
        // A SELECT with an exact-ID CSS locator and a visible label is one
        // dropdown DSL block. Never leave a top-level value next to dropdown.
        if ("SELECT".equals(text(step.get("action")))
                && step.get("value") instanceof Map<?,?> value
                && map(value).containsKey("label") && step.get("target") instanceof Map<?,?>) {
            String css = text(map(step.get("target")).get("css"));
            java.util.regex.Matcher exact = java.util.regex.Pattern
                    .compile("^select\\[id=\"([^\"]+)\"\\]$").matcher(css);
            if (exact.matches()) {
                Map<String,Object> dropdown = new LinkedHashMap<>();
                dropdown.put("id", exact.group(1));
                dropdown.put("value", map(value).get("label"));
                step.remove("target");
                step.remove("value");
                step.put("dropdown", dropdown);
            }
        }
        // PIN fields: preserve original CSS id-suffix semantics (unlike unbound label: PIN).
        if ("FILL".equals(text(step.get("action"))) && step.get("value") != null) {
            Map<String,Object> oldTarget=map(step.get("target"));
            String css=text(oldTarget.get("css"));
            java.util.regex.Matcher pin=java.util.regex.Pattern.compile("^:is\\(input,textarea\\)\\[id\\$=\"([^\"]+)\"\\]$").matcher(css);
            if(pin.matches()) {
                Map<String,Object> textbox = new LinkedHashMap<>();
                textbox.put("idEndsWith", pin.group(1));
                textbox.put("value", step.remove("value"));
                step.remove("target"); step.put("textbox",textbox);
            }
        }
        // Plain HTML submit buttons are usually represented as input[id="..."].
        if ("CLICK".equals(text(step.get("action"))) && step.get("target") instanceof Map<?,?>) {
            String css=text(map(step.get("target")).get("css"));
            java.util.regex.Matcher input=java.util.regex.Pattern.compile("^input\\[id=\"([^\"]+)\"\\]$").matcher(css);
            if (input.matches()) {
                // Known e-card submit control: its visible HTML value is '(W)eiter'.
                // Restrict this translation to the exact control; never guess visible text
                // for other IDs.
                step.remove("target");
                if ("WelcomeForm:continue".equals(input.group(1))
                        && "Kartenleserauswahl & PIN-Eingabe (001)_new".equals(text(step.get("page")))) {
                    step.put("button", Map.of("text", "(W)eiter"));
                } else {
                    step.put("button", Map.of("id", input.group(1)));
                }
            }
        }
        Map<String,Object> target = map(step.get("target"));
        if (!target.isEmpty() && Set.of("css","nth","role","name","exact","linkText").containsAll(target.keySet())) {
        Map<String,Object> element=new LinkedHashMap<>();String kind=null;
        if(target.containsKey("linkText")) {
            kind="link";element.put("text",target.get("linkText"));element.put("exact",target.getOrDefault("exact",true));
        } else if (target.containsKey("role") && target.containsKey("name") && !target.containsKey("css")) {
            String role=text(target.get("role"));
            if(Set.of("button","heading","checkbox","radio").contains(role)) {
                kind=role;element.put(Set.of("checkbox","radio").contains(role)?"label":"text",target.get("name"));
                element.put("exact",target.getOrDefault("exact",true));
            }
        } else if (target.get("css") instanceof String css && !target.containsKey("role") && !target.containsKey("name") && !target.containsKey("exact")) {
            if(css.startsWith("a:text-is(") && css.endsWith(")")) {
                String value=decodeJsonString(css.substring("a:text-is(".length(),css.length()-1));
                if(value!=null){kind="link";element.put("text",value);element.put("exact",true);}
            } else {
                for(String tag:List.of("a","button","select","textarea")) {
                    String prefix=tag+"[id=";
                    if(css.startsWith(prefix) && css.endsWith("]")) {
                        String value=decodeJsonString(css.substring(prefix.length(),css.length()-1));
                        if(value!=null) {
                            kind=switch(tag){case "a"->"link";case "button"->"button";case "select"->"dropdown";default->"textbox";};
                            element.put("id",value);break;
                        }
                    }
                }
            }
        }
        if(kind != null) {
        if(target.containsKey("nth")) {
            Object nth=target.get("nth");
            if(!(nth instanceof Integer || nth instanceof Long) || ((Number)nth).longValue()<0 || ((Number)nth).longValue()>Integer.MAX_VALUE) kind=null;
            element.put("index",nth);
        }
        if (kind!=null && element.values().stream().noneMatch(v->v instanceof String t && t.isBlank())) {
            step.remove("target");step.put(kind,element);
        }
        }
        }
        normalizeGuiNullTokens(step);
        reorderActionBeforePage(step);
        // Tosca business page/module names are not proven browser captions.
    }
    /** NULL in a text field is empty; NULL inside a text glob contributes no characters.
     * Apply only to these two GUI actions, leaving other NULL semantics untouched. */
    static void normalizeGuiNullTokens(Map<String,Object> step) {
        if (!"GUI".equals(text(step.get("type")))) return;
        String action = text(step.get("action"));
        if ("FILL".equals(action)) {
            Object value = step.get("value");
            if (value instanceof String s && isNullToken(s)) step.put("value", "");
            Object textbox = step.get("textbox");
            if (textbox instanceof Map<?,?> t && t.get("value") instanceof String s && isNullToken(s)) {
                Map<String,Object> normalized = new LinkedHashMap<>(map(t));
                normalized.put("value", "");
                step.put("textbox", normalized);
            }
        } else if ("TEXT_GLOB".equals(action)) {
            Object expected = step.get("expected");
            if (expected instanceof String s) {
                step.put("expected", s.replace("\"{NULL}\"", "").replace("{NULL}", ""));
            }
        }
    }

    private static boolean isNullToken(String raw) {
        return "{NULL}".equals(raw) || "\"{NULL}\"".equals(raw);
    }

    private static void reorderActionBeforePage(Map<String,Object> step) {
        if (!step.containsKey("action") || !step.containsKey("page")) return;
        Map<String,Object> ordered=new LinkedHashMap<>();
        for (var entry:step.entrySet()) {
            if (entry.getKey().equals("page")) {
                ordered.put("action",step.get("action"));
                ordered.put("page",entry.getValue());
            } else if (!entry.getKey().equals("action")) ordered.put(entry.getKey(),entry.getValue());
        }
        step.clear(); step.putAll(ordered);
    }
    private static String decodeJsonString(String json) {
        // The selector generator emits JSON-style strings; CSS hexadecimal escapes stay technical.
        if(!json.startsWith("\"") || !json.endsWith("\""))return null;
        try{return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json,String.class);}
        catch(Exception ignored){return null;}
    }

    private static boolean isBooleanReference(String value) {
        return value.matches("\\{(?:PL|P|B|E|R|XL)\\[[^\\[\\]{}]+\\]\\}");
    }
    private static boolean isBoolean(String value) {return Set.of("true","false","true*","false*","1","0","x").contains(value.toLowerCase(Locale.ROOT));}
    private static boolean truth(String value) {return Set.of("true","true*","1","x").contains(value.toLowerCase(Locale.ROOT));}
    private static String text(Object value) {return Objects.toString(value,"");}
    @SuppressWarnings("unchecked") private static Map<String,Object> map(Object value) {return value instanceof Map?(Map<String,Object>)value:Map.of();}
    @SuppressWarnings("unchecked") private static List<Map<String,Object>> maps(Object value) {return value instanceof List?(List<Map<String,Object>>)value:List.of();}
}
