# GINO-Gruppe Konfiguration ermitteln (OPC_CLV_FKT_141)

# \_Toc172190074Vorbedingungen

Keine

# \_Toc172190075Beschreibung

Anhand der übergebenen GINO-Gruppen-ID werden in der Enitiät „GINO-Konfiguration-Zuordnung" die auf den Eintrag in „GINO-Gruppe" referenzierenden Einträge ermittelt und in den Ausgangsdaten zurückgeliefert.
  
Wird in den Eingangsdaten keine GINO-Gruppen-ID gesetzt, so werden in der Entität „GINO-Konfiguration" alle aktuellen Einträge ermittelt und in den Ausgangsdaten zurückgeliefert, dies sind also alle Parameter die dem System bekannt sind.

# \_Toc172190076Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gruppen-ID | ID der Gruppe | numerisch | **K** |

# \_Toc172190077Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Konfiguration-Parameter" | Liste von „GINO-Konfiguration-Parameter", kann auch leer sein. |  | **M** |

# \_Toc172190078Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-006 | Bitte wählen Sie einen Eintrag aus der Liste aus. | Message | Error |