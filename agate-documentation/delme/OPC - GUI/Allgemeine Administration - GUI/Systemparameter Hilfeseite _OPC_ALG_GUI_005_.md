# Systemparameter Hilfeseite (OPC_ALG_GUI_005)

Anzeigename: Hilfe [<Parametername>]  
Diese Maske stellt die Informationen zu einem Parameter dar und wird nicht innerhalb des OPCs geöffnet, sondern in einem zusätzlichen Tab bzw. Fenster.

Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Bearbeiten | Es werden die Parameter "Beschreibung" und "Regular Expression" zu Eingabefeldern umgeschaltet |
| Speichern (nur nach dem Start von "Bearbeiten") | Es wird die Funktion [Systemparameter bearbeiten (OPC\_ALLG\_FKT\_004)](../../OPC%20-%20Funktionen/Allgemeine%20Administration%20-%20Funktionen/Systemparameter-Metainfo%20bearbeiten%20_OPC_ALLG_FKT_004_.md) mit den geänderten Werten aufgerufen. |
| Löschen | Es wird die Funktion [Systemparameter löschen (OPC\_ALLG\_FKT\_005)](../../OPC%20-%20Funktionen/Allgemeine%20Administration%20-%20Funktionen/Systemparameter%20l%C3%B6schen%20_OPC_ALLG_FKT_005_.md) aufgerufen. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Parametername | M | A | Der vollständige Name des Parameters. | Alphanumerisch |
| Defaultwert | K | A | Der Standardwert des Parameters, wenn vorhanden. Es ist jener Wert, der beim Anlegen für diesen Parameter eingetragen wurde. | Alphanumerisch |
| Beschreibung | M | E/A | Die Beschreibung des Parameters. Ausgabe einer entsprechenden Meldung, falls der Parameter leer ist. | Alphanumerisch (max. 1000 Zeichen) |
| Regular Expression | M | E/A | Die Regular Expression, mit der dieser Parameter validiert wird (insofern sie nicht leer gelassen wird).  Ausgabe einer entsprechenden Meldung, falls der Parameter leer ist. | (max. 1000 Zeichen) |