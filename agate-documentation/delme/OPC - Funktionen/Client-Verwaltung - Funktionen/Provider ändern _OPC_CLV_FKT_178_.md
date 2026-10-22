# Provider ändern (OPC_CLV_FKT_178)

# \_Toc172190254Vorbedingungen

Es wurde bereits ein Provider angelegt. Dieser Provider wird geändert.

# \_Toc172190255Beschreibung

Diese Funktion speichert den Providername zu einem bestehenden Provider.   
Hierzu werden folgende Schritte durchgeführt:

1. Aus der Entität „Provider" wird der Provider über die Provider-ID ermittelt
2. Der übergebene Providername (neu) und der Providername aus der Datenbank (alt) werden miteinander verglichen.
   1. Sind die Namen dieselben, wird das Speichern der Änderung nicht zugelassen und eine Fehlermeldung ausgegeben.
   2. Unterscheiden sich die Namen, aber der neue Providername existiert bereits in der Datenbank, wird das Speichern der Änderung nicht zugelassen und eine Fehlermeldung ausgegeben.
   3. Unterscheiden sich die beiden Namen, wird der Eintrag in der Datenbank geändert. (Keine Historisierung)

# \_Toc172190256Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Provider |  | **M** |
| Provider | Entität |  | **M** |
| Providername | Name der dem Provider zugeordnet werden sollen. |  | **M** |

# \_Toc172190257Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Entität |  | **M** |

# \_Toc172190258Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-216 | Der Provider <Provider> konnte nicht aktualisiert werden, da der Providername bereits vorhanden ist. | Message/Log | Error |
| CLV-217 | Wollen sie den Provider aktualisieren? (Ja/Nein) | Message | Warning |
| CLV-218 | Der Provider <Provider> wurde erfolgreich geändert. | Message/Log | Info |