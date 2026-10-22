# Komponenten Liste (OPC_VPV_GUI_012)

Anzeigename: Komponentenliste \[Komponentenliste\]
Anzeige der Liste aller verügbarer Komponenten. Bei Aufruf der Maske wird die Funktion „[Komponenten ermitteln (OPC\_VPV\_FKT\_013)|Komponenten ermitteln (OPC\_VPV\_FKT\_013)]" ausgeführt und das Ergebnis in der Liste aufbereitet.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Komponente ändern | Im Erfolgsfall: Aufruf der Maske „Komponente ändern (OPC\_VPV\_GUI\_013)" für die durch „Auswahl" festgelegte Komponente.   Im Fehlerfall: Ausgabe der Fehlermeldung (nichts ausgewählt) |
|  |  |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| pro Komponente |  |  |  |  |
| Auswahl | M | E | Listenauswahl Element | RadioButton |  |
| Komponente | M | A | Name der Komponente | Alphanumerisch |  |
| Sperre möglich | M | A | Indikator ob diese Komponente gesperrt ist. | Checkbox |  |
| Pilot möglich | M | A | Indikator ob diese Komponente ein Pilot ist. | Checkbox |  |
| Voll Ab | M | A | Startdatum des Vollbetriebs | ‚TT.MM.JJJJ' |  |
| Pilot Ab | M | A | Startdatum des Pilotbetriebs | ‚TT.MM.JJJJ' |  |
| Beschreibung | M | A | Komponentenbeschreibung | Alphanumerisch |  |