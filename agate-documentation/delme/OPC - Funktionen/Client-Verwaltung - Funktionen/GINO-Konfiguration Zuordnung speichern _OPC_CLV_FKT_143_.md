# GINO-Konfiguration Zuordnung speichern (OPC_CLV_FKT_143)

# \_Toc172190086Vorbedingungen

Keine

# \_Toc172190087Beschreibung

Für Konfigurationsparameter mit crontab Einträgen (Parameter die mit START\_ oder CHECK\_ beginnen) wird die Syntax der übergebenen Parameterwerte überprüft. Die Einträge müssen die Form, wie in KALVE\_FKT\_009 beschrieben, haben. Sollten diese ungültig sein, wird mit der Fehlermeldung GUI-025 abgebrochen.
  
Anhand der in den Eingangsdaten übergebenen GINO-Gruppen-ID werden in der Entität „GINO-Konfiguration-Zuordnung" gespeicherten Einträge mit Referenzen auf den entsprechenden Eintrag in „GINO-Gruppe" ermittelt, und aus der Datenbank gelöscht.
  
Für jeden in den Eingangsdaten übergebenen Eintrag in der „GINO-Konfiguration-Parameter" Liste wird ein neuer Eintrag in „GINO-Konfiguration-Zuordnung" mit Refernzen auf den Eintrag in „GINO-Gruppe" (via GINO-Gruppen-ID) und auf den Eintrag in „GINO-Konfiguration" angelegt:

* Die Attribute „USE\_DEFAULT" und „REMOVE" werden entsprechend den Eingangsdaten gesetzt

Ist „Default" in den Eingangsdaten („GINO-Konfiguration-Parameter") auf „true" gesetzt, dann wird das Attribut „VALUE" nicht versorgt ansonsten wird der „VALUE" auf den übergebe

* nen „Wert" gesetzt

# \_Toc172190088Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| „GINO-Konfiguration-Parameter" Liste | Siehe „GINO-Konfiguration-Parameter", Liste kann auch leer sein. |  | **M** |

# \_Toc172190089Ausgangsdaten

Keine.

# \_Toc172190090Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI\_Message\_025GUI-025 | Die eingegebenen Parameter <Parameter1, Parameter2, …> sind ungültig. Die Konfiguration wurde nicht übernommen. | Message | Error |