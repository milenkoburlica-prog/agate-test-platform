# Anschluss Detailinformationen (OPC_CLV_GUI_106)

Anzeigename: Anschluss Detailinformationen (OPC\_CLV\_GUI\_106)  
Auf dieser Maske werden Detailinformationen der Anschlüsse angezeigt und geändert.   
Für die Anzeige sind die Felder nicht editierbar.Folgende Aktionen stehen zur Verfügung:

|  |  |
| --- | --- |
| Stammdaten anzeigen | Anzeigen der Maske „" für die dem Anschluss zugeordneten Stammdaten |
| Gino(s) anzeigen | Die Ginos des Anschlusses werden ermittelt.  Aufruf der Funktion „".(Suchebene Gino) Abhänig vom Ergebnis der Suche wird mit folgender Aktion fortgesetzt:   * kein Treffer: Anzeige einer Fehlermeldung „Kein Eintrag gefunden" * genau ein Treffer: Anzeigen der Maske „" * mehr als ein Treffer: Anzeigen der Maske „" (als Suchkriterium werden Name, Gruppe, Vertragspartnernummer und Ordinationsnummer, angezeigt) |
| Gina ausstatten | Für den Anschluss wird eine weitere Gina angelegt.  Aufruf der Funktion „GINA ausstatten (CV\_BAS\_FKT\_107)". und Anzeigen der zurückgelieferten Daten in der Maske „" für das Anlegen.Im Fehlerfall: Ausgabe der Fehlermeldung.  *Hinweis: Der Button wird nur dann angezeigt, falls KZ\_FREIGABE gesetzt ist.* |
| Ändern | Anzeigen dieser Maske zum Ändern von Daten  *Hinweis: Der Button wird nur dann angezeigt, falls KZ\_FREIGABE gesetzt ist.* |
| Freigeben | Aufruf der Funktion „Anschluss freigeben (CV\_BAS\_FKT\_105)"  Das Kennzeichen für die Freigabe wird gesetzt, die restlichen Felder werden unverändert angezeigt.  *Hinweis: Der Button wird nur dann angezeigt, falls KZ\_FREIGABE nicht gesetzt ist.* |
| Sperren | Aufruf der Funktion „Anschluss sperren (CV\_BAS\_FKT\_104)"  Das Kennzeichen für die Freigabe wird entfernt, die restlichen Felder werden unverändert angezeigt.  *Hinweis: Der Button wird nur dann angezeigt, falls KZ\_FREIGABE gesetzt ist.* |
| Löschen | Die Aktion muss bestätigt werden. („Wollen Sie den Eintrag wirklich löschen?"). Der Anschluss wird durch Aufruf der Funktion „Anschluss löschen (CV\_BAS\_FKT\_106)" gelöscht  Im Erfolgsfall: Ausgabe der Erfolgsmeldung und Anzeige der Maske „" .Im Fehlerfall: Ausgabe der Fehlermeldung. |

Für das Ändern/Anlegen der Daten sind bis auf Freigabestatus (immer gesetzt) alle Felder editierbar. Das Netz ist nur im Zuge eine Neuanlage änderbar.  
  
Folgende Aktionen stehen zur Verfügung

|  |  |
| --- | --- |
| Netz ändern | Falls das Netz geändert wird, erfolgt ein Aufruf der Funktion „Anschluss ausstatten (CV\_BAS\_FKT\_102" und die zurückgelieferten Daten werden angezeigt. |
| Speichern | Die Aktion muss bestätigt werden. („Wollen Sie die Änderung durchführen"). Die Änderungen werden durch Aufruf der Funktion „Anschluss speichern (CV\_BAS\_FKT\_103)" gespeichert.  Falls sich der Eintrag in „weitere Client-IPs" ändert, werden durch die Funktion auch Zuordnungen in der Entität „zusammenhängede IPs" angepasst, d.h. neue Ips werden eingetragen und nicht mehr aufgelistete IPs entfernt.  Im Erfolgsfall: Ausgabe der Erfolgsmeldung „Die Daten wurden erfolgreich geändert".Im Fehlerfall: Ausgabe der Fehlermeldung. |

Die Maske dient zur Verwaltung der Entität Stammdaten (CL\_ANSCHLUSS)

| **Maskenfeld** | **Zuordnung Entität Anschluss** | **Plausibilität** |
| --- | --- | --- |
| Netz | Anhand „Netz Id" wird aus der Entität Netz die Bezeichnug ermittelt | Drop-Down Menü im Änderunsmodus, ansonsten max 3 Zeichen |
| Client-IP | Client-IP | Max. 15 Zeichen (0-9.?\*) |
| Router-IP | Router-IP. falls die Router-IP nicht belegt ist, wird das Feld nicht angezeigt. | Max. 15 Zeichen (0-9.?\*) |
| Anlegezeitpunkt | Anlegezeitpunkt | Timestamp im Format „DD.M.YYYYY HH:MM" |
| Anschlussnummer | Anschlussnummer | 60 alphanumerische Zeichen |
| Provider | Provider | Drop-Down Menü im Änderunsmodus, ansonsten max 20 Zeichen |
| Callcentergruppen-Id | Callcenter ID | 2 alphanumerische Zeichen |
| Freigabestatus | KZ Freigabe | Checkbox" |
| Automatisch Löschen | Kennzeichen automatisch löschbar | Checkbox" |