# Anschluss suchen (OPC_CLV_GUI_104)

Anzeigename: Anschluss suchen (OPC\_CLV\_GUI\_104)  
Diese Maske dient zum Suchen von Einträgen der Entität Anschluss.   
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Ausgabe einer Liste der laut Suchkriterien gefundenen Anschlüsse.   Aufruf der Funktion „Client-Stammdaten suchen (OPC\_CLV\_FKT\_109)".(Suchebene Anschluss) Abhänig vom Ergebnis der Suche wird mit folgender Aktion fortgesetzt:   * kein Anschluss: Anzeige einer Fehlermeldung „Es wurde kein Eintrag gefunden" * genau ein Anschluss: Anzeigen der Maske „"Anschluss Detailinformationen (OPC\_CLV\_GUI\_106) * mehr als ein Anschluss: Anzeigen der Maske „Anschluss Liste (OPC\_CLV\_GUI\_105)" |

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
| Subnetz | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |
| Router-IP | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |