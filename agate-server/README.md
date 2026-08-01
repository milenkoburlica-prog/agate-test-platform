# Was ist Agate Test Studio?

# 

# Frage: "Willst du Tests schnell und effizient automatisieren?"

# 

# Agate Test Studio ist ein leichtgewichtiges Testautomatisierungs-Framework, das weder Programmiercode noch teure Software-Lizenzen erfordert. Es hilft dir, komplexe Systemabläufe (REST, SOAP, Datenbanken, OpenShift) ohne aufwendige Installationen direkt zu validieren. Agate bringt Ordnung in deine Testlandschaft.

# 

# Systemvoraussetzungen (Was du brauchst)

# 

# Damit Agate einwandfrei funktioniert, stelle sicher, dass folgende Werkzeuge auf deinem Rechner bereitstehen:

# 

# Java Runtime Environment (JRE/JDK) 17+: Das ist die Grundvoraussetzung, um das Agate-Backend auszuführen.

# 

# OC-Client (oc.exe): Wenn deine Tests mit OpenShift-Umgebungen interagieren sollen, muss der OpenShift-Client installiert und im System-PATH verfügbar sein.

# 

# SQLPlus: Wenn du DB-Engine-Tests durchführst, wird SQLPlus benötigt, um die Datenbankverbindungen aufzubauen.

# 

# Schritt 1: Die erste Berührung (Erste Schritte)

# 

# Frage: "Kann ich sofort sehen, wie Agate funktioniert?"

# 

# Ja! Agate Test Studio ist flexibel. Du startest deine Tests direkt über die startTests.bat in deiner Windows-Konsole. Dabei kannst du genau steuern, wer du bist, wo du testest und was genau ausgeführt werden soll.

# 

# 1\. Der Start: So rufst du startTests.bat auf

# 

# Das Skript folgt einem einfachen Schema: 

# 

# startTests.bat \[USER] \[INSTANCE] \[APP] \[TEST\_SUITE] \[TEST\_CASE] \[PRIORITY]

# 

# Hier sind die gängigsten Szenarien, wie du deine Tests starten kannst:

# 

# Den kompletten Testlauf für eine App starten: 

# 

# &#x20;startTests.bat DEMOS DEMOS demo

# 

# &#x20;   (Führt alle verfügbaren Tests in der App "demo" aus.)

# 

# Nur einen spezifischen Test-Suite (Datei) ausführen: 

# 

# startTests.bat Tester1 DEMOS demo cmd\_engine\_demo 

# 

# &#x20;    (Führt nur die Tests in dieser einen Datei aus.)

# 

# Einen ganz bestimmten Testfall (Test Case) isolieren: 

# 

# startTests.bat Tester1 DEMOS demo cmd\_engine\_demo "TC Hello World 1"

# 

# &#x20;    (Hilfreich, wenn du an einem einzigen Test feilst.)

# 

# Nach Priorität filtern (z.B. nur kritische Tests): 

# 

# startTests.bat Milenko DEMOS demo cmd\_engine\_demo "" HIGH

# 

# &#x20;    (Hier setzen wir für den Test Case ein Leer-String "" ein, um nur nach Priorität HIGH zu filtern.)

# 

# Wichtig: Achte darauf, dass du in der Konsole immer im richtigen Projektordner bist, damit das Skript die target/-Datei und deine Konfigurationen findet.

# 

# 2\. Die Analyse: Dein HTML-Report

# 

# Während die Konsole dir den Live-Status zeigt, ist der HTML-Report dein wichtigstes Werkzeug für die Analyse.

# 

# Warum ein Report? Die Konsole ist nur für den Moment. Der HTML-Report hingegen speichert deine Testergebnisse dauerhaft, strukturiert und übersichtlich.

# 

# Wo finde ich ihn? Nach jedem Lauf wird automatisch ein Report im Ordner reports/ erstellt.

# 

# Latest\_Report.html: Immer der aktuellste Lauf.

# 

# Zeitlich gestempelte Reports: Historische Ergebnisse für spätere Vergleiche.

# 

# Schritt 2: Die Umgebung verstehen (Env-Setup)

# 

# Frage: "Wo arbeite ich und wie konfiguriere ich mein System?"

# 

# Um mit Agate Test Studio zu arbeiten, musst du dem System sagen, in welcher Umgebung du dich befindest. Das Setup ist in zwei Dateien unterteilt, die sich im env/-Ordner befinden:

# 

