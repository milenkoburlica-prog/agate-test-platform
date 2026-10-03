# Izmene 0.1.4 — runtime variables i YAML redosled

Početni Runtime variables: `R: {}`, `B: {}`, `E: {PASSWORD: ""}`. Nema demo VSNR/PIN vrednosti ni početnog L objekta. L nije ponuđen u Parameterize dijalogu, ali interno ostaje kompatibilnost za reusable primer. Za demo ručno uneti PASSWORD demo; za e-card stvarni test PIN.

Svi glavni i reusable koraci u prikazanom/sačuvanom/exportovanom YAML-u imaju redosled id → type → op, zatim ostala polja. ID se može preimenovati u opis koraka; preporučeno je jedinstveno ime. Stari YAML se učitava bez promene ponašanja.

Confluence opis je u **docs/CONFLUENCE-GUI-DSL.md**: pregled kontrola/rola i detaljan OPEN/CLICK/SELECT/FILL opis sa isprobanim e-card primerima.

Za nadogradnju sačuvaj data i node_modules, zameni source fajlove i restartuj Studio. Setup nije potreban u postojećem folderu. Osveži Studio stranicu u browseru; novi početni Runtime variables zamenjuju stare vrednosti, pa ponovo upiši PIN pre testa.

---

# Izmene 0.1.3 — recorder dijagnostika

- Sintetički click događaji (`isTrusted=false`, npr. JavaScript `.click()`) se loguju, ali ne postaju dodatni CLICK koraci. Stvarni mouse/keyboard click ostaje snimljen. Input/change događaji se i dalje obrađuju zbog frontend komponenti.
- SELECT se snima i na `input`, ne samo na `change`. Uzastopni identični SELECT događaji spajaju se u jedan korak. Ovo hvata promenu opcije ranije; ne briše različite izbore niti uzastopne legitimne klikove na Weiter.
- **Recorder logs** ekran ima Refresh log i Download recorder log. Zapis se čuva i u `data/recordings/<session>/recorder.jsonl`, nezavisno od Save projekta.
- Log ima sourceSequence/time/document ID, event type/isTrusted, URL/frame, readyState, active element, poslednji pointerdown, SELECT label/value, odluku added/merged/ignored i stepId. Time možemo povezati sporni CLICK sa njegovim izvornim događajem i promenom stranice. Browser event vreme je zabeleženo pre asinhrone provere locatora; zapis redova prati prijem na serveru. Različiti iframe dokumenti imaju sopstvene sourceSequence brojače.
- Dijagnostika maskira value password polja. Ostali unosi i URL-ovi mogu sadržati testne podatke. Iz loga se ne šalju automatski podaci nikome.
- Greška tokom enrich provere se loguje i zadržava source locator kao fallback umesto da se akcija neprimetno izgubi.

## Šta sada probati

1. Restartuj Studio posle zamene source fajlova; `data` i `node_modules` sačuvaj. Zavisnosti nisu promenjene, setup nije potreban u postojećem folderu.
2. Snimi novi e-card tok: PIN → Weiter → izbor adrese → Weiter → izbor Fachgebiet → OK.
3. Stop, pa Recorder logs → Download recorder log.
4. Ako timeline ponovo ima višak klikova ili pogrešan redosled, pošalji JSONL i export YAML. Stari sačuvani YAML nije automatski popravljan.

Nismo dokazali tačan uzrok dodatnog Weiter događaja na SVC stranici. Ova verzija popravlja potvrđeno slabe tačke u recorderu i dodaje podatke za otkrivanje preostalog problema; ne obećava da sintetički click objašnjava upravo taj slučaj. Ciljni sistem nije testiran ovde.

---

# Izmene 0.1.2 — lokalna mreža

Recorder i svaki Run/Debug/CLI browser context automatski dobijaju Playwright dozvolu `local-network-access` pre otvaranja stranice. Time testovi ne moraju svaki put ručno da kliknu Chromium „Zulassen“ za pristup lokalnoj mreži. Odobrenje važi za stranice u tom test contextu, kako je traženo; ne menja podešavanja tvog redovnog browsera. Ne odobrava druge dozvole kao camera/microphone/geolocation i ne rešava Windows firewall, TLS sertifikat ili nedostupan čitač.

Za novu verziju zaustavi Studio, zameni source fajlove (sačuvaj `data`), i pokreni `startStudio.bat`. Zavisnosti su iste kao u 0.1.1; `setup.bat` nije potreban ako koristiš postojeći folder sa instaliranim node_modules. Za novi folder pokreni setup.bat i kopiraj data/projects. Sačuvani YAML ne treba menjati za ovu dozvolu. Runtime PIN mora ponovo biti unet u Runtime variables ako restartuješ Studio.

