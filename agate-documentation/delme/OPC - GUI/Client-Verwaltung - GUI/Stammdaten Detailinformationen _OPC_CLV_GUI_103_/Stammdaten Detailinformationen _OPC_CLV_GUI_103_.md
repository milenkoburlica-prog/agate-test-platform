# Stammdaten Detailinformationen (OPC_CLV_GUI_103)

Anzeigename: Stammdaten Detailinformationen (OPC\_CLV\_GUI\_103)  
Auf dieser Maske werden Detailinformationen der Stammdaten angezeigt und geändert.  
Zum Anzeigen der Daten stehen folgende Aktionen zur Verfügung. In diesem Fall sind die Felder nicht editierbar.

|  |  |
| --- | --- |
| Ändern | Anzeigen dieser Maske zum Ändern/Anlegen von Daten.. |
| Ord. Übernehmen | Anzeige dieser Maske für „Ordination übernehmen".  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN nicht gesetzt ist.* |
| Übersiedlung Stornieren | Die vorbereitete Übersiedlung wird stoniert.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten ein Vorgänger eingetragen ist und das Kennzeichen KZ\_UEBERNOMMEN nicht gesetzt ist.*  Aufruf der Funktion „Übersiedlung stornieren (OPC\_CLV\_FKT\_117)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung. |
| Übernahme Stornieren | Die Übernahme wird stoniert.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN gesetzt ist.*  Aufruf der Funktion „Übernahme stornieren (OPC\_CLV\_FKT\_118)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung. |
| Anschluss anzeigen | Die Anschlüsse des Stammdatensatzes werden ermittelt.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN nicht gesetzt ist.*  Aufruf der Funktion „".(Suchebene Anschluss, Stammdaten-ID wird übergeben) Abhänig vom Ergebnis der Suche wird mit folgender Aktion fortgesetzt:   * kein Treffer: Anzeige einer Fehlermeldung „Kein Eintrag gefunden" * genau ein Treffer: Anzeigen der Maske „" * mehr als ein Treffer: Anzeigen der Maske „" (als Suchkriterium werden Vertragspartnernummer und Ordinationsnummer angezeigt) |
| Anschluss ausstatten | Für die Stammdaten wird ein weiterer Anschluss angelegt.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN nicht gesetzt ist.*  Aufruf der Funktion „Anschluss ausstatten (CV\_BAS\_FKT\_102)". und Anzeigen der zurückgelieferten Daten in der Maske „" für das Anlegen.Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Vorgänger | Die Daten des Vorgängers angezeigt.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERNOMMEN gesetzt ist.*  Es werden alle Stammdaten gesucht, welche die aktuellen Stammdaten als Nachfolger eingetragen und das Kennzeichen KZ\_UEBERGEBEN gesetzt haben.  Im Erfolgsfall: Ausgabe der Daten des Vorgängers in dieser Maske oder Ausgabe einer Liste von Stammdaten..Im Fehlerfall (nur bei ungültiger Datenkonstellation): Ausgabe der Fehlermeldung. |
| Nachfolger | Die Daten des Nachfolgers werden angezeigt.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN gesetzt ist und ein Nachfolger eingetragen.*  Im Erfolgsfall: Ausgabe der Daten des Nachfolgers in dieser Maske.Im Fehlerfall ((nur bei ungültiger Datenkonstellation): Ausgabe der Fehlermeldung. |

Für das Ändern/Anlegen der Daten sind folgende Eingaben möglich :

Bei Zurodnungstyp Exot sind alle Maskenfelder bis auf Zuordnungstyp und die Felder für Vorgänger, Nachfolger,"Provider Export Aktion" und „Callcenter Export Aktion" editierbar.

Bei Standard sind folgende weitere Felder nicht editierbar: Ausprägung, Ordination aktiv" (Checkbox), Ordinationsnummer, Anrede, Titel Vorne, Vorname, Zuname, Titel Hinten, Straße, PLZ, Ort, Bezirk, Bundeslandcode.  
Die VPNR sollte nur dann geändert werden, wenn ein Stempelverlust vorangegangen ist. Ein entsprechender Hinweis wird ausgegeben.  
  
Hinweis: Das Ändern der Daten für den Export ist in folgenden 2 Fällen sinnvoll:

* Kennzeichen für den Export deaktivieren, fallls man einen für den Export vorgesehenen Datensatz nicht exportieren möchte. In diesem Fall kann man das Kennzeichen später wieder aktivieren. Das Aktivieren für einen nicht vorgesehenen Datensatz hat keine Auswirkungen (Z:B. wird ein Anschluss im EHI\_Netz auch dann nicht für den Providerexport vorgesehenen, falls man das Kennzeichen aktiviert.)
* Löschen es Exportdatums, um einen Datensatz erneut zu exportieren. Das Setzen oder Ändern des Exportdatums hat keine fachlichen Auswirkungen (bei Änderungen von relevanten Daten, wird das Datum unabhängig von der Eingabe auf leer gesetzt).
* Datum "Providerdaten aktualisiert": Damit könnte man einen Datensatz in die Liste der aktualisierten Daten hinzufügen oder entfernen. Falls man (zusätzlich)  ein für den Export relevantes Datum ändert, wird dieses Datum auf alle Fälle auf den aktuellen Wert gesetzt.

Folgende Aktionen stehen zur Verfügung:

|  |  |
| --- | --- |
| Speichern | Die Aktion muss bestätigt werden. („Wollen Sie die Änderung durchführen"). Die Änderungen werden durch Aufruf der Funktion „Stammdaten speichern (CV\_BAS\_FKT\_101)" gespeichert.  Im Erfolgsfall: Ausgabe der Erfolgsmeldung „Die Daten wurden erfolgreich geändert" Im Fehlerfall: Ausgabe der Fehlermeldung. |

Für die Übernahme einer Ordination. sind die Maskenfelder „Vorgänger VPNR" und „Vorgänger Ordinationsnummer" editierbarFolgende Aktion steht zur Verfügung

|  |  |
| --- | --- |
| Ord. übernehmen | Aufruf der Funktion „Ordination übernehmen(CV\_BAS\_FKT\_113)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung „Die Daten wurden erfolgreich geändert".Im Fehlerfall: Ausgabe der Fehlermeldung. |

Die Maske dient zur Verwaltung der Entität Stammdaten (CL\_STAMMDATEN)

| **Maskenfeld** | **Zuordnung Entität Stammdaten** | **Plausibilität** |
| --- | --- | --- |
| Zuordnungstyp | * KZ\_EXOT==1: Exot * sonst: Standard | „Exot", „Standard" |
| Vertragspartnernummer | VPNR | Max. 6 alphanumerische Zeichen (0-9a-zA-Z-.\_) |
| Ausprägung | Ausprägung Code | alphanumerische Zeichen |
| Ordinationsnummer | Ordinationsnummer | 2 numerische Zeichen |
| Ordinationaktiv? | KZ aktiv | Checkbox Mögliche Werte:  „aktiv", „nicht aktiv" |
| Anrede | Anredecode | „Herr", „Frau", „An" |
| Titel Vorne | Titel vorne | 30 alphanumerische Zeichen |
| Vorname | Vorname | 70 alphanumerische Zeichen |
| Zuname | Zuname | 70 alphanumerische Zeichen |
| Titel Hinten | Titel hinten | 30 alphanumerische Zeichen |
| Straße | Strasse | 60 alphanumerische Zeichen |
| PLZ | PLZ | 9 alphanumerische Zeichen |
| Ort | Ort | 40 alphanumerische Zeichen |
| Bezirk | Politischer Bezirk | 2 alphanumerisches Zeichen |
| Bundeslandcode | Bundeslandcode | 2 alphanumerisches Zeichen |
| Vorgänger VPNR | Vorgänger: Anhand der eingetragenen Id, wird Vertragspartnernummer ermittelt | 6 alpha-numerische Zeichen (0-9a-zA-Z-.\_) |
| Vorgänger Ordinationsnummer | Vorgänger: Anhand der eingetragenen Id, wird Ordinationsnummer ermittelt | 2 numerische Zeichen |
| Vorgänger übernommen | KZ übernommen | „ausgewählt", „nicht ausgewählt" (Checkbox) |
| Nachfolger VPNR | Nachfolger: Anhand der eingetragenen Id, wird Vertragspartnernummer ermittelt | 6 alpha-numerische Zeichen (0-9a-zA-Z-.\_) |
| Nachfolger Ordninationsnummer | Nachfolger: Anhand der eingetragenen Id, wird Ordinationsnummer ermittelt | 2 numerische Zeichen |
| Nachfolger übergeben | KZ übergeben | „ausgewählt", „nicht ausgewählt" (Checkbox) |
| Telefonnummer | Telefonnummer | 60 alphanumerische Zeichen |
| KUMS | KUMS | 100 alphanumerische Zeichen |
| Providerdaten aktualisiert | Provider Export Änderung | Timestamp im Format „DD.M.YYYYY HH:MM" |
| Anschlussart | Anschlussart | "GIN",  "" |
| Provider-Export am | Provider Export am | Datum |
| Provider-Export Aktion | Provider Export Aktion | alphanumerische Zeichen |
| Callcenter Export | Kennzeichen Callcenterexport | Checkbox |
| Callcenter-Export Aktion | Callcenter Export am | alphanumerische Zeichen |
| Callcenter-Export am | Callcenter Export Aktion | Datum |
| Billing-Kennzeichen GINA | Billing KZ Gina | „SV", „VP", „KA" |
| Billing-Kennzeichen Karte | Billing KZ Karte | „SV", „VP", „KA" |