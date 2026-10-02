# AGATE SQL Engine

The SQL Engine provides database access for AGATE integration and system tests.

It supports:

- executing SQL statements,
- storing query results for later steps,
- extracting individual values into AGATE buffers,
- validating cells, result sets and row counts,
- filtering query results with constraints,
- conditional execution,
- and switching between configured database connections.

> This document describes the SQL Engine only. General AGATE concepts such as buffers, placeholders, templates, test data and re-instantiation are documented separately.

---

## 1. Quick Reference

The SQL Engine supports three operations:

```text
EXEC
ASSERT
BUFFER
```

### EXEC

```yaml
- type: SQL
  op: EXEC
  condition: "..."          # Optional
  datasource: "..."         # Optional named datasource
  command: |
    SELECT ...
    FROM ...
  response: sql_response
  constraints:              # Optional pre-filter
    - column: "STATUS"
      action: EQUALS
      expected: "ACTIVE"
```

### ASSERT

```yaml
- type: SQL
  op: ASSERT
  condition: "..."          # Optional
  response: sql_response
  source: ROW_COUNT         # Optional special source
  action: EQUALS
  row: 0                    # Optional, default: 0
  column: "STATUS"          # Name or zero-based index
  expected: "ACTIVE"
```

### BUFFER

```yaml
- type: SQL
  op: BUFFER
  condition: "..."          # Optional
  response: sql_response
  row: 0
  column: "ID"
  name: customer_id
```

---

## 2. Database Configuration

Database connection details are not stored directly in the test YAML.

AGATE loads them dynamically from the environment configuration for the active instance.

This keeps credentials and environment-specific connection information outside the test definition.

### 2.1 Default datasource

If an SQL `EXEC` step does not specify `datasource`, AGATE uses:

```text
<INSTANCE>.database.*
```

Example for Oracle:

```properties
# --- INSTANCE: ECS_SYST_AUT1 ---

ECS_SYST_AUT1.instanzKl=aut1
ECS_SYST_AUT1.instanzGr=AUT1

ECS_SYST_AUT1.database.user=...
ECS_SYST_AUT1.database.password=...
ECS_SYST_AUT1.database.connectionString=jdbc:oracle:thin:@ora-stag-scan.staging.infra.svc.co.at:1521/...
```

Real credentials and database-specific values are intentionally hidden with `...`.

The test then needs no connection details:

```yaml
- type: SQL
  op: EXEC
  command: SELECT STATUS FROM SYSTEM_STATUS
  response: res_status
```

### 2.2 PostgreSQL example

```properties
# --- INSTANCE: KS_AUT_INT_ECS_SYST_MIG ---

KS_AUT_INT_ECS_SYST_MIG.ks.instanzKl=aut
KS_AUT_INT_ECS_SYST_MIG.ks.instanzGr=AUT

KS_AUT_INT_ECS_SYST_MIG.ks.database.url=pgsql-kams-stag-15.staging.infra.svc.co.at
KS_AUT_INT_ECS_SYST_MIG.ks.database.user=...
KS_AUT_INT_ECS_SYST_MIG.ks.database.password=...
KS_AUT_INT_ECS_SYST_MIG.ks.database.dsn=...

KS_AUT_INT_ECS_SYST_MIG.database.user=...
KS_AUT_INT_ECS_SYST_MIG.database.password=...
KS_AUT_INT_ECS_SYST_MIG.database.connectionString=jdbc:postgresql://pgsql-kams-stag-15.staging.infra.svc.co.at:5432/...
KS_AUT_INT_ECS_SYST_MIG.database.dsn=...
```

### 2.3 Named datasource

Additional named datasources can be defined:

```properties
<INSTANCE>.ks.database.user=...
<INSTANCE>.ks.database.password=...
<INSTANCE>.ks.database.connectionString=...
<INSTANCE>.ks.database.dsn=...
```

Use:

```yaml
- type: SQL
  op: EXEC
  datasource: ks
  command: SELECT STATUS FROM SYSTEM_STATUS
  response: res_status
```

Mapping:

```text
no datasource
    -> <INSTANCE>.database.*

datasource: ks
    -> <INSTANCE>.ks.database.*
```

This allows one test to work with multiple databases without embedding credentials or connection strings in YAML.

> `datasource` is relevant only for `SQL / EXEC`. `ASSERT` and `BUFFER` operate on an already stored SQL `response`.

---

## 3. Operations

| Operation | Purpose |
|---|---|
| `EXEC` | Executes SQL. SELECT results are stored as a table under `response`. |
| `ASSERT` | Validates a previously stored SQL result. |
| `BUFFER` | Extracts one cell from a stored SQL result into an AGATE buffer. |

