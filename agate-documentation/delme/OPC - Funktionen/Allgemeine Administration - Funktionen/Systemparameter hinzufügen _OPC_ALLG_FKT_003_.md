# Systemparameter hinzufügen (OPC_ALLG_FKT_003)

# Beschreibung

Es wird ein neuer Systemparameter mit den übergebenen Werten angelegt.

Dabei werden folgende Einträge in der Entität SYS\_PARAMETER erstellt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <WERT> | <GRUPPE> | 1 | <AKTUELLES\_DATUM> | <ID> | 1 | 0 |

Falls er Wert "Beschreibung" nicht leer ist, wird ein weiterer Parameter angelegt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <BESCHREIBUNG> | <GRUPPE>.desc | 1 | <AKTUELLES\_DATUM> | <ID> | 0 | 1 |

Falls der Wert "Regex" nicht leer ist, wird ein weiterer Parameter angelegt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <REGEX> | <GRUPPE>.regex | 1 | <AKTUELLES\_DATUM> | <ID> | 0 | 1 |

# Vorbedingungen

Ausfüllen des Formulars zum Hinzufügen neuer Systemparameter und betätigen von „Hinzufügen“.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des neuen Parameters | Max. 100 Zeichen | M |
| Wert | Der Standardwert des neuen Parameters | Max. 1000 Zeichen | M |
| Gruppe | Die Gruppe des neuen Parameters | Max. 44 Zeichen (damit .regex und .desc noch angehängt werden können) | M |
| Beschreibung | Die Beschreibung des neuen Parameters | Max. 1000 Zeichen | K |
| Regex | Die "Regex" Regel zur Validierung des Parameters | Max. 1000 Zeichen | K |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-070 | Der Systemparameter <PARAMETER\_NAME> wurde erfolgreich angelegt. | Message | Info |
| GUI-071 | Der Systemparameter <PARAMETER\_NAME> konnte nicht angelegt werden (<FEHLERMELDUNG>) | Message | Error |
| GUI-072 | Der Systemparameter <PARAMETER\_NAME> wurde vom Nutzer <USER\_ID> angelegt. | Log | Info |