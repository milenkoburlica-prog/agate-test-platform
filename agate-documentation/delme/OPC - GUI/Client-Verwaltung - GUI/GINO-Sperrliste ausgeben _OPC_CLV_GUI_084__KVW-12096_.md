# GINO-Sperrliste ausgeben (OPC_CLV_GUI_084)_KVW-12096_

Anzeigename: GINO-Sperrliste ausgeben \[GINO-Sperrliste ausgeben\]
In dieser Maske werden die GINO Seriennummern, die gesperrt wurden, dargestellt.
Beim Aufruf dieser Maske wird die Funktion „[GINO-Sperrliste ermitteln (OPC\_CLV\_FKT\_170)|GINO-Sperrliste ermitteln (OPC\_CLV\_FKT\_170)\_KVW-12096\_]" ausgeführt und die zurückgelieferten Einträge als Liste mit den unten angegebenen Feldern ausgegeben.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| OK | Funktion „GINO-Sperrliste ermitteln (OPC\_CLV\_FKT\_170)" wird erneut mit dem Eingangsparameter „Filter Seriennummer" aufgerufen und die Liste der gesperrten GINO-Produktionsdaten mit den neuen Werten ersetzt. |
| Check-Box | Markierung des jeweiligen Gerätes |
| Gerät entsperren | Ausgewählte Geräte werden wieder entsperrt: Aufruf der Funktion GINOs entsperren (OPC\_CLV\_FKT\_172)   Im Erfolgsfall: Ausgabe der Erfolgsmeldung.Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| **Filter Seriennummer** | **Seriennummer** | **K** | **E** | **Eingangswert für den Aufruf der Funktion „****GINO-Sperrliste ermitteln (OPC\_CLV\_FKT\_170)****"** | **10 Zeichen**   <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="9eb45735-2204-44ad-bda5-9548e9c62824"><ac:plain-text-body><![CDATA[**[0-9?**]\* | ]]></ac:plain-text-body></ac:structured-macro> |
| Seriennummer |  | M | A | Seriennummer des (aktuell) gesperrten Gerätes | 50 alphanumerische Zeichen |
| Sperrdatum |  | M | A | Datum und Uhrzeit an dem das Gerät gesperrt wurde | Datum |
| Kommentar | KOMMENTAR\_SPERRE | K | A/E | Kommentar warum die Sperrung/Entsperrung vorgenommen wurde/wird. | 100 alphanumerische Zeichen |
| Bearbeiter |  | M | A | Benutzerkennung, der die Gerätsperre gesetzt hat | 20 alphanumerische Zeichen |