---

## 4. EXEC

Typical statements include:

```text
SELECT
INSERT
UPDATE
DELETE
```

### 4.1 Basic SELECT

```yaml
- type: SQL
  op: EXEC
  command: SELECT * FROM CUSTOMER WHERE ID = 100
  response: customer_result
```

### 4.2 Multi-line SQL

```yaml
- type: SQL
  op: EXEC
  command: |
    SELECT
      CURRENT_DATE AS TODAY_COL,
      150.55 AS PRICE,
      NULL AS DELETED_AT,
      'Active' AS STATUS,
      '5' AS COUNTER
    FROM DUAL
  response: res_demo
```

### 4.3 AGATE values inside SQL

```yaml
- type: SQL
  op: EXEC
  command: "SELECT ID FROM FOTO_INFO WHERE FK_SV_NUMMER = {R[SVNR]}"
  response: foto_result
```

```yaml
- type: SQL
  op: EXEC
  command: "SELECT * FROM MH_MELDUNG WHERE ID='{B[B_meldungsId]}'"
  response: meldung_result
```

The semantics of `{B[...]}`, `{R[...]}`, templates and re-instantiation are documented in the shared AGATE data-flow documentation.

### 4.4 Conditional execution

```yaml
- type: SQL
  op: EXEC
  condition: "(('{B[COUNT_ID]}' == '1'))"
  command: "DELETE FROM FOTO_INFO WHERE ID={B[ID]}"
  response: delete_foto_info_res
```

If the condition evaluates to false, the step is skipped.

---

## 5. Constraints

`constraints` pre-filter the result set of an `EXEC` operation before the result is stored under `response`.

```yaml
- type: SQL
  op: EXEC
  command: |
    SELECT 'COMPLETED' AS STATUS, 100 AS AMOUNT FROM DUAL
    UNION ALL
    SELECT 'COMPLETED' AS STATUS, 200 AS AMOUNT FROM DUAL
    UNION ALL
    SELECT 'FAILED' AS STATUS, 50 AS AMOUNT FROM DUAL
  response: active_rows
  constraints:
    - column: "STATUS"
      action: EQUALS
      expected: "COMPLETED"
    - column: "AMOUNT"
      action: EQUALS
      expected: "100"
```

Supported constraint actions:

```text
EQUALS
NOT_EQUALS
CONTAINS
IS_NULL
IS_NOT_NULL
```

| Field | Meaning |
|---|---|
| `column` | Column name. Matching is case-insensitive. |
| `action` | Filter operation. |
| `expected` | Comparison value; not required for `IS_NULL` / `IS_NOT_NULL`. |

Constraints are useful when later assertions should operate only on a filtered subset.

---

## 6. BUFFER

`SQL / BUFFER` extracts one cell from a previously stored SQL result table.

```yaml
- type: SQL
  op: BUFFER
  name: B_VSNR
  row: "0"
  column: "FK_SV_NUMMER"
  response: suche_meldung_in_db_res
```

Rows are zero-based.

Columns can be addressed by name or zero-based index.

```yaml
- type: SQL
  op: BUFFER
  name: B_ID
  row: 0
  column: "ID"
  response: sql_result
```

```yaml
- type: SQL
  op: BUFFER
  name: B_FIRST_COLUMN
  row: 0
  column: "0"
  response: sql_result
```

If `row` is omitted, the default is `0`.

### Important rule

Use a different name for the extracted buffer and the SQL response.

Good:

```yaml
response: res_multi
name: amount
```

Avoid:

```yaml
response: res_multi
name: res_multi
```

The SQL response is a table object. Reusing the same context key for a scalar value can cause runtime errors.

`SQL / BUFFER` does **not** use `action`.

---

## 7. ASSERT

`SQL / ASSERT` validates a previously stored SQL result.

There are three main assertion styles:

1. cell assertions,
2. row-count assertions,
3. collection assertions.

### 7.1 Cell addressing

```yaml
- type: SQL
  op: ASSERT
  action: EQUALS
  row: 0
  column: "COUNTER"
  expected: "5"
  response: res_demo
```

The same column can be addressed by numeric index:

```yaml
- type: SQL
  op: ASSERT
  action: EQUALS
  row: 0
  column: 4
  expected: 5
  response: res_demo
```

`row` defaults to `0`.

### 7.2 Supported cell actions

