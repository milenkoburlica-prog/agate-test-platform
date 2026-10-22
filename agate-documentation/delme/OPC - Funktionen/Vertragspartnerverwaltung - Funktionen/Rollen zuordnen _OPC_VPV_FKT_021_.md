# Rollen zuordnen (OPC_VPV_FKT_021)

# \_Toc172189995Beschreibung

Die Funktion ordnet der angegebenen Ordination die angegebenen Rollen durch Einträge in der Entität „Systembenutzer Rolle" zu. Existierende Zuordnungen, die nicht in der Liste enthalten sind, werden historisiert.

# \_Toc172189996Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189997Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |
| ListeRollen | Liste von 0-n RollenIds |  | **M** |

# \_Toc172189998Ausgangsdaten

Keine

# \_Toc172189999Meldungen

Keine