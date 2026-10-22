# GINO-Konfigurationsparameter erstellen (OPC_CLV_FKT_154)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion legt einen neuen GINO-Konfigurationsparameter an, indem ein neuer Eintrag in der Entität "CL\_GINO\_CONFIG" angelegt wird. Die Werte Name, Standardwert, Beschreibung werden übernommen. Als "last Change" wird das aktuelle Datum verwendet. Als User-ID die ID des angemeldeten Users.

Sollte der Parameter "Zur allgemeinen Konfiguration hinzufügen" ausgewählt sein, wird dieser Parameter mit dem Standardwert zu jeder allgemeinen Konfigurationsgruppe hinzugefügt. Eintrag in der Entität "CL\_GINO\_KONFIG\_ZUORDNUNG"

Sollte ein Fehler auftreten, wird die Meldung GUI-061 ausgegeben, ansonsten die Meldung GUI-060.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des Parameters | (max. 100 Zeichen) | **M** |
| Standardwert | Der Name des Parameters | (max. 4000 Zeichen) | **M** |
| Beschreibung | Die Beschreibung des Parameters | (max. 1000 Zeichen) | **M** |
| Zur allgemeinen Konfiguration hinzufügen | Auswahl, ob der Wert zu den allgemeinen Gruppen hinzugefügt wird oder nicht. | Ja/Nein | **M** |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-060 | Der GINO-Konfigurationsparameter <PARAMETER> wurde erfolgreich gespeichert. | Message | Info |
| GUI-061 | Der Gino GINO-Konfigurationsparameter <PARAMETER> konnte nicht gespeichert werden. (<GRUND>) | Message | Error |
| CLV-050 | Der Gino Parameter <PARAMETER> wurde vom Nutzer <USER\_ID> erstellt. | Log | Info |