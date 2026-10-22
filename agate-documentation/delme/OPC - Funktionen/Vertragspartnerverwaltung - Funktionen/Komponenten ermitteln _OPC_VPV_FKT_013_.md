# Komponenten ermitteln (OPC_VPV_FKT_013)

# \_Toc172189947Beschreibung

Die Funktion ermittelt die in eCS verfügbaren Komponenten, bei denen eine Zuordnung über den OPC zulässig ist.  
Dies sind alle gültigen Einträge der Entität „Komponente".

# \_Toc172189948Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc172189949Eingangsdaten

Keine

# \_Toc172189950Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Komponenten (0-n) |  |  |  |  |
| KomponentenCode | Code der Komponete |  | **M** |
| Beschreibung | Beschreibung der Komponente |  | **M** |
| SperreFlag | True, falls die Zuordnung als Sperre möglich ist. |  | **M** |
| PilotFlag | True, falls die Zuordnung als Pilot möglich ist |  | **M** |
| Pilot ab | Zeitpunkt ab dem Pilotbetrieb gestartet werden soll. |  | **K** |
| Vollbetrieb ab | Zeitpunkt ab dem Vollbetrieb gestartet werden soll. |  | **M** |

# \_Toc172189951Meldungen

Keine