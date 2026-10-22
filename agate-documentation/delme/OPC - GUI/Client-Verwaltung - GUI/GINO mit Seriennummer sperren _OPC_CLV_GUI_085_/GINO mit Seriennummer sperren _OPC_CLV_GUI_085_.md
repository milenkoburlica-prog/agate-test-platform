# GINO mit Seriennummer sperren (OPC_CLV_GUI_085)

Anzeigename: GINO mit Seriennummer sperren (OPC\_CLV\_GUI\_085) \[GINO mit Seriennummer sperren\]
In dieser Maske kann die Seriennummer für das zu sperrende Gerät angegeben werden. Alternativ kann eine Datei mit mehreren Seriennummern importiert werden um entsprechende Geräte gleichzeitig zu sperren. Weiters kann ein beliebiger Kommentar zum Sperren angegeben werden.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Sperren | Die Funktion „GINOs sperren (OPC\_CLV\_FKT\_180)" wird aufgerufen. Als Seriennummern werden entweder die mit „Seriennummern importieren" hinzugefügten, oder falls keine importiert wurden, die vom Eingabefeld Seriennummer verwendet. Im Erfolgsfall: Ausgabe der Erfolgsmeldung, sowie Freigabe der Aktion „Weitere GINOs sperren".Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Seriennummern importieren | Es wird die Funktion „GINO Seriennummern aus Datei lesen (OPC\_CLV\_FKT\_179)" ausgeführt und die ausgelesenen Seriennummern dem Feld „Ausgewählte Seriennummern" hinzugefügt. Nach einem erfolgreichen Import, wird diese Aktion gesperrt und es kann keine Seriennummer mehr angegeben werden. |
| Weitere GINOs sperren | Die Maske wird zurück gesetzt. Erst nach einem erfolgreichem Ausführen der Aktion Sperren aktivierbar. |
| Zurück | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| Seriennummer | SERIENNUMMER | K | E | Seriennummer des zu sperrenden Gerätes | 50 alphanumerische Zeichen |
| Kommentar | KOMMENTAR\_SPERRE | K | E | Kommentar warum die Sperrung vorgenommen wurde | 100 alphanumerische Zeichen |
| Datei auswählen |  | K | E | Datei mit Seriennummern | Dateiupload |
| Ausgewählte Seriennummern |  | K | A | Liste mit Seriennummern | Durch „Seriennummern importieren" ausgefüllt |