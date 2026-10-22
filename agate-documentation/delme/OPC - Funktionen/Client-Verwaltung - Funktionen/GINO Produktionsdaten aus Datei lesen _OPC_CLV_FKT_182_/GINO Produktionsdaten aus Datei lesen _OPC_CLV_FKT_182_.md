# GINO Produktionsdaten aus Datei lesen (OPC_CLV_FKT_182)

# Vorbedingungen

Keine

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Datei | Datei mit Produktionsdaten |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| String | Produktionsdaten |  | **M** |

# Beschreibung

Falls keine Datei übermittelt wurde, wird mit der Meldung GUI-030 abgebrochen.  
Tritt beim Lesen ein Fehler auf, wird die Funktion mit der Meldung GUI-026 abgebrochen.  
Falls die Datei leer ist, wird die Funktion mit der Meldung GUI-027 abgebrochen.

Der Inhalt der Datei wird zurückgegeben.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-026 | Fehler beim Lesen der Datei <Dateiname>. | Message | Error |
| GUI-027 | Die Datei <Dateiname> enthält keine Einträge. | Message | Error |
| GUI-030 | Es wurde keine Datei angegeben. | Message | Error |