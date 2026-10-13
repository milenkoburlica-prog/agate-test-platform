# AGATE Studio — GUI kontrole, Recorder i YAML DSL

Verzija dokumentacije: **0.1.4**. Namenjeno kopiranju u Confluence; YAML i HTML primere prikazati u Code block komponentama. Opis se odnosi na samostalni AGATE Studio prototip. Integracija sa Java/Quarkus `agate-server` još nije implementirana; usklađen je redosled polja koraka, ne celokupni server contract.

## 1. Osnovni model

Svaki korak opisuje akciju nad browserom ili elementom:

```yaml
- id: click_login_link
  type: GUI
  op: CLICK
  page: e-card (000)
  target:
    role: link
    name: Am e-card Produktiv(s)ystem anmelden (verschlüsselt)
    exact: true
```

| Polje | Značenje |
|---|---|
| `id` | Slobodno izabran identifikator/opis koraka. Preporučeno obavezno i jedinstveno u glavnom testu. Nije HTML ID. Recorder generiše ime, a tester ga može preimenovati. |
| `type` | Engine tip; ovde uvek `GUI`. |
| `op` | Operacija: OPEN, CLICK, SELECT, FILL itd. |
| `page` | Opciona opisna oznaka iz recordera. Ne otvara stranicu i nije assertion trenutnog ekrana. |
| `url` | Adresa za OPEN. |
| `target` | Locator elementa ili referenca `page_module.element`. |
| `value` | Podatak operacije, npr. unos PIN-a ili izbor opcije. Stoji izvan `target`. |
| `timeout` | Opcioni timeout koraka u ms. Bez njega važi timeout projekta. |
| `breakpoint` | `true`: debugger staje pre koraka. |

Redosled izlaznog YAML-a je **id → type → op**, zatim ostala polja. Promena redosleda polja ne menja izvršavanje YAML-a. Redosled koraka u `steps` određuje izvršavanje. Učitani stari format radi i dalje; Save/Export ga ispisuju u novom redosledu.

Može se koristiti opisni ID, npr. `id: Izaberi citac kartice`. Trenutni engine ga tretira kao identifikator u rezultatima; poseban display-name nije implementiran.

## 2. Kontrole i role — pregled stvarne podrške

`role` opisuje semantičku/accessibility ulogu, a `op` akciju. OPEN nije role, nego operacija. CSS locator ne zahteva role.

| Kontrola | Tipična role / identifikacija | Operacije | Status |
|---|---|---|---|
| Link `<a href>` | `link` | CLICK, HOVER, PRESS, ASSERT, EXTRACT | Isprobano na e-card toku |
| Dugme `<button>` ili input submit/button/reset/image | `button` | CLICK, HOVER, PRESS, ASSERT | Submit dugmad isprobana na e-card toku; ostali input tipovi pokriveni browser regression testom |
| Native dropdown `<select>` | `combobox` za običan single-select; CSS/label | SELECT, ASSERT VALUE/COUNT, EXTRACT source:value | Isprobano: čitač, adresa, Fachgebiet |
| Tekstualni input / textarea | `textbox`, label, CSS | FILL, PRESS, ASSERT VALUE, EXTRACT source:value | Runtime implementiran; PIN unos isproban |
| Password / PIN input | label ili CSS | FILL, PRESS, ASSERT VALUE | Isprobano; password polje nema implicitnu ARIA textbox role, zato koristiti label/CSS |
| Checkbox | `checkbox` | CHECK, UNCHECK, ASSERT | Runtime i recorder implementirani; e-card scenario ih nije proverio |
| Radio | `radio` | CHECK | Runtime/recorder podrška; UNCHECK se ne koristi za radio |
| Native multiple select | obično `listbox`, CSS | SELECT sa nizom | Playwright runtime prihvata niz; recorder/editor nisu kompletan multi-select designer |
| Custom combobox | eksplicitni `role: combobox` | CLICK/FILL/PRESS i CLICK na option | Nije isto što i native SELECT; nije isprobano na e-card toku |
| Tabela | `table` | ASSERT, EXTRACT | Capture prepoznaje; semantičke row/cell operacije nisu poseban DSL feature |
| Zaglavlje kolone | `columnheader` | ASSERT, EXTRACT | Capture prepoznaje TH |
| Naslov | `heading` | ASSERT, EXTRACT | Capture prepoznaje H1/H2/H3 |
| Slika | `img`, name iz alt | ASSERT, EXTRACT | Capture identifikacija; svaka slika nije automatski uključena u scan |
| Slider / range input | `slider`, CSS | PRESS, ASSERT VALUE | Heuristika prepoznaje role; nema posebnog SET_SLIDER engine koraka |
| Upload input | CSS/label | UPLOAD | Runtime implementiran; recorder zahteva ručno postavljanje file putanje |
| Download link/button | link/button/testId/CSS | DOWNLOAD | Runtime implementiran; ručno pretvaranje CLICK u DOWNLOAD |

