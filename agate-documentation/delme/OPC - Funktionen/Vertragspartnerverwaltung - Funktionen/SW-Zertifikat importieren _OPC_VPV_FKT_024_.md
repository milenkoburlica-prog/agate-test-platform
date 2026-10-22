# SW-Zertifikat importieren (OPC_VPV_FKT_024)

# \_Toc172190013Vorbedingungen

Das SW-Zertifikat wurde mittels „SW-Zertifikat bereitstellen (OPC\_VPV\_FKT\_006)" eingelesen, und ist noch nicht in der Datenbank vorhanden.  
Die Vertragspartnernummern (VPNR) wurden durch den HVB-Import übernommen.  
Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172190014Beschreibung

Anhand dieser Funktion wird ein SW-Zertifikat zu der angegebenen Vertragspartnernummer (VPNR) in der Entität ‚SW-Zertifikat' nach HM1 gespeichert.  
Zu einer VPNR können mehrere SW-Zertifikate bereitgestellt werden. Ein SW-Zertifikat selbst kann jedoch nur einmalig für eine VPNR bereitgestellt werden. Über die Beziehungsentität „HVB\_Beziehung" ist allerdings auch die Zuordnung von SW-Zertifikaten für ganze Vertragspartnergruppen möglich (z.B. mehrere Vertragspartner stehen in Beziehung zu einem übergeordneten Vertragspartner, der das SW-Zertifikat bereitstellt).  
In der DB wird als Fingerprint der SHA1-Wert gespeichert.

# \_Toc172190015Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| SwZertifikat | SW-Zertifikat |  | M |
| VPNR | Vertragspartnernummer zu der das SW-Zertifikat bereitgestellt werden soll |  | M |

# \_Toc172190016Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Entität ‚SW-Zertifikat' | Neues SW-Zertifikat zur angegebenen Vertragspartnernummer |  | M |

# \_Toc172190017Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-014 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wurde erfolgreich für den Vertragspartner <VPNR>' bereitgestellt. | Log, Message | Info |