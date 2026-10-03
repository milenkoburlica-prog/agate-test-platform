# Plan za zajednički review

Označi: [ ] nije probano, [x] zadovoljavajuće, [~] treba korekcija. Svaka originalna ideja ima mesto ovde; ništa se ne podrazumeva kao production-ready.

| # | Ideja | Implementacija u 0.1 | Šta proveriti |
|---|---|---|---|
| 1 | RECORD | Klik, FILL, SELECT, CHECK/UNCHECK, Enter/Tab/Escape; pravi browser | [ ] Login i navigacija na tvojoj aplikaciji |
| 2 | Live YAML + timeline | SSE live prikaz istog dokumenta | [ ] Da li je tok jasan testeru |
| 3 | Uređivanje grešaka | Delete, multi-delete, move, operation/value/target edit | [ ] Pogrešan klik i Back; Run od početka |
| 4 | SCAN/CAPTURE | DOM + open shadow scan; biranje relevantnih elemenata | [ ] SVC stranica, custom JSF kontrole |
| 5 | Smart Capture | Hover highlight u Pick/Assert, alternativa, quality razlog, match count | [ ] Accessible names i lokalizacija |
| 6 | Page modules | Dot references, kopije page YAML-a, inline locatori | [ ] Konvencija putanja i imena |
| 7 | Auto modules iz recording-a | Vidljivi heading + OPEN, heuristika za SPA | [ ] Granice stranica, dupli nazivi |
| 8 | Assertions recording | Visible/hidden/enabled/disabled/text/value/count | [ ] Tekst, tabela, disabled kontrole |
| 9 | ARIA snapshots | Save baseline; ASSERT_PAGE_STRUCTURE exact match | [ ] Dinamički sadržaj; partial match kao sledeća dorada |
| 10 | Parametrize | R/B/E/L, prompt editor i direktni YAML | [ ] Uskladiti sa tvojim buffer resolverom |
| 11 | Extract variable | EXTRACT text/value/attribute → B | [ ] Koristi {B[name]} u sledećem koraku |
| 12 | Reusable | Consecutive selected block → named CALL, L parameters | [ ] Povezati sa AGATE external CALL resolverom |
| 13 | RUN | Playwright library runtime + CLI | [ ] Realna aplikacija; timeout i auth |
| 14 | DEBUG | Start paused, next, continue, breakpoint, pause/cancel | [ ] Breakpoint i step evidence |
| 15 | Trace | Lokalni Trace Viewer + trace ZIP | [ ] Snapshot/action/console/network pregled |
| 16 | Failure explain | Prava greška + reason/checklist + candidate role matches | [ ] Promena button naziva; heuristika, ne AI |
| 17 | Locator Health | missing/unique/ambiguous na trenutnoj stranici | [ ] Ostale stranice normalno pokazuju missing |
| 18 | Reviewed repair | Use once ili update locator/module + rerun from start | [ ] Nema tihog self-healinga; proveri kandidata |
| 19 | Beginner/tester/expert | Record, Capture, YAML dele model | [ ] UX i učenje, bez posebnog recorded formata |
| 20 | Runtime operacije | OPEN/BACK/CLICK/FILL/SELECT/CHECK/UNCHECK/HOVER/PRESS/UPLOAD/DOWNLOAD/WAIT/ASSERT/EXTRACT/SCREENSHOT/ASSERT_PAGE_STRUCTURE/CALL | [ ] Posebno upload/download, iframe, custom controls |
| 21 | Jednostavan UX | Record, Capture tab, Run, Debug; guide i demo | [ ] Šta korisnik traži prvih 10 minuta |
| 22 | Studio/server granica | Designer API i nezavisan runner/model | [ ] Java/Quarkus GuiEngine integracija tek s tvojim kodom |

## Posle prvog prolaska

1. Uskladiti GUI schema sa postojećim AGATE engine interfejsom.
2. Recorder poboljšati na stvarnim JSF/SPA stranicama i odabrati semantiku locatora.
3. Povezati postojeći validator, shared/app reusable i R/B/E/L resolver.
4. Dodati multi-tab, drag/drop, persist auth state, kompletan element editor ako se pokažu potrebni.
5. Partial ARIA matcher, bolje failure candidate rangiranje i module health po posećenim stranicama.
6. Production arhitektura: više sesija, autentikacija, browser worker lifecycle i redakcija osetljivih artefakata.
