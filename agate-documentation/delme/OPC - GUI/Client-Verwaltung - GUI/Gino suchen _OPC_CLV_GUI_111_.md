# Gino suchen (OPC_CLV_GUI_111)

Anzeigename: GINO suchen (OPC\_CLV\_GUI\_111)  
Diese Maske dient zum Suchen von Einträgen der Entität Gino und zugeordneten Entitäten.   
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Ausgabe einer Liste der laut Suchkriterien gefundenen Ginos.   Aufruf der Funktion „Client-Stammdaten suchen (OPC\_CLV\_FKT\_109)".(Suchebene Gino) Abhänig vom Ergebnis der Suche wird mit folgender Aktion fortgesetzt:   * kein Treffer: Anzeige einer Fehlermeldung „Es wurde kein Eintrag gefunden" * genau ein Treffer: Anzeigen der Maske „Gino Detailinformationen (OPC\_CLV\_GUI\_113)" * mehr als ein Treffer: Anzeigen der Maske „Gino Liste (OPC\_CLV\_GUI\_112)" |

Folgende Suchkriterien können angegeben werden. (keine Vorbelegung)

| **Suchkriterium** | **Anmerkung** |
| --- | --- |
| Stammdaten-relevante Felder |  |
| Zuordnungstyp | Drop-Down-Liste („alle", „Exot", „Standard") |
| Vertragspartnernummer | Wildcardsuche möglich   Max. 6 alphanumerische Zeichen (0-9a-zA-Z-.\_?\*) |
| Ordinationsnummer | Max 2 numerische Zeichen |
| Zuname | Wildcardsuche möglich   70 alphanumerische Zeichen |
| Vorname | Wildcardsuche möglich   70 alphanumerische Zeichen |
| Anschluss-relevante Felder |  |
| Client-IP | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |
| Subnetz-IP | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |
| Router-IP | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |
| GINO-relevante Felder |  |
| GINO-Seriennummer | 10 alphanumerische Zeichen   Wildcardsuche möglich   Max. 10 Zeichen (0-9.?\*) |
| GINO-Firmware | Nach klicken des Buttons „Zeige Firmwarefilter" erscheint eine Mehrfachauswahlliste.   Die Liste besteht aus allen verfügbaren Firmware-Versionen. Zusätlich wird zu jeder Firmware-Version in Klammer die Anzahl der aktiven/Gesamtzahl Ginos angegeben.   Bsp.: 01.01.25 (236)   Wird kein Element ausgewählt, so wird die Firmware Version bei der Suche ignoriert, ansonten werden nur Ginos mit den entsprechend ausgewählten Firmware Version angezeigt. |