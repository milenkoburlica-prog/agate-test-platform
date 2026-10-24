# AGATE AI Platform 0.9.0 – Demo-Tests

Öffne http://127.0.0.1:8091/. Pro Test einen neuen Chat starten, die jeweilige `.log`-Datei über `+` hochladen und folgende Nachricht senden:

`Analysiere diesen fehlgeschlagenen AGATE-Test. Nenne die Fehlerbelege und geeignete Prüfschritte.`

| Datei | Erwarteter Befund | Status |
|---|---|---|
| 00-soap-xpath.log | Falscher Constraint-XPath (Groq + Java) | XPATH_LOKAL_BESTÄTIGT, wenn das Modell einen gültigen Vorschlag liefert |
| 01-sql-oracle-ora00942.log | ORA-00942, Tabelle/View nicht zugänglich | MUSTER_ERKANNT_NICHT_VERIFIZIERT |
| 02-cmd-command-not-found.log | Kommando nicht gefunden, Exit-Code | MUSTER_ERKANNT_NICHT_VERIFIZIERT |
| 03-openshift-crashloop.log | CrashLoopBackOff + OOMKilled | MUSTER_ERKANNT_NICHT_VERIFIZIERT |
| 04-rest-http500.log | HTTP 500 | MUSTER_ERKANNT_NICHT_VERIFIZIERT |
| 05-gui-playwright-timeout.log | Playwright-Locator-Timeout | MUSTER_ERKANNT_NICHT_VERIFIZIERT |
| 06-buffer-missing.log | MISSING_BUFFER | MUSTER_ERKANNT_NICHT_VERIFIZIERT |
| 07-postgresql-duplicate.log | PostgreSQL Unique-Constraint | MUSTER_ERKANNT_NICHT_VERIFIZIERT |
| 08-unbekannter-fehler.log | Kein sicheres Muster | NICHT_PRÜFBAR |

Wichtig: Nur der SOAP-Pfad nutzt derzeit Groq. Alle anderen Muster werden deterministisch auf Basis des Logtextes erkannt. Keine Datenbank-, OpenShift- oder Shell-Verbindungen. Keine tatsächliche Fix-Verifikation für diese Engines.