```text
EQUALS
NOT_EQUALS
CONTAINS
IS_NULL
IS_NOT_NULL
IS_EMPTY
IS_NOT_EMPTY
GREATER_THAN
GREATER_THAN_OR_EQUAL
LESS_THAN
LESS_THAN_OR_EQUAL
BETWEEN
DATE_EQUALS
```

#### Equality

```yaml
- type: SQL
  op: ASSERT
  response: res_demo
  row: 0
  column: "COUNTER"
  action: NOT_EQUALS
  expected: "10"
```

#### Null / empty checks

```yaml
- type: SQL
  op: ASSERT
  action: IS_NULL
  row: 0
  column: "DELETED_AT"
  response: res_demo
```

```yaml
- type: SQL
  op: ASSERT
  action: IS_NOT_EMPTY
  column: "COUNTER"
  response: res_demo
```

These actions do not require `expected`.

#### Numeric comparison

```yaml
- type: SQL
  op: ASSERT
  response: res_demo
  row: 0
  column: "COUNTER"
  action: GREATER_THAN
  expected: 3
```

#### BETWEEN

```yaml
- type: SQL
  op: ASSERT
  response: res_demo
  row: 0
  column: "COUNTER"
  action: BETWEEN
  expected: "3..5"
```

Format:

```text
minimum..maximum
```

### 7.3 Date assertions

```yaml
- type: SQL
  op: ASSERT
  column: "DAY_COL"
  action: DATE_EQUALS
  expected: "TODAY"
  response: res_demo
```

Known keywords include:

```text
TODAY
YESTERDAY
```

AGATE date placeholders can also be used when appropriate:

```yaml
- type: SQL
  op: ASSERT
  column: "TODAY_COL"
  action: EQUALS
  expected: "{DATE[][][]}"
  response: res_demo
```

### 7.4 Collection assertions

Supported actions:

```text
ANY_MATCH
ALL_MATCH
```

`ANY_MATCH` passes if at least one value in the selected column matches:

```yaml
- type: SQL
  op: ASSERT
  column: "STATUS"
  action: ANY_MATCH
  expected: "FAILED"
  response: res_multi
```

`ALL_MATCH` evaluates the entire selected column.

For `ANY_MATCH` / `ALL_MATCH`, `row` is not used.

---

## 8. ROW_COUNT

The current SQL Engine models row count as a special assertion **source**:

```yaml
source: ROW_COUNT
```

A normal comparison action is then applied to the row count.

```yaml
- type: SQL
  op: ASSERT
  response: res_demo
  source: ROW_COUNT
  action: EQUALS
  expected: "1"
```

Supported ROW_COUNT actions:

```text
EQUALS
NOT_EQUALS
GREATER_THAN
GREATER_THAN_OR_EQUAL
LESS_THAN
LESS_THAN_OR_EQUAL
```

Examples:

```yaml
- type: SQL
  op: ASSERT
  response: res_demo
  source: ROW_COUNT
  action: NOT_EQUALS
  expected: "0"
```

```yaml
- type: SQL
  op: ASSERT
  response: res_demo
  source: ROW_COUNT
  action: GREATER_THAN_OR_EQUAL
  expected: "1"
```

For `source: ROW_COUNT`:

- `expected` is required,
- `row` is not used,
- `column` is not used.

> **Important:** Older documentation may show `action: ROW_COUNT`. The current SQL Engine uses `source: ROW_COUNT` plus a comparison action. AI-generated tests should use the current model.

---

## 9. Parameter Reference

### EXEC

| Parameter | Required | Description |
|---|---:|---|
| `command` | Yes | SQL statement. Supports AGATE placeholders. |
| `response` | For SELECT result use | Stores the result table. |
| `datasource` | No | Selects a named datasource. |
| `constraints` | No | Pre-filters the result set. |
| `condition` | No | Conditional execution. |

### BUFFER

| Parameter | Required | Description |
|---|---:|---|
| `response` | Yes | Previous SQL EXEC result. |
| `column` | Yes | Column name or zero-based index. |
| `row` | No | Zero-based row index, default `0`. |
| `name` | Yes | Target AGATE buffer name. |
| `condition` | No | Conditional execution. |

### ASSERT

| Parameter | Required | Description |
|---|---:|---|
| `response` | Yes | Previous SQL EXEC result. |
| `action` | Yes | Assertion action. |
| `source` | No | Special source; currently `ROW_COUNT`. |
| `column` | Depends | Required for cell/collection assertions, not ROW_COUNT. |
| `row` | No | Default `0`; not used for collection or ROW_COUNT assertions. |
| `expected` | Depends | Required by comparison actions, not unary null/empty checks. |
| `condition` | No | Conditional execution. |

---

## 10. Real AGATE Usage Patterns

