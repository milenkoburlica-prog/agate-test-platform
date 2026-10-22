# Zugeordnete Piloten ermitteln (OPC_VPV_FKT_019)

# \_Toc172189983Beschreibung

Die Funktion ermittelt die der angegebenen Ordination zugeordneten Piloten anhand der Einträge in der Entität „Systembenutzer Komponente" mit gesetztem Flag „Pilot möglich".

# \_Toc172189984Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189985Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |

# \_Toc172189986Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Komponenten (0-n), die der Ordination als Pilot zugeordnet sind. |  |  |  |  |
| KomponentenCode | Code der Komponente |  | **M** |
| Beschreibung | Beschreibung der Komponente |  | **M** |

# \_Toc172189987Meldungen

Keine