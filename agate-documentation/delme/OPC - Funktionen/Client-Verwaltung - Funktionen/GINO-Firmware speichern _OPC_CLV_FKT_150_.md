# GINO-Firmware speichern (OPC_CLV_FKT_150)

# \_Toc172190128Vorbedingungen

Keine

# \_Toc172190129Beschreibung

Die Funktion prüft ob in der Entität „GINO Firmware" bereits ein Eintrag mit den in den Eingangsdaten übergebenen Parametern „Software Version" und „Hardware Version" gespeichert ist.  
Falls ja, wird die Funktion fehlerhaft beendet.   
Falls in den Eingangdaten der Parameter „Letzte kompatible Version" übergeben wird, wird geprüft, ob die übergebene Parameter kleiner als der in „GINO-Firmware Version" Wert ist, falls nicht, wird die Funktion fehlerhaft beendet.   
Falls nein, wird in „GINO Firmware" ein neuer Eintrag mit „Software Version", „Hardware Version" und der übergebenen Firmware in der Datenbank gespeichert.

# \_Toc172190130Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Firmware Version | Siehe „GINO-Firmware Version". |  | **M** |
| GINO-Hardware Version | Siehe „GINO-Hardware Version" |  | **M** |
| Letzte kompatible Version | Letzte kompatible GINO-Firmware Version, siehe „GINO-Firmware Version" |  | **K** |
| Datei | Die Firmware. |  | **M** |

# \_Toc172190131Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Firmware-ID | Siehe „GINO-Firmware-ID". |  | **M** |

# \_Toc172190132Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-008 | Eine Firmware mit dieser Hardware- und Softwareversion existert bereits. | Message | Error |
| GUI-016 | Die letzte kompatible Version muss kleiner sein, als die Software Version | Message | Error |