Only SQL Engine steps are shown below.

### 10.1 Query, validate and extract values

```yaml
- type: SQL
  op: EXEC
  command: >
    SELECT *
    FROM MH_MELDUNG
    WHERE TYP='A'
      AND FK_AUSPRAEGUNG_ID<>(
        SELECT AUSPRAEGUNG_ID
        FROM HVB_AUSPRAEGUNG
        JOIN HVB_VPNR
          ON HVB_AUSPRAEGUNG.FK_VP_ID=HVB_VPNR.FK_VP_ID
        WHERE HVB_VPNR.VPNR='336047'
      )
  response: suche_meldung_in_db_res

- type: SQL
  op: BUFFER
  name: B_VSNR
  row: "0"
  column: FK_SV_NUMMER
  response: suche_meldung_in_db_res

- type: SQL
  op: ASSERT
  action: EQUALS
  expected: A
  row: "0"
  column: TYP
  response: suche_meldung_in_db_res

- type: SQL
  op: BUFFER
  name: B_Strasse
  row: "0"
  column: PATIENTIN_STRASSE
  response: suche_meldung_in_db_res

- type: SQL
  op: BUFFER
  name: B_Plz
  row: "0"
  column: PATIENTIN_PLZ
  response: suche_meldung_in_db_res

- type: SQL
  op: BUFFER
  name: B_Ort
  row: "0"
  column: PATIENTIN_ORT
  response: suche_meldung_in_db_res

- type: SQL
  op: BUFFER
  name: B_Staatencode
  row: "0"
  column: PATIENTIN_STAAT
  response: suche_meldung_in_db_res

- type: SQL
  op: BUFFER
  name: B_meldungsId
  row: "0"
  column: ID
  response: suche_meldung_in_db_res
```

Pattern:

```text
SQL EXEC
   |
   +--> ASSERT database value
   |
   +--> BUFFER values for later test steps
```

### 10.2 Conditional cleanup

```yaml
- type: SQL
  op: EXEC
  command: "SELECT COUNT(*) ID FROM FOTO_INFO WHERE FK_SV_NUMMER = {R[SVNR]}"
  response: buffere_foto_id_count_res

- type: SQL
  op: BUFFER
  name: COUNT_ID
  row: "0"
  column: "0"
  response: buffere_foto_id_count_res

- type: SQL
  op: EXEC
  condition: "(('{B[COUNT_ID]}' == '1'))"
  command: "SELECT ID FROM FOTO_INFO WHERE FK_SV_NUMMER = {R[SVNR]}"
  response: buffere_foto_id_res

- type: SQL
  op: BUFFER
  name: ID
  row: "0"
  column: "0"
  response: buffere_foto_id_res

- type: SQL
  op: EXEC
  condition: "(('{B[COUNT_ID]}' == '1'))"
  command: "DELETE FROM FOTO_INFO_AUSKUNFT_HM13 WHERE FK_FOTO_INFO_ID={B[ID]}"
  response: delete_foto_info_auskunft_hm13_res

- type: SQL
  op: EXEC
  condition: "(('{B[COUNT_ID]}' == '1'))"
  command: "DELETE FROM FOTO_INFO_AUSKUNFT WHERE FK_FOTO_INFO_ID={B[ID]}"
  response: delete_foto_info_auskunft_res

- type: SQL
  op: EXEC
  condition: "(('{B[COUNT_ID]}' == '1'))"
  command: "DELETE FROM FOTO_INFO_HM13 WHERE FK_ID={B[ID]}"
  response: delete_foto_info_hm13_res

- type: SQL
  op: EXEC
  condition: "(('{B[COUNT_ID]}' == '1'))"
  command: "DELETE FROM FOTO_INFO WHERE ID={B[ID]}"
  response: delete_foto_info_res
```

This demonstrates how an SQL query result can drive later conditional SQL steps.

### 10.3 Database validation after a business operation

```yaml
- type: SQL
  op: EXEC
  command: "SELECT * FROM MH_MELDUNG WHERE ID='{B[B_meldungsId]}'"
  response: pruefe_meldung_in_db_res

- type: SQL
  op: ASSERT
  action: EQUALS
  expected: '3364013041'
  row: '0'
  column: FK_SV_NUMMER
  response: pruefe_meldung_in_db_res

- type: SQL
  op: ASSERT
  action: EQUALS
  expected: A
  row: '0'
  column: TYP
  response: pruefe_meldung_in_db_res

- type: SQL
  op: ASSERT
  action: EQUALS
  expected: A
  row: '0'
  column: PATIENTIN_STRASSE
  response: pruefe_meldung_in_db_res

- type: SQL
  op: ASSERT
  action: EQUALS
  expected: '101'
  row: '0'
  column: PATIENTIN_PLZ
  response: pruefe_meldung_in_db_res

- type: SQL
  op: ASSERT
  action: EQUALS
  expected: A
  row: '0'
  column: PATIENTIN_ORT
  response: pruefe_meldung_in_db_res

- type: SQL
  op: ASSERT
  action: EQUALS
  expected: NLD
  row: '0'
  column: PATIENTIN_STAAT
  response: pruefe_meldung_in_db_res
```

