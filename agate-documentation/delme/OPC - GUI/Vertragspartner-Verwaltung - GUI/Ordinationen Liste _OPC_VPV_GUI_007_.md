# Ordinationen Liste (OPC_VPV_GUI_007)

Anzeigename: Ordinationen \[Suchergebnis\]
Listenansicht aller übergebenen Ordinationen.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Alle Auswählen | Wählt alle „Auswahl" Elemente in der Liste aus. |
| Auswahl aufheben | Setzt alle „Auswahl" Elemente in der Liste zurück. |
| Rollen zuordnen | Im Erfolgsfall: Aufruf der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „ROLLE"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Sperren zuordnen | Im Erfolgsfall: Aufruf der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „SPERREN"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Piloten zuordnen | Im Erfolgsfall: Aufruf der Maske: „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „PILOTEN"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Zurück | Anzeigen der Maske „Ordination Suchen (OPC\_VPV\_GUI\_006)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Treffer | M | A | Anzahl der Treffer | Numerisch |  |
| Pro Ordination |  |  |  |  |
| Auswahl | M | E |  | LOV |  |
| VPNR | M | A | Link zur Maske „Zuordnungen anzeigen (OPC\_VPV\_GUI\_008)" für das gewählte Element. | Link, Numerisch |  |
| ORDNR | M | A | Link zur Maske „Zuordnungen anzeigen (OPC\_VPV\_GUI\_008)" für das gewählte Element. | Link, Numerisch |  |
| Ausprägung | M | A |  | Alphanumerisch |  |