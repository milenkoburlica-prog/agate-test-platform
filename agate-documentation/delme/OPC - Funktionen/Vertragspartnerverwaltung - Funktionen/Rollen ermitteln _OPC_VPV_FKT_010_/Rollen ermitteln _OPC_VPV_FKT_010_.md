# Rollen ermitteln (OPC_VPV_FKT_010)

# \_Toc172189935Beschreibung

Die Funktion ermittelt die in eCS verfügbaren Rollen, bei denen die Zuordnung von Vertragspartnern über den OPC zulässig ist.  
Dies sind alle Einträge der Entität „Rolle" für die das Kennzeichen „Zuordnung editierbar" gesetzt ist.

# \_Toc172189936Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189937Eingangsdaten

Keine

# \_Toc172189938Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Rollen (0-n), zu denen über den OPC Vertragspartner zugeordnet werden können. |  |  |  |  |
| Rollen ID | Künstlicher Schlüssel der Rolle |  | **M** |
| Bezeichnung | Bezeichnung der Rolle | z.B. BKF-AssessmentVDAS-Stichtagabfrage | **M** |
| Beschreibung | Beschreibung der Rolle (Feld Beschreibung\_Vergabe\_durch) |  | **M** |

# \_Toc172189939Meldungen

Keine