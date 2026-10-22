# SW-Zertifikat Details (OPC_VPV_GUI_018)

Anzeigename: SW-Zertifikat Detailinformationen \[SW-Zertifikat\]
Anzeige der Detaildaten eines SW-Zertfikats und Optionen zum aktivieren / deaktivieren des aktuellen Zertifikates.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Sperren | Anzeigen der Maske „Rückfrage (OPC\_GEN\_GUI\_004)"   * Bei BestätigungAusführen der Funktion „SW-Zertifikat sperren / entsperren (OPC\_VPV\_FKT\_009)" mit dem Sperren Kennzeichen.Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" * Bei AblehnungAnzeige der Maske „SW-Zertifikat Details (OPC\_VPV\_GUI\_018)"     Angezeigt wenn das SW-Zertifikat aktiv ist. |
| Entsperren | Anzeigen der Maske „Rückfrage (OPC\_GEN\_GUI\_004)"   * Bei BestätigungAusführen der Funktion „SW-Zertifikat sperren / entsperren (OPC\_VPV\_FKT\_009)" mit dem Entsperren Kennzeichen.Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" * Bei AblehnungAnzeige der Maske „SW-Zertifikat Details (OPC\_VPV\_GUI\_018)"     Angezeigt wenn das SW-Zertifikat nicht aktiv ist. |
| Zurück | Anzeigen der Maske „SW-Zertifikatsliste (OPC\_VPV\_GUI\_017)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A | Die Nummer des Vertragspartners | Numerisch |
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