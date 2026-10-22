# Fotoinformation Details (OPC_CKV_GUI_008)

Anzeigename: Fotoinformation \[Details\]
Anzeige der Details zu einer einzelnen Fotoinformation.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Fotoinformation beenden | Führt die Funktion „Fotoinformation beenden (OPC\_CKV\_FKT\_007)" aus.   Im Erfolgsfall wird die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" angezeigt.   Im Fehlerfall Ausgabe der Fehlermeldung.. |
| Zurück | Anzeige der Maske „Fotoinformation Suchen (OPC\_CKV\_GUI\_007)" |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| VSNR | M | A | VSNR | Numerisch |
| Typ | M | A | Typ der Fotoinformation | „FA - Foto-Aviso"„FZ – Foto zwingend notwendig" |
| Einmeldezeitpunkt | M | A | Beginn der Fotoinformation | TT.MM.JJJJ |
| Sperre ab | K | A | Datum der KSE-Sperre | TT.MM.JJJJ |
| Sperre durch | K | A | Kennzeichen des Akteurs, der die Sperre getriggert hat. | „VP – Vertragspartner"„SD – Sachbearbeiter"„CK - CKV" |
| Meldung durch | K | A | Kennzeichen des Akteurs der Meldung | „SD – Sachbearbeiter"„CK – CKV"„OP – OPC" |
| ID Meldung | K | A | ID des Akteurs der Meldung | alphanumerisch |
| Hinweis | K | E/A | Freitextfeld   Hinweis: ein bestehender Eintrag kann beim Löschen überschrieben werden. |  |
| Neuer Ausstellungsantrag angefordert | M | A | Flag | Checkboc |