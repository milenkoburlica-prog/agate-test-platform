# Piloten zuordnen (OPC_VPV_FKT_023)

# \_Toc172190007Beschreibung

Die Funktion ordnet der angegebenen Ordination die angegebenen Piloten durch Einträge in der Entität „Systembenutzer Komponente" mit Flag „Pilot" zu. Existierende Zuordnungen, die nicht in der Liste enthalten sind, werden historisiert.

# \_Toc172190008Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172190009Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |
| ListeKomponenten | Liste von 0-n KomponentenCodes |  | **M** |

# \_Toc172190010Ausgangsdaten

Keine

# \_Toc172190011Meldungen

Keine