Playwright permission podrška zavisi od verzije Chromiuma. Ako grant nije podržan, start prikazuje grešku; ne prećutkujemo blokiranu dozvolu. Proverena dozvola u svežim recorder/run contextima u Chromiumu 153. Ciljni SVC sajt nije dostupan u našem test okruženju, pa je tamo potrebno potvrditi ceo tok.

---

# Izmene 0.1.1 — Recorder popravka

- Snimaju se i input submit/button/image/reset dugmad, uključujući Weiter.
- Step inspector ima timeout (ms) i WAIT state polja. Prazan timeout nasleđuje project timeout.
- Timeout pojedinačnog koraka se primenjuje i na CLICK/FILL/SELECT i ostale operacije, ne samo WAIT/assertions.
- Novi projekti počinju sa timeoutom 30000 ms. Učitani projekti zadržavaju postojeći timeout: promeni `timeout: 5000` u `timeout: 30000` u YAML-u ako želiš duže čekanje.
- Failure suggestions za SELECT traže combobox kontrole umesto nepovezanih dugmadi.
- Browser regression test pokriva PIN → input submit Weiter → odloženo pojavljivanje Ordinationsadresse → SELECT.

## Nadogradnja sa 0.1.0

Zaustavi Studio (Ctrl+C). Raspakuj ovaj ZIP u novi folder, pokreni setup.bat, kopiraj `data/projects` iz starog foldera i pokreni startStudio.bat. Postojeći projekti ostaju sačuvani. Alternativno zameni source fajlove u starom folderu; ne briši `data`.

**Stari recording već nema klik na Weiter: nova verzija ga ne može retroaktivno rekonstruisati.** Snimi scenario ponovo, ili ručno dodaj CLICK na stvarni Weiter locator između PIN FILL i SELECT Ordinationsadresse. Runtime variables E.PASSWORD treba da sadrži stvarni test PIN.

Ne dodajemo proizvoljna čekanja posle svakog klika. Sledeća akcija koristi Playwright auto-wait sa podesivim timeoutom; po potrebi dodaj WAIT za karakteristični element sledećeg ekrana.

---

# AGATE Studio 0.1.1 — radni prototip

Samostalan lokalni GUI test designer sa pravim Playwright izvršavanjem. Namenjen je zajedničkom isprobavanju i doradi Recorder + Capture + YAML pristupa. Nije integrisan sa postojećim `agate-server` projektom: taj kod nije bio dostupan u ovom zadatku. GUI YAML je predloženi dialect za integraciju, a ne tvrdnja o kompatibilnosti sa sadašnjim AGATE parserom.

## Windows — prvi start

1. Instaliraj Node.js **22 ili noviji** (LTS).
2. Raspakuj ceo folder `agate-studio`.
3. Pokreni **setup.bat** — instalira npm zavisnosti i Chromium. Potreban internet; u firmi možda npm/proxy konfiguracija.
4. Pokreni **startStudio.bat**.
5. Otvori **http://127.0.0.1:4310** u svom browseru.

Studio je kontrolni prozor. **Record** otvara drugi, Playwright Chromium prozor sa ciljnom aplikacijom. Drži oba vidljiva. Browser radi na računaru na kojem radi Node proces. Studio nije proxy za prikaz udaljenog browsera.

Linux/macOS: `bash setup.sh`, pa `npm start`. Na Linuxu može biti potrebno `npx playwright install --with-deps chromium` za sistemske biblioteke.

## Prvi demo, korak po korak

1. Ostavi URL `http://127.0.0.1:4310/demo` i uključen **Show browser**.
2. Klikni **Record**.
3. U ciljnom browseru: Username `milenko`, Password `demo`, **Login**.
4. Unesi VSNR `1234567890`, pa **Search**.
5. U Studio klikni **Record assertion**. U browseru klikni **ACTIVE**.
6. U Studio izaberi `TEXT_EQUALS`, expected `ACTIVE`, pa **Add assertion**.
7. Klikni **Stop**. Password je zamenjen sa `{E[PASSWORD]}`; pre Run-a ručno postavi E.PASSWORD na `demo` u Runtime variables.
8. Klikni **Create modules**. Pregledaj rezultate u **Modules** i **YAML**.
9. Klikni VSNR FILL korak → **Parameterize** → `R` → `Request.VSNR`.
10. **Save**, pa **Run**. Novi browser izvršava test od početka.
11. Klikni rezultat koraka da vidiš screenshotove, accessibility DOM snapshot i report. **Open trace** otvara lokalni Playwright Trace Viewer kada run završi.
12. **Debug** počinje od praznog browsera; **Step →** izvršava sledeći korak. Breakpoint postavljaš u Step inspectoru.

Brisanje/move u timeline-u menja budući test. Ne vraća trenutni browser unazad. Za proveru izmenjenog scenarija pokreni nov Run/Debug.

Možeš i direktno **Import** → `examples/patient-search.yaml` → Run. Primer uključuje CALL reusable, lokalni parametar, EXTRACT i screenshot.

