# VSNRs für Fotoinformation lesen (OPC_CKV_FKT_010)

# \_Toc172190375Vorbedingungen

# \_Toc172190376Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR-Datei | Datei mit VSNRs |  | **M** |

# \_Toc172190377Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR-Liste | Liste mit VSNRs |  | **M** |

# \_Toc172190378Beschreibung

Die übergebene Datei wird zeilenweise gelesen.  
Tritt dabei ein Fehler auf, wird die Funktion mit der Meldung CKV-110 abgebrochen.  
Falls die Datei leer ist, wird die Funktion mit der Meldung CKV-111 abgebrochen   
Für jede Zeile werden folgende Schritte durchgeführt:

* In jeder Zeile wird genau eine VSNR erwartet. Es wird daher geprüft, ob sich um eine 10-stellige Zahl handelt.
* Ist dies nicht der Falls wird die Funktion mit der Meldung CKV-112 abgebrochen.
* Falls die ermittelte VSNR bereits in der ausgehenden Liste eingetragen ist,wird die Meldung CKV-113 gelogged und mit der nächsten Zeile fortgesetzt.
* Ansonsten wird die VSNR in die ausgehende Liste aufgenommen.

# \_Toc172190379Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CKV-110 | Fehler beim Lesen der Datei <Dateiname>. | MessageLog | ErrorError |
| CKV-111 | Die Datei <Dateiname> enthält keine Einträge. | MessageLog | ErrorError |
| CKV-112 | Die <Zeilennummer>. Zeile der Datei <Dateiname> enthält keine VSNR. | MessageLog | ErrorError |
| CKV-113 | Die VSNR <VSNR> ist mehrfach vorhanden | Log | Info |

\_Toc5693342\_Toc6478223\_Toc198518436\_Toc244394831