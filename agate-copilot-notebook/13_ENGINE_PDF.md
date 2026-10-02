# AGATE PDF Engine

The PDF Engine analyzes and validates PDF documents by extracting their text content and executing one or more assertions against that text.

It also supports password-protected PDFs and dynamic AGATE placeholders in relevant fields.

The canonical operation is:

```text
ASSERT
```

> This document describes PDF-specific behavior. General AGATE test structure, YAML rules, placeholders and conditions are documented separately.

---

# 1. Quick Reference

```yaml
- type: PDF
  op: ASSERT
  targetPDF: '../Tosca/Daten/Files/MUHI/{B[B_FileName]}'
  pdfPassword: 'secret_password'
  condition: "DialogException == NULL AND MuhiException == NULL"
  assertions:
    - value: 'Versicherungsnummer'
      action: EXIST

    - value: 'Greska'
      action: NO_EXIST

    - value: 'MeldungsId'
      action: COUNT
      expected: '{B[expected_count]}'
```

---

# 2. Purpose

Use the PDF Engine when a test must validate textual content in a PDF document.

Typical use cases:

- verify that expected text exists,
- verify that forbidden/error text does not exist,
- count exact occurrences of a term,
- validate generated PDF output,
- validate password-protected PDFs,
- use dynamically resolved file names or paths.

---

# 3. Operation

The documented PDF operation is:

```text
ASSERT
```

Example:

```yaml
- type: PDF
  op: ASSERT
  targetPDF: 'output/report.pdf'
  assertions:
    - value: 'OK'
      action: EXIST
```

---

# 4. Default Operation

`op: ASSERT` is optional.

Therefore:

```yaml
- type: PDF
  targetPDF: 'output/report.pdf'
  assertions:
    - value: 'OK'
      action: EXIST
```

is equivalent to:

```yaml
- type: PDF
  op: ASSERT
  targetPDF: 'output/report.pdf'
  assertions:
    - value: 'OK'
      action: EXIST
```

For generated tests, prefer writing:

```yaml
op: ASSERT
```

explicitly for readability and consistency.

---

# 5. Core Parameters

| Field | Required | Description |
|---|---:|---|
| `targetPDF` | Yes | File name or path of the PDF to validate. Supports dynamic buffer values. |
| `pdfPassword` | No | Password used to open a protected PDF. |
| `condition` | No | Common AGATE execution condition for the complete PDF step. |
| `assertions` | Yes | List of validation objects. |

Canonical structure:

```yaml
- type: PDF
  op: ASSERT
  targetPDF: '...'
  pdfPassword: '...'
  condition: '...'
  assertions:
    - value: '...'
      action: EXIST
```

---

# 6. targetPDF

`targetPDF` identifies the PDF file to validate.

Example:

```yaml
targetPDF: 'output/report.pdf'
```

Dynamic placeholders are supported.

Example:

```yaml
targetPDF: '../Tosca/Daten/Files/MUHI/{B[B_FileName]}'
```

This allows the file name or path to be determined by earlier test steps.

---

# 7. pdfPassword

Use `pdfPassword` when the PDF is password protected.

Example:

```yaml
pdfPassword: 'secret_password'
```

If the document is not encrypted, omit this field.

Do not invent or add a password when none is required.

---

# 8. condition

The PDF step supports the common AGATE `condition`.

Example:

```yaml
condition: "DialogException == NULL AND MuhiException == NULL"
```

If the condition evaluates to false, the PDF validation step is skipped according to the common AGATE condition rules.

---

# 9. assertions

`assertions` is a list.

Each assertion contains:

```text
value
action
expected   # only required for COUNT
```

Example:

```yaml
assertions:
  - value: 'Versicherungsnummer'
    action: EXIST

  - value: 'Greska'
    action: NO_EXIST

  - value: 'MeldungsId'
    action: COUNT
    expected: '2'
```

Assertions are executed sequentially.

If one assertion fails, the step follows fail-fast behavior and stops immediately.

---

# 10. EXIST

`EXIST` checks whether the specified text exists anywhere in the extracted PDF text.

Example:

```yaml
- value: 'Versicherungsnummer'
  action: EXIST
```

Meaning:

```text
The extracted PDF text must contain:
Versicherungsnummer
```

No `expected` value is required.

---

# 11. NO_EXIST

`NO_EXIST` checks that the specified text does not occur in the extracted PDF text.

Example:

```yaml
- value: 'Greska'
  action: NO_EXIST
```

Meaning:

```text
The extracted PDF text must not contain:
Greska
```

