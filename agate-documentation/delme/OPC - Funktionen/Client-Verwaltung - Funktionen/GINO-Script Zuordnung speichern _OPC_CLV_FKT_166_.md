# GINO-Script Zuordnung speichern (OPC_CLV_FKT_166)

# \_Toc172190182Vorbedingungen

Keine

# \_Toc172190183Beschreibung

Anhand der in den Eingangsdaten übergebenen GINO-Gruppen-ID wird ein eventuell in der Entität „GINO-Script-Zuordnung" gespeicherter Eintrag mit Referenz auf den entsprechenden Eintrag in „GINO-Gruppe" ermittelt, und aus der Datenbank gelöscht.
  
Für den in den Eingangsdaten übergebenen Parameter „GINO-Script-Task" wird ein neuer Eintrag in „GINO-Script-Zuordnung" mit Refernz auf den Eintrag in „GINO-Gruppe" (via GINO-Gruppen-ID) angelegt:

# \_Toc172190184Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| GINO-Script-Task | Siehe „GINO-Script-Task" |  | **M** |

# \_Toc172190185Ausgangsdaten

Keine.

# \_Toc172190186Meldungen

Keine.