# IP-Adresse aus Bereich ermitteln (OPC_CLV_FKT_125)

# \_Toc172190062Vorbedingungen

Keine

# \_Toc172190063Beschreibung

Für den übergebenen Bereich wird in der Entität „IP Frei" eine als frei markierte IP Adresse gewählt.   
Zuerst werden IP-Adressen gesucht die noch nie verwendet wurden, danach jene die am längsten frei sind.  
Falls keine freie IP-Adresse existiert, werden auch die gesperrten Adressen berücksichtigt, jene die am Längsten gesperrt sind zuerst.  
Gibst es mehrere gleichwertige Einträge wird die numerisch kleinste IP-Adresse gewählt.   
Anmerkung: Die gewählte IP Adresse wird nicht als „belegt" markiert. Dies erfolgt erst beim Speichern der Ginadaten.  
Wird keine IP-Adresse gefunden wird die Meldung CLV-1090 ausgegeben.

# \_Toc172190064Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Bereichs-Id | Zuvor ermittelter Bereich. |  | **M** |
| Ausprägungscode | Nur für Meldung relevant. |  | **M** |
| Bundesländercode | Nur für Meldung relevant. |  | **M** |

# \_Toc172190065Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| IP-Adresse |  |  | **M** |

# \_Toc172190066Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| Mldg\_IpBerErm\_keine\_Ip\_ermitteltCLV-1090 | Für die Ausprägung <Ausprägung> und den Bundesländercode <Bundesländercode> konnte keine IP-Adresse ermittelt werden. | MessageLog | Error |

\_Ref274730541Die Meldungen werden nur dann in die Eventlog-Datei geschrieben, falls die Funktion von einem Batch aufgerufen wird.