# Provider (OPC_CLV_GUI_089)

Anzeigename: Provider \[Provider\]
In dieser Maske werden die Provider dargestellt. Dabei werden je Provider-ID und der Providername dargestellt.
Beim Aufruf dieser Maske werden die Funktionen „[Provider ermitteln (OPC\_CLV\_FKT\_174)|Provider ermitteln (OPC\_CLV\_FKT\_174)]" und „[Provider anzeigen (OPC\_CLV\_FKT\_175)|Provider anzeigen (OPC\_CLV\_FKT\_175)]" ausgeführt.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Radio-Button | Markierung des jeweiligen Provider |
| Provider anlegen | Anlegen eines neuen Provider. Anzeigen der Maske „Provider anlegen (OPC\_CLV\_GUI\_090)". |
| Provider löschen | Löschen eines Provider.Aufruf der Funktion „Provider löschen (OPC\_CLV\_FKT\_176)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung.Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Provider ändern | Ändern eines Provider.   Anzeigen der Maske „Provider ändern (OPC\_CLV\_GUI\_091)". |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="fc7b21a5-e154-4754-a432-3bfeee3b9b53"><ac:plain-text-body><![CDATA[ | Provider-ID |  | M | A[1:n] | Liste mit den gültigen Provider-IDs | alphanumerische Zeichen | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="18e4862d-861c-4033-b6f1-f1cb44387192"><ac:plain-text-body><![CDATA[ | Providername |  | M | A[1:n] | Liste mit den Namen der Provider | 30 alphanumerische Zeichen | ]]></ac:plain-text-body></ac:structured-macro> |