# 1\. Die env/env.conf (Die Welt deiner Instanzen)

# 

# Hier definierst du die "technischen Eckdaten" deiner Zielumgebungen. Jede Instanz beginnt mit einem eindeutigen Namen (z.B. DEMOS).

# 

# Wie füge ich eine neue Umgebung hinzu? Kopiere einfach einen bestehenden Block und passe den Namen vor dem Punkt an.

# 

# Was ist wichtig?

# 

# Jede Zeile folgt dem Muster: InstanzName.Parameter=Wert.

# 

# Hier legst du globale Werte fest, die für die gesamte Instanz gelten (z.B. IP-Adressen für Services oder Pfade zu Log-Check-Tools).

# 

# Hinweis für Profis: Manche Parameter (wie für den Datenbank-Zugriff oder OpenShift-Login) haben feste Namen, die vom Code erwartet werden. Diese werden wir in einem späteren Kapitel im Detail besprechen, wenn wir den SQL- und OC-Engine erklären. Für den Start konzentriere dich erst einmal auf das allgemeine Setup.

# 

# 2\. Die env/users.conf (Deine individuelle Arbeitsweise)

# 

# Während die env.conf das "Wo" definiert, legt die users.conf fest, "wer" mit welchen Daten arbeitet. Hier kannst du spezifische Variablen pro Benutzer innerhalb einer Instanz hinterlegen.

# 

# Wann brauche ich das? Wenn ein Testfall spezifische Daten eines Benutzers benötigt (z.B. eine Seriennummer einer Karte oder eine spezifische IP-Adresse), wird dies hier definiert.

# 

# Wie sieht das aus? InstanzName.BenutzerName.VariablenName=Wert Beispiel: DEMOS.Tester1.seriennummer=99999090

# 

# Schritt 3: Dein eigener Bereich (Application Setup)

# 

# Frage: "Wie organisiere ich meine eigenen Tests?"

# 

# In Agate Test Studio ist eine "Applikation Unter Test" keine komplizierte Installation. Eine Applikation ist einfach ein Ordner unterhalb von data/, in dem alle deine Test-Suites, Konfigurationen und Daten zusammengefasst sind.

# 

# 1\. Die Struktur verstehen

# 

# Wenn du in das Verzeichnis data/ schaust, siehst du bereits vorhandene Applikationen wie demo, foooder boo. Jede dieser Applikationen ist ein eigenständiger Bereich für bestimmte Test-Themen oder Projekte.

# 

# 2\. Deine erste Applikation erstellen

# 

# Das ist der schnellste Weg, um zu starten. Du musst nichts im Code ändern oder irgendetwas registrieren – Agate erkennt neue Ordner automatisch.

# 

# Schritt-für-Schritt:

# 

# Gehe in den Ordner data/.

# 

# Erstelle einen neuen Ordner mit dem Namen deiner Applikation, zum Beispiel: MeinErstesProjekt.

# 

# Das war's! Du hast jetzt eine neue Applikation angelegt.

# 

# 3\. Was gehört in den App-Ordner?

# 

# Damit Agate deine Tests sauber verarbeiten kann, empfiehlt es sich, die Struktur beizubehalten, die du bereits bei den anderen Applikationen siehst:

# 

# Hauptordner: Hier liegen direkt deine .yaml Test-Suite Dateien (z.B. 001\_mein\_test.yaml).

# 

# Unterordner (optional):

# 

# modules/: Für wiederverwendbare Anfragen oder API-Definitionen.

# 

# reusable/: Für allgemeine Test-Bausteine, die du in verschiedenen Tests brauchst (z.B. "Login").

# 

# template/: Für Vorlagen oder Testdaten-Dateien (CSV, XML, etc.).

# 

# Tipp: Wenn du eine neue Applikation anlegst, schau dir einfach den Ordner data/demo an. Dort siehst du eine perfekte Vorlage, wie du deine Dateien ordnen kannst. Kopiere diese Struktur einfach in deinen neuen Ordner, und du bist sofort startklar.

# 

# 

# 

# Schritt 4: Dein erster Testfall (YAML-Struktur)

# 

# Frage: "Wie schreibe ich meinen ersten Test?"

# 

# Ein Test in Agate Test Studio besteht aus einer YAML-Datei. Um zu verstehen, wie man diese aufbaut, gehen wir schrittweise vor. Wir arbeiten hier in deinem neuen Ordner MeinErstesProjekt.

# 

