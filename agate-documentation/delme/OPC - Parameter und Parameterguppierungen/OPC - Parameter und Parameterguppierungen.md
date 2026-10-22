# OPC - Parameter und Parameterguppierungen

*Hinweis: Hier sind nur Parameter bzw. Parametergruppen angeführt, die die GINO Verwaltung betreffen.*

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **Länge** |
| Par\_GinoAnschlussIdGINO-Anschluss-ID | Künstlicher Schlüssel eines Anschlusses | Long |  |
| Par\_GinoFirmwareIdGINO-Firmware-ID | Künstlicher Schlüssel einer Firmware | Long |  |
| Par\_GinoFirmwareIZuordnungdGINO-Firmware Zuordnung-ID | Künstlicher Schlüssel der Zuordnung einer GINO-Gruppe zu einer Firmware | Long |  |
| Par\_GinoScriptZuordnungdGINO-Script-Task-Zuordnung-ID | Künstlicher Schlüssel der Zuordnung einer GINO-Gruppe zu einem Script-Task | Long |  |
| Par\_GinoGruppentypGINO-Gruppentyp | Typ der GINO-Gruppe.   Mögliche Werte:   * KONFIGURATION * FIRMWARE * TASK | String |  |
| Par\_GinoKonfigurationsTypGINO-Gruppen-Mitgliedertyp | Typ der GINO-Gruppen Mitglieder.   Mögliche Werte:   * SPEZIELL * DYNAMSICH * ALLGEMEIN |  |  |
| Par\_GinoGruppenIdGINO-Gruppen-ID | Künstlicher Schlüssel einer GINO-Gruppe | Long |  |
| PG\_GinoGruppenNameGINO-Gruppen-Name | Name der GINO-Gruppe | String | 30 |
| Par\_GinoSoftwareVersionGINO-Firmware Version | Version der GINO Software im Format   dd.dd.dd (‚d' ist jeweils eine dezimale Ziffer)   Hinweis: zu einer Software Version kann es mehre Hardware Versionen geben. | String | 8 |
| Par\_GinoHardwareVersionGINO-Hardware Version | Version der GINO Hardware im Format dd.dd | String | 5 |
| Par\_GinoResourceTypGINO-Resourcetyp | Typ der GINO Resource. Mögliche Werte:   * alive * fwcheck * event * tls-certs-client * tls-certs-server * cvc-certs * local-config * server-config * diagnosis * statistic * logs * maintenance * scriptcheck * scriptresult | String |  |
| Par\_GinoScriptIdGINO-Script-ID | Künstlicher Schlüssel eines GINO-Scripts | Long |  |
| Par\_GinoSeriennummerGINO-Seriennummer | Eindeutige Seriennummer eines GINOs | String | 50 |
| Par\_GinoTaskIdGINO-Task-ID | Künstlicher Schlüssel eines GINO-Tasks | Long |  |