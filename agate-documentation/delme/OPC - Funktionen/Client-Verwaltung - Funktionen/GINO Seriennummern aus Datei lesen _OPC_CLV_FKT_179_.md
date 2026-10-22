# GINO Seriennummern aus Datei lesen (OPC_CLV_FKT_179)

# \_Toc172190260Vorbedingungen

Keine

# \_Toc172190261Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Datei | Datei mit Seriennummern |  | **M** |

# \_Toc172190262Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Seriennummern | Menge von Seriennummern |  | **M** |

# \_Toc172190263Beschreibung

Falls keine Datei übermittelt wurde, wird mit der Meldung GUI-030 abgebrochen.  
Die übergebene Datei wird zeilenweise gelesen.  
Tritt dabei ein Fehler auf, wird die Funktion mit der Meldung GUI-026 abgebrochen.  
Falls die Datei leer ist, wird die Funktion mit der Meldung GUI-027 abgebrochen.  
Für jede Zeile werden folgende Schritte durchgeführt:

* In jeder Zeile wird genau eine Seriennummer erwartet. Es wird daher geprüft, ob es sich um einen String bestehend aus Ziffern und Buchstaben handelt.
* Ist dies nicht der Fall wird die Funktion mit der Meldung GUI-028 abgebrochen.
* Ansonsten wird die Seriennummer in die ausgehende Liste aufgenommen.

# \_Toc172190264Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-026 | Fehler beim Lesen der Datei <Dateiname>. | Message | Error |
| GUI-027 | Die Datei <Dateiname> enthält keine Einträge. | Message | Error |
| GUI-028 | Die <Zeilennummer>. Zeile der Datei <Dateiname> enthält keine Seriennummer. | Message | Error |
| GUI-030 | Es wurde keine Datei angegeben. | Message | Error |