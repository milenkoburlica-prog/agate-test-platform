# GINO Produktionsdaten importieren (OPC_CLV_FKT_183)

# Vorbedingungen

Das clv-Backend wurde konfiguriert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| String | Produktionsdaten | .csv Datei nach im SS51-Format | **M** |
|  | Import erzwingen | Ob Daten trotz Duplikaten importiert werden. | **K** |

# Beschreibung

Falls die Produktionsdaten leer sind, wird mit der Fehlermeldung GUI-036 abgebrochen.

Der Import wird über die Schnittstelle "Import Produktionsdaten" vom clv-Backend durchgeführt.

Falls Felder in den Produktionsdaten nicht valide sind, wird mit der Fehlermeldung GUI-037 abgebrochen.

Falls ein sonstiger Fehler auftritt, wird mit der Fehlermeldung GUI-038 bzw. GUI-039 abgebrochen.

Falls vom Backend zurückgemeldet wurde, dass Daten bereits in der DB vorhanden sind, bzw. Daten im File doppelt vorhanden sind, wird die Warnung GUI-040 ausgegeben, für welche der Nutzer bestätigen kann, die Daten trotzdem zu importieren (Import erzwingen). Falls "ja" ausgewählt wurde, wir diese Funktion mit "Import erzwingen" aufgerufen.

Die Funktion wird unter Ausgabe der Meldung GUI-041 beendet.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-036 | Es wurde keine Daten ausgewählt. | Message | Error |
| GUI-037 | Fehler bei Validation von <NAME> (<WERT>): <GRUND>. | Message | Error |
| GUI-038 | Beim Aufruf vom CLV trat der folgende Fehler auf: <NAME> (<STATUS\_CODE>) - <DETAILS>. | Message | Error |
| GUI-039 | Beim Aufruf vom CLV trat der folgende Fehler auf: <DETAILS> | Message | Error |
| GUI-040 | Die Produktionsdaten mit den Seriennummern <LISTE SERIENNUMMERN> sind bereits in der Datenbank vorhanden. <OPTIONAL "Die Produktionsdaten mit den Seriennummern <LISTE SERIENNUMMERN> sind doppelt im angegebenen File vorhanden. ">Trotzdem importieren? | Message | Warnung |
| GUI-041 | Die Seriennummern <LISTE SERIENNUMMERN> wurden erfolgreich importiert. | Message | Info |