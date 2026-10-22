# Systemparameter ändern (OPC_ALLG_FKT_002)

# Beschreibung

Mittels dieser Funktion werden ein oder mehrere Systemparameter geändert. Die Änderungen werden protokolliert.

Dabei werden die Werte gegen den Regex-Wert aus der Datenbank validiert, insofern einer existiert und der Wert tatsächlich geändert wurde.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
|  | Alle Systemparameter |  | **M** |

# Ausgangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Return | Quittung OK oder Fehlernummer |  | **M** |

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
|  | Parameter "<PARAMETER\_NAME>" hat einen ungültigen Wert (Regex: <REGEX>). | Message | Error |
|  | Die Parameter <LISTE\_PARAMETER> wurden gespeichert. | Message | Info |