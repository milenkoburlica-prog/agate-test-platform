# GINO Gruppenmitglieder hinzufügen (OPC_CLV_FKT_148)

# \_Toc172190116Vorbedingungen

Keine

# \_Toc172190117Beschreibung

Die Funktion entfernt ermittelt sowohl für Konfigurations- als auch Firmwaregruppen aus der in den Eingangsdaten übergebenen Liste von IP-Adresse Duplikate und ruft mit der in den Eingangsdaten übergebenen Liste von IP-Adresse die Funktion GINO Anschlüsse ermitteln (OPC\_CLV\_FKT\_147) auf.   
Liefert die Funktion in den Ausgangsdaten IP-Adressen zurück, die nicht zugeordent werden konnten, so wird diese in den Ausgangsdaten zurückgeliefert und die Funkton wird fehlerhaft beendet.  
Es wird geprüft ob die ermittelten Anschlüsse bereits der Gruppe mit der GINO-Gruppen-ID aus den Eingangsdaten zugeordnet sind, in diesem Fall werden die entsprechenden IP-Adressen in der Ausgangsdatenliste „Duplikat Liste von IP-Adressen" zurückgeliefert und die Funktion fehlerhaft beendet.
  
Andernfalls wird für jeden Eintrag der von der obigen Funktion zurückgelieferten Liste von GINO-Gruppen-Mitglied ein Eintrag in „GINO-Gruppe-Mitglieder" gespeichert. Die Liste wird mit den neuen Anschluss-IDs angereichtert und in den Ausgangsdaten zurückgeliefert.

# \_Toc172190118Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| Liste von IP-Adressen | Liste der Client-IP-Adressen für jeweils einen Anschluss |  |  |

# \_Toc172190119Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Gruppen-Mitglied" | Siehe „GINO-Gruppen-Mitglied".   Die Liste kann auch leer sein. |  | **M** |
| Liste von IP-Adressen | IP-Adressen die keinem Anschluss zugeordnet werden konnten.   Die Liste kann auch leer sein. |  | **M** |
| Duplikat Liste von IP-Adressen | IP-Adressen die der Gruppe bereits zugeordnet sind.   Die Liste kann auch leer sein. |  | **M** |

# \_Toc172190120Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
|  | Anschluss-IP unbekannt <IP> | Message | Warning |
|  |  |  |  |