# GINO-Gruppe Firmware anzeigen_erstellen_ändern (OPC_CLV_GUI_143)

Anzeigename: GINO-Gruppe Firmware anlegen \[Anlegen\] oder
Anzeigename: GINO-Gruppe Firmware ändern \[Ändern\]
Anzeigename: GINO- Gruppe Firmware anzeigen \[Anzeigen\]
\\
In dieser Maske wird eine GINO-Firmware Gruppe angezeigt, eine neue Gruppe erstellt oder eine bestehende Gruppe geändert.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
|  |  |
| Gruppe Speichern | Speichern der neu angelegten Gruppe oder geänderten GruppeAufruf der Funktion „" sowie „". Im Fehlerfall: Ausgabe der Fehlermeldung.  Ist nur für „Gruppe anlegen" oder „Gruppe ändern" sichtbar. |
| Gruppe Bearbeiten | Ist nur für „Gruppe anzeigen" sichtbar.  Eine allgemeine Gruppe mit "Gültig Bis" in der Vergangenheit kann nicht verändert werden. |
| Gruppe Löschen | Löschen einer Gruppe,  Ist nur für „Gruppe anzeigen" sichtbar.  Aufruf der Funktion „" nach einer Bestätigungsabfrage und anschließend Anzeigen der Maske  „".  Eine allgemeine Gruppe kann nur gelöscht werden, wenn "Gültig bis" in der Vergangenheit liegt. |
|  |  |
| Gruppenmitglieder verwalten | Gruppenmitglieder verwalten, ist nur aktiv für GINO-Gruppen-Mitgliedertyp „SPEZIELL".Aufruf der Funktion „" und anschließend Anzeige der Maske „". |
| Zurück | Anzeige der GruppenlisteAnzeigen der Maske „" |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung () |

| **Maskenfeld** | **DB –**  **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Gruppenname |  | M | E/A | Name der Gruppe | 30 alphanumerische Zeichen |
| Mitgliedertyp |  | M | E/A | Siehe „GINO-Gruppen-Mitgliedertyp" | Drop-Down-Liste |
| Filter Seriennummer | Seriennummer | K | E | Filter für Seriennummer | 1000 alphanumerische Zeichen |
| Filter Firmware | Firmware | K | E | Filter für Firmware | 1000 alphanumerische Zeichen |
| Anzahl Mitglieder |  | M | A | Anzahl der Mitglieder der Gruppe |  |
| Gültig ab |  | M | E/A | Start des Gültigkeitszeitraums im Format  ‚TT.MM.JJJJ HH:mm'  *Hinweis: für eine allgemeine Gruppe kann das Datum nur beim Neuanlegen oder beim Ändern der zweiten, bis "31.12.3999" gültigen, Gruppe eingegeben werden*. *Dieses Datum wird dann wird dann für die erste relevante allgemeine Gruppe als "Gültig bis" gesetzt.* |  |
| Gültig bis |  | M | E/A | Ende des Gültigkeitszeitraums im Format  ‚TT.MM.JJJJ HH:mm' |  |
| Firmware Version | Firmware-Version | M | E/A | Auswahl an bekannten Software Versionen durch Aufruf von „" ermittelt. | Drop-Down-Liste |

*Hinweise:*   
*Für den Fall „*GINO-Gruppe Firmware anzeigen" *sind alle Felder als reine Ausgabefelder ausgegraut dargestellt.*   
*Eine allgemeine Gruppe kann nur dann ausgestellt werden, falls es nur eine relevante (d.h. Startdatum nach aktuellem Datum) allgemeine Gruppe gibt, in diesem Fall ist "Gültig bis" mit "31.12.3999" vorgegeben und das eingegebene Startdatum (muss in Zukunft liegen) wird als "Gültig bis" der bestehenden relevanten Gruppe gesetzt. Mit dieser Einschränkung und den Einschränkungen bei "Gruppe bearbeiten" und "Gruppe löschen" kann man im Zuge des ROW bereits vorab die neue Defaultfirmware-Version festlegen, man kann aber nicht mehrere (gleichzeitig gültige) Defaultfirmware-Versionen setzen.*