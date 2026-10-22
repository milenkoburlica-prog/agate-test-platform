# Sperren zuordnen (OPC_VPV_FKT_022)

# \_Toc172190001Beschreibung

Die Funktion ordnet der angegebenen Ordination die angegebenen Sperren durch Einträge in der Entität „Systembenutzer Komponente" mit Flag „Sperre" zu. Existierende Zuordnungen, die nicht in der Liste enthalten sind, werden historisiert.

# \_Toc172190002Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172190003Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |
| ListeKomponenten | Liste von 0-n KomponentenCodes |  | **M** |

# \_Toc172190004Ausgangsdaten

Keine

# \_Toc172190005Meldungen

Keine