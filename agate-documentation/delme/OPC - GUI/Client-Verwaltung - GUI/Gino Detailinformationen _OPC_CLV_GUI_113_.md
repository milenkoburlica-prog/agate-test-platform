# Gino Detailinformationen (OPC_CLV_GUI_113)

Anzeigename: GINO Detailinformationen (OPC\_CLV\_GUI\_113)   
Auf dieser Maske werden Detailinformationen der Gino angezeigt.  
Die neben den Client-Stammdaten notwendigen Daten für die Anzeige, werden durch Aufruf der Funktion „Detailinformation – GINO ermitteln (OPC\_CLV\_FKT\_026)" ermittelt.  
Für die Anzeige sind die Felder nicht editierbar. Ausnahme: der Streuwert kann editiert und gespeichert werdenFolgende Aktion steht zur Verfügung:

|  |  |
| --- | --- |
| Anschluss anzeigen | Anzeigen der Maske „Anschluss Detailinformationen (OPC\_CLV\_GUI\_106)" für den dem Gino zugeordneten Anschluss |
| Speichern | Aktualisert den entpsrechenden Eintrag in der Entität GINO. |
| Zurück | Anzeigen der Maske „Gino Liste (OPC\_CLV\_GUI\_112) |
| Resource Status | Anzeigen der Maske „Gino Resource Status (OPC\_CLV\_GUI\_114)" |
| Aktuelle Gruppen Zuordnung | Anzeigen der Maske „Gino Gruppen Zuordnungen (OPC\_CLV\_GUI\_115)" |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

Die Maske dient zur Anzeige der Entität Gino (CL\_GINO)

| **Maskenfeld** | **Zuordnung Entität** | **Plausibilität** |
| --- | --- | --- |
| Entität GINO |  |  |
| Name | Name |  |
| Gruppe | Gruppe |  |
| Seriennummer | Seriennummer |  |
| Status | „nicht freigegeben" falls KZ\_FREIGABE nicht gesetzt, „nicht aktiv" falls „letzter Kontakt" zu weit zurück", „aktiv" falls letzter Kontakt im dafür vorgesehnen Zeitraum (definiert durchKonfigurationsparameter „clv.gino.kontakt", Default 31 Minuten) stattgefunden hat. |  |
| GINO-IP | IP-Adresse | Max. 15 Zeichen (0-9.?\*) |
| Zugriff NAT IP | ZUGRIFF\_NAT\_IP |  |
| Zugriff DNS | ZUGRIFF\_DNS |  |
| Satellit Seriennummer | Serienummer des Satelliten |  |
| Anlegezeitpunkt | Anlegezeitpunkt im Format „DD.M.YYYYY HH:MM" |  |
| Letzter Kontakt | Letzter Kontakt im Format „DD.M.YYYYY HH:MM" |  |
| Firmware Version | Firmware Version |  |
| Streuwert | Streuwert |  |
| Entität Gino Produktionsdaten |  |  |
| MAC Adresse | MAC-Adresse | 20 alphanumerische Zeichen |