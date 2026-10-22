# GINO-Script Gruppe löschen (OPC_CLV_FKT_167)

# \_Toc172190188Vorbedingungen

Keine

# \_Toc172190189Beschreibung

Diese Funktion löscht die angegebene Gruppe physikalisch aus der Entität „GINO-Gruppe", sowie die referenzierenden Einträge in „GINO-Script-Zuordnung" und „GINO-Gruppe-Mitglieder"

# \_Toc172190190Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# \_Toc172190191Ausgangsdaten

Keine

# \_Toc172190192Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-085 | Wollen sie die Gruppe <Gruppenname> wirklich löschen? Ja/Nein | Message | Warning |
| CLV-086 | Die Gruppe <Gruppenname> wurde erfolgreich gelöscht" | Message   Log | Info   Info |