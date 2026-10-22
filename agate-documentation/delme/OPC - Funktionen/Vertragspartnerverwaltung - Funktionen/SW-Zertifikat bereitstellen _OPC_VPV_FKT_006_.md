# SW-Zertifikat bereitstellen (OPC_VPV_FKT_006)

# \_Toc172189911Vorbedingungen

Eine Datei mit der Dateierweiterung ‚CER' oder ‚PEM' wurde am definierten Ablageort für die Bereitstellung eines SW-Zertifikats abgelegt. Das Zertifikat wurde noch nicht für einen Vertragspartner bereitgestellt.   
Die Vertragspartnernummern (VPNR) wurden durch den HVB-Import übernommen.  
Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189912Beschreibung

Anhand dieser Funktion wird ein SW-Zertifikat aus einer Datei extrahiert und für das System zur weiteren Verarbeitung bereitgestellt.  
Zu einer VPNR können mehrere SW-Zertifikate bereitgestellt werden. Ein SW-Zertifikat selbst kann jedoch nur einmalig für eine VPNR bereitgestellt werden. Über die Beziehungsentität „HVB\_Beziehung" ist allerdings auch die Zuordnung von SW-Zertifikaten für ganze Vertragspartnergruppen möglich (z.B. mehrere Vertragspartner stehen in Beziehung zu einem übergeordneten Vertragspartner, der das SW-Zertifikat bereitstellt).  
Die eingegebene VPNR darf nur aus numerischen Zeichen bestehen. Bei Eingabe von ungültigen Zeichen wird die allgemein gültige Fehlermeldung ‚Der Wert ‚<Eingabe>' für das Feld ‚<VPNR>' ist nicht korrekt.' Ausgegeben.  
Bei der Datei mit dem bereitzustellenden SW-Zertifikats muss es sich um eine CER- oder PEM- Datei handeln. Andernfalls wird die Fehlermeldung ‚BNV-010' ausgegeben.  
Serverseitig wird das SW-Zertifikat aus der hochgeladene Datei als ‚X509Certificate' gelesen. Kann der Dateninhalt des SW-Zertifikats nicht gelesen werden, so wird die Fehlermeldung ‚BNV-011' ausgegeben.  
Nachdem das SW-Zertifikat erfolgreich gelesen wurde, werden die folgenden Prüfungen durchgeführt:

* Die angegebene Vertragspartnernummer muss existieren. Im Fehlerfall wird die Fehlermeldung ‚BNV-012' ausgegeben.
* Das SW-Zertifikat darf in der Entität ‚SW-Zertifikat' noch nicht bereitgestellt worden sein. Die Prüfung erfolgt anhand der Seriennummer und dem Aussteller des SW-Zertifikats. Im Fehlerfall wird die Fehlermeldung ‚BNV-013' ausgegeben.
* Die geforderten Sicherheitsrichtlinien eines SW-Zertifikats müssen erfüllt sein. Diese sind:
  + Priorität- oder RSA-Schlüssel, andernfalls Fehlermeldung BNV-022
  + Schlüssellänge in Bit entsprechend der Systemparameter *SWZ\_Import\_Keylength\_Min/Max* (Gruppe *Schnittstellenparameter*), andernfalls Fehlermeldung BNV-022
  + Zertifikatssignatur nicht unter Verwendung eines gesperrten Algorithmus, andernfalls Fehlermeldung BNV-021. Hierzu wird folgendes Stringformat gegen den im Systemparameter *SWZ\_Import\_Algorithm\_Blacklist* (Gruppe *Schnittstellenparameter*) hinterlegten regulären Ausdruck geprüft: *<Digest>/<Verschlüsselung>,<Schlüsseltyp>-<Schlüssellänge> (z.B. SHA/RSA,RSA-1024)*

# \_Toc172189913Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Datei | Datei mit dem bereitzustellenden SW-Zertifikat |  | M |
| VPNR | Vertragspartnernummer zu der das SW-Zertifikat bereitgestellt werden soll |  | M |

# \_Toc172189914Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Entität ‚SW-Zertifikat' | Neues SW-Zertifikat zur angegebenen Vertragspartnernummer |  | M |

# \_Toc172189915Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-010 | Die Bereitstellung des SW-Zertifikats aus der Datei ‚<Dateiname>' ist nicht möglich, da die Dateierweiterung der Datei nicht erlaubt ist. | Message | Error |
| BNV-011 | Die Bereitstellung des SW-Zertifikats aus der Datei ‚<Dateiname>' ist nicht möglich, da das SW-Zertifikat nicht aus der Datei ermittelt werden kann. | Message | Error |
| BNV-012 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' konnte nicht bereitgestellt werden, da die Vertragspartnernummer ‚<VPNR>' nicht existiert. | Message | Error |
| BNV-013 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wurde bereits bereitgestellt. | Message | Error |
| BNV-022 | Die Bereitstellung des angegebenen SW-Zertifikats ist nicht möglich, da das SW-Zertifikat nicht den geforderten SicherheitsrichtliniePrioritätDSA oder RSA mit Schlüssellänge <min. Schlüssellänge> - <max. Schlüssellänge>) entspricht. | Message | Error |
| BNV-021 | Die Bereitstellung des angegebenen SW-Zertifikats ist nicht möglich, da der verwendete Signaturalgorithmus <Algorithmus> nicht den geforderten Sicherheitsrichtlinien entspricht. | Message | Error |