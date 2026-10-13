# AGATE learning demo

Samostalan lokalni web servis na Javi 17, bez dodatnih biblioteka, baze ili interneta pri radu. Sluša samo na 127.0.0.1:8767.

## Pokretanje

1. Raspakuj ZIP i pokreni `start-demo.bat` (Linux: `sh start-demo.sh`). Potrebna je Java 17 ili novija.
2. Otvori http://127.0.0.1:8767 . Svaka lekcija ima jedinstveni `body` ID `page-L01` … `page-L12`, a elementi imaju prefiks lekcije.
3. Kopiraj `agate/data/learningdemo` u `agate-server/data/learningdemo` i `agate/demo-upload.txt` u osnovni direktorijum `agate-server`.
4. Pokreni iz tog direktorijuma: `startTests.bat Milenko ECS_SYST_SYST LEARNINGDEMO "Learning_All.yaml"`.
   Koristi ime korisnika i instance koji postoje u tvojoj konfiguraciji. Primeri ne koriste njihove poslovne podatke.
5. Pojedinačna lekcija: `startTests.bat Milenko ECS_SYST_SYST LEARNINGDEMO "06-tables.yaml"`.
6. Servis zaustavi sa Ctrl+C. Port je fiksiran u YAML primerima; promena porta zahteva izmenu URL-ova.

## Organizacija i način učenja

`Learning_All.yaml` poziva 12 reusable fajlova. Svaki otvara svoju stranicu i ima nezavisno početno stanje. Komentari na početku reusable fajlova objašnjavaju varijante. YAML `page` je opis; stvarni locator je `link`, `button`, `textbox`, `dropdown`, `checkbox` ili `target`. Biraj jednu strategiju po koraku.

`negative/` sadrži pet namernih neuspeha. Pokreći ih pojedinačno, npr. `negative/glob_wrong_prefix.yaml`. Ne pripadaju pozitivnoj suite.

Prvo koristi label, accessible name ili stabilni ID. Index i pozicije demonstriraju alternative, ali su manje stabilni pri promeni redosleda. `exact: true` znači celo normalizovano ime, a ne identične sirove HTML razmake. `TEXT_CONTAINS` nema joker; `TEXT_GLOB` ima joker `*`. Znak `?` je u ovoj AGATE implementaciji literal.

## Potrebna AGATE podrška

Primeri koriste unapređenja iz naših korekcija: portabilni TEXT_GLOB i normalizaciju NBSP (1.0.54), redakciju (1.0.56), DOWNLOAD_PDF (1.0.59), rowCellsAll (1.0.60), izbor browsera (1.0.61). Brojevi označavaju pakete korekcija, ne Maven verziju servera. Ne kopiraj stariju GuiSession preko novije. Za instalirani Edge koristi `java -Dagate.gui.browser=edge -jar ...` ili AGATE_GUI_BROWSER=edge uz odgovarajući kod servera.

## Granice demonstracije

Ovo je širok, proširiv skup HTML elemenata i trenutno podržanih AGATE strategija, ne tvrdnja da pokriva svaku moguću tehnologiju. SVG, canvas, date/time/range/color, progress i meter prikazani su kao dodatni elementi. Posebne akcije za canvas piksele, closed shadow DOM, OS dijaloge, drag-and-drop i dozvole browsera nisu implementirane ovim projektom. PDF poređenje je izostavljeno jer FILE COMPARE nije podržan. PDF je originalni servisni odgovor, ne screenshot ili štampanje HTML-a.

Web servis i struktura YAML/HTML provereni su automatski; izvršavanje kompletne suite u korisnikovom AGATE/Edge okruženju ostaje integraciona provera. Nijedan namerni neuspeh nije prikazan kao uspešan test.


## Katalog

| ID | Stranica / reusable | Tema |
|---|---|---|
| L01 | `01-links` | Links and locator strategies |
| L02 | `02-buttons` | Buttons and exact matching |
| L03 | `03-inputs` | Inputs, labels and keyboard |
| L04 | `04-selects` | Dropdowns and multiple selection |
| L05 | `05-checks` | Checkboxes and radio buttons |
| L06 | `06-tables` | Tables: coordinates and row criteria |
| L07 | `07-text` | Text, whitespace, glob and regex |
| L08 | `08-states` | Visibility, enabled state and waiting |
| L09 | `09-frames` | Frames and scoped locators |
| L10 | `10-tabs` | Tabs and navigation |
| L11 | `11-files` | Upload, download and original PDF |
| L12 | `12-advanced` | Shadow DOM, editable content and dialogs |
