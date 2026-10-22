# Komponente ändern (OPC_VPV_GUI_013)

Anzeigename: Komponente ändern \[Komponente ändern\]
Anischt zum Ändern von Komponenten.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Komponente ändern | Im Erfolgsfall: Aufruf der Maske „Rückfrage (OPC\_GEN\_GUI\_004)"   * Bei BestätigungAufruf der Funktion „Komponente ändern (OPC\_VPV\_FKT\_014)" Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" * Bei Ablehnung: Anzeige der Maske „Komponente ändern (OPC\_VPV\_GUI\_013)"     Im Fehlerfall: Anzeige der Fehlermeldung (nur Validierungsfehler) |
| Zurück | Anzeige der Maske Komponenten Liste (OPC\_VPV\_GUI\_012) |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Komponente | M | A | Name der Komponente | Alphanumerisch |
| Sperren möglich | M | A | Indikator ob eine Sperre für diese Komponente gesetzt werden kann. | Checkbox |
| Pilot möglich | M | A | Indikator ob diese Komponente im Pilotbetrieb ist. | Checkbox |
| Pilot ab | K | E | Startdatum des Pilotbetriebes.   Nur aktiv wenn | ‚TT.MM.JJJJ' |
| Voll ab | M | E |  | ‚TT.MM.JJJJ' |