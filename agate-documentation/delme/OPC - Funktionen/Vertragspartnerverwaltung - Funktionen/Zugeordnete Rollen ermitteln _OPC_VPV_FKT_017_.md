# Zugeordnete Rollen ermitteln (OPC_VPV_FKT_017)

# \_Toc172189971Beschreibung

Die Funktion ermittelt die der angegebenen Ordination zugeordneten Rollen anhand der Einträge in der Entität „Systembenutzer Rolle".  
Es werden nur Einträge der Entität „Rolle", für die das Kennzeichen „Zuordnung editierbar" gesetzt ist, berücksichtigt.

# \_Toc172189972Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189973Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |

# \_Toc172189974Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Rollen (0-n), die der Ordination zugeordnet sind |  |  |  |  |
| Rollen ID | Künstlicher Schlüssel der Rolle |  | **M** |
| Bezeichnung | Bezeichnung der Rolle | z.B. BKF-AssessmentVDAS-Stichtagabfrage | **M** |
| Beschreibung | Beschreibung der Rolle |  | **M** |

# \_Toc172189975Meldungen

Keine