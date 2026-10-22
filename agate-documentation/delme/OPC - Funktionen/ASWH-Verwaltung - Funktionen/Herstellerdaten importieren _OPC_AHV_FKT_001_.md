# Herstellerdaten importieren (OPC_AHV_FKT_001)

# \_Toc172190394Vorbedingungen

# \_Toc172190395Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Datei | Datei mit Herstellerdaten |  | **M** |

# \_Toc172190396Ausgangsdaten

keine

# \_Toc172190397Beschreibung

Die übergebene Datei wird im Format UTF8 zeilenweise gelesen.  
Tritt dabei ein Fehler auf, wird die Funktion mit der Meldung AHV-001 abgebrochen.  
Die erste Zeile der Datei muss dem definierten Header entsprechen.  
Hersteller-ID;Firmenname;Ablaufdatum;gesperrt.  
Ist dies nicht der Fall, wird die Funktion mit dem Fehler AHV-002 abgebrochen.  
Danach werden die weiteren Zeilen gelesen und folgende Prüfungen durchgeführt.   
Die Anzahl der Datenfelder (getrennt durch  muss 4 sein. Im Fehlerfall Abbruch mit AHV-003  
Für die einzelnen Felder werden folgende Prüfungen durchgeführt. Im Fehlerfall Abbruch mit AHV-004.   
Vor den Prüfungen werden führende und nachfolgende Whitespace entfernt (trim), bis auf die Hersteller-Id dürfen die Felder nicht leer sein.

* Hersteller-ID: leer oder eine 10-stellige alphanumerische Zeichenkette ohne die „leicht verwechselbaren" Zeichen „0", „O", „I", „l" *d.h. es stehen 58 Zeichen zur Verfügung: 9 Ziffern (1-9), 24 Grossbuchstaben(A-H, J-N, P-Z), 25 Kleinbuchstaben* (a-k, m-z)Falls das Feld nicht leer ist, wird es auf Eindeutigkeit geprüft. Ist die ID mehrfach vorhanden, so wird mit dem Fehler AHV-005 abgebrochen.
* Firmenname: beliebige Zeichenkette, Länge kleiner 100
* Ablaufdatum: gültiges Datum im Format „JJJJMMTT", kann auch in Vergangenheit liegen
* gesperrt: „J" oder „N"

Die Liste der gültigen eingelesenen Hersteller-IDs wird gegen die derzeit aktiven (d.h. nicht gesperrten) Hersteller-IDs aus der Entität „ASWH" geprüft. Falls einer der aktiven Einträge nicht in der Liste der eingelesenen Datensätze enthalten ist, wird die Funktion mit der Meldung SVH-007 abgebrochen.  
*Hinweis: Der Partnersupport sollte immer eine komplette Liste der Hersteller liefern. Bereits gesperrt hersteller müssen jedoch nicht enthalten sein.*  
Der Benutzer muss die weiteren Schritte mit der Meldung GUI-901 bestätigen. Ansonsten wird die Funktion abgebrochen.  
Die geprüften Daten werden folgendermassen in die Entität „ASWH" übernommen.

* Falls die Hersteller-ID leer ist, wird aus den zur Verfügung stehenden Zeichen eine zufällig noch nicht existierende ID generiert und diese mit den anderen Werten eigetragen.

Ansonsten wird geprüft, ob die Hersteller-ID bereits in der Entität vorhanden ist.

* Ist dies nicht der Fall, so wird ein neuer Datensatz mit den übergebenen Werten angelegt.

Ist die Hersteller-ID bereits vorhanden, so wird geprüft, ob sich die übergebenen Werte von den bereits eingetragenen Werten unterscheiden.

* Falls ja, werden die Daten aktualisiert.
* Falls nein werden keine DB-Aktualisierungen vorgenommen.

Zum Abschluss wird die Meldung AHV-006 (dabei wird auch die Ursache eines Neueintrag/einer Änderung protokolliert) ausgegeben und die Funktion beendet.

# \_Toc172190398Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-901 | Wollen Sie wirklich für <Anzahl der Hersteller in Datei> Hersteller die Daten aktualisieren? | Message | Info |
| AHV-001 | Fehler beim Lesen der Datei <Dateiname>. | MessageLog | ErrorError |
| AHV-002 | Die Datei <Dateiname> enthält nicht den erwarteten Header. | MessageLog | ErrorError |
| AHV-003 | Die <Zeilennummer>. Zeile der Datei <Dateiname> enthält eine ungültige Anzahl von Feldern | MessageLog | ErrorError |
| AHV-004 | Die <Zeilennummer>. Zeile der Datei<Dateiname> enthält im Feld <Feldname> einen ungültigen Eintrag | MessageLog | ErrorError |
| AHV-005 | Die Datei<Dateiname> enthält die Hersteller-ID <Hersteller-ID> mehrmals. | MessageLog | ErrorError |
| AHV-006 | Die Daten der Datei <Dateiname> wurden erfolgreich importiert.   (Datensätze gesamt: <Anzahl>, davon neu generierte IDs: <Anzahl>, neu übernommene IDs: <Anzahl>, gesperrte IDs: <Anzahl>. entsperrte IDs: <Anzahl>, geänderte Firmennamen: <Anzahl>, unveränderte Datensätze: <Anzahl>) | MessageLog | InfoInfo |
| AHV-007 | Die Datei <Dateiname> enthält nicht alle aktiven Hersteller.   (<Lister der nicht enthaltenen Hersteller-IDs durch Beistrich getrennt, maximal 10 Einträge>) | MessageLog | ErrorError |