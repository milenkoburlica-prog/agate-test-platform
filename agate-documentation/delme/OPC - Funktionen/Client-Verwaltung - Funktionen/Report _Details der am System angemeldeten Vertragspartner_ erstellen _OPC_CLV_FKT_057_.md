# Report „Details der am System angemeldeten Vertragspartner“ erstellen (OPC_CLV_FKT_057)

# \_Toc172190032Vorbedingungen

Der Instant-Report „Rollout – Details der am System angemeldeten Vertragspartner" wurde im Report-Framework eingebunden.

# \_Toc172190033Beschreibung

In dieser Funktion wird der Report „Rollout – Details der am System angemeldeten Vertragspartner" erstellt. Dabei werden die Vertragspartner, die sich jemals bereits am System angemeldet oder nicht angemeldet haben, ermittelt.  
Je Vertragspartner werden folgende Informationen ausgegeben:

* + Vertragspartnernummer
  + Status der Anmeldung

Bei diesem Report werden immer alle zum Zeitpunkt der Erstellung dieses Reports aktuellen Vertragspartner bewertet.  
Zur Ermittlung der Vertragspartner, die sich noch nicht am System gemeldet haben, werden die Vertragspartner zum angegebenen Filterkriterium (Postleitzahl) ermittelt. Dabei werden die Vertragspartner, auch wenn diese mehreren Ordinationen zugeordnet sind (Vertragspartner hat mehrere Ordinationen im angegebenen Filterbereich) nur einmal bewertet.  
Wird kein Postleitzahlenbereich angegeben, so werden alle Vertragspartner berücksichtigt.  
Die sich tatsächlich angemeldeten Vertragspartner werden anhand der protokollierten o-card ID ermittelt. Über die dazu protokollierte Ordinations-ID kann der jeweilige Vertragspartner dem Filterbereich (Postleitzahl) zugeordnet werden. Auch hier wird der Vertragspartner nur einmal im angegebenen Filterbereich bewertet, unabhängig in welchen Ordinationen die Anmeldung erfolgte.  
Zu den Vertragspartnern, die sich bereits angemeldet haben, wird die Vertragspartnernummer und zusätzlich das Kennzeichen „Vertragspartner angemeldet"  für die durchgeführte Anmeldung ausgegeben. Hat sich ein Vertragspartner noch nicht angemeldet, so wird das Kennzeichen „Vertragspartner nicht angemeldet"  ausgegeben.

# \_Toc172190034Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Filterkriterium | Postleitzahlenbereich. Wurde kein Filterkriterium angegeben, so wird eine Auswertung aller Vertragspartner durchgeführt |  | **K** |
| Vertragspartner | Entität |  | **M** |
| Ordination | Entität |  | **M** |
| VP Dialog | Entität |  | **M** |
| o-card | Entität |  | **M** |
| Vertragspartnernummer | Entität |  | **M** |

# \_Toc172190035Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Reporterstellung | Zeitstempel der Erstellung dieses Reports |  | **M** |
| Filterkriterium | Postleitzahlenbereich. Wurde kein Filterkriterium angegeben, so wird eine Auswertung aller Vertragspartner durchgeführt |  | **M** |
| VPNR | Liste mit Vertragspartnernummern |  | **M** |
| Status Anmeldung | Status der Anmeldung, ob sich der Vertragspartner bereits angemeldet hat oder nicht | True (Vertragspartner bereits angemeldet)   False (Vertragspartner noch nicht angemeldet) | **M** |

# \_Toc172190036Meldungen

Keine  
\_Ref274556325