# 1\. Die Basis: hello\_world\_01.yaml

# 

# Jeder Test beginnt mit dem testCases-Block. Auch ein leerer Testfall ist ein valider Startpunkt.

# 

# YAML

# 

# \# Hello World 1 Demo

# testCases:

# 

# 

# Ergebnis: Wenn du jetzt versuchst, den Test zu starten: startTests.bat Milenko ECS\_SYST\_AUT1 MeinErstesProjekt hello\_world\_01 Agate erkennt die Datei, findet aber noch keine auszuführenden Fälle.

# 

# ======================================================================

# &#x20;             Starting Agate Test Suite via Windows CMD

# ======================================================================

# 

# \[INFO] No test cases found matching the provided combination INSTANCE (ECS\_SYST\_AUT1) and PRIORITY ().

# 

# ======================================================================

# &#x20;             Execution finished.

# ======================================================================

# 

# 2\. Ein leerer Testfall: hello\_world\_02.yaml

# 

# Jetzt definieren wir einen Testfall. Er ist noch ohne Schritte ("Steps"), aber das Grundgerüst steht:

# 

# YAML

# 

# testCases:

# &#x20; - id: TC Hello World 2

# &#x20;   description: Hello World 2

# &#x20;   stage: "\*"

# &#x20;   priority: HIGH  

# &#x20;   steps:

# 

# 

# Lerneffekt: Du siehst im Report nun ein \[PASSED], weil das Gerüst korrekt ist und Agate weiß, was es tun soll (nichts!).

# 

# startTests.bat Milenko ECS\_SYST\_AUT1 MeinErstesProjekt hello\_world\_02

# ======================================================================

# &#x20;            Starting Agate Test Suite via Windows CMD

# ======================================================================

# 

# ======================================================================

# TEST CASE: TC Hello World 2

# DESC     : Hello World 2

# STAGE    : \* | PRIO: HIGH

# \----------------------------------------------------------------------

# VARIABLES:

# \----------------------------------------------------------------------

# 

# \--------------------------------------------------------------------------------

# TEST RESULT: TC Hello World 2                              \[PASSED]

# Time: 0,23s | Steps: 0

# \--------------------------------------------------------------------------------

# 

# 

# Finalizing suite and cleaning up resources...

# ================================================================================

# &#x20;                       FINAL EXECUTION SUMMARY

# ================================================================================

# &#x20; Total Test Cases : 1

# &#x20; Passed           : 1

# &#x20; Failed           : 0

# &#x20; Success Rate     : 100,0%

# &#x20; Total Time       : 0,36s

# ================================================================================

# &#x20; OVERALL STATUS   : \[SUCCESS]

# ================================================================================

# 

# >>> Archive HTML report generated at: reports/ECS\_SYST\_AUT1/meinerstesprojekt/hello\_world\_02/SuiteReport\_20260621\_151442.html

# >>> Shortcut 'Latest' report updated at: reports/ECS\_SYST\_AUT1/meinerstesprojekt/hello\_world\_02/Latest\_Report.html

# 

# ======================================================================

# &#x20;            Execution finished.

# ======================================================================

# 

# 3\. Die Logik: hello\_world\_03.yaml (CMD Engine)

# 

# Hier führen wir Test-Steps ein. Jeder Step besteht aus:

# 

# type: Welcher Engine arbeitet? (z.B. CMD für Windows-Befehle).

# 

# op: Was soll der Engine tun? (EXEC = Ausführen, BUFFER = Speichern, ASSERT = Prüfen).

# 

# response: Ein Name, unter dem das Ergebnis gespeichert wird, um es in späteren Schritten wiederzuverwenden.

# 

# Beispiel:

# 

# YAML

# 

# steps:

# &#x20; - type: CMD

# &#x20;   op: EXEC

# &#x20;   command: "java -version"

# &#x20;   response: java\_out

# 

# &#x20; - type: CMD

# &#x20;   op: ASSERT

# &#x20;   action: EXITCODE

# &#x20;   expected: 0

# 

# 4\. Variablen nutzen: hello\_world\_04.yaml

# 

# Damit deine Tests wartbar werden, kannst du Variablen definieren. So musst du Befehle nicht mehrfach schreiben.

# 

# YAML

# 

# testCases:

# &#x20; - id: TC Hello World 1

# &#x20;   variables:

# &#x20;     command: "java -version"

# &#x20;   steps:

# &#x20;     - type: CMD

# &#x20;       op: EXEC

