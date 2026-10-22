# Session(s) suchen (OPC_SEV_FKT_001)

# \_Toc172190277Beschreibung

Diese Funktion sucht eine oder mehrere Sessions.

# \_Toc172190278Eingangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| Session Information („Datum von" oder „Datum bis" muss angegeben werden) |  |  |  |
| Vertragspartner-nummer | Vertragspartner |  | **K** |  |
| Ordinationsnummer | Ordinationsnummer |  | **K** |  |
| GinaID | Identifikation der Gina |  | **K** |  |
| TerminalID | Identifikation des Terminals |  | **K** |  |
| ExtUID\_ASW | Externe UserId (z.B. aus ASW/KIS) |  | **K** |  |
| Datum von | Beginn des Beobachtungszeitraumes   Bedingt: Mindestens eines der beiden Felder „Datum bis" und „Datum von" muss angegeben werden.   Das eingebene Datum muss kleiner gleich dem Startdatum der Session sein. |  | **B** |  |
| Datum bis | Ende des Beobachtungszeitraumes   Bedingt: Mindestens eines der beiden Felder „Datum bis" und „Datum von" muss angegeben werden.   Das eingegebene Datum (Uhrzeit „23:59:59") muss grösser oder gleich dem Endedatum der session sein. |  | **B** |  |
| Sortierung – Feld1 | Sortierfeld 1 | Ordinationsnummer, Datum,Dialog-ID | **M** |  |
| Sortierung – Reihenfolge | Sortierreihenfolge | aufsteigend oder   absteigend | **M** |  |
| Dialog/Session beenden |  |  |  |
| Vertragspartner-nummer | Vertragspartner |  | **K** |  |
| Ordinationsnummer | Ordinationsnummer |  | **K** |  |
| GinaID | Identifikation der Gina |  | **K** |  |
| TerminalID | Identifikation des Terminals |  | **K** |  |
| Sortierung – Feld1 | Sortierfeld 1 | Ordinationsnummer oder Datum | **M** |  |
| Sortierung – Reihenfolge | Sortierreihenfolge | aufsteigend oder   absteigend | **M** |  |

# \_Toc83719564\_Toc172190279Ausgangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| 0 bis n Wiederholungen |  |  |  |
| Vertragspartner-session ID | Eindeutiger Schlüssel der Vertragspa‚tnersessio' (Attribut 'Sess‚on ID' in'der Entität 'VP\_Dialog') |  | **M** |  |
| Ordination | Adresse der Ordination (PLZ, Ort und Straße) |  | **M** |  |
| Ordinationsnummer | Ordinationsnummer |  | **M** |  |
| DialogID | Identifikation des Dialoges |  | **K** |  |
| GinaID | Identifikation der Gina |  | **M** |  |
| TerminalID | Identifikation des Terminals |  | **M** |  |
| ExtUID\_ASW | Externe UserId |  | **K** |  |
| Von | Sessionbeginn |  | **M** |  |
| Bis | Sessionende |  | **K** |  |