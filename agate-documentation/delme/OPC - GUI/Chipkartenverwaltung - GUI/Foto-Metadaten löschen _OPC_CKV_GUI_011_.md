# Foto-Metadaten löschen (OPC_CKV_GUI_011)

Anzeigename: Foto-Metadaten löschen \[Foto-Metadaten löschen\]
In dieser Maske können die Foto-Metadaten für eine Versicherungsnummer und wenn vorhanden für ein oder mehrere ausgewählte Register gelöscht werden.
Die folgenden Aktionen sind aktivierbar:

|  |  |  |
| --- | --- | --- |
| Foto-Metadaten suchen | Führt die Funktion „[ Foto-Metadaten suchen (OPC\_CKV\_FKT\_011) | Foto-Metadaten suchen (OPC\_CKV\_FKT\_011)]" aus.   Falls Metadaten gefunden wurden, werden die entsprechenden Register in einer Liste mit Checkbox zur Auswahl ausgegeben und das Eingabefeld, sowie die Aktion „Foto-Metadaten suchen" gesperrt und die Aktionen „Foto-Metadaten löschen" und „Abbrechen" freigegeben.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Foto-Metadaten löschen   (Initial gesperrt) | Führt die Funktion „[ Foto-Metadaten löschen (OPC\_CKV\_FKT\_012) | Foto-Metadaten löschen (OPC\_CKV\_FKT\_012)]" aus.   Im Erfolgsfall wird die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" angezeigt und die Aktion „Foto-Metadaten suchen" wieder freigeschalten, sowie die Aktionen „Foto-Metadaten löschen" und „Abbrechen" wieder gesperrt.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Abbrechen   (Initial gesperrt) | Setzt die Seite auf den Startzustand zurück. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| SV-Nummer | M | E | Sozialversicherungsnummer | Numerisch |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Checkbox | M | E | Auswahl zum Löschen | Checkbox |
| Register Kennzeichen |  | A | Kennzeichen des Registers in dem das Foto vorhanden ist. | Text |
| Ermittlungszeitpunkt |  | A | Zeitpunkt der Aktualisierung oder der Speicherung dieses Eintrags. | Text |
| Dokumenten Ausstellungsdatum |  | A | Ausstellungsdatum des Dokuments, aus dem das Foto verwendet wird. | Text |
| Beauskunftungs Enddatum |  | A | Datum bis zu dem das Foto beauskunftet wird. | Text |