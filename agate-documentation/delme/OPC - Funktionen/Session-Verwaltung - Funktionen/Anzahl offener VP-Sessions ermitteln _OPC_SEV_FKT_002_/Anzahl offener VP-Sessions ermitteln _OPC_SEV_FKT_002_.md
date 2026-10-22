# Anzahl offener VP-Sessions ermitteln (OPC_SEV_FKT_002)

# \_Toc172190281Beschreibung

Es wird die Anzahl der zum aktuellen Zeitpunkt offenen Dialoge und Sessions ermittelt. Die Ausgabeinformation kann durch die Angabe einer Zeitspanne eingeschränkt werden. Die Dauer der Session wird vom Zeitpunkt der Abfrage betrachtet.  
Beim Aufbau einer Session wird ein eindeutiger Schlüssel (DialogID) zur Session gespeichert, falls diese vom Ordinationsclient generiert und an den e-card Server übergeben wurde. Anhand dieser eindeutigen DialogID werden Sessions zusammengefasst. Wird bei einer Session keine DialogID gespeichert, so werden diese Sessions als separate Dialoge gewertet.  
Bei der Ermittlung der Anzahl der offenen Sessions über das Suchkriterium ‚GinaIP' (IP-Adresse), erfolgt keine Überprüfung, ob es sich bei der angegebene IP-Adresse um eine gültige IP-Adresse handelt.

# \_Toc172190282Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartner |  | **K\_Ref147292398** Entweder Vertragspartnernummer oder IP-Adresse muss gegeben sein |
| GinaIP | IP-Adresse der GINA |  | **K1** |
| Zeitspanne | Zeit seit der die Sessions offen sein sollen |  | **M** |

# \_Toc172190283Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartner |  | **K1** |
| GinaIP | IP-Adresse der GINA |  | **K1** |
| OffeneDialoge | Gesamte Anzahl an offenen VP-Dialogen zum Abfragezeitpunkt |  | **M** |
| OffeneSessions | Gesamte Anzahl an offenen VP-Sessions zum Abfragezeitpunkt |  | **M** |
| Zeitspanne | Angegebene Zeit seit der die Sessions schon offen sein sollen |  | **M** |
| OffeneDialogeZeitspanne | Anzahl der offenen VP-Dialoge zum Abfragezeitpunkt, die länger als die angegebene Zeitspanne offen sind |  | **M** |
| OffeneSessionsZeitspanne | Anzahl der offenen VP-Sessions zum Abfragezeitpunkt, die länger als die angegebene Zeitspanne offen sind |  | **M** |

# \_Toc172190284Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| SES-130 | Es wurden beide Suchkriterien eingegeben, die Bearbeitung wird abgebrochen. | Log | Error |