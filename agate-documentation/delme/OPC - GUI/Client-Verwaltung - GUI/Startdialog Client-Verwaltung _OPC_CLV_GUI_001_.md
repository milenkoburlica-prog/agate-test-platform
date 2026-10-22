# Startdialog Client-Verwaltung (OPC_CLV_GUI_001)

Im Startdialog des Menüpunktes „Client-Verwaltung" werden die Administrationsfunktionen der Client-Verwaltung ausgegeben.  
Allgemeine Beschreibung:   
Für die neuen Masken, (>100) ist folgendes einheitliches Verhalten umgesetzt:

* Alle Masken haben einen Button „Schliessen" (ganz rechts) welcher wieder den Startdialog (ohne Maske) anzeigt.
* Die Masken haben einen Button "Zurück" (zweiter Button von rechts), welche im Normalfall die zuvor aufgerufene Maske anzeigt.
  + Falls der Aufruf direkt vom Menü erfolgt, so fehlt dieser Button.
  + Bei der Erfolgsmeldung fehlt der Button. (Nur „Anzeigen" und „Schliessen" möglich).
  + Die angezeigte Maske ist nicht immer die tatsächlich zuletzt angezeigte Maske, da manchmal intern eine Funktionalität aufgerufen wird, welche dann bei Zurück angezeigt wird. Z.B. wird beim Button Vorgänger der Maske „die Maske „Stammdaten Detailinformationen (OPC\_CLV\_GUI\_103)"" intern die Suche aufgerufen. D.h. der Button Zurück, zeigt dann die die Maske „Stammdaten suchen (OPC\_CLV\_GUI\_101)" an.
* Jede Funktion die den Datenbestand ändert, ist durch eine Abfrage zu bestätigen. Die Abfrage wird oberhalb der Maske mit den Daten angezeigt.
* Nach Ändern von Daten wird eine sinnvolle Erfolgsmeldung (diese wird im PH nicht genau definiert)) angezeigt. Danach kann man wieder zur Anzeige der Daten wechseln („Button Anzeige").

Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Provider | Link auf die Maske „Provider (OPC\_CLV\_GUI\_089)" |
| Client Stammdaten | Nicht aktivierbare Gruppe für folgende 4 Einträge |
| Exot anlegen | Link auf die Maske „Stammdaten Detailinformationen (OPC\_CLV\_GUI\_103)" zur Eingabe von Daten |
| Stammdaten | Link auf die Maske „Stammdaten suchen (OPC\_CLV\_GUI\_101)" |
| Anschluss | Link auf die Maske „Anschluss suchen (OPC\_CLV\_GUI\_104)" |
| GINO | Link auf die Maske „Gino suchen (OPC\_CLV\_GUI\_111)" |
| GINO Gruppe | Nicht aktivierbare Gruppe für folgende 3 Einträge |
| GINO-Konfiguration | Link auf die Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| GINO-Firmware | Link auf die Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| GINO-Task | Link auf die Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| GINO-Script | Link auf die Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| GINO-Firmware | Nicht aktivierbare Gruppe für folgende 2 Einträge |
| Firmwareliste ausgeben | Link auf die Maske „GINO-Firmwareliste ausgeben (OPC\_CLV\_GUI\_150)" |
| Firmware hochladen | Link auf die Maske „GINO-Firmware hochladen (OPC\_CLV\_GUI\_151)" |
| GINO-Script | Nicht aktivierbare Gruppe für folgende 2 Einträge |
| Scriptliste ausgeben | Link auf die Maske „GINO-Scriptliste ausgeben (OPC\_CLV\_GUI\_152)" |
| Script hochladen | Link auf die Makse „GINO-Script hochladen (OPC\_CLV\_GUI\_153)" |
| GINO-Sperre |  |
| Gino Sperrliste ausgeben | Link auf die Maske „GINO-Sperrliste ausgeben (OPC\_CLV\_GUI\_084)" |
| Gino sperren | Link auf die Maske „GINO mit Seriennummer sperren (OPC\_CLV\_GUI\_085)" |
| Gino Gerät exportieren | Link auf die Maske „GINO mit Seriennummer für andere Instanz exportieren (OPC\_CLV\_GUI\_086)" |
| Home | Link auf das Hauptmenü OPC (OPC\_GEN\_GUI\_001) |