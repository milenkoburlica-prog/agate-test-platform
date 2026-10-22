# GINO-Firmware hochladen (OPC_CLV_GUI_151)

Anzeigename: GINO-Firmware hochladen \[GINO-Firmware hochladen\]
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Speichern | Sichtbar nachdem eine Datei ausgewählt wurde.Aufruf der Funktion „GINO-Firmware speichern (OPC\_CLV\_FKT\_150)".   Im Gutfall Ausgabe ein Gutmeldung Anzeige der Maske mit dem Button „Weitere Firmware hochladen" und nicht änderbaren Feldern.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Durchsuchen (Datei auswählen/ andere Datei auswählen) | Hinweis: „andere Datei auswählen" wird angezeigt, falls bereits eine Datei ausgewählt wurde.   Öffnet ein Dateimenü zum Selektieren der Firmware-Datei   Bei Auswahl einer Datei, werden aus dem Dateinamen   * Die Softwareversion und * die Hardware Version         ermittelt und in die Eingabedatenfelder übernommen, diese können bei Bedarf überschrieben werden.     Für die Übernahme der Versionsfelder wird ein Dateinamensschema     ginofirmware\_<hwversion>\_<dd.dd.dd>.tar.ogz vorausgesetzt, wobei ‚d' für eine dezimale Ziffer steht.         Folgt die Datei einem anderen Schema, so erfolgt keine Übernahme der Versiondaten in den Eingabefeldern. |
| Weitere Firmware hochladen | Zeigt wieder Initialzustand der Maske zur Auswahl einer weiteren Datei. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Software Version |  | M | E/A | Software Version der Firmware | 3 Zahlen durch „." getrennt, z.B. 1.20.3 |
| Hardware Version |  | M | E/A | Hardware Version der Firmware | 100 Zeichen |
| Letzte kompatilble Version |  | M | E/A | Letzte kompatilble Version der Firmware | 3 Zahlen durch „." getrennt, z.B. 1.20.1 |
| Dateiname |  | M | A | Name der Firmware Datei, wird durch Dateiauswahl mit Button „Durchsuchen" befüllt. | 255 alphanumerische Zeichen (keine Überprüfung) |