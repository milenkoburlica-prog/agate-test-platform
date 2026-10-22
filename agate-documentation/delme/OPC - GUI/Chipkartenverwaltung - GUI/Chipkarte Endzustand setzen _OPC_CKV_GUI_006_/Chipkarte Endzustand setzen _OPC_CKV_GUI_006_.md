# Chipkarte Endzustand setzen (OPC_CKV_GUI_006)

Anzeigename: Karte verwalten \[Endstatus setzen\]
Maske zum ändern des Endzustandes von einer Chipkarte. Bei jeder Änderung ist eine Bemerkung anzugeben.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Speichern | Aufruf der Funktion „Endstatus setzen (OPC\_CKV\_FKT\_006)"   Im Erfolgsfall: Aufruf der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Zurück | führt zur Maske „Chipkarten Ergebnis Liste (OPC\_CKV\_GUI\_003)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Hinweis | M | A | „Wird der Endstatus gesetzt, so muss zwingend eine Bemerkung angegeben werden" |  |
| Bemerkung | M | E | Bemerkungstext des Benutzers | Alphanumerisch |