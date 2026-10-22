# Datei Info anzeigen (OPC_VPV_GUI_011)

Anzeigename: Ordinationen aus Datei zuordnen \[Ordinationen aus Datei zuordnen\]
Zeigt die Details zu einer eingelesene Ordinationsliste an.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Rollen zuordnen | Im Erfolgsfall: Aufruf der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „ROLLE"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Sperren zuordnen | Im Erfolgsfall: Aufruf der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „SPERREN"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Piloten zuordnen | Im Erfolgsfall: Aufruf der Maske: „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „PILOTEN"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Zurück | Anzeigen der Maske „Ordination Suchen (OPC\_VPV\_GUI\_006)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Ausgewählte Datei | M | A | Der Dateiname des Uploads. | Alphanumerisch |
| Anzahl Ordinationen | M | A | Anzahl der Ordinationen die in der Datei gefunden wurden. | Numerisch |