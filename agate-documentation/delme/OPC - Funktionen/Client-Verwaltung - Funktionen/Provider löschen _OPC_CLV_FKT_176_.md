# Provider löschen (OPC_CLV_FKT_176)

# \_Toc172190242Vorbedingungen

Zum angegebenen Provider ist keine Zuordnung zu einem Anschluss vorhanden.

# \_Toc172190243Beschreibung

Diese Funktion löscht den angegebenen Provider aus der Entität „Provider".  
Vor dem Löschen werden folgende Prüfungen durchgeführt:

* + Zum Provider darf keine Zuordnung zu einem Anschluss existieren

Sind zum angegebenen Provider noch Zuordnungen zu einem Anschluss vorhanden, so wird das Löschen nicht zugelassen und eine Fehlermeldung ausgegeben und im Ereignis-Log protokolliert.  
Vor dem Löschen muss der Benutzer, falls die Prüfungen erfolgreich waren, das Löschen des Provider bestätigen.

# \_Toc172190244Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Entität |  | **M** |

# \_Toc172190245Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Entität |  | **M** |

# \_Toc172190246Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-210 | Der Provider <Provider> kann nicht gelöscht werden, da noch <Anzahl der Zuordnungen> Anschlüsse diesem Provider zugeordnet sind. | Log   Message | Error   Error |
| CLV-211 | Wollen sie den Provider <Provider> wirklich löschen (Ja/Nein)? | Message | Warning |
| CLV-212 | Der Provider <Provider> wurde erfolgreich gelöscht. | MessageLog | InfoInfo |