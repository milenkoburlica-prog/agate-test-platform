# Session-Informationen suchen (OPC_SEV_GUI_006)

Anzeigename: Session Information \[Suchen\]
Eingabe der Kriterien für die Suche nach Session Informationen.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Aufruf der Funktion „Session(s) suchen (OPC\_SEV\_FKT\_001)"   Im Erfolgsfall: Anzeige der Maske „Session-Informationen Liste (OPC\_SEV\_GUI\_007)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | K | E |  | Numerisch |
| Ordinationsnummer | K | E |  | Numerisch |
| GINA-IP | K | E |  | IP4 / IP6 |
| Terminal-ID | K | E |  | Alphanumerisch |
| Externe User-ID | K | E |  | Alphanumerisch |
| Datum von | K | E |  | ‚TT.MM.JJJJ' |
| Datum bis | K | E |  | ‚TT.MM.JJJJ' |
| Feld 1 | M | E | Mögliche Werte   * Ordinationsnummer * Datum von * Dialog-ID | Auswahl |
| Reihenfolge | M | E |  | „aufsteigend", „absteigend" |