# GINO-Firmware Zuordnung speichern (OPC_CLV_FKT_144)

# \_Toc172190092Vorbedingungen

Keine

# \_Toc172190093Beschreibung

Anhand der in den Eingangsdaten übergebenen GINO-Gruppen-ID werden in der Entität „GINO-Firmware Zuordnung" gespeicherte Einträge mit Referenzen auf den entsprechenden Eintrag in „GINO-Gruppe" ermittelt. Kann ein Eintrag ermittelt werden, und die Daten unterscheiden sich nicht, so wird die Funkion unter Rücklieferung der entsprechenden Ausgangsdaten erfolgreich beendet. Unterscheiden sich die Daten, so wird der Eintrag aus der Datenbank gelöscht.
  
In der Enitiät „GINO-Firmware Zuordnung" wird ein neuer Eintrag mit Referenz auf die GINO-Gruppe (Parameter „GINO-Gruppen-ID" aus den Eingangsdaten) und der „GINO-Firmware Version" aus den Eingangsdaten gespeichert.

# \_Toc172190094Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **K** |
| GINO-Firmware Version | Siehe „GINO-Firmware Version". |  | **M** |

# \_Toc172190095Ausgangsdaten

Keine.

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Firmware Zuordnung-ID | Siehe „GINO-Firmware Zuordnung-ID". |  | **M** |

# \_Toc172190096Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-009 | Es existiert keine Firmware Version <Version>. | Message | Error |