No `expected` value is required.

---

# 12. COUNT

`COUNT` counts the exact number of occurrences of the specified search text.

Example:

```yaml
- value: 'MeldungsId'
  action: COUNT
  expected: '2'
```

`expected` is required for COUNT.

Dynamic values are supported.

Example:

```yaml
- value: 'MeldungsId'
  action: COUNT
  expected: '{B[expected_count]}'
```

---

# 13. COUNT Default

The supplied documentation states that the expected count has a default of:

```text
1
```

when COUNT is used.

However, for generated tests, prefer specifying `expected` explicitly.

Preferred:

```yaml
- value: 'MeldungsId'
  action: COUNT
  expected: '1'
```

This makes the intended assertion clear.

---

# 14. Text Extraction Model

The PDF file is opened and its text is extracted using:

```text
PDFTextStripper
```

The complete text is loaded once into memory.

All assertions defined in the same PDF step are then evaluated against that already extracted text.

Conceptually:

```text
PDF file
   |
   v
PDFTextStripper
   |
   v
full extracted text in memory
   |
   +--> EXIST
   +--> NO_EXIST
   +--> COUNT
```

---

# 15. Performance Behavior

Even if a PDF step contains many assertions, the PDF is not reopened for every individual assertion.

Example:

```yaml
assertions:
  - value: 'A'
    action: EXIST
  - value: 'B'
    action: EXIST
  - value: 'C'
    action: NO_EXIST
  - value: 'MeldungsId'
    action: COUNT
    expected: '2'
```

The documented behavior is:

```text
open PDF once
extract text once
run all assertions in memory
```

This is the preferred way to validate multiple values in the same PDF.

---

# 16. Dynamic Placeholder Usage

Dynamic AGATE placeholders can be used in supported PDF fields.

Example:

```yaml
targetPDF: 'output/{B[file_name]}.pdf'
```

Example:

```yaml
- value: '{B[expected_name]}'
  action: EXIST
```

Example:

```yaml
- value: 'MeldungsId'
  action: COUNT
  expected: '{B[expected_count]}'
```

There is no need to create intermediate strings when direct placeholder composition is sufficient.

---

# 17. Complete Example

```yaml
testCases:

  - id: 'TC_PDF_CONTENT_VERIFICATION'
    description: 'Überprüfung eines generierten PDF-Dokuments auf Inhalt und Fehlerfreiheit'
    stage: '*'
    priority: HIGH

    variables:
      expected_count: '2'

    steps:

      - type: PDF
        op: ASSERT
        targetPDF: '../Tosca/Daten/Files/MUHI/{B[B_FileName]}'
        pdfPassword: 'secret_password'
        condition: "DialogException == NULL AND MuhiException == NULL"

        assertions:

          - value: 'Versicherungsnummer'
            action: EXIST

          - value: 'Greska'
            action: NO_EXIST

          - value: 'MeldungsId'
            action: COUNT
            expected: '{B[expected_count]}'
```

---

# 18. Scanned PDFs

The PDF Engine validates text extracted by `PDFTextStripper`.

If a PDF contains only scanned images and no embedded text layer, the engine cannot validate that visual text.

Example symptom:

```text
ASSERT FAILED: Text [...] does NOT exist in PDF
```

even though the text is visibly present on the page.

Reason:

```text
visible image text != extractable PDF text
```

The supplied documentation does not define OCR support for the PDF Engine.

Therefore, do not assume OCR is performed.

---

# 19. Line Breaks and Hyphenation

PDF text extraction can differ from how text visually appears.

Potential problems include:

- line breaks,
- control characters,
- text split across lines,
- hyphenation at line endings,
- words stored as separate PDF text fragments.

For example, a visually displayed word may internally be represented as:

```text
Versicherungs-
nummer
```

instead of:

```text
Versicherungsnummer
```

When an assertion unexpectedly fails, inspect the actually extracted text representation.

---

# 20. File Paths

`targetPDF` may use relative or absolute paths.

Prefer the shared AGATE YAML rule:

```text
use single quotes for path strings
```

especially for Windows paths.

Preferred:

```yaml
targetPDF: '..\Tosca\Daten\Files\MUHI\report.pdf'
```

or when forward slashes are supported by the environment:

```yaml
targetPDF: '../Tosca/Daten/Files/MUHI/report.pdf'
```

Avoid unnecessary escaping caused by double-quoted YAML strings.

---

# 21. Password-Protected Files

For encrypted PDFs:

