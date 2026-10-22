# Systemparameter löschen (OPC_ALLG_FKT_005)

# Beschreibung

Es wird der übergebene Systemparameter gelöscht.

Dabei wird die Historisierung verwendet und die Spalte "Aktuell" auf "0" gesetzt. Dies wird sowohl für den Parameter mit dem entsprechenden Namen an sich, als auch für die Beschreibung sowie den Regex durchgeführt.

# Vorbedingungen

Keine.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des Parameters | Max. 100 Zeichen | M |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-076 | Der Systemparameter <PARAMETER\_NAME> wurde erfolgreich gelöscht | Message | Info |
| GUI-077 | Der Systemparameter <PARAMETER\_NAME> konnte nicht gelöscht werden (<FEHLERMELDUNG>) | Message | Error |
| GUI-078 | Der Systemparameter <PARAMETER\_NAME> wurde vom Nutzer <USER\_ID> entfernt. | Log | Info |