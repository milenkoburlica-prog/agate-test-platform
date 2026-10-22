# Fotoinformation beenden (OPC_CKV_FKT_007)

# \_Toc172190357Vorbedingungen

Es wurde bereits geprüft, ob es sich um eine zu beendende Fotoinformation handelt.

# \_Toc172190358Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Fotoinformation | Fotoinformationsobjekt |  | **M** |
| Hinweis | Hinweis zur besseren Nachvollziebarkeit. | Freitextfeld | **K** |

# \_Toc172190359Ausgangsdaten

Keine

# \_Toc172190360Beschreibung

Falls der Aufruf über die Maske „Fotoinformation Details (OPC\_CKV\_GUI\_008)" erfolgt muss die Aktion mit der Meldung GUI-802 bestätigt werden.  
Falls ein Hinweis übergeben wurde, so wird dieser im übergebenen Fotoinformationsobjekt gesetzt.  
Das Attribut InformationsendeAm wird auf das aktuelle Datum gesetzt und der Eintrag in der Entität „Fotoinformation" historisiert (LOESCH\_KZ = 1).  
Es wird die Meldung CKV-101 ausgegeben. (An der GUI nur falls der Aufruf über die Maske „Fotoinformation Details (OPC\_CKV\_GUI\_008)" erfolgt.)

# \_Toc172190361Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-802 | Wollen Sie die Fotoinformation wirklich beenden? | Message | Info |
| CKV-101 | Die Fotoinformation (VSNR=<VSNR>, Typ=<Typ>, Einmeldezeitpunkt=<Einmeldezeitpunkt>) wurde gelöscht. | MessageLog | InfoInfo |