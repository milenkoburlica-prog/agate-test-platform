# Adressaten einer Nachricht ermitteln (OPC_MES_FKT_007)

# \_Toc172190318Beschreibung

Über diese Funktion werden die Adressaten einer Nachricht (siehe Ausgangsdaten) zur angegebenen Meldungsnummer in Abhängigkeit des Adressaten-Typs (Vertragspartnernummer Ordination, Session) ermittelt.  
Die Ausgangsdaten (Adressaten) selbst werden dabei in Abhängigkeit des Adressen-Typs erstellt.  
***Session (Session-ID):***  
Es werden alle Ausgangsdaten, falls die erforderlichen Daten zur Ermittlung der Ordination und Vertragspartner zur angegebenen Session-ID gespeichert wurden, ermittelt.  
Können aus den in der Session gespeicherten Informationen die Vertragspartnernummer bzw. Ordinationsnummer nicht ermittelt werden, so wird lediglich die Session-ID ausgegeben. Andernfalls werden anhand der gespeicherten Informationen die Daten aus der Entität „Ordination" bzw. „Vertragspartner" ermittelt und entsprechend eingetragen.  
***Ordination (Ordinations-ID):***  
Anhand der Odinations-ID werden bis auf die Session-ID alle Informationen ermittelt und in den Ausgangsdaten eingetragen.  
***Vertragspartnernummer:***  
Anhand der bei diesem Adressen-Typ eingetragenen Vertragspartnernummer werden die Vertragspartnernummer und der Nachname des Vertragspartners selbst ermittelt und in den Ausgangsdaten eingetragen.   
Die Informationen zur Ordination und Session stehen nicht zur Verfügung.

# \_Toc172190319Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | K |

# \_Toc172190320Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Adressaten | Liste von 1-n Adressaten |  | M |
| VPNR | Vertragspartnernummer |  | K |
| Zuname | Zuname des Vertragspartners |  | K |
| Ordinationsnummer | Ordinationsnummer |  | K |
| Bundesland | Bundesland der Ordination |  | K |
| PLZ | PLZ der Ordination |  | K |
| Ort | Ort der Ordination |  | K |
| Strasse | Strasse der Ordination |  | K |
| Besitzverhältnis | Besitzverhältnis des Vertragspartners |  | K |
| Session ID | Session ID |  | K |