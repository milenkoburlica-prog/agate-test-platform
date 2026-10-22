# Systemparameter-Metainfo bearbeiten (OPC_ALLG_FKT_004)

# Beschreibung

Es wird der übergebene Systemparameter bearbeitet.

Dabei werden folgende Einträge in der Entität SYS\_PARAMETER erstellt:

Falls der Wert "Beschreibung" sich geändert hat, wird der Bestehende mit Name <NAME> und Gruppe <GRUPPE>.desc mit Aktuell <1> auf nicht aktuell gesetzt und ein weiterer Parameter angelegt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <BESCHREIBUNG> | <GRUPPE>.desc | 1 | <AKTUELLES\_DATUM> | <ID> | 0 | 1 |

Falls der Wert "Regex" sich geändert hat, wird der Bestehende mit Name <NAME> und Gruppe <GRUPPE>.regex mit Aktuell <1> auf nicht aktuell gesetzt und ein weiterer Parameter angelegt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <REGEX> | <GRUPPE>.regex | 1 | <AKTUELLES\_DATUM> | <ID> | 0 | 1 |

# Vorbedingungen

Änderung eines Systemparameters und betätigen von „Ändern“.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des Parameters | Max. 100 Zeichen | M |
| Beschreibung | Die neue Beschreibung des neuen Parameters | Max. 1000 Zeichen | K |
| Regular Expression | Die Regular Expression zur Validierung des Parameters | Max. 1000 Zeichen | K |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-073 | Der Systemparameter <PARAMETER\_NAME> wurde erfolgreich editiert. | Message | Info |
| GUI-074 | Der Systemparameter <PARAMETER\_NAME> konnte nicht editiert werden (<FEHLERMELDUNG>) | Message | Error |
| GUI-075 | Der Systemparameter <PARAMETER\_NAME> wurde vom Nutzer <USER\_ID> bearbeitet. | Log | Info |