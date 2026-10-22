# Systemparameter (OPC_ALG_GUI_002)

Anzeigename: Systemparameter konfigurieren [Systemparameter]  
Anzeigename der Unterpunktaufrufe: Systemparameter konfigurieren [<Gruppenname>]

Maske zur Anzeige und Bearbeitung von Systemparametern.

Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Speichern | Es wird die Eingabe auf der Seite deaktiviert und eine Bestätigung durch den Benutzer eingefordert.  Bestätigt der Benutzer die Eingaben, so wird die Funktion „“ aufgerufen  Im Erfolgsfall wird die Maske „“ angezeigt. Im Fehlerfall die Fehlermeldung ausgegeben.  Lehnt der Benutzer die Bestätigung ab, so wird die Seite neu aufgebaut und die Felder sind wieder gemäß ihren Parameter ediertbar. |
| Hinzufügen (nur in den Untergruppen) | Es wird die Liste "Systemparameter" ausgeblendet und stattdessen das Formular für das Hinzufügen "Neuer Systemparameter" angezeigt, welche vom Benutzer wie gewünscht ausgefüllt werden kann.  Durch ein erneutes Auswählen des "Hinzufügen" Buttons wird ein neuer Systemparameter mit den eingegebenen Werten angelegt.  [Systemparameter hinzufügen (OPC\_ALLG\_FKT\_003)](https://confluence.tools.svc.co.at/pages/viewpage.action?pageId=209535703)  Im Erfolgsfall wird die Maske „“ angezeigt. Im Fehlerfall die Fehlermeldung ausgegeben. Am Ende des Prozesses wird die Seite neu aufgebaut und die Felder sind wieder gemäß ihren Parameter ediertbar. |

Formular "Systemparameter"

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Parameter Name | M | A | Wurde keine Kategorie ausgewählt, wird hinter dem Namen des Parameters auch die Gruppe angefügt.   Wurde eine Kategorie ausgewählt, wird nur der Name des Parameters ausgegeben, ohne Kategorie. | Formatspezifikation gemäß Rückmeldung |
| Parameter Wert | M | E | Der aktuelle Wert des Parameters.   Das Feld ist editierbar, wenn das „Editable“ Flag des Parameters gesetzt ist, sonst nicht. | Die Validierung erfolgt gegen das Feld „Valid" des Parameters. |
| Hilfe Link | M | A | Öffnet die Hilfeseite für den aktuellen Parameter in einem neuen Fenster/Tab, siehe „“ | Link |

Formular "Neuer Systemparameter"

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Gruppe | M | A | Die Gruppe des neuen Parameters |  |
| Name | M | E | Der Name des neuen Parameters | Max. 100 Zeichen |
| Wert | M | E | Der Standardwert des neuen Parameters | Max. 1000 Zeichen |
| Beschreibung | K | E | Die Beschreibung des neuen Parameters | Max. 1000 Zeichen |
| Regex | K | E | Die "Regex" Regel zur Validierung des Parameters | Max. 1000 Zeichen |