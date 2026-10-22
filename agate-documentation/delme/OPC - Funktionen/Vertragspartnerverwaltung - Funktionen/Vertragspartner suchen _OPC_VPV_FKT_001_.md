# Vertragspartner suchen (OPC_VPV_FKT_001)

# \_Toc83719386\_Toc172189887Beschreibung

Diese Funktion sucht Vertragspartner anhand der in Folge angegebenen Suchparameter und zeigt sie in Form einer Vertragspartnerliste an. Jeder einzelne Listeneintrag enthält Attribute laut Ausgangsdaten.

# \_Toc83719387\_Toc172189888Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc83719388\_Toc172189889Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  | **K** |
| Zuname | Zuname des Vertragspartners. |  | **K** |
| Vorname | Vorname des Vertragspartners. |  | **K** |
| PLZ | Postleitzahl der Zustelladresse der Ordination des Vertragspartners |  | **K** |
| Suspendiert | Suspendierungskennzeichen des Vertragspartners | alle / suspendiert / nicht suspendiert | **M** |

# \_Toc83719389\_Toc172189890Ausgangsdaten

| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| --- | --- | --- | --- |
| 0 bis n Wiederholungen |  |  |  |
| Kennung | Userid des Vertragspartners. |  | **M** |
| Zuname | Zuname des Vertragspartners. |  | **M** |
| Vorname | Vorname des Vertragspartners. |  | **K** |
| PLZ | Postleitzahl der Zustelladresse der Ordination des Vertragspartners |  | **M** |
| Ort | Ort der Zustelladresse der Ordination des Vertragspartners |  | **K** |
| BLD | Bundesland der Zustelladresse der Ordination des Vertragspartners |  | **M** |
| VP Nr. | Vertragspartnernummer |  | **M** |
| Suspendiert | Suspendierungskennzeichen des Vertragspartners | J/N (Checkbox – nicht überschreibbar) | **M** |

# \_Toc83719390\_Toc172189891Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-001 | Vertragspartner anhand der Suchparameter <Suchparameter> nicht gefunden. | Log, Message | Warning |