```yaml
- type: PDF
  op: ASSERT
  targetPDF: 'protected/report.pdf'
  pdfPassword: '{B[pdf_password]}'
  assertions:
    - value: 'Versicherungsnummer'
      action: EXIST
```

If the password is wrong or missing, PDF loading cannot proceed successfully.

---

# 22. Multiple Assertions vs. Multiple PDF Steps

Preferred:

```yaml
- type: PDF
  op: ASSERT
  targetPDF: 'report.pdf'
  assertions:
    - value: 'A'
      action: EXIST
    - value: 'B'
      action: EXIST
    - value: 'ERROR'
      action: NO_EXIST
```

Instead of repeatedly reopening the same file:

```yaml
- type: PDF
  op: ASSERT
  targetPDF: 'report.pdf'
  assertions:
    - value: 'A'
      action: EXIST

- type: PDF
  op: ASSERT
  targetPDF: 'report.pdf'
  assertions:
    - value: 'B'
      action: EXIST
```

When all checks target the same PDF under the same condition, group them into one PDF step.

---

# 23. Fail-Fast

Assertions inside the PDF step are executed one after another.

If an assertion fails:

```text
the step stops immediately
```

Remaining assertions in that PDF step are not expected to continue after the failure.

This follows the documented fail-fast behavior.

---

# 24. Troubleshooting Checklist

## Text visibly exists but EXIST fails

Check whether the PDF contains real extractable text or only a scanned image.

The supplied PDF Engine documentation does not define OCR.

---

## Search term is split unexpectedly

Check:

- line breaks,
- hyphenation,
- whitespace,
- special/control characters,
- PDF internal text layout.

---

## targetPDF cannot be found

Check:

- relative path base,
- path separators,
- dynamic placeholder resolution,
- file name,
- whether the file was already generated by a previous step.

---

## condition behaves unexpectedly

Check the common AGATE condition syntax and variable names.

Example:

```yaml
condition: "DialogException == NULL AND MuhiException == NULL"
```

---

## Protected PDF cannot be opened

Check:

```yaml
pdfPassword: '...'
```

and verify that the supplied password matches the document.

---

# 25. Best Practices

1. Group multiple validations for the same PDF into one PDF step.
2. Prefer explicit `op: ASSERT`.
3. Use `EXIST` for required text.
4. Use `NO_EXIST` for forbidden/error text.
5. Use `COUNT` only when the exact number of occurrences matters.
6. Specify `expected` explicitly for COUNT.
7. Use dynamic placeholders directly in `targetPDF`, `value` and `expected` where appropriate.
8. Prefer single-quoted path values.
9. Do not assume OCR capability.
10. Do not create multiple PDF steps for the same file unless conditions or lifecycle requirements differ.
11. If validation fails unexpectedly, inspect the extracted text rather than only the visual PDF layout.
12. Use `condition` when PDF generation depends on a successful earlier flow.

---

# 26. AI Guidance

When generating PDF tests:

1. Use `type: PDF`.
2. Use `op: ASSERT`; although optional, prefer making it explicit.
3. Always provide `targetPDF`.
4. Always provide `assertions`.
5. Use only the documented actions:
   - `EXIST`
   - `NO_EXIST`
   - `COUNT`
6. `COUNT` requires an expected count; specify it explicitly.
7. Do not invent PDF operations such as `EXEC`, `READ`, `BUFFER` or `EXTRACT`.
8. Do not invent XPath, JSONPath or selectors for PDF validation.
9. Do not invent OCR behavior.
10. Do not assume visible scanned text is extractable.
11. Prefer one PDF step with multiple assertions for the same file.
12. Use `pdfPassword` only for protected files.
13. Follow shared AGATE YAML quoting rules for paths.
14. Allow placeholders directly in `targetPDF`, assertion `value` and `expected`.
15. Follow shared AGATE `condition` rules.
16. Do not convert PDF text-layout uncertainty into undocumented engine functionality.
17. Do not add `response`, `source`, `path`, `selector` or `name` to PDF assertions unless future PDF documentation explicitly defines them.

---

# 27. Short Reference

```text
PDF
└── ASSERT
    ├── targetPDF       [required]
    ├── pdfPassword     [optional]
    ├── condition       [optional]
    └── assertions      [required]
        ├── value
        ├── action
        │   ├── EXIST
        │   ├── NO_EXIST
        │   └── COUNT
        └── expected    [COUNT]
```

Execution model:

```text
open PDF once
    |
extract complete text
    |
run assertions sequentially
    |
fail immediately on first failed assertion
```
