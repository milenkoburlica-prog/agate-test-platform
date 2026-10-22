# Nachrichten Detail (OPC_MES_GUI_006)

Anzeigename: Nachricht Detail \[Nachricht\]
Anzeige der Details zu einer Nachricht.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Anzeige der Maske „Startdialog Nachrichten (OPC\_MES\_GUI\_001)" |
| Nachricht löschen | Anzeige der Maske „Rückfrage (OPC\_GEN\_GUI\_004)"   * Bei Bestätigung         Aufruf der Funktion „Nachricht löschen (OPC\_MES\_FKT\_005)"     Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"     Im Fehlerfall: Ausgabe der Fehlermeldung * Bei Ablehnung     Anzeige der Maske „Nachrichten Detail (OPC\_MES\_GUI\_006)" |
| Adressaten Anzeigen | Aufruf der Funktion „Adressaten einer Nachricht ermitteln (OPC\_MES\_FKT\_007)"   Im Erfolgsfall: Anzeige der Maske „Adressaten Liste (OPC\_MES\_GUI\_007)"   Im Fehlerfall: Ausgabe der Fehlermeldung |
| Zurück | Anzeige der Maske „Nachrichten Liste (OPC\_MES\_GUI\_005)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Meldungsname |  | A |  |  |
| Meldungstext |  | A |  |  |
| Gültig von |  | A |  | ‚TT.MM.JJJJ'   ‚TT.MM.JJJJ HH:mm' |
| Gültig bis |  | A |  | ‚TT.MM.JJJJ'   ‚TT.MM.JJJJ HH:mm' |
| Nachricht aktiv? |  | A |  |  |
| Ersteller |  | A |  |  |
| Erstelldatum |  | A |  |  |
| Löschdatum |  | A |  |  |
| Anzahl Adressaten |  | A |  |  |
| Filter |  |  | Anzeigeblock, optisch abgegrenzt |  |
| Vertragsparternummern |  | A |  |  |
| Zuname |  | A |  |  |