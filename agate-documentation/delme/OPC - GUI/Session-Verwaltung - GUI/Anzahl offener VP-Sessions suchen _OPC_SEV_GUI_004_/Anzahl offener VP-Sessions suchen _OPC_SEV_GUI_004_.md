# Anzahl offener VP-Sessions suchen (OPC_SEV_GUI_004)

Anzeigename: Anzahl offener VP-Sessions anzeigen \[Suche\]
Maske zur Suche nach offenen Sessions
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Aufruf der Funktion „Anzahl offener VP-Sessions ermitteln (OPC\_SEV\_FKT\_002)"   Im Erfolgsfall: Anzeige der Maske „Anzahl offene VP-Session Details (OPC\_SEV\_GUI\_005)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | K | E | Die VPNR nach der gesucht wurde | Numerisch |
| GINA-IP | K | E |  | IP4 / IP6 |
| Session offen seit | M | E | Mögliche Werte:   * 1 min * 5 min * 10 min * 20 min * 30 min * 60 min * 2 Std * 1 Tag | Auswahl |