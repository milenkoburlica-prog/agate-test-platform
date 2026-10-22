# Ordinationen Zuordnung ändern (OPC_VPV_GUI_009)

Anzeigename: Rollen zuordnen \[Rollen zuordnen\]
Anzeigename: Sperren zuordnen \[Sperren zuordnen\]
Anzeigename: Piloten zuordnen \[Piloten zuordnen\]
\\
Maske für die Änderung von Zuordnungen zwischen Ordinationen und Rollen / Sperren / Piloten entsprechend dem übergebenen Parameter. Die betroffenen Ordinationen müssen vorselektiert werden, entweder über eine Listenauswahl oder durch das einlesen einer Datei.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Zuordnung durchführen | Aufruf der Maske: „Rückfrage (OPC\_GEN\_GUI\_004)",   * Bei BestätigungParameter = „ROLLEN": Aufruf der Funktion „Rollen zuordnen (OPC\_VPV\_FKT\_021)" Parameter = „SPERREN": Aufruf der Funktion „Sperren zuordnen (OPC\_VPV\_FKT\_022)"Parameter = „PILOTEN": Aufruf der Funktion „Piloten zuordnen (OPC\_VPV\_FKT\_023)"         Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"     Im Fehlerfall: Ausgabe der Fehlermeldung. * Bei Ablehnung Anzeige der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" |
| Zurück | Wurden die Ordinationen über Suchkriterien ausgewählt: Aufruf der Maske: „Ordinationen Liste (OPC\_VPV\_GUI\_007)"   Wurden die Ordinationen über eine Datei ausgewählt: Aufruf der Maske „Datei Info anzeigen (OPC\_VPV\_GUI\_011)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| pro ZuordnungParameter |  |  |  |  |
| Rolle | M | A | Kurze Bezeichnung der Rolle.     Nur angezeigt wenn die Zuordnungsart auf „ROLLE" gesetzt ist. | Alphanumerisch |  |
| Komponente | M | A | Kurze Bezeichnung der Komponente.     Nur angezeigt wenn die Zuordnungsart nicht auf „ROLLE" gesetzt ist. | Alphanumerisch |  |
| Aktuell | M | A | Aktueller Status der Zuordnung.   Mögliche Werte   * „gesetzt" alle Ordinationen sind zugeordnet * „nicht gesetzt" keine Ordination ist zugeordnet * „unterschiedlich" es existieren sowohl Ordinationen die zugeordnet sind, als auch solche die nicht zugeordent sind. |  |  |
| Zuordnen | M | E | Flag um die Ordinationen dem aktuellen Parameter zuzuordnen.     Eingabe deaktiviert, wenn keine Zuordnung möglich ist (bereits alle zugeordnet) | Checkbox |  |
| Entfernen | M | E | Flag um die Zuordnung der Ordinationen zumaktuellen Parameter aufzuheben.     Eingabe deaktiviert, wenn kein Entfernen möglich ist (es existiert keine Zuordnung) | Checkbox |  |
| Beschreibung | M | A | Lange Beschreibung des Parameters | Alphanumerisch |  |