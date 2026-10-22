# GINOs sperren (OPC_CLV_FKT_180)

# \_Toc172190266Vorbedingungen

Keine

# \_Toc172190267Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Seriennummern | Menge an Seriennummern | Möglicherweise die leere Menge | **M** |
| Kommentar | Kommentar warum die Geräte gesperrt werden (max. 100 Zeichen) |  | **K** |

# \_Toc172190268Beschreibung

Falls die Menge mit Seriennummern leer ist, wird mit der Meldung GUI-031 abgebrochen.   
Falls zu den angegebenen Seriennummern bereits ein freigegebener GINO existiert, muss die Aktion mit Meldung GUI-034 bestätigt oder abgebrochen werden (Ja/Nein) und die Aktion wird erst nach einer Bestätigung fortgesetzt.  
Für alle Seriennummern wird die Funktion „GINO sperren (OPC\_CLV\_FKT\_171)" mit dem angegebenen Kommentar aufgerufen.  
Im Fehlerfall wird die gesamte Aktion abgebrochen und die entsprechende Fehlermeldung ausgegeben.  
Die Funktion wird unter Ausgabe der Meldung GUI-013 beendet.

# \_Toc172190269Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-031 | Es wurde keine Seriennummer ausgewählt. | Message | Error |
| GUI-034 | Die ausgewählten GINOs <Menge Seriennummern> wurden bereits freigegeben, wirklich sperren? | Message | Warning |
| GUI-013 | Die GINOs mit den Seriennummern <Menge Seriennummern> wurden gesperrt. | Message | Info |