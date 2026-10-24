# AGATE AI Platform 0.9.1 – Execution-Aware Diagnostics

Java 21 / Quarkus Web Chat, Groq SOAP/XPath diagnosis, read-only multi-engine log tools.

## Änderung gegenüber 0.9.0

- `AgateExecutionParser`: Ermittelt den **tatsächlich fehlgeschlagenen AGATE-DSL-Schritt** vor der Engine-Auswahl. Ein `Exit Code: 1` am Testende wird nicht als CMD-Ursache fehlinterpretiert.
- `SqlResponseCorrelationTool`: Verknüpft `SQL BUFFER` mit einem vorausgehenden `SQL EXEC` desselben Response-Namens und erkennt `SKIPPED (Condition false: ...)`.
- In der Kurzfassung ohne EXEC-Details meldet die Diagnose fehlende Belege, statt eine Ursache zu erfinden.
- Testfall `examples/demo-logs/09-tf080-sql-skipped.log` reproduziert einen synthetischen TF080-Fall.
- Bestehende Groq- und SOAP-Analyse, Web Chat, Tools und Tests bleiben erhalten.

## Start

```powershell
mvn clean test
mvn quarkus:dev -Ddebug=5091
```

Web Chat: http://127.0.0.1:8091/

In einem neuen Chat `examples/demo-logs/09-tf080-sql-skipped.log` hochladen und eingeben:

`Analysiere den Fehler und schlage eine überprüfbare Korrektur vor.`

**Erwartung:** ENGINE SQL, SQL EXEC übersprungen (`COUNT_ID=0`), SQL BUFFER greift trotzdem zu; Bedingung des Verbrauchers prüfen und mit `COUNT=0`/`COUNT=1` testen. Status `KORREKTUR_VORGESCHLAGEN_NICHT_VERIFIZIERT`.

Die vorgeschlagene YAML-Anpassung ist **nicht** automatisch vorgenommen oder in AGATE ausgeführt worden. Bei produktiven Daten zunächst Tokens, Personenkennungen und Credentials anonymisieren.

## Grenzen

Der Parser ist zunächst auf das AGATE-Protokollformat mit `>>> DSL`/`STEP FINISHED` ausgerichtet. Bei interleaved/parallelen Step-Logs oder abweichenden Engine-Ausgaben sind zusätzliche Tests und Parser-Anpassungen erforderlich. Der SQL-Response-Scope wird nur aus dem Log abgeleitet; er wird nicht mit einer laufenden Engine-Instanz abgeglichen.


## 0.9.2 – Diagnostic UX & Evidence Explorer
- Strukturierter Diagnose-Response (`diagnostic`) mit Titel, Kurzbefund, Korrektur, Verifikationsgrenze und belegten Testschritten. Bestehende Felder bleiben erhalten.
- Testschritte sind mit Zeilenreferenzen aus dem **tatsächlich hochgeladenen Log** verknüpft; Klick öffnet den Original-Log-Viewer.
- Die vollständige ursprüngliche Tool-Ausgabe ist unter „Vollständige technische Analyse“ abrufbar.
- Keine automatische Änderung der YAML-Dateien und keine AGATE-Testausführung.
- Logtext verbleibt im Speicher der Browserseite; SOAP-Fälle können an Groq übertragen werden. Vertrauliche Tokens vor Upload maskieren.
