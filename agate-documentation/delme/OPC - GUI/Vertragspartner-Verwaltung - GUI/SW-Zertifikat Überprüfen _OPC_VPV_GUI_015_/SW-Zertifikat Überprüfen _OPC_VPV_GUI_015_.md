# SW-Zertifikat Überprüfen (OPC_VPV_GUI_015)

Anzeigename: SW-Zertifikat bereitstellen \[Prüfen\]
Anzeigemaske für die Überprüfung des Hochgeladenen Zertifikates.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Speichern | Aufruf der Funktion „SW-Zertifikat importieren (OPC\_VPV\_FKT\_024)"   Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A |  | Numerisch |
| Zuname | M | A |  | Alphanumerisch |
| Seriennummer | M | A |  | Numerisch |
| Seriennummer (HEX) | M | A |  | Alphanumerisch |
| Aussteller | M | A |  | Alphanumerisch |
| SHA1-Fingerprint | M | A |  | Alphanumerisch |
| SHA256-Fingerprint | M | A |  | Alphanumerisch |
| Gültigkeitsbeginn | M | A |  | ‚TT.MM.JJJJ' |
| Gültigkeitsende | M | A |  | ‚TT.MM.JJJJ' |
| Aktiv-KZ | M | A |  | Checkbox |
| Algorithmus | M | A |  | Alphanumerisch |
| DN | M | A |  | Alphanumerisch |