# Systemparameter ausgeben (OPC_ALLG_FKT_001)

# Beschreibung

Es werden in der Datenbank alle Systemparameter durchsucht und jene retourniert, die der Gruppe in den Eingangsdaten entspricht, oder alle, wenn keine Gruppe angegeben wurde (Die Gruppen mit den Suffixen ".regex" und ".desc" werden ignoriert).  
Zu jedem ermittelten Parameter werden die Regeln zur Validierung für gültige Eingaben geladen und wenn vorhanden in das Ausgabefeld „Valid“ gespeichert.  
Zu jedem ermittelten Parameter wird versucht eine Beschreibung aus der Datenbank zu laden und bei Erfolg wird der gefundene Wert in „Description“ gesetzt.

# Vorbedingungen

Der aufrufende Benutzer hat eine Rolle, die ihn berechtigt, diese Systemparameter zu verwalten.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Gruppe | Filterkriterium, wenn gesetzt, werden nur Parameter dieser Gruppe ausgelesen. |  | K |

# Ausgangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |  |
| --- | --- | --- | --- | --- |
| pro Parameter |  |  |  |  |
| Name | Die Menge der eingerichteten Systemparameter wird aus der Datenbank ermittelt und in den entsprechenden Feldern angezeigt. |  | **M** |  |
| Name | Der Name des Parameters |  | **M** |  |
| Gruppe | Die Gruppe zu der dieser Parameter gehört |  | **M** |  |
| Wert | Der aktuelle Wert des Parameters |  | **M** |  |
| Default Wert | Der Standardwert für den Parameter |  | **K** |  |
| Valid | REGEX zur Beschreibung der gültigen Wertebereiche. |  | **K** |  |
| Editierbar | Flag zur Indikation, ob Änderungen am Wert des Parameters zulässig sind oder nicht. | ‚true', ‚false' | **M** |  |
| Unsichtbar | Flag zur Indikation, ob der Parameter angezeigt werden soll oder nicht. | ‚true', ‚false' | **M** |  |
| Description | Beschreibung zu dem Parameter |  | **K** |  |