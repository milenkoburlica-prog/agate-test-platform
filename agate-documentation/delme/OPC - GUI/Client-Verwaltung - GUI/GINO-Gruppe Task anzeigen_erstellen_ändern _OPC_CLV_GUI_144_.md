# GINO-Gruppe Task anzeigen_erstellen_ändern (OPC_CLV_GUI_144)

Anzeigename: GINO-Gruppe Task anlegen \[Anlegen\] oder
Anzeigename: GINO-Gruppe Task ändern \[Ändern\]
Anzeigename: GINO- Gruppe Task anzeigen \[Anzeigen\]
\\
In dieser Maske wird eine GINO-Task Gruppe angezeigt, eine neue Gruppe erstellt oder eine bestehende Gruppe geändert.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
|  |  |
| Gruppe Speichern | Speichern der neu angelegten Gruppe oder geänderten GruppeAufruf der Funktion „GINO-Gruppe speichern (OPC\_CLV\_FKT\_142)" sowie „GINO-Task Zuordnung speichern (OPC\_CLV\_FKT\_162)". Im Fehlerfall: Ausgabe der Fehlermeldung.   Ist nur für „Gruppe anlegen" oder „Gruppe ändern" sichtbar. |
| Gruppe Bearbeiten | Ist nur für „Gruppe anzeigen" sichtbar |
| Gruppe Löschen | Löschen einer Gruppe,   Ist nur für „Gruppe anzeigen" sichtbar.   Aufruf der Funktion „GINO-Task Gruppe löschen (OPC\_CLV\_FKT\_163)" nach einer Bestätigungsabfrage und anschließend Anzeigen der Maske   „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)". |
|  |  |
| Gruppenmitglieder verwalten | Gruppenmitglieder verwalten, ist nur aktiv für GINO-Gruppen-Mitgliedertyp „SPEZIELL".Aufruf der Funktion „GINO Gruppenmitglieder ermitteln (OPC\_CLV\_FKT\_146)" und anschließend Anzeige der Maske „GINO Gruppenmitglieder verwalten (OPC\_CLV\_GUI\_145)". |
| Zurück | Anzeige der GruppenlisteAnzeigen der Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Gruppenname |  | M | E/A | Name der Gruppe | 30 alphanumerische Zeichen |
| Mitgliedertyp |  | M | E/A | Siehe „GINO-Gruppen-Mitgliedertyp" | Drop-Down-Liste |
| Filter Seriennummer | Seriennummer | K | E | Filter für Seriennummer | 1000 alphanumerische Zeichen |
| Filter Firmware | Firmware | K | E | Filter für Firmware | 1000 alphanumerische Zeichen |
| Anzahl Mitglieder |  | M | A | Anzahl der Mitglieder der Gruppe |  |
| Start |  | M | E/A | Start des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| Ende |  | M | E/A | Ende des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="4d92d9dd-1875-4bd7-8737-99b4239dfb0d"><ac:plain-text-body><![CDATA[ | Auswahl |  | M | E[0:n] | Kennzeichen ob der gewählte Parameter in die Gruppe aufgenommen werden soll | Checkbox | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="4bf9c9cd-e667-4466-b607-10f42399e319"><ac:plain-text-body><![CDATA[ | Call |  | M | A[0:n] | Siehe „[GINO-Task | Parametergruppe GINO-Task]", Attribut „call" | Drop-Down-Liste | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="73c7abdb-d685-4e5e-978f-b517f1b292be"><ac:plain-text-body><![CDATA[ | Uhrzeit-Ab |  | M | E[0:n] | Siehe „[GINO-Task | Parametergruppe GINO-Task]", Attribut „Uhrzeit-Ab"  ]]></ac:plain-text-body></ac:structured-macro>  Vorbelegung „00:00"   Das Feld bleibt ausgegraut wenn Auswahl = false | Gültige Uhrzeit |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="5f43f6cc-0930-47ea-86a2-77ef049c7147"><ac:plain-text-body><![CDATA[ | Uhrzeit-Bis |  | M | E[0:n] | Siehe „[GINO-Task | Parametergruppe GINO-Task]", Attribut „Uhrzeit-Bis"  ]]></ac:plain-text-body></ac:structured-macro>  Vorbelegung „00:00"   Das Feld bleibt ausgegraut wenn Auswahl = false | Gültige Uhrzeit |
|  |  |  |  |  |  |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="896bc5ae-18ba-4f9e-92e5-a1cc10e1dc98"><ac:plain-text-body><![CDATA[ | Intervall |  | K | E[0:n] | Siehe „[GINO-Task | Parametergruppe GINO-Task]", Attribut „Intervall"  ]]></ac:plain-text-body></ac:structured-macro>  Das Feld bleibt ausgegraut wenn Auswahl = false |  |

*Hinweis: für den Fall „*GINO-Gruppe Task anzeigen" *sind alle Felder als reine Ausgabefelder ausgegraut dargestellt.*