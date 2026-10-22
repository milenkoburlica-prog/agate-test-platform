# Chipkarten Suchkriterien (OPC_CKV_GUI_002)

Anzeigename: Karte verwalten \[Suchen\]
In dieser Maske können die Kriterien für die Suche nach Chipkarten eingegeben werden. Beim ersten Aufruf aus dem Startdialog sind alle Felder mit ihren default Werten belegt.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Führt die Funktion „Karten suchen (OPC\_CKV\_FKT\_001)" aus.   Im Erfolgsfall: Anzeige der Maske „Chipkarten Ergebnis Liste (OPC\_CKV\_GUI\_003)"   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Karte | M | E | Kartentyp nach dem gesucht werden soll. | „e-card", „o-card" |
| Card-ID | K | E | Die Id der Karte. | Numerisch |
| SV-/VP-Nummer | K | E | Nimmt entweder eine SV-Nummer oder eine VP-Nummer als Eingabe an. Das gewählte Format hängt von der Einstellung unter dem Feld Karte ab. „e-card" erlaubt nur SV-Nummern, „o-card" VP-Nummern. | Numerisch |
| Kartenfolgenummer | K | E |  | Numerisch |
| CIN | K | E |  | Numerisch |
| Status | K | E | Mögliche Werte   * 04 Produktion fehlgeschlagen * 12 Endgültige Sperre fehlerhaft * 23 Vorläufige Sperre fehlerhaft * 33 Sperre storniert fehlerhaft | Auswahl |