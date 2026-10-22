# Details einer Nachricht ermitteln (OPC_MES_FKT_004)

# \_Toc172190314Beschreibung

Über diese Funktion werden die Details einer Nachricht (siehe Ausgangsdaten) zur angegebenen Meldungsnummer ermittelt. In den Ausgangsdaten werden für das ‚Bundesland' und das ‚Besitzverhältnis' die Codes ausgegeben.

# \_Toc172190315Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | K |

# \_Toc172190316Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | M |
| Meldungsname | Name der Meldung |  | M |
| Gültigkeitsintervall | Gültigkeitsintervall |  | M |
| Von | Startzeitpunkt der Gültigkeit der Nachricht |  | M |
| Bis | Endzeitpunkt der Gültigkeit der Nachricht |  | K |
| Meldungsstatus | Status ob Meldung noch aktiv ist |  | M |
| Ersteller | Ersteller der Nachricht |  | M |
| Erstelldatum | Erstelldatum der Nachricht |  | M |
| Löschdatum | Löschdatum der Nachricht |  | M |
| Filter | Filterkriterien |  | M |
| VPNR | Liste mit Vertragspartnernummern |  | K |
| Zuname | Liste mit Zunamen von Vertragspartnern |  | K |