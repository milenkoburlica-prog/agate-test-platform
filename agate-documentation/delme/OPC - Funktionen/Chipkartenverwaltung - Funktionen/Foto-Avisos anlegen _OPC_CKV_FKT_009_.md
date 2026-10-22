# Foto-Avisos anlegen (OPC_CKV_FKT_009)

# \_Toc172190369Vorbedingungen

# \_Toc172190370Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR-Datei | Datei mit VSNRs |  | **M** |
| Hinweis | Hinweis zur besseren Nachvollziebarkeit. | Freitextfeld | **K** |

# \_Toc172190371Ausgangsdaten

Keine

# \_Toc172190372Beschreibung

Mit Hilfe der Funktion „VSNRs für Fotoinformation lesen (OPC\_CKV\_FKT\_010)" wird die Liste von VSNRs ermittelt. Liefert die Funktion einen Fehler wird auch diese Funktion abgebrochen.  
Die weiteren Schritte müssen mit der Meldung GUI-804 bestätigt werden.  
Für jede VSNR werden folgende Schritte durchgeführt:

* Es wird geprüft, ob in der Entität HVB\_SVPERSON ein aktueller Eintrag für die VSNR existiert.
* Konnte keine SV-Person gefunden werden, wird die Meldung CKV-104 gelogged und mit der nächsten VSNR fortgesetzt.
* Ist für die SV-Person das Flag „Todesdatum bestätigt" gesetzt so wird die Meldung CKV-108 gelogged und mit der nächsten VSNR fortgesetzt.
* Es wird die Funktion „Fotoinformation suchen (OPC\_CKV\_FKT\_002)" mit nicht gesetzen Typ aufgerufen.
* Falls bereits eine Fotoinformation existiert, wird die Meldung CKV-105 gelogged und mit der nächsten VSNR fortgesetzt.
* Für die VSNR wird ein Fotoinformationsobjekt mit folgenden weiteren Daten erzeugt.
  + Typ=FA
  + Einmeldezeitpunkt=aktuelles Datum
  + InformationsendeAm=31.12.3999
  + Meldung durch=OP
  + Hinweis=falls vorhanden Hinweis aus den Eingangsdaten, sonst leer
* Die Fotoinformation wird in der Entitöt Foto\_Info gespeichert und die Meldung CKV-106 gelogged.

Die Meldung CKV-107 wird ausgegeben.

# \_Toc172190373Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-804 | Wollen Sie wirklich für <Anzahl der VSNR in Liste> VSNRs ein Foto-Aviso anlegen? | Message | Info |
| CKV-104 | Die VSNR <VSNR> ist im System nicht bekannt. | Log | Info |
| CKV-105 | Es existiert bereits eine aktuelle Fotoinformation (VSNR=<VSNR>, Typ=<Typ>, Einmeldezeitpunkt=<Einmeldezeitpunkt>). | Log | Info |
| CKV-106 | Für die VSNR <VSNR> wurde ein Foto-Aviso angelegt. | Log | Info |
| CKV-107 | Für <Anzahl der angelegten Avisos> von <Anzahl VSNR insgesamt> VSNRs wurde eine Foto-Aviso angelegt. | MessageLog | InfoInfo |
| CKV-108 | Die Person mit der VSNR <VSNR> ist bereits verstorben. | Log | Info |