# &#x20;       command: "{B\[command]}"

# 

# 

# Lerneffekt: {B\[variable\_name]} ist die Syntax, um auf deine definierten Variablen zuzugreifen.

# 

# Warum ist das wichtig?

# 

# Modularität: Durch type und op kannst du Agate später auf alles "loslassen" (SQL, REST, SOAP), ohne die Logik des Testfalls zu ändern.

# 

# Datenfluss: Der response-Parameter sorgt dafür, dass Daten von einem Schritt zum nächsten fließen.

# 

# Variablen: Halten deinen Test sauber und erlauben es dir, Konfigurationen vom eigentlichen Testablauf zu trennen.

# 

# Tipp: Wenn du einen Fehler im Test bekommst, wirf immer einen Blick in den Latest\_Report.html. Agate zeigt dir dort genau an, welcher type und op in welchem Schritt gescheitert ist.

# 

# Beispiel Console:

# 

# startTests.bat Milenko ECS\_SYST\_AUT1 MeinErstesProjekt hello\_world\_04

# ======================================================================

# &#x20;            Starting Agate Test Suite via Windows CMD

# ======================================================================

# 

# ======================================================================

# TEST CASE: TC Hello World 1

# DESC     : Hello World 1

# STAGE    : \* | PRIO: HIGH

# \----------------------------------------------------------------------

# VARIABLES:

# &#x20; command         = java -version

# \----------------------------------------------------------------------

# 

# >>> DSL      # 1. Execute and store full response

# >>> DSL      - type: CMD

# >>> DSL        op: EXEC

# >>> DSL        command: "{B\[command]}"

# >>> DSL        response: java\_out

# &#x20;   >>> CMD       : java -version

# &#x20;   <<< OUT       : openjdk version "21.0.4" 2024-07-16 LTS

# &#x20;   <<< OUT       : OpenJDK Runtime Environment (Red\_Hat-21.0.4.0+7-1) (build 21.0.4+7-LTS)

# &#x20;   <<< OUT       : OpenJDK 64-Bit Server VM (Red\_Hat-21.0.4.0+7-1) (build 21.0.4+7-LTS, mixed mode, sharing)

# &#x20;   >>> RESULT    : SUCCESS | Exit: 0

# &#x20;   >>> CMD STEP FINISHED | Status: SUCCESS | Duration: 99 ms

# 

# >>> DSL      # 2. Buffer complete output-text

# >>> DSL      - type: CMD

# >>> DSL        op: BUFFER

# >>> DSL        response: java\_out

# >>> DSL        action: TEXT

# >>> DSL        name: var\_version\_only

# &#x20;   >>> BUFFER    : \[java\_out] | Save Full Output to \[var\_version\_only]

# &#x20;   <<< OUT       : openjdk version "21.0.4" 2024-07-16 LTS

# &#x20;   <<< OUT       : OpenJDK Runtime Environment (Red\_Hat-21.0.4.0+7-1) (build 21.0.4+7-LTS)

# &#x20;   <<< OUT       : OpenJDK 64-Bit Server VM (Red\_Hat-21.0.4.0+7-1) (build 21.0.4+7-LTS, mixed mode, sharing)

# &#x20;   >>> RESULT    : SUCCESS | Buffered

# &#x20;   >>> CMD STEP FINISHED | Status: SUCCESS | Duration: 0 ms

# 

# >>> DSL      # 3. Assert Exit Code

# >>> DSL      - type: CMD

# >>> DSL        op: ASSERT

# >>> DSL        response: java\_out

# >>> DSL        action: EXITCODE

# >>> DSL        expected: 0

# &#x20;   >>> ASSERT    : EXITCODE | Expected: \[0]

# &#x20;   >>> RESULT    : SUCCESS | Exit: 0

# &#x20;   >>> CMD STEP FINISHED | Status: SUCCESS | Duration: 0 ms

# 

# \--------------------------------------------------------------------------------

# TEST RESULT: TC Hello World 1                              \[PASSED]

# Time: 0,34s | Steps: 3

# \--------------------------------------------------------------------------------

# 

# 

# Finalizing suite and cleaning up resources...

# ================================================================================

# &#x20;                       FINAL EXECUTION SUMMARY

# ================================================================================

# &#x20; Total Test Cases : 1

# &#x20; Passed           : 1

# &#x20; Failed           : 0

# &#x20; Success Rate     : 100,0%

# &#x20; Total Time       : 0,47s

# ================================================================================

