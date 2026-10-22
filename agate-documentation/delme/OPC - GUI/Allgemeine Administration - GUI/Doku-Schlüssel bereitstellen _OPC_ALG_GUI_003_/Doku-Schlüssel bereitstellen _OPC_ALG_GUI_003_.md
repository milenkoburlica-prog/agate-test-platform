# Doku-Schlüssel bereitstellen (OPC_ALG_GUI_003)

Anzeigename: Doku-Schlüssel bereitstellen \[Doku-Schlüssel\]
Die Maske dient dem Upload eines Doku-Schlüssels und dessen Zuordnung den unten angegebenen Parametern.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Bereitstellen | Aufruf der Funktion „Doku-Schlüssel lesen (OPC\_ALLG\_FKT\_010)".   Im Erfolgsfall, Weiterleitung auf die Maske „Doku-Schüssel prüfen".   Im Fehlerfall, verbleib auf dieser Maske und ausgabe der Fehlermeldung. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Allgemeine Administration (OPC\_ALG\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Dateiname | M | E | Dateiupoad eines validen Zertifikates. | Dateiinput |
| Empfängertyp | M | E | Mögliche Werte:   * Psuedonymisierungsstelle * ÖGGH | Auswahl |
| Auftragstyp | M | E | Mögliche Werte:   * Datenblätter der Vorsorgeuntersuchung * Datenblätter DMP * Dokumentationsblätter BKF | Auswahl |
| Schüsseltyp | M | E | Mögliche Werte:   * Sensibles Objekt * Identifikator * Standort | Auswahl |