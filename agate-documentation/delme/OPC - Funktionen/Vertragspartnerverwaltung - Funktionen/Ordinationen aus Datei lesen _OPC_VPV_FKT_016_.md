# Ordinationen aus Datei lesen (OPC_VPV_FKT_016)

# \_Toc172189965Beschreibung

Diese Funktion prüft die in einer Datei angegebenen Ordinationen und retourniert diese als Liste.  
Die Ordinationen werden im folgendenen Format in der angegebenen Datei erwartet:

* Pro Zeile eine Ordination
* Vertragspartnernummer, Ordinationsnummer und Ausprägungscode durch Strichpunkt getrennt. Es müssen alle 3 Parameter angegeben sein

Falls die Datei nicht gelesen werden kann, wird die Funktion mit der Meldung BNV-024 abgebrochen.  
Falls die Datei nicht dem vorgesehenen Format entspricht, wird die Funktion mit der Meldung BNV-025 abgebrochen.  
Für jeden einzelnen Eintrag wird geprüft, ob dafür eine aktive Ordination existiert. Ist dies nicht der Fall wird die Funktion mit der Meldung BNV-026 abgebrochen.  
*Hinweis: Die Funktion wird beim ersten Fehler abgebrochen, d.h. es werden keine weiteren Zeilen geprüft.*

# \_Toc172189966Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189967Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Datei | Name der Datei |  | **M** |

# \_Toc172189968Ausgangsdaten

| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| --- | --- | --- | --- | --- |
| 0 bis n Wiederholungen |  |  |  |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  | **M** |  |
| Ordinationsnummer | Nummer der Ordination. |  | **M** |  |
| Ausprägung | Ausprägung der Ordination. |  | **M** |  |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |  |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |  |

# \_Toc172189969Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-024 | Fehler beim Lesen der Datei <Datei>. | MessageLog | Error |
| BNV-025 | Die <Zeilennummer>. Zeile der Datei <Datei> entspricht nicht dem vorgesehenen Format. | MessageLog | Error |
| BNV-026 | Die in der <Zeilennummer>. Zeile der Datei <Datei> eingetragene Ordination (<Vertragspartnernummer>, <Ordinationsnummer>, <Ausprägungscode>) ist nicht gültig. | MessageLog | Error |