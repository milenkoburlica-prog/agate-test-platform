# Nachricht suchen (OPC_MES_FKT_003)

# \_Toc172190309Beschreibung

Über diese Funktion werden die Nachrichten zu einem angegebenen Suchkriterium ermittelt.  
Anhand der Suchkriterien für die Nachrichtenliste wird, falls ein Vertragspartner (Vertragspartnernummer) angegeben wurde, die Meldungsliste zum angegebenen Vertragspartner eingeschränkt.  
Eine Wildcard-Angabe für den Vertragspartner (Vertragspartnernummer) und dem Ersteller wird nicht unterstützt.  
Wird ein Vertragspartner angegeben, so werden die Nachrichten mit dem Adressaten-Typ „Session" nicht bei der Filterung berücksichtigt und diese Meldungen auch nicht dargestellt.  
Das Suchkriterium ‚Nachricht aktiv' bezieht sich immer auf das aktuelle Datum. Wurde eine Nachricht gelöscht, so wird diese Nachricht ebenfalls als ‚nicht aktiv' ausgegeben.  
Die Suchkriterien ‚VON-Datum' und ‚BIS-Datum' beziehen sich immer auf den Gültigkeitsbereich der Meldung (sprich ob die Meldung im angegebenen Zeitraum gültig war).  
***Eingabe ‚VON-Datum':***

* Meldungen, bei denen das eingegeben VON-Datum im Meldungs-Gültigkeitszeitraum liegt

***Eingabe ‚BIS-Datum':***

* Meldungen, bei denen das eingegeben BIS-Datum im Meldungs-Gültigkeitszeitraum liegt (eingegebenes BIS-Datum <= Gültigkeitswert ‚Gültig bis' und eingegebenes BIS-Datum > Gültigkeitswert ‚Gültig von').

***Eingabe ‚VON-Datum' und ‚BIS-Datum':***

* Meldungen, bei denen das eingegebene VON- und BIS-Datum im Meldungs-Gültigkeitszeitraum liegt.

# \_Toc172190310Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | K |
| Meldungsname | Name der Nachricht |  | K |
| VON-Datum | Startzeitpunkt der Gültigkeit der Nachricht |  | K |
| BIS-Datum | Endzeitpunkt der Gültigkeit der Nachricht |  | K |
| Meldungsstatus | Status der Nachricht | aktiv/inaktiv/alle | K |
| Erfasser | Ersteller der Nachricht |  | K |
| Erfassdatum | Erstelldatum der Nachricht |  | K |
| VPNR | Vertragspartnernummer |  | K |

# \_Toc172190311Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | M |
| Meldungsname | Name der Meldung |  | M |
| Gültigkeitsintervall | Gültigkeitsintervall |  | M |
| Von | Startzeitpunkt der Gültigkeit der Nachricht |  |  |
| Bis | Endzeitpunkt der Gültigkeit der Nachricht |  |  |
| Meldungsstatus | Status ob Meldung noch aktiv ist | aktiv/inaktiv | M |
| Löschstatus | Status ob Meldung gelöscht wurde | nicht gelöscht/gelöscht | M |
| Ersteller | Ersteller der Nachricht |  | M |
| Erstelldatum | Erstelldatum der Nachricht |  | M |

# \_Toc172190312Meldungen / Ausnahmen

|  |
| --- |
| **Ausnahmen** |
| Zum angegebenen Suchkriterium stehen keine Nachrichten zur Verfügung |