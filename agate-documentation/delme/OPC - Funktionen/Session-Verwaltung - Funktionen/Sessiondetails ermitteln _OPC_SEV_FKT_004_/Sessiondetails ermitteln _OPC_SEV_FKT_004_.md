# Sessiondetails ermitteln (OPC_SEV_FKT_004)

# \_Toc172190290Beschreibung

Diese Funktion ermittelt Detailinformationen zu einer Vertragspartnersession.

# \_Toc172190291Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-session ID | Eindeutiger Schlüssel der Vertragspa‚tnersessio' (Attribut 'Sess‚on ID' in'der Entität 'VP\_Dialog') |  | **M** |

# \_Toc172190292Ausgangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| Ordination | Adresse der Ordination (PLZ, Ort und Straße) |  | **M** |  |
| Ordinationsnummer | Ordinationsnummer |  | **M** |  |
| AusprägungsCode | Code der Ausprägung |  | **M** |  |
| DialogID | Identifikation des Dialoges | Zeichenkette | **K** |  |
| GinaID | Identifikation der Gina |  | **M** |  |
| TerminalID | Identifikation des Terminals |  | **K** |  |
| ProduktId\_ASW | Produkt Id des ASW/KIS-Herstellers | Zeichenkette | **K** |  |
| ProduktVersion\_ASW | Produkt Version des ASW/KIS-Herstellers | Zeichenkette | **K** |  |
| ExtUID\_ASW | Externe UserId (z.B. aus ASW/KIS) |  | **K** |  |
| Distribution | Versionsnummer der Softwaredistribution |  | **M** |  |
| von | Startzeitpunkt der Session |  | **M** |  |
| bis | Endzeitpunkt der Session |  | **K** |  |
| Beendigungsgrund | Grund zur Beendigung der Session |  |  |  |
| Anmeldetyp | „O" bei Anmeldung mittels Ordinationskarte oder „S" bei Anmeldung mittels SW-Zertifikat | „O" bzw. „S" | **M** |  |
| Kartennummer | Vertragspartnernummer und laufende Nummer der Karte (Anmeldung mittels Ordinationskarte) | Zeichenkette | **K** |  |
| SW-Zertifikat | Vertragspartnernummer und Seriennummer des SW-Zertifikats (Anmeldung mittels SW-Zertifikat) | Zeichenkette | **K** |  |
| Applikationsserver | Liste der Server zu denen der Request gesendet wurde. Im Normalfall ein Server, im Fall von Load Balancing oder Failover kann es mehr als einer sein |  | **M** |  |
| User Agent | Kennung des User Agents, von dem der Request abgeschickt wurde (z.B. Browser des Benutzers). | ZeiPrioritätkette | **K** |  |
| GINA Host | HTTP-Host-Header des Requests an der GINA. | Zeichenkette | **K** |  |
| Rolle | ELGA-Rolle die bei der Anmeldung angegeben wurde | Zeichenkette | **K** |  |
| GDAMA | GDA Mitarbeiter (nur möglich falls ELGA-Rolle gesetzt ist)   Setzt sich aus den Komponenten „Titel vorne", Vorname, Zuname, Titel hinten" und Zusatzinformation zusammen | Zeichenkette | **K** |  |
| Servicename | Kann anstelle des GDA-Mitarbeiters angegeben werden. | Zeichenkette | **K** |  |
| 0 bis n Wiederholungen der Requestinformationen |  |  |  |
| Datum | Zeitpunkt der Absendung des Requests vom Client |  | **K** |  |
| Applikationsserver | Name des Applikationsserver, auf dem der Request abgearbeitet wird bzw. wurde |  | **K** |  |
| Applikation | am Server aufgerufene Applikation |  | **K** |  |
| Methode | von der Applikation aufgerufene Methode |  | **K** |  |
| Dauer | Dauer der Abarbeitung des Requests (Client – Client) |  | **K** |  |
| InterfaceName | Schnittstellenname des SS12-Funktionsaufrufes |  | **K** |  |
| InterfaceVersion | Schnittstellenversion des SS12-Funktionsaufrufes |  | **K** |  |