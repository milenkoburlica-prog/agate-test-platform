# GINO-Task Zuordnung speichern (OPC_CLV_FKT_162)

# \_Toc172190158Vorbedingungen

Keine

# \_Toc172190159Beschreibung

Anhand der in den Eingangsdaten übergebenen GINO-Gruppen-ID werden in der Entität „GINO-Task-Zuordnung" gespeicherten Einträge mit Referenzen auf den entsprechenden Eintrag in „GINO-Gruppe" ermittelt, und aus der Datenbank gelöscht.
  
Für jeden in den Eingangsdaten übergebenen Eintrag in der „GINO-Konfiguration-Parameter" Liste wird ein neuer Eintrag in „GINO-Task-Zuordnung" mit Refernzen auf den Eintrag in „GINO-Gruppe" (via GINO-Gruppen-ID) angelegt:

# \_Toc172190160Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| „GINO-Task" Liste | Siehe „GINO-Task", Liste kann auch leer sein. |  | **M** |

# \_Toc172190161Ausgangsdaten

Keine.

# \_Toc172190162Meldungen

Keine.