# Vertragspartnerdetails suchen (OPC_VPV_FKT_002)

# \_Toc83719392\_Toc172189893Beschreibung

Diese Funktion zeigt sämtliche im Folgenden angeführten Details eines Vertragspartners an:

# \_Toc83719393\_Toc172189894Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc83719394\_Toc172189895Eingangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** |  | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  |  | **M** |

# \_Toc83719395\_Toc172189896Ausgangsdaten

| **Name** | **Beschreibung** |  | **mögliche Werte** |  | **Muss/Kann** |  |
| --- | --- | --- | --- | --- | --- | --- |
| Karteikarte ‚Vertragspartner' |  |  |  |  |  |  |
| Vertragspartner-nummer |  | Vertragspartnernummer |  |  |  | **M** |  |
| LEA Partner-ID |  | LEA Partner-Identifikation |  |  |  | **M** |  |
| ZPV Partner-ID |  | ZPV Partner-Identifikation |  |  |  | **M** |  |
| Zuname |  | Zuname des Vertragspartners. |  |  |  | **M** |  |
| Vorname |  | Vorname des Vertragspartners. |  |  |  | **K** |  |
| Straße |  | Straße und Hausnummer der Zustelladresse der Ordination des Vertragspartners |  |  |  | **M** |  |
| PLZ/Ort |  | Postleitzahl und Ort der Zustelladresse der Ordination des Vertragspartners |  |  |  | **K** |  |
| Bundesland |  | Bundesland der Zustelladresse der Ordination des Vertragspartners |  |  |  | **M** |  |
| Suspendierungs-grund |  | Grund der Suspendierung des Vertragspartners |  |  |  | **M** |  |
| Suspendierungs-datum |  | Datum und Zeitpunkt der Suspendierung des Vertragspartners |  |  |  | **M** |  |
| Bearbeiter |  | Userid des eingeloggten Systembenutzers. |  |  |  | **M** |  |

# \_Toc83719396\_Toc172189897Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-002 | Es wurden keine Detailinformationen zum Vertragspartner <VPNR> konnte gefunden | Log | Error |