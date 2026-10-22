# Fotoinformationen beenden (OPC_CKV_FKT_008)

# \_Toc172190363Vorbedingungen

# \_Toc172190364Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR-Datei | Datei mit VSNRs |  | **M** |
| Typ | Typ der Fotoinformation | „FZ" oder „FA" | **K** |
| Hinweis | Hinweis zur besseren Nachvollziebarkeit. | Freitextfeld | **K** |

# \_Toc172190365Ausgangsdaten

Keine

# \_Toc172190366Beschreibung

Mit Hilfe der Funktion „VSNRs für Fotoinformation lesen (OPC\_CKV\_FKT\_010)" wird die Liste von VSNRs ermittelt. Liefert die Funktion einen Fehler wird auch diese Funktion abgebrochen.  
Die weiteren Schritte müssen mit der Meldung GUI-803 bestätigt werden.  
Für jede VSNR werden folgende Schritte durchgeführt:

* Es wird die Funktion „Fotoinformation suchen (OPC\_CKV\_FKT\_002)" mit dem Typ laut Eingangsdaten aufgerufen.
* Falls keine Fotoinformation gefunden wurde, wird die Meldung CKV-102 gelogged und mit der nächsten VSNR fortgesetzt.
* Die Funktion „Fotoinformation beenden (OPC\_CKV\_FKT\_007)" wird mit der gefundenen Fotoinformationen und den Hinweis aus den Eingangsdaten aufgerufen.

Die Meldung CKV-103 wird ausgegeben.

# \_Toc172190367Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-803 | Wollen Sie wirklich die Fotoinformation für <Anzahl der VSNR in Liste> VSNRs beenden? | Message | Info |
| CKV-102 | Für die VSNR <VSNR> und den Typ <Typ, falls nicht gesetzt „*alle*"> ist keine aktuelle Fotoinformation vorhanden. | Log | Info |
| CKV-103 | Für <Anzahl der durchgeführten Beendigungen> von <Anzahl VSNR insgesamt> VSNRs wurde eine Fotoinformation beendet. | MessageLog | InfoInfo |