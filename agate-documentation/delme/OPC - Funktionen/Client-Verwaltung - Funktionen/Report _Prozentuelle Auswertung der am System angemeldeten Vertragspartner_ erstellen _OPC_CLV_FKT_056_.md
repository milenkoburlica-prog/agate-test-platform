# Report „Prozentuelle Auswertung der am System angemeldeten Vertragspartner“ erstellen (OPC_CLV_FKT_056)

# \_Toc172190026Vorbedingungen

Der Instant-Report „Rollout – Prozentuelle Auswertung der am System angemeldeten Vertragspartner" wurde im Report-Framework eingebunden.

# \_Toc172190027Beschreibung

In dieser Funktion wird der Report „Rollout – Prozentuelle Auswertung der am System angemeldeten Vertragspartner" erstellt. Dabei werden die Vertragspartner, die sich jemals am System angemeldet haben, ermittelt.  
Die Auswertung wird, falls als Filterkriterium ein Postleitzahlenbereich angegeben wurde, entsprechend gefiltert. Dabei werden die Vertragspartner, auch wenn diese mehreren Ordinationen zugeordnet sind (Vertragspartner hat mehrere Ordinationen im angegebenen Filterbereich) nur einmal angegeben. Wird kein Postleitzahlenbereich angegeben, so wird eine Gesamtauswertung aller Vertragspartner (gültige Vertragspartnernummer) durchgeführt, die im aktuellen Datenbestand zumindest eine gültige Ordination haben.  
Bei diesem Report werden immer alle zum Zeitpunkt der Erstellung dieses Reports aktuellen Vertragspartner bewertet.  
Die Vertragspartner werden aus der Entität „Vertragspartner" ermittelt. Über die Entität „Ordination" werden jedoch nur jene Vertragspartner ausgewertet, die einer Ordination im angegebenen Postleitzahlenbereich zugeordnet sind.  
Die sich tatsächlich angemeldeten Vertragspartner werden anhand der protokollierten o-card ID ermittelt. Über die dazu protokollierte Ordinations-ID kann der jeweilige Vertragspartner dem Filterbereich (Postleitzahl) zugeordnet werden. Auch hier wird der Vertragspartner nur einmal bewertet unabhängig in welchen Ordinationen die Anmeldung erfolgte.

# \_Toc172190028Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Filterkriterium | Postleitzahlenbereich. Wurde kein Filterkriterium angegeben, so wird eine Auswertung aller Vertragspartner durchgeführt |  | **K** |
| Vertragspartner | Entität |  | **M** |
| Ordination | Entität |  | **M** |
| VP Dialog | Entität |  | **M** |
| o-card | Entität |  | **M** |
| Vertragspartnernummer | Entität |  | **M** |

# \_Toc172190029Ausgangsdaten

| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| --- | --- | --- | --- |
| Reporterstellung | Zeitstempel der Erstellung dieses Reports |  | **M** |
| Filterkriterium | Postleitzahlenbereich. Wurde kein Filterkriterium angegeben, so ist eine Auswertung aller Vertragspartner gefordert |  | **M** |
| Wirksystem | Kennzeichen, ob es sich um das Wirk- (Dispositiv-DB) oder VPSWH (Vertragspartnerhersteller) DB Schema handelt | True (Wirksystem), False (VPSWH) | **M** |
| Anzahl der Vertragspartner insgesamt | Anzahl der Vertragspartner zum jeweiligen Filterkriterium |  | **M** |
| Anzahl der angemeldeten Vertragspartner | Anzahl der Vertragspartner zum jeweiligen Filterkriterium, die sich am jeweiligen System (Wirk- bzw. VPSWH) angemeldet haben |  | **M** |

# \_Toc172190030Meldungen

Keine