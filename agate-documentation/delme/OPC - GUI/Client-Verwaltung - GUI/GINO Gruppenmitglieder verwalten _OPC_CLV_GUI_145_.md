# GINO Gruppenmitglieder verwalten (OPC_CLV_GUI_145)

\\
Anzeigename: GINO-Konfiguration Gruppenmitglieder verwalten \[GINO-Konfiguration Gruppenmitglieder verwalten\]
Anzeigename: GINO-Firmware Gruppenmitglieder verwalten \[GINO-Firmware Gruppenmitglieder verwalten\]
Anzeigename: GINO-Task Gruppenmitglieder verwalten \[GINO-Task Gruppenmitglieder verwalten\]
Anzeigename: GINO-Script-Task Gruppenmitglieder verwalten \[GINO-Script-Task Gruppenmitglieder verwalten\]
\\
In dieser Maske werden die Gruppenmitglieder der gewählten Gruppe ausgegeben.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Mitglieder neu zuordnen | Erstellen neuer Gruppenmitglieder   Aufruf der Funktion „GINO Gruppenmitglieder löschen (OPC\_CLV\_FKT\_149) und anschließend GINO Gruppenmitglieder hinzufügen (OPC\_CLV\_FKT\_148)   Im Erfolgsfall: Aktualisierung der Maske.   Im Fehlerfall: Ausgabe der Fehlermeldung der jeweiligen Funktion. |
| Zurück | Anzeigen der Maske   * „GINO-Gruppe Konfiguration anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_142)" * GINO-Gruppe Firmware anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_143) * GINO-Gruppe Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_144) bzw.     -GINO-Gruppe Script-Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_146)     im Anzeigemodus |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Gruppen-ID |  | M | A | ID der Gruppe | numerisch |
| Gruppenname |  | M | A | Name der Gruppe | 30 alphanumerische Zeichen |
| Gruppentyp |  | M | A | Siehe „GINO-Gruppentyp" |  |
| Mitgliedertyp |  | M | A | Siehe „GINO-Gruppen-Mitgliedertyp" |  |
| Anschluss-IP |  | B\* | E | Liste mit per Komma getrennten IP-Adressen | Max 50 alphanumerische Zeichen |
| Dateiname |  | B\* | E | Name der CSV-Datei welche   IP-Adressen beinhaltet. Dient als Input für die Anschluss-IPs (In jeder Zeile der Datei wird genau eine IP-Adresse erwartet - keine weiteren Trennzeichen.) | Max 15 alphanumerische Zeichen pro Zeile, keine weitern Trennzeichen zwischen den IP-Adressen. |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="f9e8deaa-8f9a-4de5-975e-1acda74c2a88"><ac:plain-text-body><![CDATA[ | IP-Adresse |  | M | A[1:n] | IP-Adresse  ]]></ac:plain-text-body></ac:structured-macro>   Hinweis: wird nur angezeigt wenn es < 10 Einträge gibt | 15 alphanumerische Zeichen |

*B\* Ist eine Datei ausgewählt, so wird das Eingabefeld ignoriert.*