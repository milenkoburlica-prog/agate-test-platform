# Provider speichern (OPC_CLV_FKT_177)

# \_Toc172190248Vorbedingungen

Keine

# \_Toc172190249Beschreibung

Diese Funktion speichert den neuen Provider in der Entität „Provider".  
Vor dem Speichern wird folgende Prüfung durchgeführt:

* + Der angegebene Provider darf noch nicht existieren (Providername darf nur einmal vorhanden sein)

Im Fehlerfall wird das Speichern des Provider nicht zugelassen und eine Fehlermeldung ausgegeben und im Ereignis-Log protokolliert.  
Vor dem Anlegen muss der Benutzer, falls die Prüfung erfolgreich war, das Anlegen des neu erfassten Provider bestätigen.

# \_Toc172190250Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Provider |  | **M** |
| Provider | Entität |  | **M** |

# \_Toc172190251Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Entität |  | **M** |

# \_Toc172190252Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-213 | Der Provider <Provider> konnte nicht angelegt werden, da der Provider bereits existiert. | Log   Message | Error   Error |
| CLV-214 | Wollen Sie den Provider anlegen? (Ja/Nein) | Message | Warning |
| CLV-215 | Der Provider <Provider> wurde erfolgreich angelegt. | MessageLog | InfoInfo |