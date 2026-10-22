# Dialog_Session beenden Liste (OPC_SEV_GUI_011)_KVW-12428_

Anzeigename: Sessions beenden \[Session beenden\]
Anzeige aller übergebenen Dialoge/Sessions als Liste.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Alle Sessions auswählen | Aktiviert alle „Auswahl" Felder in der Liste. |
| Ausgewählte Dialoge/Sessions beenden | **Maske wird neuerlich angezeigt:**   * Die Eingabe für „Auswahl" und „Zeitpunkt" sind deaktiviert. * Button „Alle Sessions auswählen" wird nicht angezeigt. * Ausblenden aller nicht ausgewählten Elemente. * Ausgabe der Rückfragemeldung * Bei Bestätigung Aufruf der Funktion „Session beenden (OPC\_SEV\_FKT\_005)" für alle gewählten Elemente Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"         Im Fehlerfall: Ausgabe der Fehlermeldung * Bei Ablehnung Anzeige der Maske „Dialog/Session beenden Liste (OPC\_SEV\_GUI\_011)" (Rücksetzten aller Änderungen) |
| Zurück | Anzeige der Maske „Dialog/Session beenden suchen (OPC\_SEV\_GUI\_010)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A |  |  |  |
| Ord.Nr. | M | A |  |  |  |
| GINA-IP | M | A |  |  |  |
| Terminal-ID | M | A |  |  |  |
| Zeitpunkt | M | E | Mögliche Werte:   * sofort * 10 min * 60 min | Auswahl |  |
| pro Dialog/Session |  |  |  |  |
| Auswahl | M | E | Mehrfachauswahl | Checkbox |  |
| Ordination | M | A |  |  |  |
| Ord.Nr. | M | A |  |  |  |
| Dialog-ID | M | A |  |  |  |
| GINA-IP | M | A |  |  |  |
| Terminal-ID | M | A |  |  |  |
| Hersteller-ID | K | A | Die ID des Herstellers |  |  |
| Hersteller-Name | K | A | Der Firmenname des Herstellers (SYS\_ASWH) |  |  |