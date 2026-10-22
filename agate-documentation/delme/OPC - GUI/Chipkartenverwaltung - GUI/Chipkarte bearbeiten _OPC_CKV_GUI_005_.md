# Chipkarte bearbeiten (OPC_CKV_GUI_005)

Anzeigename: Karte verwalten \[Bemerkung erfassen\]
Maske zur Bearbeitung von Chipkarten. Bei jeder Änderung ist eine Bemerkung anzugeben.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Speichern | Aufruf der Funktion: „Bemerkung zur Karte ändern (OPC\_CKV\_FKT\_003)"   Im Erfolgsfall: Aufruf der Maske „Chipkarten Details (OPC\_CKV\_GUI\_004)" mit der aktualisierten Karte.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Zurück | führt zur Maske „Chipkarten Ergebnis Liste (OPC\_CKV\_GUI\_003)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Bemerkung | M | E | Bemerkungstext des Benutzers | Alphanumerisch |