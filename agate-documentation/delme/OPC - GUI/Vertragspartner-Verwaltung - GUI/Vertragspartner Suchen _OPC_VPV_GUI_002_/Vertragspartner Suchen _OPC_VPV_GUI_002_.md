# Vertragspartner Suchen (OPC_VPV_GUI_002)

Anzeigename: Vertragspartner Suchen \[Suche\]
Diese Maske bietet Eingabefelder für die Suche nach Vertragspartnern.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Suchen | Aufruf der Funktion „Vertragspartner suchen (OPC\_VPV\_FKT\_001)"   Im Erfolgsfall Weiterleitung auf die Maske „Vertragspartner Liste (OPC\_VPV\_GUI\_003)".   Im Fehlerfall: Ausgabe der Fehlermeldung. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | K | E | Die VPNR eines Vertragspartners. | Numerisch |
| Zuname | K | E |  | Alphanumerisch |
| Vorname | K | E |  | Alphanumerisch |
| PLZ | K | E |  | Numerisch |
| Suspendierung | M | E | Mögliche Werte:   * alle * suspendiert * nicht suspendiert | Auswahl |