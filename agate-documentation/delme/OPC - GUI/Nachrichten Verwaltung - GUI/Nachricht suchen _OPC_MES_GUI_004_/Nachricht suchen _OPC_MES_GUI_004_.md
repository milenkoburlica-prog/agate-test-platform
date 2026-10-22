# Nachricht suchen (OPC_MES_GUI_004)

Anzeigename: Nachricht suchen \[Suchen\]
Eingabemaske für die Kriterien der Nachrichtensuche.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Aufruf der Funktion „Nachricht suchen (OPC\_MES\_FKT\_003)"   Im Erfolgsfall: Anzeige der Maske „Nachrichten Liste (OPC\_MES\_GUI\_005)"   Im Fehlerfall: Ausgabe der Fehlermeldung. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Meldungsnummer | K | E |  | Numerisch |
| Meldungsname | K | E |  | Alphanumerisch |
| Gültig von | K | E |  | ‚TT.MM.JJJJ'   ‚TT.MM.JJJJ HH:mm' |
| Gültig bis | K | E |  | ‚TT.MM.JJJJ'   ‚TT.MM.JJJJ HH:mm' |
| Nachrichten | K | E |  | Mögliche Werte:   * alle * aktiv * inaktiv |
| Ersteller | K | E |  | Alphanumerisch |
| Erstelldatum | K | E |  | ‚TT.MM.JJJJ HH:mm |
| Vertragspartnernummer | K | E |  | Numerisch |
| Feld1 | K | E | Sortierungsfeld | Mögliche Werte:   * Meldungsnummer * Meldungsname * Gültig Von * Gültig Bis |
| Reihenfolge | K | E | Sortierungsreihenfolge | Mögliche Werte:   * aufsteigend * absteigend |