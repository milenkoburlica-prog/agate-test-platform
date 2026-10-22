# Zugeordnete Verträge ermitteln (OPC_VPV_FKT_020)

# \_Toc172189989Beschreibung

Die Funktion ermittelt die der angegebenen Ordination zugeordneten gültigen Verträge anhand der Einträge in der Entität „VP Vertrag".

# \_Toc172189990Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189991Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |

# \_Toc172189992Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der gültigen Verträge (0-n), die der Ordination zugeordnet sind. |  |  |  |  |
| Vertragstypcode | Code des Vertragstyps |  | **M** |
| Fachgebietscode | Code des Fachgebiets |  | **M** |
| SVT\_L | Leistungszuständiger SV-Träger |  | **M** |
| SVT\_V | Verrechnungsszuständiger SV-Träger |  | **M** |

# \_Toc172189993Meldungen

Keine