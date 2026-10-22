# GINO-Task Gruppe löschen (OPC_CLV_FKT_163)

# \_Toc172190164Vorbedingungen

Keine

# \_Toc172190165Beschreibung

Diese Funktion löscht die angegebene Gruppe physikalisch aus der Entität „GINO-Gruppe", sowie die referenzierenden Einträge in „GINO-Task-Zuordnung" und „GINO-Gruppe-Mitglieder"

# \_Toc172190166Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# \_Toc172190167Ausgangsdaten

Keine

# \_Toc172190168Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-085 | Wollen sie die Gruppe <Gruppenname> wirklich löschen? Ja/Nein | Message | Warning |
| CLV-086 | Die Gruppe <Gruppenname> wurde erfolgreich gelöscht" | Message   Log | Info   Info |