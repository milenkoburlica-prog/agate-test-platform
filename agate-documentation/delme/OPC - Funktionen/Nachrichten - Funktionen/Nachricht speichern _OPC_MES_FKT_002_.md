# Nachricht speichern (OPC_MES_FKT_002)

# \_Toc172190304Beschreibung

Die Eingabe des Gültigkeitsintervalls ist optional (Kann-Feld). Wird kein Gültigkeitsintervall ‚BIS' erfasst, ist die Nachricht gültig, bis diese gelöscht wird (Gültigkeitintervall ‚VON' muss angegeben werden).  
Zum Speichern der Nachricht wird die erstellte Nachricht (inklusive Filterkriterien und Ersteller) mit den zugehörigen Adressaten und der System-Identifikation ‚SYSTEM\_OPC' an das Messaging-System (siehe Funktion „BF\_MS\_FKT\_001") zum Speichern der Nachricht übergeben. Bei erfolgreicher Speicherung wird eine eindeutige Identifikation für die Nachricht (Meldungsnummer) zurückgeliefert.

# \_Toc172190305Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Nachricht (Message) | Nachricht |  | M |
| Kategorie | Meldungsname |  | M |
| Text | Meldungstext |  | M |
| Gültigkeitsintervall | Gültigkeitsintervall. |  | M |
| Von | Startzeitpunkt der Gültigkeit der Nachricht |  | M |
| Bis | Endzeitpunkt der Gültigkeit der Nachricht |  | K |
| Filter | Filterkriterien |  | M |
| VPNR | Liste mit Vertragspartnernummern |  | K |
| Zuname | Liste mit Zunamen von Vertragspartnern |  | K |
| Adressaten | Angabe von 1 bis n Adressaten. Ein Adressat kann dabei eine Vertragspartnernummer, Ordination oder eine einzelne Vertragspartner-Session sein. |  | M |

# \_Toc172190306Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Identifikationen | Eindeutige Identifikation der Nachrichten. |  | M |

# \_Toc172190307Meldungen / Ausnahmen

|  |
| --- |
| **Ausnahmen** |
| Die Nachricht mit der Meldungsnummer <eindeutige Identifikation für die Nachricht> wurde gespeichert. |