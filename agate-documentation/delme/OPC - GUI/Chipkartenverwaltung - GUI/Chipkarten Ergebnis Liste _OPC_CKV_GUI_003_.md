# Chipkarten Ergebnis Liste (OPC_CKV_GUI_003)

Anzeigename: CKV-Auftragsliste \[Suchergebnis\]
Ausgabe einer Liste an Chipkarten für die weitere Auswahl.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Details ausgeben | Anzeige der Maske „Chipkarten Details (OPC\_CKV\_GUI\_004)" für die gewählte Karte an.   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Karte ausgewählt). |
| Zurück | Anzeige der Maske „Chipkarten Suchkriterien (OPC\_CKV\_GUI\_002)" |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Treffer | M | A | Anzahl der gefundenen Karten | Numerisch |  |
| Karte | M | A | Ausgabe des Suchparameters „Karte", bezeichnet den Kartentyp nach dem gesucht wurde. | „e-card", „o-card" |  |
| Card-ID | K | A | Ausgabe des Suchparameters „Card-ID" | Numerisch |  |
| SV-/VP-Nummer | K | A | Ausgabe des Suchparameters „SV-/VP-Nummer) | Numerisch |  |
| Kartenfolgenummer | K | A | Ausgabe des Suchparameters „Kartenfolgenummer" | Numerisch |  |
| CIN | K | A | Ausgabe des Suchparameters „CIN" | Numerisch |  |
| Status | K | A | Ausgabe des Suchparameters „Status" | Numerisch |  |
| Pro gefundener Karte. |  |  |  |  |
| Auswahlfeld | M | E | Legt die Karte fest auf die Button-Aktionen wirken. | RadioButton |  |
| SV-/VP-Nummer | M | A | Link zu „Chipkarten Details (OPC\_CKV\_GUI\_004)" | Numerisch |  |
| Kartenfolgenummer | M | A |  | Numerisch |  |
| Card-ID | M | A |  | Numerisch |  |
| CIN | M | A |  | Numerisch |  |
| Status | M | A |  | Numerisch |  |
| Datum | M | A |  | ‚TT.MM.JJJJ HH:mm' |  |