# SW-Zertifikat Hochladen (OPC_VPV_GUI_014)

Anzeigename: SW-Zertifikat bereitstellen \[SW-Zertifikat\]
Einstiegsmaske zur Bereitstellung eines neuen SW-Zertifikates für einen Vertragspartner.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Bereitstellen | Aufruf der Funktion „SW-Zertifikat bereitstellen (OPC\_VPV\_FKT\_006)"   Im Erfolgsfall: Anzeige der Maske „SW-Zertifikat Überprüfen (OPC\_VPV\_GUI\_015)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| SW-Zertifikat | M | E | Datei mit dem Zertifikat. | Dateiupload |
| Vertragspartnernummer | M | E | VPNR eines existierenden Vertragspartners | Numerisch |