Locator factory prihvata i druge važeće Playwright ARIA role koje ručno zadaš. To ne znači da svaki tip kontrole ima specijalizovani engine ili pouzdan recorder. Capture koristi jednostavnije heuristike za role/name, a Playwright proverava broj podudaranja. Custom widgets, open shadow DOM i iframe scenariji zahtevaju proveru na konkretnoj aplikaciji; closed shadow DOM nije podržan.

## 3. Kako pronalazimo element — target

U jednom targetu koristi jednu primarnu strategiju:

| Strategija | Primer | Na šta se odnosi |
|---|---|---|
| `testId` | `testId: patient-search` | Podrazumevani HTML `data-testid` |
| `role` + `name` | `{role: button, name: '(W)eiter', exact: true}` | Accessibility role i accessible name |
| `label` | `{label: PIN, exact: true}` | Pravilno povezan HTML label / accessibility label |
| `text` | `{text: ACTIVE, exact: true}` | Tekst elementa; može biti nejedinstven |
| `placeholder` | `{placeholder: VSNR}` | Placeholder input polja |
| `css` | `css: '#Ordinationsadresse'` | CSS selector: ID, atributi ili struktura DOM-a |

`target.name` uz role nije HTML atribut `name`. Accessible name se može dobiti iz aria-label, povezanog labela, teksta dugmeta ili input button value-a. Običan tekst pored dropdown-a nije automatski pravilno povezan label.

Ako se u isti target upiše više strategija, one se ne kombinuju kao uslovi: implementacija ima prioritet testId, role, label, text, placeholder, css. Zato koristiti jedan jasan način identifikacije. `exact: true` znači tačno podudaranje accessible name/teksta; ne odnosi se na način izbora dropdown opcije.

Opcioni `frames` je niz CSS selector-a za iframe-ove, a `scope` sužava pretragu na parent locator. `nth` bira match po poziciji i treba ga koristiti pažljivo. Locator može imati jedan match sada i ipak biti nestabilan posle promene aplikacije.

## 4. OPEN — otvaranje stranice

```yaml
- id: open_ecard_login
  type: GUI
  op: OPEN
  url: https://services-t.ecard-test.sozialversicherung.at:5443/auth-gui
```

Run/Debug kreiraju novi Chromium context i početni about:blank. OPEN zatim navigira na URL i čeka `domcontentloaded`. Ne garantuje da su svi kasniji AJAX zahtevi završeni. Sledeća akcija čeka svoj target do timeouta.

U Debug režimu klik na Step izvršava OPEN. Aktuelna verzija automatski odobrava `local-network-access` u test contextu pre navigacije. To ne rešava nedostupan uređaj, TLS ili firewall problem.

## 5. CLICK na link

```yaml
- id: open_ecard_productive_system
  type: GUI
  op: CLICK
  page: e-card (000)
  target:
    role: link
    name: Am e-card Produktiv(s)ystem anmelden (verschlüsselt)
    exact: true
```

Pronalazi link po role i accessible name-u. Ne mora poznavati njegov href. CSS alternativa je moguća ako postoji stabilan id. Playwright čeka da target bude pogodan za klik; nedostajući ili nejedinstven target izaziva grešku.

## 6. SELECT — native dropdown lista

Pronalaženje liste i biranje opcije su nezavisni:

```html
<select id="WelcomeForm:cardReaderSelect" name="WelcomeForm:cardReaderSelect">
  <option value="SIMU_6666901090_index10">SIMU_6666901090</option>
</select>
```

- ID liste: `WelcomeForm:cardReaderSelect`.
- HTML name liste: ista vrednost u ovom primeru, ali drugi atribut.
- Value opcije: `SIMU_6666901090_index10`.
- Tekst/label opcije: `SIMU_6666901090`.
- Stvarna pozicija opcije: nezavisan broj u trenutnoj listi.

