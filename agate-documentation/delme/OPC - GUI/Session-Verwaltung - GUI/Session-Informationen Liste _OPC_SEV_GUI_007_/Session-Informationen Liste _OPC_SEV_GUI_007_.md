# Session-Informationen Liste (OPC_SEV_GUI_007)

Anzeigename: Session Information \[Session Information\]
Anzeige aller übergebenen Session Informationen.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Ausgewählte Sessioninformation anzeigen | Aufruf der Funktion „Sessiondetails ermitteln (OPC\_SEV\_FKT\_004)" für das durch „Auswahl" bestimmte Element.   Im Erfolgsfall: Anzeige der Maske „Session Details (OPC\_SEV\_GUI\_008)"   Im Fehlerfall: Ausgabe der Fehlermeldung |
| Zurück | Anzeige der Maske „Session-Informationen suchen (OPC\_SEV\_GUI\_006)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A | Vertragspartnernummer nach der gesucht wurde, oder leer | Numerisch |  |
| Ord.Nr. | M | A | Ordinationsnummer nach der gesucht wurde, oder leer | Numerisch |  |
| GINA-IP | M | A | GINA-IP nach der gesucht wurde, oder leer | IP4 / IP6 |  |
| Terminal-ID | M | A | Id nach der gesucht wurde, oder leer | Alphanumerisch |  |
| Externe User-Id | M | A | User-Id nach der gesucht wurde, oder leer | Alphanumerisch |  |
| von | M | A | „Datum von" nach dem gesucht wurde, oder leer | ‚TT.MM.JJJJ' |  |
| bis | M | A | „Datum bis" nach dem gesucht wurde, oder leer | ‚TT.MM.JJJJ' |  |
| pro Session Information |  |  |  |  |
| Auswahl | M | E |  | RadioButton |  |
| Ordination | M | A | Link auf die Aktion „Ausgewählte Sessioninformation anzeigen" mit dem aktuellen Element als „Auswahl". | „Straße Haus/Tür, PLZ Ort" |  |
| Ord. Nr. | M | A |  | Numerisch |  |
| Dialog-ID | M | A |  | Numerisch |  |
| GINA-IP | M | A |  | IP4 / IP6 |  |
| Terminal-ID | M | A |  | Alphanumerisch |  |
| Externe User-ID | M | A |  | Alphanumerisch |  |
| von | M | A |  | ‚TT.MM.JJJJ HH:mm:SS' |  |
| bis | M | A |  | ‚TT.MM.JJJJ HH:mm:SS' |  |