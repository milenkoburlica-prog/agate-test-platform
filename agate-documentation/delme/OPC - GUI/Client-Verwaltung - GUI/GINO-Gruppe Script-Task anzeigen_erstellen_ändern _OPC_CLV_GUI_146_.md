# GINO-Gruppe Script-Task anzeigen_erstellen_ändern (OPC_CLV_GUI_146)

Anzeigename: GINO-Gruppe Script Task anlegen \[Anlegen\] oder
Anzeigename: GINO-Gruppe Script Task ändern \[Ändern\]
Anzeigename: GINO- Gruppe Script Task anzeigen \[Anzeigen\]
\\
In dieser Maske wird eine GINO-Script Task Gruppe angezeigt, eine neue Gruppe erstellt oder eine bestehende Gruppe geändert.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
|  |  |
| Gruppe Speichern | Speichern der neu angelegten Gruppe oder geänderten GruppeAufruf der Funktion „GINO-Gruppe speichern (OPC\_CLV\_FKT\_142)" sowie „GINO-Script Zuordnung speichern (OPC\_CLV\_FKT\_166)".   Im Fehlerfall: Ausgabe der Fehlermeldung.   Ist nur für „Gruppe anlegen" oder „Gruppe ändern" sichtbar. |
| Gruppe Bearbeiten | Ist nur für „Gruppe anzeigen" sichtbar |
| Gruppe Löschen | Löschen einer Gruppe,   Ist nur für „Gruppe anzeigen" sichtbar.   Aufruf der Funktion „GINO-Script Gruppe löschen (OPC\_CLV\_FKT\_167)" nach einer Bestätigungsabfrage und anschließend Anzeigen der Maske   „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)". |
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
| Skript |  | M | E/A | Siehe „GINO-Script-Task", Attribut „Skript"   Die Auswahl an bekannten Scripten wird durch den Aufruf von „GINO-Scripte ermitteln (OPC\_CLV\_FKT\_168)" ermittelt.     Das Feld bleibt ausgegraut wenn Auswahl = false | Drop-Down-Liste |
| Uhrzeit-Ab |  | M | E/A | Siehe „GINO-Script-Task"", Attribut „Uhrzeit-Ab"   Vorbelegung „00:00"   Das Feld bleibt ausgegraut wenn Auswahl = false | Gültige Uhrzeit |
| Uhrzeit-Bis |  | M | E/A | Siehe „GINO-Script-Task"", Attribut „Uhrzeit-Bis"   Vorbelegung „00:00"   Das Feld bleibt ausgegraut wenn Auswahl = false | Gültige Uhrzeit |
| Intervall |  | K | E/A | Siehe „GINO-Script-Task"", Attribut „Intervall"   Das Feld bleibt ausgegraut wenn Auswahl = false |  |

*Hinweis: für den Fall „*GINO-Gruppe Script-Task anzeigen" *sind alle Felder als reine Ausgabefelder ausgegraut dargestellt.*