# Herstellerdaten exportieren (OPC_AHV_FKT_002)

# \_Toc172190400Vorbedingungen

# \_Toc172190401Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Datei | Datei mit Herstellerdaten |  | **M** |

# \_Toc172190402Ausgangsdaten

keine

# \_Toc172190403Beschreibung

Aus der Entität „ASWH" werden alle Daten ermittelt.  
Falls keine Daten zur Verfügung stehen, wird mit der Fehlermeldung AHV-011 abgebrochen,  
Eine Ausgabedatei mit dem Header Hersteller-ID;Firmenname;Ablaufdatum;gesperrt und den ermittelten Daten wird erzeugt.  
Die Datei wird mit dem Dateinamen aus den Eingangsdaten für den Download zur Verfügung gestellt.  
Zum Abschluss wird die Meldung AHV-012 ausgegeben und die Funktion beendet.

# \_Toc172190404Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| AHV-011 | Es stehen keine Herstellerdaten für den Exportzur Verfügung. | MessageLog | ErrorError |
| AHV-012 | In der Datei <Dateiname> wurden <Anzahl> Hersteller exportiert. | MessageLog | Info Info |