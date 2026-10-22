# Bereich für IP-Adresse ermitteln (OPC_CLV_FKT_124)

# \_Toc172190056Vorbedingungen

Keine

# \_Toc172190057Beschreibung

Der Bereich für die IP Adresse wird ermittelt. Nur für das GIN.Netz möglich  
Anhand der übergebenen Daten wird die „IP Range" aufgrund der Einträge in der Entität „IP Ranges" ermittelt.Wurde kein Bundesländercode übergeben, werden nur jene Einträge in der Entität „IP Ranges" berücksichtigt, bei denen ebenfalls kein Bundesländercode gesetzt ist.  
Kann kein Ergebnis ermittelt werden, wird die Meldung CLV-1080 ausgegeben.  
Ansonsten wird die „IP Range" zurückgegeben und die Funktion beendet.

# \_Toc172190058Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ausprägungscode | Ausprägungscode der Gina |  | **M** |
| Bundesländercode | Bundesländercode |  | **K** |

# \_Toc172190059Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Bereichs-Id | Ermittelter Bereich. |  | **K** |

# \_Toc172190060Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| \_Ref274557272Fkt\_IP\_Adresse\_aus\_Bereich\_ermitteln**MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-1080 | Für die Ausprägung <Ausprägung> und den Bundesländercode <Bundesländercode> konnte keine IP-Range ermittelt werden. | Log   Message | Warning   Error |

Die Meldungen werden nur dann in die Eventlog-Datei geschrieben, falls die Funktion von einem Batch aufgerufen wird.