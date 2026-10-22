# Nachricht erstellen (OPC_MES_GUI_002)

Anzeigename: Nachricht erstellen \[Nachricht erstellen\]
Eingabemaske zum Erfassen einer neuen Nachricht.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Prüfen | Aufruf der Funktion „"  Im Erfolgsfall: Anzeige der Maske „"  Im Fehlerfall: Ausgabe der Fehlermeldung. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Meldungsname | M | E | Bezeichner der Meldung   Angezeigter Hinweis: „*Achtung: Diese Information wird beim GDA ausgegeben!"* | alphanumerisch |
| Meldungstext | M | E | Vollständiger Text der Meldung | Alphanumerisch, Mehrzeilig |
| Gültig von | M | E |  | ‚TT.MM.JJJJ'  ‚TT.MM.JJJJ HH:mm' |
| Gültig bis | M | E |  | ‚TT.MM.JJJJ'  ‚TT.MM.JJJJ HH:mm' |
| Nachricht ungefiltert an alle Vertragspartner versenden? | M | E | Bei Aktivierung werden alle Filter-Eingaben deaktiviert und für die weitere Verarbeitung ignoriert. | Checkbox |
| Nachricht nicht an aktive Sessions versenden? | M | E |  | Checkbox |
| Vertragspartner-Filter |  |  | Anzeigeblock, optisch abgegrenzt |  |
| Vertragspartnernummer | K | E |  | Numerisch |
| Dateiname | K | E | Dateiupload für eine Auswahlliste an VP Nummern im CSV Format. | Dateiupload |
| Zuname | K | E |  | Alphanumerisch |