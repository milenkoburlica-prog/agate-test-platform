# Client-Stammdaten suchen (OPC_CLV_FKT_109)

# \_Toc172190038Vorbedingungen

Keine.

# \_Toc172190039Beschreibung

Die Funktion sucht in der Entität Stammdaten, Anschluss und Gino nach Client-Stammdaten die den Eingangsdaten entsprechen.  
Je nach Angabe der Suchebene werden entweder nur die VP-Stammdaten, die VP-Stammdaten und Anschlüsse, die VP-Stammdaten, Anschlüsse und Ginas oder oder die VP-Stammdaten, Anschlüsse und Ginos ermittelt und retourniert.  
Wird ein für die VP-Stammdaten relevantes Eingangsdatum übergeben so wird zuerst in den VP-Stammdaten gesucht.   
Falls auch die Anschlüsse ermittelt werden sollen, werden die ermittelten IDs der VP-Stammdaten zu den für die Entität Anschluss relevanten Eingangsdaten hinzugefügt. Mit diesen Daten werden dann die relevanten Datensätze in der Entität Anschluss gesucht.  
Falls auch die Ginos ermittelt werden sollen, werden die ermittelte IDs der Anschlüsse zu den für die Entität Gino relevanten Eingangsdaten hinzugefügt. Mit diesen Daten werden dann die relevanten Datensätze in der Entität gesucht.   
Für das Stats-Logging wird die Dauer des Funktionsaufrufs mit den nachfolgenden Parametern geloggt

* Komponente: Client-Verwaltung – Client-Stammdaten
* Operation: Suchen

# \_Toc172190040Eingangsdaten

| **Suchkriterium** | **Anmerkung** |
| --- | --- |
| Suchebene | „Stammdaten", „Anschluss", „GINO" |
| Stammdaten-relevante Felder |  |
| Zuordnungstyp | „alle", „Exot", „Standard" |
| Vertragspartnernummer |  |
| Ordinationsnummer |  |
| Zuname | Wildcardsuche möglich |
| Vorname | Wildcardsuche möglich |
| Anschlussart | "alle", "GIN", "GIN\_CONNECT", "leer" |
| Anschluss-relevante Felder |  |
| Stammdaten-ID | Nur anstatt stammdatenrelevanten Feldern möglich |
| Client-IP | Wildcardsuche möglich |
| Subnetz | Wildcardsuche möglich |
| Router-IP | Wildcardsuche möglich |
| GINO-relevante Felder |  |
| GINO-Seriennummer | Wildcardsuche möglich |
| GINO-Firmware | Mehrfachsuche mittels Liste |

# \_Toc172190041Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Client-Stammdaten | Listen von Stammdaten, Anschlüsse, Ginos oder Ginas, gegebenenfalls erweitert um Gerät Info (HW-Typ, SW-Distribution)   Sortierung ist abhängig von der Art der zurückgegebenen Objekte:   * Stammdaten: Vertragspartnernummer, Ordinationsnummer * Anschluss: Client-IP * GINO: Seriennummer |  | **K** |

# \_Toc172190042Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-015 | Zum angegebenen Filter konnten keine Einträge ermittelt werden. | Message | Error |