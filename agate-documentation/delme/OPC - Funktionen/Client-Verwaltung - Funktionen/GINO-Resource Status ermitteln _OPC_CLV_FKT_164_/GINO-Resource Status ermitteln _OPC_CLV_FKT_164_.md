# GINO-Resource Status ermitteln (OPC_CLV_FKT_164)

# \_Toc172190170Vorbedingungen

Keine

# \_Toc172190171Beschreibung

Diese Funktion ermittelt anhand der übergebenen Eingangsdaten für jede Resource aus „GINO-Resourcetyp" den aktuellsten Status aus der Entität „GINO-Resource" samt referenzierenden Einträgen in „GINO-Resouce Data". Letztere werden in den Attributen „Input-Liste"/"Output-Liste"" von „GINO-Resource Status" in den Ausgangsdaten zurückgeliefert.   
\_Hinweis: Sortierung siehe  „**2.4.35* *GINO-Resource Historie ermitteln (OPC\_CLV\_FKT\_173)**"\_

# \_Toc172190172Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| Seriennummer | GINO-Seriennummer |  |  |
| Client-IP | Client-IP der GINO | String | M |

# \_Toc172190173Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Resource Status." | Siehe „GINO-Resource Status". |  | **M** |

# \_Toc172190174Meldungen

Keine.