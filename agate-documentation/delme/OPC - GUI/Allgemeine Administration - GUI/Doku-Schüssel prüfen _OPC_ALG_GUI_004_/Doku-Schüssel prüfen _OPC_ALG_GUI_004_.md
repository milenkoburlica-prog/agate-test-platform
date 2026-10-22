# Doku-Schüssel prüfen (OPC_ALG_GUI_004)

Anzeigename: Doku-Schlüssel bereitstellen \[Prüfen\]
Maske zur Kontrolle des bereitgestellten Doku-Schlüssels durch den Benutzer vor dem Speichern.
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Speichern | Aufruf der Funktion „Doku-Schlüssel speichern (OPC\_ALLG\_FKT\_011)"   Im Erfolgsfall Weiterleitung auf die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)". |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Allgemeine Administration (OPC\_ALG\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Empfängertyp | M | A | Der gewählte Empfängertyp | Alphanumerisch |
| Auftragstyp | M | A | Der gewählte Auftragstyp | Alphanumerisch |
| Schlüsseltyp | M | A | Der gewählte Schlüsseltyp | Alphanumerisch |
| Seriennummer | M | A | Die ermittelte Seriennummer des hochgeladenen Doku-Schlüssels | Numerisch |
| Aussteller | M | A | Der ermittelte Aussteller des hochgeladenen Doku-Schlüssels | Alphanumerisch |
| Gültigkeitsbeginn | M | A | Der Beginn der ermittelten Gültigkeit des hochgeladenen Doku-Schlüssels | ‚TT.MM.JJJJ' |
| Gültigkeitsende | M | A | Das Ende der ermittelten Gültigkeit des hochgeladenen Doku-Schlüssels | ‚TT.MM.JJJJ' |
| Algorithmus | M | A | Der ermittelte Algorithmus des hochgeladenen Doku-Schlüssels | Alphanumerisch |
| DN | M | A | Die ermittelte DN des hochgeladenen Doku-Schlüssels | Alphanumerisch |