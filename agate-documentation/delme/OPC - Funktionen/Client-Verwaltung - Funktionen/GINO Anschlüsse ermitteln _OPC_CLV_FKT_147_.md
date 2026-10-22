# GINO Anschlüsse ermitteln (OPC_CLV_FKT_147)

# \_Toc172190110Vorbedingungen

Keine

# \_Toc172190111Beschreibung

Diese Funktion ermittelt anhand der übergegenen IP-Adressen die entsprechenden Einträge in der Entität „Anschluss". Für jeden ermittelten Anschluss wird ein neues Element von GINO-Gruppen-Mitglied erzeugt und mit Anschluss-ID und IP-Adresse in den Ausgangsdaten zurückgeliefert.  
IP-Adressen für die kein Anschluss ermittelt werden konnte, werden ebenfalls in der Liste der IP-Adresse der Ausgangsdaten zurückgeliefert.

# \_Toc172190112Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von IP-Adressen | Liste der Client-IP-Adressen für jeweils einen Anschluss |  | **M** |

# \_Toc172190113Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Gruppen-Mitglied" | Siehe „GINO-Gruppen-Mitglied".   Die Liste kann auch leer sein. |  | **M** |
| Liste von IP-Adressen | IP-Adressen die keinem Anschluss zugeordnet werden konnten.   Die Liste kann auch leer sein. |  | **M** |

# \_Toc172190114Meldungen

Keine