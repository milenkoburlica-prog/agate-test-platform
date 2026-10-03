# Predloženi AGATE GUI DSL v1

```yaml
name: Example
version: 1
timeout: 5000
pages:
  search:
    elements:
      query: {label: VSNR, exact: true}
      submit: {role: button, name: Search, exact: true}
reusable: {}
steps:
  - id: open
    type: GUI
    op: OPEN
    url: https://example.test
  - id: fill
    type: GUI
    op: FILL
    target: search.query
    value: '{R[Request.VSNR]}'
  - id: search
    type: GUI
    op: CLICK
    target: search.submit
    breakpoint: true
```

Target: page.element string ili inline object. Strategije: `testId`, `role` + `name`, `label`, `text`, `placeholder`, `css`; opciono `exact`, `nth`, `frames` (array iframe CSS selector-a), `scope` (inline parent locator). Strict match tokom akcija je Playwright default: ne biramo nevidljivo prvi od više match-eva.

| op | Polja |
|---|---|
| OPEN | url (ili value) |
| BACK | bez targeta |
| CLICK/HOVER/CHECK/UNCHECK | target |
| FILL/PRESS/SELECT | target + value; SELECT koristi option value |
| UPLOAD | target + value kao lokalna file putanja |
| DOWNLOAD | target; opcioni value za filename, buffer za lokalnu saved putanju |
| WAIT | target + state: visible/hidden/attached/detached; ili value milisekunde |
| ASSERT | target + assertion: {type, expected}; opcioni timeout |
| EXTRACT | target + buffer; default innerText, source: value ili attribute: href |
| SCREENSHOT | value filename; fullPage |
| ASSERT_PAGE_STRUCTURE | target page ime sa snimljenim aria poljem |
| CALL | command internal reusable ime; opcioni parameters dostupni u L |

ASSERT types: VISIBLE, HIDDEN, ENABLED, DISABLED, TEXT_EQUALS, TEXT_CONTAINS, VALUE, COUNT. Assertions pokušavaju do timeouta. TEXT_EQUALS poredi trimovan tekst, TEXT_CONTAINS substring, VALUE input value, COUNT broj match-eva.

Placeholders: `{R[key]}`, `{B[key]}`, `{E[key]}`, `{L[key]}`. JSON namespace koristi doslovni key; nije nested path traversal. Nedostajuća vrednost je greška. EXTRACT i DOWNLOAD upisuju B u okviru run-a. CALL parameters u prvoj verziji su direktne vrednosti, ne rekurzivno razrešavani placeholder izrazi.

Opcioni `page` koraka je recorder metapodatak za grupisanje modula; runtime ga ignoriše. Breakpoint se primenjuje pre koraka u runneru. ARIA baseline u `pages.<name>.aria` je ceo body accessibility snapshot trenutne stranice, čak i kada module sadrži samo izabrane elemente.
