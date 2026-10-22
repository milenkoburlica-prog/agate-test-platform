# GINO-Firmwaregruppe löschen (OPC_CLV_FKT_153)

# \_Toc172190146Vorbedingungen

Keine

# \_Toc172190147Beschreibung

Diese Funktion löscht die angegebene Gruppe physikalisch aus der Entität „GINO-Gruppe", sowie die referenzierenden Einträge in „GINO-Firmware-Zuordnung" und „GINO-Gruppe-Mitglieder"

# \_Toc172190148Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# \_Toc172190149Ausgangsdaten

Keine

# \_Toc172190150Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-085 | Wollen sie die Gruppe <Gruppenname> wirklich löschen? Ja/Nein | Message | Warning |
| CLV-086 | Die Gruppe <Gruppenname> wurde erfolgreich gelöscht" | Message   Log | Info   Info |