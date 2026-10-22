# Anmeldungen ermitteln (OPC_SEV_FKT_003)

# \_Toc172190286Beschreibung

Diese Funktion ermittelt die letzte erfolgreiche Anmeldung mittels Ordinationskarten bzw. SW-Zertifikate eines Vertragspartners.

# \_Toc172190287Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartnernummer |  | **M** |

# \_Toc172190288Ausgangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| 0 bis n Wiederholungen (zuerst werden die Anmeldungen mittels Ordinationskarte und dann die Anmeldungen mittels SW-Zertifikat ausgegeben) |  |  |  |
| Identifikation | Anmeldung mittels Ordinationskarte:   o-card ID |  | **M** |  |
|  | Anmeldung mittels SW-Zertifikat:   Seriennummer des SW-Zertifikats |  |  |  |
| Anmeldetyp | „O" bei Anmeldung mittels Ordinationskarte oder „S" bei Anmeldung mittels SW-Zertifikat | „O" bzw. „S" | **M** |  |
| Zeitpunkt | Zeitpunkt der letzten erfolgreichen Anmeldung | Datum/Uhrzeit | **M** |  |
| Laufnummer | Die Laufnummer bei Anmeldungen mittels Ordinationskarte |  | **K** |  |