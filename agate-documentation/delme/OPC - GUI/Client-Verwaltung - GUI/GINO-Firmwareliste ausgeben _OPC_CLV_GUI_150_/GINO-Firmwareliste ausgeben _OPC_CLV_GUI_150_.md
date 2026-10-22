# GINO-Firmwareliste ausgeben (OPC_CLV_GUI_150)

Anzeigename: GINO-Firmwareliste ausgeben \[GINO-Firmwareliste ausgeben\]
\\
Beim Aufruf dieser Maske wird die Funktion „[GINO-Firmware Versionen ermitteln (OPC\_CLV\_FKT\_152)|GINO-Firmware Versionen ermitteln (OPC\_CLV\_FKT\_152)]" ausgeführt.
\\
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Ausgewählte Löschen | Die ausgewählten Versionen werden gelöscht. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="22275ebe-cc75-4caa-b5c5-3255b2f020df"><ac:plain-text-body><![CDATA[ | Auswahl |  | M | E[1..n] | Checkbox zur Auswahl der zu löschenden Firmware |  | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="32fb14e6-21df-4a37-943f-97fa63a6b4e2"><ac:plain-text-body><![CDATA[ | Firmware Version |  | M | A[1..n] | Version der Firmware | Regulärer Ausdruck „^\d{2}\.\d{2}\.\d{2}$" | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="bb9040f6-2866-4b27-b54b-f5e4593abf8f"><ac:plain-text-body><![CDATA[ | Hardware Version |  | M | A[1..n] | Hardware Version der Firmware | Regulärer Ausdruck „^\d{2}\.\d{2}$" | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="f28c10aa-0a9f-42a3-b090-40207534951d"><ac:plain-text-body><![CDATA[ | Letzte kompatilble Version |  | M | A[1..n] | Letzte kompatilble Version der Firmware | Regulärer Ausdruck „^\d{2}\.\d{2}\.\d{2}$" | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="f503b40d-56c1-48ab-8959-846de72a46a0"><ac:plain-text-body><![CDATA[ | Dateiname |  | M | A[1..n] | Name der Firmware Datei | 255 alphanumerische Zeichen (keine Überprüfung) | ]]></ac:plain-text-body></ac:structured-macro> |
| Letzte Änderung | LASTCHANGE | M |  | Zeitpunkt der letzten Änderung | Formattiertes Datum |