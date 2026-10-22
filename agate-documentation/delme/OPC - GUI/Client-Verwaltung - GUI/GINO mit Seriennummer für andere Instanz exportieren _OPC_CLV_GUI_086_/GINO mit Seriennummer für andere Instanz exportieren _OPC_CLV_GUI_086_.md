# GINO mit Seriennummer für andere Instanz exportieren (OPC_CLV_GUI_086)

Anzeigename: GINO mit Seriennummer für andere Instanz exportieren (OPC\_CLV\_GUI\_086) \[GINO Gerät exportieren\]
In dieser Maske kann die Seriennummer für das zu exportierende Gerät angegeben werden. Alternativ kann eine Datei mit mehreren Seriennummern importiert werden um entsprechende Geräte gleichzeitig zu exportieren.

|  |  |
| --- | --- |
| Die folgenden Aktionen sind aktivierbar:Exportieren | Die Funktion „GINOs für andere Instanz vorbereiten (OPC\_CLV\_FKT\_181)" wird aufgerufen. Als Seriennummern werden entweder die mit „Seriennummern importieren" hinzugefügten, oder falls keine importiert wurden, die vom Eingabefeld Seriennummer verwendet. Im Erfolgsfall: Ausgabe der Erfolgsmeldung sowie Freigabe der Aktionen „Download" und „Weitere Gino exportieren". Die Aktion „Seriennummer importieren" wird gesperrt.Im Fehlerfall: Ausgabe der entsprechenden Fehlermeldungen. |
| Seriennummern importieren | Es wird die Funktion „GINO Seriennummern aus Datei lesen (OPC\_CLV\_FKT\_179)" ausgeführt und die ausgelesenen Seriennummern dem Feld „Ausgewählte Seriennummern" hinzugefügt. Nach einem erfolgreichen Import, wird diese Aktion gesperrt und es kann keine Seriennummer mehr angegeben werden. |
| Download | SS51 konforme .csv Datei mit den Herstellerdaten wird heruntergeladen um den GINO in der ASWH/Test-Instanz einspielen zu können. Erst nach erfolgreichem Ausführen der Aktion Exportieren aktivierbar. |
| Weitere GINOs exportieren | Die Maske wird zurück gesetzt. Erst nach einem erfolgreichem Ausführen der Aktion Exportieren aktivierbar. |
| Zurück | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| Seriennummer | SERIENNUMMER | K | E | Seriennummer des zu exportierenden Gerätes | 50 alphanumerische Zeichen |
| Datei auswählen |  | K | E | Datei mit Seriennummern | Dateiupload |
| Ausgewählte Seriennummern |  | K | A | Liste mit Seriennummern | Durch „Seriennummern importieren" ausgefüllt |