### Čitač kartice — recorder format

```yaml
- id: select_card_reader
  type: GUI
  op: SELECT
  page: e-card
  target:
    css: "#WelcomeForm\\:cardReaderSelect"
  value: SIMU_6666901090_index10
```

`#` označava HTML ID. Dvotačka u ID-u je escape-ovana za CSS; YAML double-quoted string `\\` daje jedan backslash. Alternativa bez escape-a:

```yaml
target:
  css: 'select[id="WelcomeForm:cardReaderSelect"]'
```

Po HTML name atributu:

```yaml
target:
  css: 'select[name="WelcomeForm:cardReaderSelect"]'
```

### Value, label i index

Aktuelni runtime direktno prosleđuje `value` u Playwright selectOption. Podržani oblici:

```yaml
# Eksplicitan izbor po HTML value-u:
value:
  value: SIMU_6666901090_index10

# Izbor po prikazanom tekstu:
value:
  label: SIMU_6666901090

# Izbor po poziciji, počevši od nule:
value:
  index: 10
```

Prosta string vrednost je recorder default. Playwright za taj oblik može podudarati value ili label; eksplicitni object oblik uklanja tu dvosmislenost. Tekst `_index10` unutar HTML value-a nije DSL positional index. Ako aplikacija generiše value prema poziciji, reorganizacija opcija ipak može promeniti taj value.

Izbor po labelu nije zavisan od pozicije, ali može biti pogođen promenom jezika ili naziva. Dupli label zahteva jasniji izbor. Stabilan business value može biti bolji od labela; ne postoji univerzalno najbolji izbor.

Object oblike uređivati u YAML tabu, pa Validate & apply. Step inspector još prikazuje value kao prost tekst i nema specijalizovane Label/Value/Index kontrole. Placeholderi unutar object value-a trenutno se ne razrešavaju rekurzivno. Prosta vrednost `{R[key]}` se razrešava.

### Ordinacijska adresa

```yaml
- id: select_ordination_address
  type: GUI
  op: SELECT
  page: e-card
  target:
    css: '#Ordinationsadresse'
  value: '2'
```

`'2'` je vrednost opcije, a ne druga pozicija. Za label treba uneti stvarni prikazani tekst iz option elementa; nije poznat iz ovog primera.

### Fachgebiet — postojeći structural locator

```yaml
- id: select_fachgebiet
  type: GUI
  op: SELECT
  page: e-card
  target:
    css: body > div > div:nth-of-type(3) > div:nth-of-type(2) > form >
      table:nth-of-type(2) > tbody > tr > td:nth-of-type(2) > select
  value: '07'
```

Ovaj selector opisuje strukturu stranice i zavisi od nje. Promena layout-a može ga pokvariti. Ako se potvrdi stabilan HTML name/id ili pravilno povezan label, treba ga zameniti tim locatorom. Ne izmišljati accessible name iz spiska svih opcija: to nije naziv dropdown kontrole.

SELECT je namenjen native `<select>` elementu. Custom dropdown od div/button/listbox elemenata obično zahteva CLICK otvaranja, zatim CLICK konkretne option kontrole.

## 7. FILL — textbox i PIN

```yaml
- id: enter_pin
  type: GUI
  op: FILL
  page: e-card
  target:
    css: "#WelcomeForm\\:PIN"
  value: '{E[PASSWORD]}'
```

FILL postavlja sadržaj polja, zamenjujući prethodnu vrednost. Recorder za password polja ne stavlja stvarni PIN u YAML, već placeholder. PASSWORD je naziv ključa, ne dokaz da je poslovno polje lozinka; ovde predstavlja PIN.

Pre Run/Debug uneti stvarnu testnu vrednost u Runtime variables:

```json
{
  "R": {},
  "E": {"PASSWORD": "<testni-PIN>"},
  "B": {}
}
```

`<testni-PIN>` je oznaka za zamenu, nije vrednost koju treba izvršiti. Studio ne čita E automatski iz OS environment-a. R nema unapred zadatu demo VSNR vrednost. L nije ponuđen u početnom prikazu ni u Parameterize dijalogu; interne reusable L vrednosti ostaju podržane radi kompatibilnosti sa postojećim prototip primerom.

R su ulazni parametri; B su bufferi koje runtime može upisati kroz EXTRACT. U ovom e-card toku nisu korišćeni. Runtime variables nisu sačuvane u project.yaml i treba ih ponovo zadati posle restartovanja UI-a. Maskiranje recordera nije potpuna redakcija Playwright trace/screenshots/report artefakata.

