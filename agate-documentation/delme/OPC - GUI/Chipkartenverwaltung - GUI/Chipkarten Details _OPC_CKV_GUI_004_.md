# Chipkarten Details (OPC_CKV_GUI_004)

Anzeigename: Karte verwalten \[Detailinformationen\]
Anzeige der Details zu einer einzelnen Chipkarte.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Bemerkung erfassen | Anzeige der Maske „". |
| Endzustand setzen | Anzeige der Maske „".  Ist nur aktiv wenn die Karte das Flag EndStatusAllowed besitzt. |
| Zurück | Anzeige der Maske „" |
| Löschen | Anzeige der Maske "" wird nur für die e-cards mit Status 13 ("Quittung Endgültige Sperre OK") aktiv. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Treffer | M | A | Anzahl der gefundenen Karten | Numerisch |  |
| Karte | M | A | Kartentyp | „e-card", „o-card" |  |
| Card-ID | M | A | Karten ID | Numerisch |  |
| CIN | M | A |  | Numerisch |  |
| SV-/VP-Nummer | M | A | VPNR/VSNR | Numerisch |  |
| Kartenfolgenummer | M | A |  | Numerisch |  |
| Bemerkung | M | A |  | Alphanumerisch |  |
| Bemerkungsbearbeiter | M | A | Bemerkungsuser | Alphanumerisch |  |
| Bemerkungsdatum | M | A |  | ‚TT.MM.JJJJ HH:mm' |  |
| pro Statuseintrag |  |  |  |  |
| Statuscode | M | A | Status | Numerisch |  |
| Grundcode | K | A | Grund Code | Numerisch |  |
| Grundtext | K | A | Bemerkung | Alphanumerisch |  |
| Datum | M | A |  | ‚TT.MM.JJJJ HH:mm' |  |
| Bearbeiter | K | A | User | Alphanumerisch |  |