# &#x20; OVERALL STATUS   : \[SUCCESS]

# ================================================================================

# 

# >>> Archive HTML report generated at: reports/ECS\_SYST\_AUT1/meinerstesprojekt/hello\_world\_04/SuiteReport\_20260621\_151756.html

# >>> Shortcut 'Latest' report updated at: reports/ECS\_SYST\_AUT1/meinerstesprojekt/hello\_world\_04/Latest\_Report.html

# 

# ======================================================================

# &#x20;            Execution finished.

# ======================================================================

# 

# 

# 

# Beispiel Report:

# 

# 

# Schritt 6: Dein erster "Hello World" Test (Zusammenfassung)

# 

# Frage: "Kann ich meinen eigenen Test jetzt ausführen?"

# 

# Du hast jetzt alle Werkzeuge in der Hand. Hier ist der "Fast-Track", um alles zu verifizieren:

# 

# Projekt anlegen: Erstelle einen neuen Ordner in data/ (z.B. MeinErstesProjekt).

# 

# Datei erstellen: Lege eine hello\_world\_05.yaml Datei an.

# 

# Test definieren: Nutze die Struktur aus Schritt 4 (id, description, variables, steps).

# 

# Ausführen: Starte den Test über die Konsole: startTests.bat Milenko ECS\_SYST\_AUT1 MeinErstesProjekt hello\_world\_05

# 

# Analysieren: Öffne reports/.../Latest\_Report.html und klicke auf den Testnamen, um deine Schritte "aufzuklappen".

# 

# 

# 

# 7\. Steuerung des Testlaufs: Stage und Priority (Header-Filterung)

# 

# Frage: "Wie steuert Agate, welche Testfälle in welcher Umgebung ausgeführt werden?"

# 

# Im Header jedes Testfalls (TestCase) befinden sich zwei Steuerungsfelder: stage und priority. Das Agate-Backend, um vor jedem Testlauf anhand der System-Properties (-DSTAGE und -DPRIO) zu entscheiden prüft, ob ein Testfall ausgeführt oder übersprungen wird.

# 

# &#x20; - id: FOO anfrage

# &#x20;   description: FOO anfrage

# &#x20;   stage: "DEMOS"

# &#x20;   priority: HIGH

# &#x20;   variables:

# &#x20;     {}

# 

# 

# 2.1.1. Das stage-Feld (Umgebungs-Filterung)

# 

# Das stage-Feld bestimmt, für welche Zielinstanz oder Testumgebung (z. B. Entwicklung, Demo, Produktion) der Testfall geschrieben wurde. Beim Starten des Test-Skripts wird dieser Wert mit dem Parameter \[INSTANCE] (System-Property STAGE) verglichen.

# 

# Verhaltensmatrix für stage (Case-Insensitive):

# 

# Zustand im YAML (stage)

# 

# Aufruf im Befehl (STAGE)

# 

# Verhalten der Engine \& Begründung

# 

# \* (Wildcard)

# 

# Beliebige Instanz (z.B. DEMOS)

# 

# Wird ausgeführt. Der Asterisk \* ist ein Jokerzeichen und bedeutet, dass der Testfall absolut umgebungsunabhängig ist.

# 

# Nicht definiert (null)

# 

# Beliebige Instanz

# 

# Wird übersprungen (FALSE). Wenn im YAML kein stage-Wert definiert ist (tc.getStage() == null), bricht der Filter für diesen Testfall ab, da keine Zuweisung existiert.

# 

# Leerer String ("")

# 

# Beliebige Instanz

# 

# Wird übersprungen (FALSE). Ein leeres Feld matcht nicht den übergebenen Instanznamen.

# 

# Definiert (z.B. DEMOS)

# 

# Nicht übergeben / Leer

# 

# Wird ausgeführt (TRUE). Wenn der Benutzer beim Start keine spezifische Instanz erzwingt (stageParam == null), lässt Agate standardmäßig alle Testfälle durch.

# 

# Definiert (z.B. DEMOS)

# 

# DEMOS (oder demos)

# 

# Wird ausgeführt (TRUE). Der String-Vergleich matcht exakt (Groß-/Kleinschreibung wird ignoriert).

# 

# Komma-getrennt (foo, DEMOS, boo)

# 

# DEMOS

# 

