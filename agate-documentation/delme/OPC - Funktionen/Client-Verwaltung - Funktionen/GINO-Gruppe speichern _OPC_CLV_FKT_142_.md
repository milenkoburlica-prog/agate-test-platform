# GINO-Gruppe speichern (OPC_CLV_FKT_142)

# \_Toc172190080Vorbedingungen

Keine

# \_Toc172190081Beschreibung

Wird in der Eingangsdaten eine GINO-Gruppen-ID übergeben, so handelt es sich um eine Änderung einer bestehenden Gruppe, ansonsten um wird eine neue Gruppe angelegt.   
Für eine neue Gruppe wird wird in der Entität „GINO-Gruppe" geprüft, ob bereits eine Gruppe mit dem übergebenen GINO-Gruppen-Namen existiert, falls ja, wird die Funktion unter Ausgabe der unten angebebenen GUI-Meldung beendet.
  
Der ermittelte Eintrag in „GINO-Gruppe" wird mit den übergebenen Eingangsdaten aktualisiert, bzw. wird ein neuer Eintrag angelegt:

* TYP wird mit GINO-Gruppentyp
* MITGLIEDER\_TYP wird mit „GINO-Gruppen-Mitgliedertyp"
* NAME wird mit GINO-Gruppen-Name
* FILTER\_SERIENNUMMER wird mit dem Filter Seriennummer und
* FILTER\_FIRMWARE wird mit dem Filter Firmware

jeweils aus den Eingangsdaten versorgt.

# \_Toc172190082Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **K** |
| GINO-Gruppen-Name | Siehe „GINO-Gruppen-Name". |  | **M** |
| GINO-Gruppentyp | Siehe „GINO-Gruppentyp" |  | **M** |
| GINO-Gruppen-Mitgliedertyp | Siehe „GINO-Gruppen-Mitgliedertyp". |  | **M** |
| Filter Seriennummer | Filter für GINO-Seriennummer | String(1000) | **K** |
| Filter Firmware | Filter für Firmware | String(1000) | **K** |

# \_Toc172190083Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# \_Toc172190084Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-007 | Eine Gruppe mit diesem Namen existiert bereits. | Message | Error |
| GUI-008 | Eine allgemeine Gruppe existiert bereits. | Message | Error |