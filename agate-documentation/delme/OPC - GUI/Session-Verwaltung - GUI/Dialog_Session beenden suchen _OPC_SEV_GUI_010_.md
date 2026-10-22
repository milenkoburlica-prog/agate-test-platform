# Dialog_Session beenden suchen (OPC_SEV_GUI_010)

Anzeigename: Dialog/Session beenden \[Suchen\]
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Aufruf der Funktion: „Session(s) suchen (OPC\_SEV\_FKT\_001)"   Im Erfolgsfall: Anzeige der Maske „Dialog/Session beenden Liste (OPC\_SEV\_GUI\_011)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | K | E |  | Numerisch |
| Ordinationsnummer | K | E |  | Numerisch |
| GINA-IP | K | E |  | IP4 / IP 6 |
| Terminal-ID | K | E |  | Numerisch |
| Feld 1 | M | E | Mögliche Werte   * Ordinationsnummer * Datum von * Dialog-ID | Auswahl |
| Reihenfolge | M | E |  | „aufsteigend", „absteigend" |