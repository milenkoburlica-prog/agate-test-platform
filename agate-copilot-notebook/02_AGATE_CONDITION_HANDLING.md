# AGATE Condition Handling

`condition` is a shared AGATE DSL feature and is not engine-specific.

It can be used by normal steps and is also used by control-flow components.

## 1. General Semantics

```text
condition == true
    -> execute step

condition == false
    -> step is SKIPPED
```

`SKIPPED` is not `FAILED`.

Example:

```yaml
- type: WAIT
  condition: "{B[enabled]} == 'true'"
  value: '1000'
```

---

## 2. Placeholder Resolution

AGATE placeholders are resolved before condition evaluation.

Typical sources:

```text
{B[...]}
{R[...]}
```

Example:

```yaml
condition: "{B[status]} == 'READY'"
```

---

## 3. Supported Equality Operators

Equivalent forms:

```text
==
=
EQUALS
```

Examples:

```yaml
condition: "{B[status]} == 'READY'"
condition: "{B[status]} = 'READY'"
condition: "{B[status]} EQUALS 'READY'"
```

---

## 4. Supported Inequality Operators

```text
!=
<>
NOT_EQUALS
```

Examples:

```yaml
condition: "{B[status]} != 'FAILED'"
condition: "{B[status]} <> 'FAILED'"
condition: "{B[status]} NOT_EQUALS 'FAILED'"
```

---

## 5. CONTAINS

`CONTAINS` performs a case-sensitive substring check.

```yaml
condition: "{B[message]} CONTAINS 'SUCCESS'"
```

---

## 6. Logical Operators

Supported:

```text
AND
OR
NOT
```

Aliases:

```text
&& -> AND
|| -> OR
```

Preferred public/test syntax:

```text
AND
OR
```

Examples:

```yaml
condition: "{B[status]} == 'READY' AND {B[result]} == 'OK'"
```

```yaml
condition: "{B[status]} == 'READY' OR {B[status]} == 'DONE'"
```

```yaml
condition: "NOT {B[status]} == 'FAILED'"
```

---

## 7. Parentheses and Precedence

Parentheses are supported.

Effective precedence:

```text
(...)
NOT
AND
OR
```

Example:

```yaml
condition: "({B[a]} == 'X' OR {B[b]} == 'Y') AND {B[c]} == 'Z'"
```

Prefer explicit parentheses for complex expressions.

---

## 8. NULL

Use unquoted `NULL`.

Examples:

```yaml
condition: "{B[value]} == NULL"
```

```yaml
condition: "{B[value]} != NULL"
```

---

## 9. Empty / Null Unary Operators

Supported:

```text
IS_EMPTY
IS_NOT_EMPTY
IS_NULL
IS_NOT_NULL
```

Examples:

```yaml
condition: "{B[result]} IS_EMPTY"
condition: "{B[result]} IS_NOT_EMPTY"
```

The supplied documentation prefers these explicit NULL comparisons for generated DSL:

```yaml
condition: "{B[value]} == NULL"
condition: "{B[value]} != NULL"
```

---

## 10. String Semantics

Binary comparisons use string semantics.

Example:

```yaml
condition: "{B[count]} == 0"
```

is effectively a string comparison.

Do not interpret this as numeric ordering.

---

## 11. Unsupported Operators

Do not use:

```text
<
>
<=
>=
```

Examples that are not supported:

```yaml
condition: "{B[count]} > 5"
condition: "{B[count]} <= 20"
```

---

## 12. No Arithmetic

The condition evaluator is not a mathematical expression engine.

Do not generate:

```yaml
condition: "{B[counter]} + 1 == 5"
```

```yaml
condition: "{B[value]} * 2 == 10"
```

---

## 13. No Standalone Truthy Expressions

Do not generate:

```yaml
condition: 'true'
```

or:

```yaml
condition: '{B[enabled]}'
```

Prefer explicit comparison:

```yaml
condition: "{B[enabled]} == 'true'"
```

---

## 14. Case Sensitivity

Operator keywords are case-insensitive.

Compared values are case-sensitive.

```text
READY == READY -> true
ready == READY -> false
```

---

## 15. Known Parser Limitation

The current parser respects parentheses when splitting AND/OR expressions but does not fully treat quoted strings as protected regions.

Therefore avoid string literals that themselves contain:

```text
 AND 
 OR 
```

Example not recommended:

```yaml
condition: "{B[text]} == 'A OR B'"
```

---

## 16. Normal Steps vs Control Flow

Normal step:

```text
true  -> execute
false -> SKIPPED
```

LOOP:

```text
true  -> another iteration
false -> LOOP ends
```

LOOP-specific semantics belong in the LOOP engine documentation.

---

## 17. AI Guidance

1. Conditions are shared DSL, not engine-specific syntax.
2. Prefer small explicit expressions.
3. Prefer `AND` / `OR` over `&&` / `||`.
4. Use parentheses for complex logic.
5. Use unquoted `NULL`.
6. Do not generate numeric comparison operators.
7. Do not generate arithmetic.
8. Do not generate standalone truthy expressions.
9. Treat compared values as case-sensitive strings.
10. Prefer explicit `== 'true'` for boolean-looking values.
11. Keep string literals free of embedded ` AND ` / ` OR ` where possible.
