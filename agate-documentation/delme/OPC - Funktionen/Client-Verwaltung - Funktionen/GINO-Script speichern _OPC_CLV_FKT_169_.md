# GINO-Script speichern (OPC_CLV_FKT_169)

# \_Toc172190200Vorbedingungen

Keine

# \_Toc172190201Beschreibung

Die Funktion prüft ob in der Entität „GINO Script" bereits ein Eintrag mit dem übergebenen Namen der Datei als „Scriptname" gespeichert ist. Falls ja, wird die Funktion fehlerhaft beendet und die Meldung GUI-011 ausgegeben.
  
Es werden folgende Prüfungen durchgeführt:

* Die übergebene Datei darf nicht leer sein.
* Die übergebene Signatur darf nicht leer sein.
* Die übergebene Signatur muss eine ASN1-Struktur sein.

*Hinweis: Die Prüfung sind keine exakten Prüfung, ob das Skript bzw. die Signatur gültig ist und ausgeführt werden kann. Es ist nur sichergestellt, dass die Daten gespeichert werden können und als Signaturdatei eine Datei mit dem richtigen Format angegeben wurde.*  
Im Fehlerfall wird die Meldung GUI-010 ausgegeben.  
Ansonsten wird der Name der Skriptdatei als Skriptname sowie die Daten des Skript und der Signatur gespeichert.

# \_Toc172190202Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Datei | Das Script. |  | **M** |
| Signatur | Signatur |  | **M** |

# \_Toc172190203Ausgangsdaten

Keine

# \_Toc172190204Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-010 | Das Script {0} konnte nicht gespeichert werden. | Message | Error |
| GUI-011 | Es existiert bereits ein Script mit dem Namen {0}. | Message | Error |