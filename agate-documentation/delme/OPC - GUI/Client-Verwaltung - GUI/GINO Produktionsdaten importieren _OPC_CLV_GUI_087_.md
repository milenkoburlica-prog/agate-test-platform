# GINO Produktionsdaten importieren (OPC_CLV_GUI_087)

Anzeigename: GINO Produktionsdaten importieren (OPC\_CLV\_GUI\_087) \[Importieren\]
In dieser Maske können Files mit GINO Produktionsdaten (SS51) importiert werden.

Die folgenden Aktionen sind aktivierbar

|  |  |
| --- | --- |
| Importieren | Die Funktion „“ wird aufgerufen. Als Produktionsdaten wird der Inhalt des unter "Produktionsdaten laden" geladenen Files verwendet.  Die Aktion „Produktionsdaten laden“ wird gesperrt.  Im Fehlerfall: Ausgabe der entsprechenden Fehlermeldungen.  Im Fall, dass Produktionsdaten schon vorhanden waren: Ausgabe der Warnung und Abfrage der Bestätigung "Import erzwingen".  Im Erfolgsfall: Ausgabe der Erfolgsmeldung sowie Freigabe der Aktionen „Weitere GINOs importieren“. |
| Produktionsdaten laden | Es wird die Funktion „“ mit der ausgewählten Datei ausgeführt und der Inhalt in „Ausgewählte Produktionsdaten“ hinzugefügt. Nach einem erfolgreichen Import wird diese Aktion gesperrt und es kann kein weiteres File angegeben werden. |
| Weitere GINOs importieren | Die Maske wird zurückgesetzt. Erst nach einem erfolgreichen Ausführen der Aktion Importieren aktivierbar. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung () |

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| **Maskenfeld** | **DB –**  **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| Datei auswählen |  | M | E | Datei mit Produktionsdaten | Dateiupload |
| Ausgewählte Produktionsdaten |  | M | A | Produktionsdaten | Durch „Produktionsdaten laden“ ausgefüllt |