# Wird übersprungen (FALSE). Achtung: Der aktuelle Code prüft auf exakte String-Gleichheit (equalsIgnoreCase). Eine Listen-Erkennung oder ein .contains() ist im Code nicht implementiert. Der Testfall würde nur starten, wenn die System-Property exakt als foo, demos, boo übergeben wird.

# 

# 2.1.2. Das priority-Feld (Wichtigkeit \& Testtiefe)

# 

# Mit dem Feld priority steuerst du die Testtiefe. Agate nutzt hierbei eine hierarchische Filterung. Das bedeutet: Ein höher priorisierter Testfall wird immer dann ausgeführt, wenn eine niedrigere oder gleiche Priorität angefordert wird.

# 

# Die interne Hierarchie ist wie folgt definiert:

# 

# “LOW” < “MEDIUM” < “HIGH” < “CRITICAL”

# 

# Verhaltensmatrix für priority:

# 

# Zustand im YAML (priority)

# 

# Aufruf im Befehl (PRIO)

# 

# Verhalten der Engine \& Begründung

# 

# Beliebiger Wert

# 

# Nicht definiert / Leer

# 

# Wird ausgeführt (TRUE). Wenn beim Starten kein Filter gesetzt wird, führt Agate ungeachtet der Priorität jeden Testfall aus.

# 

# Nicht definiert (oder ungültig)

# 

# HIGH

# 

# Wird übersprungen (FALSE). Wenn im YAML keine Priorität definiert ist, fällt Agate intern automatisch auf die Stufe LOW zurück. Da LOW < HIGH, wird die Bedingung nicht erfüllt.

# 

# LOW

# 

# HIGH

# 

# Wird übersprungen (FALSE). Das Test-Level (LOW) ist kleiner als das erforderliche Level (HIGH).

# 

# CRITICAL

# 

# HIGH

# 

# Wird ausgeführt (TRUE). Das Test-Level (CRITICAL) ist größer als das erforderliche Level (HIGH) .

# 

# HIGH

# 

# HIGH

# 

# Wird ausgeführt (TRUE). Exakter Match (3 = 3).

# 

# HIGH

# 

# Ungültiger String (z.B. FOO)

# 

# Wird ausgeführt (TRUE). Wenn der Benutzer einen ungültigen Prioritäts-Filter übergibt, den Agate nicht kennt, wird der Filter ignoriert und der Testfall sicherheitshalber ausgeführt.

# 

# 8\. Ausblick: Was kommt als nächstes?

# 

# Ausblick: Die Zukunft von Agate Test Studio

# 

# Agate Test Studio ist ein wachsendes Ökosystem. Damit du dein Wissen gezielt aufbauen kannst, habe ich den Lernpfad in klare Module unterteilt:

# 

# Der Lernpfad für Agate-Profis

# 

# Quick Start Guid (Dieser Guide): Du hast das Fundament – das Agate Backend, Konfiguration und den ersten eigenen Test – erfolgreich gemeistert.

# 

# Advanced User Tutorial (In Arbeit): Hier tauchen wir tief in die einzelnen Engines ein. Wir analysieren im Detail, wie CMD, SQL, OC (OpenShift), REST und SOAP im Agate-Backend funktionieren.

# 

# Expert User Tutorial (In Arbeit): Hier werden wir das volle Potenzial ausschöpfen. Wir konzentrieren uns auf das DSL-Template-Konzept und Data-Driven Testing (DDT). Du lernst, wie du mit einer einzigen Vorlage (YAML) und einem CSV-Datensatz tausende Testvariationen automatisiert ausführst.

# 

# TOSCA Migration Concept (In Arbeit): Für alle, die von Tosca umsteigen möchten – hier zeigen wir das Konzept, wie du Tosca-Testsets effizient in das Agate DSL-Format migrierst.

# 

# Status der "Next Generation" Features

# 

# Neben dem stabilen Core-Backend entwickeln sich parallel diese visionären Bereiche:

# 

# Agate Client \& AI-Integration: Die Client-Server-Architektur mit integriertem AI-Chatbot (Ollama) steht bereit, um Test-Engineering durch KI-Unterstützung zu revolutionieren.

# 

# Playwright GUI-Engine: Das nächste Level für Web-Tests mit automatisiertem Site-Capture.

# 

# Ein Wort zum aktuellen Status: Agate Test Studio ist ein leidenschaftliches Open-Source-Projekt, das ich in meiner Freizeit entwickle. 

# 

# Du hast Interesse, an der Zukunft mitzubauen oder Beta-Feedback zu geben? Ich freue mich auf den Austausch!

# 



