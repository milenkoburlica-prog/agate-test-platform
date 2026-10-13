# Arhitektura

`public/` je plain browser UI bez build procesa. `src/server.js` služi UI, lokalne REST komande i SSE promene. `src/dom.js` se ubacuje u dokumente target browsera kroz context.addInitScript; exposeBinding šalje akcije na server. Recorder događaji su serijalizovani; uzastopni FILL istog targeta se spajaju. Capture alternative se proveravaju Playwright locator.count metodom.

`src/model.js` je zajednički YAML model, validator, AGATE placeholder resolver, reusable expansion i locator factory. `src/runner.js` izvršava dokument u posebnom browser contextu. Trace, screenshotovi i accessibility snapshotovi su na disku. `src/cli.js` koristi isti runner bez UI-a.

Designer i izvršavanje dele podatkovni model, ne browser sesiju. Run/Debug kreiraju svež browser. Jedan run ima svoj context, buffer objekte i rezultate. Pauza stupa na snagu posle tekuće operacije; cancel zatvara browser. Resume preskače jednom već pogođeni breakpoint. Trajno ažuriranje locatora menja in-memory projekat; Save je potreban za disk.

## Veza sa agate-server

Nisu dodavani zavisnost, endpoint ili izmišljeni postojeći Java ugovor. Sledeća integracija treba da mapira predloženi GUI korak na tvoj `GuiEngine` i već postojeće assertion/buffer/CALL mehanizme. Možeš:

- implementirati iste operacije javnim Playwright Java API-jem u Quarkus engine-u;
- zadržati Node runner kao lokalni/browser worker i slati mu dokument i već razrešene parametre kroz definisan worker API.

Worker API, session lifecycle, parallel run isolation, auth i error mapping moraju se dogovoriti sa stvarnim `agate-server` kodom. Sadašnji REST služi lokalnom Studio prototipu. Samo promene UI-a ne zahtevaju rebuild.

## Tehnička ograničenja

Capture accessible name nije puna W3C implementacija. Generator ne koristi Playwright privatni recorder, čime izbegava oslanjanje na nestabilne interne module. TestId je poželjan samo ako je deo ugovora aplikacije; role/name često je dobar za ponašanje iz ugla korisnika. CSS structural fallback označen je za review.

Traces su Playwright library traces; nije korišćen Playwright Test runner. Ne tvrdimo da library trace automatski ima Playwright Test assertion metadata: ASSERT se izvodi našim retry mehanizmom i zapisuje u report. ARIA snapshot u reportu prikazuje accessibility strukturu; kompletan DOM je u Playwright trace-u.