## Glavni ekrani

- **Design:** timeline, inspector, parametrizacija, assertion picker, extract, reusable blokovi.
- **Capture:** scan trenutne stranice, izbor elemenata, alternative i njihov broj podudaranja, highlight, čuvanje page modula i opcioni ARIA baseline.
- **Modules:** pregled objekata, health na trenutnoj stranici, ARIA assertion.
- **YAML:** ručno uređivanje sa Validate & apply. Neprimenjene izmene ne ulaze u Run. Export izvozi primenjeni dokument.
- **Runs & debug:** status, breakpoint/step/continue/pause/stop, dokazi, console/network i trace. Kod greške: pregled kandidata, Use once & rerun ili Update locator & rerun. Oba ponavljaju scenario od početka.
- **Feature tour:** vodič unutar aplikacije.

## Format i integracija

Detalji: [docs/DSL.md](docs/DSL.md), [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
Za pregled svih planiranih ideja: [docs/FEATURE-CHECKLIST.md](docs/FEATURE-CHECKLIST.md).

Sačuvani projekti: `data/projects/<name>/project.yaml`; module kopije: `gui/pages/*.yaml`. Autoritativan je `project.yaml`; samostalno menjanje kopija modula još ne menja projekat. Run dokazi: `data/runs/<run-id>/`. Projekti i runovi ostaju na disku. Unsaved timeline je u memoriji i nestaje restartom servera.

Runtime variables su JSON u UI: R/B/E/L objekti sa doslovnim ključevima, npr. `"Request.VSNR"`. Ne čitaju se automatski iz OS okruženja. GUI `CALL` trenutno poziva interne reusable blokove; ne implementira postojeći AGATE CALL resolver za eksterne YAML fajlove.

CLI, dok Studio server radi zbog demo URL-a:

```bash
npm run cli -- examples/patient-search.yaml examples/variables.json
```

Za realne aplikacije promeni OPEN URL. CLI završava kodom 0 za uspešan test i 1 za grešku.

## Provera

```bash
npm test
```

Model testovi rade bez browsera. Ceo browser test (Windows CMD):

```bat
set AGATE_BROWSER_TEST=1
npm test
```

PowerShell: `$env:AGATE_BROWSER_TEST="1"; npm test`. Linux/macOS: `AGATE_BROWSER_TEST=1 npm test`.

U pripremi ovog paketa prošlo je svih 6 testova, uključujući stvarni headless Chromium: recording, password placeholder, capture, ARIA baseline, page modules, save/load, replay, debug/cancel, failure candidates, UI evidence, YAML/inspector ekran, reusable/local parameters, EXTRACT buffer i privremena popravka locatora. Windows skripte su pripremljene, ali nisu izvršavane na Windows računaru. Test browser u razvojnom okruženju obezbeđen je zasebno jer standardni Chromium download nije bio dostupan.

## Granice prve verzije

Ovo je funkcionalan **single-user lokalni prototip**, ne završen production alat. Recorder je custom DOM observer; koristi javni Playwright API za proveru/izvršavanje, ne privatni codegen API. U Capture-u se accessible name heuristički procenjuje; Playwright proverava broj match-eva. Broj 1 znači jedinstveno sada, ne garantovanu stabilnost sutra. Zatvoreni shadow DOM nije podržan. Iframe podrška postoji, ali treba proveriti na ciljnom sistemu.

Multi-tab/popup testovi, drag/drop i native file dialogs nisu automatski snimljeni. UPLOAD snima placeholder koji menjaš lokalnom putanjom. HOVER/WAIT/EXTRACT/DOWNLOAD dodaješ ručno. SPA grupisanje prati vidljive naslove i OPEN korake; proveri imena modula. Recorder može snimiti Enter/Tab i input akcije koje zahtevaju čišćenje u timeline-u. ARIA baseline radi exact string poređenje; nema wildcard/partial matching. Failure predlozi su ograničeni kandidati po ulozi na trenutnoj stranici, ne AI dijagnoza ili tiho self-healing izvršavanje. Use once pokreće nov run, ne nastavlja od neuspelog koraka.

Studio sluša samo na `127.0.0.1`, bez naloga i timskih funkcija. Nema automatsko povezivanje sa već otvorenim privatnim Chrome profilom: otvara svež Chromium. Prijavu radiš u njemu. Screenshotovi, trace, console/network URL-ovi i report mogu sadržati podatke iz aplikacije; čuvaj ih kao test podatke. Password se maskira u Recorder YAML-u, ali trace/screenshots i ručno unete vrednosti nisu sistem za redakciju tajni.

Opcione server postavke: `PORT` (4310), `AGATE_DATA` (data folder). `AGATE_CHROMIUM` i `AGATE_CHROMIUM_ARGS` su opcioni override za razvojni test browser; u normalnoj instalaciji nisu potrebni.
