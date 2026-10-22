# GINO-Gruppe Konfiguration anzeigen_erstellen_ändern (OPC_CLV_GUI_142)

Anzeigename: GINO-Gruppe anlegen \[Anlegen\] oder
Anzeigename: GINO-Gruppe ändern \[Ändern\]
Anzeigename: GINO-Gruppe anzeigen \[Anzeigen\]
\\
In dieser Maske wird eine Gruppe angezeigt, eine neue Gruppe erstellt oder eine bestehende Gruppe geändert.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
|  |  |
| Gruppe Speichern | Speichern der neu angelegten Gruppe oder geänderten GruppeAufruf der Funktion „GINO-Gruppe speichern (OPC\_CLV\_FKT\_142)" sowie „GINO-Konfiguration Zuordnung speichern (OPC\_CLV\_FKT\_143)"   Im Fehlerfall: Ausgabe der Fehlermeldung der jeweiligen Funktion.   Ist nur für „Gruppe anlegen" oder „Gruppe ändern" aktiv. |
| Gruppe Bearbeiten | Ist nur für „Gruppe anzeigen" aktiv |
| Gruppe Löschen | Löschen einer Gruppe   Ist nur für „Gruppe anzeigen" aktiv   Aufruf der Funktion „GINO-Konfigurationsgruppe löschen (OPC\_CLV\_FKT\_145)" nach einer Bestätigungsabfrage und anschließend Anzeigen der Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| Gruppenmitglieder verwalten | Gruppenmitglieder verwalten, ist nur aktiv für GINO-Gruppen-Mitgliedertyp „SPEZIELL". Aufruf der Funktion „GINO Gruppenmitglieder ermitteln (OPC\_CLV\_FKT\_146)" und anschließend Anzeige der Maske „GINO Gruppenmitglieder verwalten (OPC\_CLV\_GUI\_145)". |
| Zurück | Anzeige der GruppenlisteAnzeigen der Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Gruppenname |  | M | E | Name der Gruppe | 30 alphanumerische Zeichen |
| Mitgliedertyp |  | M | E/A | Siehe „GINO-Gruppen-Mitgliedertyp" | Drop-Down-Liste |
| Filter Seriennummer | Seriennummer | K | E | Filter für Seriennummer | 1000 alphanumerische Zeichen |
| Filter Firmware | Firmware | K | E | Filter für Firmware | 1000 alphanumerische Zeichen |
| Anzahl Mitglieder |  | M | A | Anzahl der Mitglieder der Gruppe |  |
| Start |  | M | E/A | Start des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| Ende |  | M | E/A | Ende des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="d5b433f9-d4c7-4798-a3cb-2d89c53efa41"><ac:plain-text-body><![CDATA[ | Auswahl |  | M | E[0:n] | Kennzeichen ob der gewählte Parameter in die Gruppe aufgenommen werden soll | Checkbox | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="18129d14-8858-4269-9c1e-29fd93ab0f18"><ac:plain-text-body><![CDATA[ | Name |  | M | A[0:n] | Name des Parameters | 50 alphanumerische Zeichen | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="619489cc-970b-46d4-b0fe-ce21b7b1c3d2"><ac:plain-text-body><![CDATA[ | Wert |  | M | E[0:n] | Wert des Parameters  ]]></ac:plain-text-body></ac:structured-macro>   Das Feld bleibt ausgegraut wenn:   * Auswahl = false oder * default = true oder * Entfernen = true             Kann ein Wert eingegeben werden, so wird dieser beim Speichern übernommen (auch wenn das Feld leer bleibt) | 4000 alphanumerische Zeichen (Feldlänge 50) |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="45e73885-e3da-4bea-93bc-927ce02bf291"><ac:plain-text-body><![CDATA[ | default |  | M | E[0:n] | Das Feld bleibt ausgegraut wenn  ]]></ac:plain-text-body></ac:structured-macro>   * Auswahl = false oder * Entfernen = true         Wird das Feld selektiert, so wird * der Default-Wert angezeigt und * das Feld Wert ausgegraut     Wird das Feld deselektiert, so wird das Feld Wert aktiv. | Checkbox |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="918f40ee-9f01-4b98-9777-499cd61354b1"><ac:plain-text-body><![CDATA[ | Entfernen |  | M | E[0:n] | Das Feld bleibt ausgegraut wenn Auswahl = false.  ]]></ac:plain-text-body></ac:structured-macro>   Wird das Feld selektiert, so wird   * Default ausgegraut * Kein Parameter-Wert angezeit und * das Feld Wert ausgegraut | Checkbox |

*Hinweise:*   
*Für den Fall GINO-Konfiguration-Gruppe anzeigen sind alle Felder als reine Ausgabefelder ausgegraut dargestellt.*   
*Für den Fall der Neuerstellung der Gruppe ist der Mitgliedertyp auf ‚SPEZIELL' oder ‚DYNAMISCH' eingeschränkt*