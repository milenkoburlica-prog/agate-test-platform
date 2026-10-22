# Nachricht prüfen (OPC_MES_GUI_003)

Anzeigename: Nachricht Detail \[Nachricht\]
Anzeige der Erfassten Nachrichtendaten und der ermitteleten Adressaten.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Speichern | Aufruf der Funktion „"  Im Erfolgsfall: Anzeige der Maske „"  Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Zurück | Anzeige der Maske „" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Meldungsname | M | A | Bezeichner der Meldung   Angezeigter Hinweis: „*Achtung: Diese Information wird beim GDA ausgegeben!"* | Alphanumerisch |
| Meldungstext | M | A | Vollständiger Text der Meldung | Alphanumerisch |
| Gültig von | M | A |  | ‚TT.MM.JJJJ'  ‚TT.MM.JJJJ HH:mm' |
| Gültig bis | M | A |  | ‚TT.MM.JJJJ'  ‚TT.MM.JJJJ HH:mm' |
| Nachricht ungefiltert an alle Vertragspartner versenden? | M | A |  | Checkbox |
| Nachricht nicht an aktive Sessions versenden? | M | A |  | Checkbox |
| Anzahl Adressaten | M | A |  | Numerisch |
| Filter |  |  | Anzeigeblock, optisch abgegrenzt |  |
| Vertragspartnernummer | M | A |  | Alphanumerisch |
| Zuname | M | A |  | Alphanumerisch |
| Adressaten |  |  | Anzeigeblock, optisch abgegrenzt |  |
| Vertragspartner | M | A |  | Alphanumerisch |
| Zuname | M | A |  | Alphanumerisch |
| Ordinationsnummer | M | A |  | Alphanumerisch |
| Bundesland | M | A |  | Alphanumerisch |
| PLZ | M | A |  | Alphanumerisch |
| Ort | M | A |  | Alphanumerisch |
| Strasse | M | A |  | Alphanumerisch |
| Besitzverhältniscode | M | A |  | Numerisch |
| VP Session ID | M | A |  | Numerisch |