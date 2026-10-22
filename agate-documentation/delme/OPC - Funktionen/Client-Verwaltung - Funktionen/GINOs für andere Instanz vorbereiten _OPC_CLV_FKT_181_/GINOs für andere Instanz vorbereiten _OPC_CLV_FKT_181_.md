# GINOs für andere Instanz vorbereiten (OPC_CLV_FKT_181)

# \_Toc172190271Vorbedingungen

Keine

# \_Toc172190272Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Seriennummern | Menge an Seriennummern | Möglicherweise die leere Menge | **M** |

# \_Toc172190273Beschreibung

Falls die Menge mit Seriennummern leer ist, wird mit der Meldung GUI-031 abgebrochen.   
Falls zu den angegebenen Seriennummern bereits ein freigegebener GINO existiert, muss die Aktion mit Meldung GUI-032 bestätigt oder abgebrochen werden (Ja/Nein) und die Aktion wird erst nach einer Bestätigung fortgesetzt.  
Für alle Seriennummern wird die Funktion „GINO sperren (OPC\_CLV\_FKT\_171)" mit dem Kommentar „Testgerät" aufgerufen.  
Im Fehlerfall wird die gesamte Aktion abgebrochen und die entsprechende Fehlermeldung ausgegeben.  
Die Funktion wird unter Ausgabe der Meldung GUI-033 beendet.

# \_Toc172190274Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-031 | Es wurde keine Seriennummer ausgewählt. | Message | Error |
| GUI-032 | Die ausgewählten GINOs <Menge Seriennummern> wurden bereits freigegeben, wirklich in anderer Instanz verwenden? | Message | Warnung |
| GUI-033 | Die GINOs mit den Seriennummern <Menge Seriennummern> wurden gesperrt. Das Exportfile kann mit „Download" heruntergeladen werden. | Message | Info |