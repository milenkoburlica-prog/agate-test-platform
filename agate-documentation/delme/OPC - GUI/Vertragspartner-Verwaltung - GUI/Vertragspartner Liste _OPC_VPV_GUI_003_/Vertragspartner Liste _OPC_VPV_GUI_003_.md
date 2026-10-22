# Vertragspartner Liste (OPC_VPV_GUI_003)

Anzeigename: Vertragspartner \[Suchergebnis\]
Die Maske listet alle übergebenen Vertragspartner auf.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Ausgewählten Vertragspartner anzeigen | Anzeige der Maske: „Vertragspartner Details (OPC\_VPV\_GUI\_004)" für den gewählten Vertragspartner.   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Zurück | Anzeigen der Maske „Vertragspartner Suchen (OPC\_VPV\_GUI\_002) |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Treffer | M | A | Anzahl der Treffer | Numerisch |  |
| Pro Vertragspartner |  |  |  |  |
| Auswahl | M | E | Auswahlelement für die Aktion „Ausgewählten Vertragspartner anzeigen" | RadioButton |  |
| VP Nr. | M | E | Link auf die Maske: „Vertragspartner Details (OPC\_VPV\_GUI\_004)" für diesen Vertragspartner | Link, Numerisch |  |
| Zuname | M | A |  | Alphanumerisch |  |
| Vorname | M | A |  | Alphanumerisch |  |
| Plz | M | A |  | Numerisch |  |
| Ort | M | A |  | Alphanumerisch |  |
| BLD | M | A |  | Numerisch |  |
| suspendiert | M | A | Indikator ob der Vertragspartner suspendiert ist, oder nicht. | Checkbox |  |