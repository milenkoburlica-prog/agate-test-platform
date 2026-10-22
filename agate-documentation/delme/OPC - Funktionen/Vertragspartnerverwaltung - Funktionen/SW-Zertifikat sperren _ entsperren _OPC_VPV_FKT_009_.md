# SW-Zertifikat sperren _ entsperren (OPC_VPV_FKT_009)

# \_Toc172189929Vorbedingungen

Keine

# \_Toc172189930Beschreibung

Diese Funktion sperrt bzw. entsperrt das gewählte SW-Zertifikat. Vor dem Sperren bzw. Entsperren wird eine Bestätigungsmeldung ausgegeben.  
Beim Sperren des SW-Zertifikats wird das gewählte SW-Zertifikat nach HM1 gelöscht. Beim Entsperren eines SW-Zertifikats wird das Lösch-Kennzeichen wieder aufgehoben. In beiden Fällen wird der ausführende Benutzer und das Änderungsdatum in der Entität ‚SW-Zertifikat' gemäß HM1 protokolliert.

# \_Toc172189931Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| SW-ZertifikatID | Primary-Key der Entität 'SW-Zertifikat' |  | M |
| KZ | Kennzeichen zum Sperren bzw. Entsperren des SW-Zertifikats | 0 = entsperren1 = sperren | M |

# \_Toc172189932Ausgangsdaten

Keine

# \_Toc172189933Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-016 | Wollen Sie das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wirklich sperren? | Message | Warning |
| BNV-017 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wurde erfolgreich gesperrt. | Log, Message | Info   Info |
| BNV-018 | Wollen Sie das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wirklich entsperren? | Message | Warning |
| BNV-019 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wurde erfolgreich entsperrt. | Log, Message | InfoInfo |