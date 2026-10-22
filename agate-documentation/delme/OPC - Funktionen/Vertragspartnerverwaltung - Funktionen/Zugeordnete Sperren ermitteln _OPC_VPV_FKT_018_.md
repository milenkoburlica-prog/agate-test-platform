# Zugeordnete Sperren ermitteln (OPC_VPV_FKT_018)

# \_Toc172189977Beschreibung

Die Funktion ermittelt die der angegebenen Ordination zugeordneten Sperren anhand der Einträge in der Entität „Systembenutzer Komponente" mit gesetztem Flag „Sperre möglich".

# \_Toc172189978Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189979Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |

# \_Toc172189980Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Komponenten (0-n), die der Ordination als Sperre zugeordnet sind. |  |  |  |  |
| KomponentenCode | Code der Komponente |  | **M** |
| Beschreibung | Beschreibung der Komponente |  | **M** |

# \_Toc172189981Meldungen

Keine