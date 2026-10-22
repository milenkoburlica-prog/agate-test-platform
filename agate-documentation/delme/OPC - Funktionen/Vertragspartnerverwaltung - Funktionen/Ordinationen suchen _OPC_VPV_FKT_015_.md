# Ordinationen suchen (OPC_VPV_FKT_015)

# \_Toc172189959Beschreibung

Diese Funktion sucht aktive Ordinationen anhand der in Folge angegebenen Suchparameter und retourniert sie in Form einer Ordinationsliste.
  
Falls angegeben wird die Zuordnung der Rolle anhand der Entität „Systembenutzer Rolle", die Zuordnung der Sperre anhand der Entität „Systembenutzer Komponente" mit gesetztem Flag „Sperre" und die Zuordnung des Piloten ebenfalls anhand der Entität „Systembenutzer Komponente" mit gesetztem Flag „Pilot" geprüft.

# \_Toc172189960Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189961Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  | **K** |
| Ordinationsnummer | Nummer der Ordination. |  | **K** |
| Ausprägung | Ausprägung der Ordination. |  | **K** |
| Rolle | Zugeordnete Rolle. |  | **K** |
| Sperre | Zugeordnete Sperre. |  | **K** |
| Pilot | Zugeordneter Pilot. |  | **K** |

# \_Toc172189962Ausgangsdaten

| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| --- | --- | --- | --- | --- |
| 0 bis n Wiederholungen |  |  |  |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  | **M** |  |
| Ordinationsnummer | Nummer der Ordination. |  | **M** |  |
| Ausprägungscode | Ausprägung der Ordination. |  | **M** |  |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |  |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |  |

# \_Toc172189963Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-023 | Ordination anhand der Suchparameter <Suchparameter> nicht gefunden. | Message | Warning |