## 8. CLICK — button

```yaml
- id: continue_after_pin
  type: GUI
  op: CLICK
  page: e-card
  target:
    role: button
    name: (W)eiter
    exact: true
```

Radi i za native `<button>` i za input submit dugme, npr.:

```html
<input type="submit" id="WelcomeForm:continue" value="(W)eiter">
```

Više ekrana može imati dugme istog naziva. Sam name ne razlikuje ekrane; redosled akcija i prisustvo elemenata sledećeg ekrana određuju tok. Po potrebi dodati WAIT ili stabilniji locator za konkretno dugme.

```yaml
- id: confirm_fachgebiet
  type: GUI
  op: CLICK
  target:
    role: button
    name: (O)K
    exact: true
```

## 9. Tranzicije i čekanje

```yaml
- id: wait_for_address_screen
  type: GUI
  op: WAIT
  target:
    css: '#Ordinationsadresse'
  state: visible
  timeout: 30000
```

WAIT ne pokreće tranziciju. CLICK na odgovarajuće dugme mora prethoditi čekanju. Selektor sledeće akcije ima auto-wait, pa poseban WAIT nije potreban posle svakog klika, ali može jasno izraziti očekivani ekran.

Isprobani tok: OPEN → login link → SELECT čitača → FILL PIN → CLICK Weiter → SELECT adrese → CLICK Weiter → SELECT Fachgebiet → CLICK OK → logout link → Beenden → OK.

## 10. Recorder i Capture logika

Recorder prati interakcije, Capture prikuplja locatore izabranih kontrola. Oba daju isti DSL model.

- Snimanje klika: pravi korisnički click postaje CLICK. Sintetički click sa isTrusted=false se dijagnostički loguje, ali ne dodaje novi korak.
- Input tekst: input/change postaju FILL; uzastopni FILL istog targeta spajaju se.
- Dropdown: input/change postaju SELECT sa trenutnim value-om. Uzastopni identični SELECT događaji istog targeta spajaju se.
- Locator: razmatra testId, role/name, label, placeholder i CSS; koristi prvi jedinstveni kandidat, a ako nema takvog, fallback zahteva review.
- ID elementa je poželjniji od structural CSS samo ako aplikacija drži taj ID stabilnim. Jedinstvenost sada nije garancija stabilnosti.
- `page` recorder metapodatak nije zaštita od klika na istoimeno dugme na drugoj stranici.

Na e-card snimanju potvrđen je dodatni sintetički click na Weiter posle stvarnog korisničkog klika. Filtriranjem se izbegava dodatni replay korak koji bi zahvatio sledeći ekran. Log dokumentuje ovaj slučaj; ne pretpostavljamo da je svaki dodatni click iste prirode.

Recorder logs → Download recorder log daje JSONL sa source događajima, vremenom, dokumentom/frame-om, odlukom added/merged/ignored i stepId. Podaci password polja su maskirani. Dijagnostika ostaje na lokalnom disku u data/recordings; ne šalje se automatski.

## 11. Ostale implementirane operacije

BACK, CHECK, UNCHECK, HOVER, PRESS, UPLOAD, DOWNLOAD, WAIT, ASSERT, EXTRACT, SCREENSHOT, ASSERT_PAGE_STRUCTURE i interni CALL. Njihova implementacija u prototipu ne znači da su sve proverene u e-card aplikaciji.

ASSERT tipovi: VISIBLE, HIDDEN, ENABLED, DISABLED, TEXT_EQUALS, TEXT_CONTAINS, VALUE, COUNT. EXTRACT upisuje B buffer. ARIA baseline poredi celu body accessibility strukturu tačno, bez wildcard/partial match pravila. CALL poziva interne reusable blokove; eksterni AGATE CALL resolver još nije integrisan.

## 12. Izvori i dalji razvoj

- [Playwright locators](https://playwright.dev/docs/locators)
- [Playwright selectOption](https://playwright.dev/docs/api/class-locator#locator-select-option)
- [Playwright actions](https://playwright.dev/docs/input)

Planirana sledeća dorada: specijalizovani SELECT editor sa izborom Label/Value/Index, pregledom option label/value para i jasnim razlikovanjem stabilnog locatora kontrole od kriterijuma izbora opcije. To nije dodato u verziji 0.1.4.
