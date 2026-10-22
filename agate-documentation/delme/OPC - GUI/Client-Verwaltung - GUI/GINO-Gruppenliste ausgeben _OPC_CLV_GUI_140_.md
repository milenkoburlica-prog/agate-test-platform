# GINO-Gruppenliste ausgeben (OPC_CLV_GUI_140)

Anzeigename für GINO-Gruppenliste Konfiguration: GINO-Gruppenliste Konfiguration [GINO-Konfiguration]  
Anzeigename für GINO-Gruppenliste Firmware: GINO-Gruppenliste Firmware [GINO-Firmware]  
Anzeigename für GINO-Gruppenliste Task: GINO-Gruppenliste Task [GINO-Task]  
Anzeigename für GINO-Gruppenliste Script: GINO-Gruppenlist Script [GINO-Script]  
  
In dieser Maske werden Konfigurations-, Firmware- bzw. Task Gruppen angezeigt.  
Beim Aufruf dieser Maske wird die Funktion “[GINO-Gruppenliste ermitteln (OPC\_CLV\_FKT\_140)](../../OPC%20-%20Funktionen/Client-Verwaltung%20-%20Funktionen/GINO-Gruppenliste%20ermitteln%20_OPC_CLV_FKT_140_.md)“ ausgeführt.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Radio-Button | Markierung der jeweiligen GINO-Konfiguration |
| Gruppen-ID | Die Gruppen-IDs werden als Link ausgeführt. Durch Auswahl des Links werden die Detailinformationen der gewählten Gruppe dargestellt.  Aufruf der Funktion   ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Konfiguration anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_142)“  bzw.  ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Firmware anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_143)“.  bzw.  ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_144)“  bzw.  ““ und anschließend Anzeige der Maske “GINO-Gruppe Script-Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_146)“ |
| Anzeigen | Die Detailinformation der gewählten Gruppe wird dargestellt.  Aufruf der Funktion   ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Konfiguration anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_142)“  bzw.  ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Firmware anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_143)“  bzw.  ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_144)“  bzw.  ““ und anschließend Anzeige der Maske “GINO-Gruppe Script-Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_146)“ |
| Neue Gruppe anlegen | Anlegen einer neuen Gruppe  Aufruf der Funktion ““ ohne Gruppen-ID und Anzeige der Maske  “GINO-Gruppe Konfiguration anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_142)“ bzw. GINO-Gruppe Firmware anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_143)  bzw. “GINO-Gruppe Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_144)“  bzw. “GINO-Gruppe Script-Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_146)“ |
| Neuen Parameter anlegen (Nur bei Konfiguration) | Anzeige der GUI Oberfläche [GINO-Konfigurationsparameter erstellen (OPC\_CLV\_GUI\_135)](GINO-Konfigurationsparameter%20erstellen%20_OPC_CLV_GUI_135_.md) |
| Parameter löschen (Nur bei Konfiguration) | Anzeige der GUI Oberfläche [GINO-Konfigurationsparameter löschen (OPC\_CLV\_GUI\_136)](GINO-Konfigurationsparameter%20l%C3%B6schen%20_OPC_CLV_GUI_136_.md) |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung () |

| **Maskenfeld** | **DB –**  **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| ID |  | M | A[0:n]  Siehe "GINO-Gruppen-ID" | Gruppen ID | numerisch |
| Gruppenname |  | M | A[0:n]  Siehe “[GINO-Gruppen-Name | Gruppenname | 30 alphanumerische Zeichen |
| Anzahl Mitglieder |  | M | A[0:n] | Anzahl der zu dieser Gruppe zugeordneten GINOs | numerisch |
| Mitgliedertyp |  |  |  | Siehe “GINO-Gruppen-Mitgliedertyp“ |  |