# GINO sperren (OPC_CLV_FKT_171)

# \_Toc172190212Vorbedingungen

Keine

# \_Toc172190213Beschreibung

In der Entität „GINO Produktionsdaten" wird anhand der Seriennummer in den Eingangsdaten der entsprechende Eintrag ermittelt. Kann kein Eintrag ermittelt werden, so wird die Funktion unter Ausgabe der Fehlermeldung GUI-012 abgebrochen.
  
Ansonsten wird geprüft ob im „GINO Produktionsdaten"-Datensatz das Sperrkennzeichen bereits gesetzt ist. Falls nicht, werden die Attribute

* „Gesperrt" auf ‚true'
* „Sperrdatum" auf das aktuelle Datum
* „Sperrkommentar" optional auf den in den Eingangsdaten übergebenen Wert und
* „Bearbeiter" auf den in den Eingangsdaten übergebenen Wert

gesetzt.  
In der Entität „GINO" wird nach einem Eintrag gesucht, der auf den oben ermittelten Eintrag in „GINO Produktionsdaten" referenziert. Kann ein Eintrag ermittelt werden, so werden folgende Schritte durchgeführt.

* Die noch gültigen TLS-Server-Zertifikate und TLS-Client-Zertifikate werden widerrufen.
  + In der Entität „Gino Resource" werden für die SeriennPrioritätr Einträge mit HTTP\_STATUS\_CODE=200 und GINO\_RESOURCE== „tls-certs-server" oder „tls-cert-client" ermittelt, für welche der zugeordnete Datensatz in der Entität „Gino Resource Data" mit KEY=„gueltig-bis" im Feld VALUE ein Datum eingetragen hat, welches grösser gleich dem aktuellen Datum ist. (Stringvergleich mit YYYYMMDD)
  + Für jedes gefundene Zertifikat wird die Widerrufsfunktion des PKI-Service aufgerufen, wobei für die Parameter Antragsteller und Seriennummer der VALUE des zugeordneten Datensatz der Entität „GINO Resource Data" mit KEY==„issuer" bzw. „seriennummer" verwendet wird.Der Widerruf wird mit der Meldung CLV-201 gelogged.Bei einem Fehler wird die Funktion unter Ausgabe der Fehlermeldung CLV-200 abgebrochen.
* Der Datensatz in der Entität GINO wird historisiert.

Im Erfolgsfall wird die Meldung CLV-202 geloggt.

# \_Toc172190214Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Seriennummer | Siehe „GINO-Seriennummer" |  | **M** |
| Kommentar | Kommentar warum das Gerät gesperrt wurde (max. 100 Zeichen) |  | **K** |

# \_Toc172190215Ausgangsdaten

Keine.

# \_Toc172190216Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-012 | Es konnte kein GINO Produktionsdatensatz mit der Seriennummer <Seriennummer> ermittelt werden. | Message | Error |
| CLV-200 | Das Zertifkat (Seriennummer=<Serinnummer Zertifikat>, Antragsteller=<Antragsteller>) zum GINO mit der Seriennummer <Seriennummer GINO> konnte nicht gesperrt werden. | MessageLOG | Error |
| CLV\_201CLV-201 | Das Zertifkat (Seriennummer=<Seriennummer Zertifikat>, Antragsteller=<Antragsteller>) zum GINO mit der Seriennummer <Seriennummer GINO> wurde gesperrt. | LOG | Info |
| CLV-202 | Der GINO mit der Seriennummer ‚<Seriennummer>' wurde gesperrt | LOG | Info |