This is a typical integration-test pattern:

```text
business operation
      |
      v
database query
      |
      v
database assertions
```

---

## 11. AI Guidance

When generating or analyzing SQL Engine steps:

1. **Do not invent database connections.**  
   Connections belong to environment configuration.

2. **Use named datasources only when required.**  
   Without `datasource`, use the default `<INSTANCE>.database.*`.

3. **Reference an earlier EXEC response.**  
   `ASSERT` and `BUFFER` operate on an already stored result.

4. **Respect zero-based addressing.**  
   Prefer column names when they improve readability.

5. **Use the current ROW_COUNT model.**  
   Use `source: ROW_COUNT` plus a comparison action.

6. **Do not invent expected business values.**  
   If the expected value is not provided by the requirement, report it as missing.

7. **Understand AGATE data flow.**  
   SQL results can be extracted into buffers and consumed by later engines.

8. **Do not redefine shared AGATE concepts here.**  
   Buffers, templates, placeholders, reusable tests and re-instantiation belong in their shared documentation.

---

# AI Query Construction Guidance

SQL syntax correctness is not enough. Generated SQL should also make the selected test data deterministic whenever the requirement provides enough information.

## Constrain the Intended Business Row

Requirement:

```text
Find an ACTIVE user and store ID and STATUS.
```

Preferred:

```yaml
- type: SQL
  op: EXEC
  command: |
    SELECT ID, STATUS
    FROM USERS
    WHERE STATUS = 'ACTIVE'
  response: user_result
```

Then:

```yaml
- type: SQL
  op: ASSERT
  response: user_result
  source: ROW_COUNT
  action: GREATER_THAN_OR_EQUAL
  expected: '1'
```

and:

```yaml
- type: SQL
  op: BUFFER
  response: user_result
  row: 0
  column: 'ID'
  name: user_id
```

Do not prefer:

```sql
SELECT ID, STATUS
FROM USERS
```

followed by an assumption that `row: 0` has the required business property.

## Row 0 Is Not a Business Selection Rule

`row: 0` means the first returned row. It does not mean:

```text
the correct user
the newest row
the ACTIVE row
the business-relevant row
```

unless the query itself makes that true.

If the requirement specifies a business key or property, constrain the query accordingly.

## Preserve Test Verification

Filtering for the intended row does not eliminate validation.

Example:

```sql
WHERE STATUS = 'ACTIVE'
```

can still be followed by:

```yaml
- type: SQL
  op: ASSERT
  response: user_result
  row: 0
  column: 'STATUS'
  action: EQUALS
  expected: 'ACTIVE'
```

This keeps the test explicit while avoiding arbitrary row selection.

## AI Rule

When generating SQL:

1. Use requirement-provided identifiers and business predicates in the SQL where appropriate.
2. Do not rely on unspecified database row order.
3. Do not create an assumption merely to justify an unrestricted query.
4. If the requirement does not provide enough information to select a row deterministically, state that as a business/test-data assumption.
5. Never invent table names, column names or ordering columns that are not provided by the requirement or documentation.


---

# Non-Blocking SQL Assumptions

Missing database schema information should not stop generation of an otherwise well-defined AGATE test.

If the requirement describes the desired behavior but does not provide concrete table or column names, the AI may use simple example names **provided they are explicitly listed as assumptions**.

Example:

```yaml
- type: SQL
  op: EXEC
  command: |
    SELECT ID, STATUS
    FROM USERS
    WHERE STATUS = 'ACTIVE'
  response: user_result
```

Assumption:

```text
The example schema contains a USERS table with columns ID and STATUS.
```

This is preferable to refusing to generate the test.

## Rules

1. Never present assumed table/column names as documented facts.
2. Keep assumed schema names simple and obvious.
3. List assumptions after the YAML.
4. If the user provided actual table/column names, use them and do not add an assumption.
5. Missing database schema is an external test-data detail, not an AGATE syntax gap.
6. Generate the complete test unless the SQL logic itself cannot be reasonably expressed.
