package at.co.svc.aga.transformator.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CleanStep {

    private int index;
    private String name;
    private String type;
    private String op;
    private String surrogate;
    private String module;
    private String moduleSurrogate;
    private String reusableName;
    private String reusableSurrogate;
    private String condition;
    private String moduleClass;
    private List<StepValueDetails> values;
    private String action;
    private List<Constraint> constraints;

    /*
     * =========================================================
     * CONTROL FLOW
     * =========================================================
     *
     * These fields are optional and are used only by structured
     * AGATE control-flow steps such as LOOP.
     *
     * Normal existing steps:
     *
     *   SQL
     *   REST
     *   SOAP
     *   WAIT
     *   CMD
     *   CALL
     *   ...
     *
     * do not use these fields and therefore continue to behave
     * exactly as before.
     */

    private Integer maxIterations;

    private List<CleanStep> steps;


    // =========================================================
    // CONTROL FLOW
    // =========================================================

    public Integer getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(
            Integer maxIterations) {

        this.maxIterations =
                maxIterations;
    }

    public List<CleanStep> getSteps() {
        return steps;
    }

    public void setSteps(
            List<CleanStep> steps) {

        this.steps =
                steps;
    }

    /**
     * Convenience helper for structured steps such as LOOP.
     *
     * This intentionally does not affect existing step processing.
     */
    @JsonIgnore
    public boolean hasNestedSteps() {

        return steps != null
                && !steps.isEmpty();
    }


    // =========================================================
    // CONSTRAINTS
    // =========================================================

    public List<Constraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(
            List<Constraint> constraints) {

        this.constraints =
                constraints;
    }


    // =========================================================
    // VALUES
    // =========================================================

    /**
     * Convenience lookup map.
     *
     * IMPORTANT:
     * Tosca may contain the same ExplicitName more than once in one step.
     * The raw list in {@link #values} is therefore the source of truth.
     *
     * For lookup-only use cases this map intentionally keeps the LAST
     * occurrence, because later Tosca assignments override earlier ones.
     *
     * Example:
     *
     *   L_Verify = not-set
     *   L_Verify = {PL[Verify]}
     *
     * Lookup must return {PL[Verify]}, not not-set.
     */
    @JsonIgnore
    public Map<String, StepValueDetails> getValuesAsMap() {

        Map<String, StepValueDetails> result =
                new LinkedHashMap<>();

        if (values == null) {
            return result;
        }

        for (StepValueDetails value : values) {

            if (value == null
                    || value.getName() == null) {

                continue;
            }

            result.put(
                    value.getName(),
                    value
            );
        }

        return result;
    }

    /**
     * Returns all values with the requested name, preserving Tosca order.
     * Use this for modules where duplicate attributes are meaningful,
     * e.g. TBox Set Buffer and repeated Start Program arguments.
     */
    @JsonIgnore
    public List<StepValueDetails> getValuesByNameIgnoreCase(
            String requestedName) {

        List<StepValueDetails> result =
                new ArrayList<>();

        if (values == null
                || requestedName == null) {

            return result;
        }

        for (StepValueDetails value : values) {

            if (value != null
                    && value.getName() != null
                    && value.getName()
                    .equalsIgnoreCase(
                            requestedName
                    )) {

                result.add(
                        value
                );
            }
        }

        return result;
    }

    /**
     * Returns all values whose name ends with the requested suffix.
     *
     * Useful for flattened Tosca attributes such as:
     *
     *   WaitForExit.StandardOutputFile
     *   Arguments.Argument
     */
    @JsonIgnore
    public List<StepValueDetails> getValuesByNameSuffixIgnoreCase(
            String suffix) {

        List<StepValueDetails> result =
                new ArrayList<>();

        if (values == null
                || suffix == null) {

            return result;
        }

        String normalizedSuffix =
                suffix.toLowerCase();

        for (StepValueDetails value : values) {

            if (value == null
                    || value.getName() == null) {

                continue;
            }

            String name =
                    value.getName()
                            .toLowerCase();

            if (name.equals(
                    normalizedSuffix)
                    || name.endsWith(
                    "." + normalizedSuffix)) {

                result.add(
                        value
                );
            }
        }

        return result;
    }


    // =========================================================
    // MODULE
    // =========================================================

    public String getModuleClass() {
        return moduleClass;
    }

    public void setModuleClass(
            String moduleClass) {

        this.moduleClass =
                moduleClass;
    }


    // =========================================================
    // ACTION
    // =========================================================

    public String getAction() {
        return action;
    }

    public void setAction(
            String action) {

        this.action =
                action;
    }


    // =========================================================
    // INDEX
    // =========================================================

    public int getIndex() {
        return index;
    }

    public void setIndex(
            int index) {

        this.index =
                index;
    }


    // =========================================================
    // NAME
    // =========================================================

    public String getName() {
        return name;
    }

    public void setName(
            String name) {

        this.name =
                name;
    }


    // =========================================================
    // TYPE
    // =========================================================

    public String getType() {
        return type;
    }

    public void setType(
            String type) {

        this.type =
                type;
    }


    // =========================================================
    // SURROGATE
    // =========================================================

    public String getSurrogate() {
        return surrogate;
    }

    public void setSurrogate(
            String surrogate) {

        this.surrogate =
                surrogate;
    }


    // =========================================================
    // MODULE
    // =========================================================

    public String getModule() {
        return module;
    }

    public void setModule(
            String module) {

        this.module =
                module;
    }

    public String getModuleSurrogate() {
        return moduleSurrogate;
    }

    public void setModuleSurrogate(
            String moduleSurrogate) {

        this.moduleSurrogate =
                moduleSurrogate;
    }


    // =========================================================
    // REUSABLE
    // =========================================================

    public String getReusableName() {
        return reusableName;
    }

    public void setReusableName(
            String reusableName) {

        this.reusableName =
                reusableName;
    }

    public String getReusableSurrogate() {
        return reusableSurrogate;
    }

    public void setReusableSurrogate(
            String reusableSurrogate) {

        this.reusableSurrogate =
                reusableSurrogate;
    }


    // =========================================================
    // CONDITION
    // =========================================================

    public String getCondition() {
        return condition;
    }

    public void setCondition(
            String condition) {

        this.condition =
                condition;
    }


    // =========================================================
    // OP
    // =========================================================

    public String getOp() {
        return op;
    }

    public void setOp(
            String op) {

        this.op =
                op;
    }


    // =========================================================
    // CONDITION COMBINATION
    // =========================================================

    /**
     * Combines a propagated condition with a local condition.
     * Avoids "(null)" and avoids adding the same propagated condition twice.
     */
    public String getCombinedCondition(
            String xCondition,
            String condition) {

        String x =
                normalizeCondition(
                        xCondition
                );

        String c =
                normalizeCondition(
                        condition
                );

        if (x.isEmpty()) {
            return c;
        }

        if (c.isEmpty()) {
            return x;
        }

        String wrappedX =
                "(" + x + ")";

        if (c.contains(
                wrappedX)
                || c.equals(x)) {

            return c;
        }

        return "("
                + c
                + ") AND ("
                + x
                + ")";
    }

    private static String normalizeCondition(
            String value) {

        return value == null
                ? ""
                : value.trim();
    }


    // =========================================================
    // RAW VALUES
    // =========================================================

    @JsonProperty("values")
    public void setValues(
            List<StepValueDetails> values) {

        this.values =
                values;
    }

    public List<StepValueDetails> getValues() {
        return values;
    }
}