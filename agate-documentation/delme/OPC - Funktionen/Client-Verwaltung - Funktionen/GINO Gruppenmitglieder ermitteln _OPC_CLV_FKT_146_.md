# GINO Gruppenmitglieder ermitteln (OPC_CLV_FKT_146)

# \_Toc172190104Vorbedingungen

Keine

# \_Toc172190105Beschreibung

Diese Funktion ermittelt für Konfigurationsgruppen, Firmwaregruppen, Task-Gruppen oder Script Gruppen anhand der übergegenen Gruppen-ID die Mitglieder aus der Entität „GINO-Gruppe-Mitglieder", reichert die Daten anhand der ermittelten Anschluss-ID um die Client-IP aus der Entität „Anschluss" an und liefert sie in den Ausgangsdaten zurück.

# \_Toc172190106Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# \_Toc172190107Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Gruppen-Mitglied" | Siehe „GINO-Gruppen-Mitglied".   Die Liste kann auch leer sein. |  | **M** |

# \_Toc172190108Meldungen

Keine