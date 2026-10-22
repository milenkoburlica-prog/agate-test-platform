# GINO-Resource Historie ermitteln (OPC_CLV_FKT_173)

# \_Toc172190224Vorbedingungen

Der Benutzer hat in der Liste der Resource-Calls einen Eintrag ausgewählt.

# \_Toc172190225Beschreibung

Es wird in der der Entität GinoResource nach dem ausgewählten Resource Call gesucht und für die Ergebnisse dann in der Entität GinoResourceData die Daten geholt. Die Anzahl der Resource Calls die angezeigt werden kann mittels Konfigurationsparameter limitiert werden.  
Die Sortierung erfolgt absteigend nach dem Zeitstempel „letzter Aufruf", d.h. der Eintrag mit dem spätestens Datum wird zuerst angezeigt.  
*Hinweis: Dies entspricht auch der Sortierung nach „erster Aufruf", Ausnahme davon sind Einträge der Resource alive. Hier wird der Eintrag der die Verfügbarkeit des GINOS anzeigen soll (Zeitraum „erster Eintrag" – „letzter Eintrag") den Einträgen mit Statuscode 200 vorgereiht.*

# \_Toc172190226Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Client-IP | Client-IP der GINO |  | **M** |
| Resource-Call | Siehe „GINO-Resourcetyp" |  | **M** |

# \_Toc172190227Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Resource Status" | Liste von „GINO-Resource Status", kann auch leer sein. |  | **M** |

# \_Toc172190228Meldungen

Keine.