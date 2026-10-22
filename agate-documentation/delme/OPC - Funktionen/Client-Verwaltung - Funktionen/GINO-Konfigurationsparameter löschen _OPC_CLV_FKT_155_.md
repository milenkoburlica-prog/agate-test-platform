# GINO-Konfigurationsparameter löschen (OPC_CLV_FKT_155)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion löscht einen GINO-Konfigurationsparameter. Der Löschvorgang wird geloggt (CLV-051).

Sollte ein Fehler auftreten, wird die Meldung GUI-012 ausgegeben, ansonsten die Meldung GUI-013.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des Parameters |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-062 | Der GINO-Konfigurationsparameter <PARAMETER> wurde erfolgreich gelöscht. | Message | Info |
| GUI-063 | Der Gino GINO-Konfigurationsparameter <PARAMETER> konnte nicht gelöscht werden. | Message | Error |
| CLV-051 | Der Gino Parameter <PARAMETER> wurde vom Nutzer <USER\_ID> gelöscht. | Log | Info |