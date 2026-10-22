# GINO-Konfigurationsgruppe löschen (OPC_CLV_FKT_145)

# \_Toc172190098Vorbedingungen

Keine

# \_Toc172190099Beschreibung

Diese Funktion löscht die angegebene Gruppe physikalisch aus der Entität „GINO-Gruppe", sowie die referenzierenden Einträge in „GINO-Konfiguration-Zuordnung" und „GINO-Gruppe-Mitglieder"

# \_Toc172190100Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# \_Toc172190101Ausgangsdaten

Keine

# \_Toc172190102Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-085 | Wollen sie die Gruppe <Gruppenname> wirklich löschen? Ja/Nein | Message | Warning |
| CLV-086 | Die Gruppe <Gruppenname> wurde erfolgreich gelöscht" | Message   Log | Info   Info |