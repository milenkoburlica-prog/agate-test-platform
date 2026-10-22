# Doku-Schlüssel speichern (OPC_ALLG_FKT_011)

# \_Toc172189880Beschreibung

Das Zertifikat wird je Auftragstyp, Empfängertyp und Schlüsseltyp (diese Kriterien werden in weiterer Folge als Key-Kriterium bezeichnet) in der DB gespeichert.  
Je Key-Kriterium kann mehrmals ein Schlüssel bereitgestellt werden, wobei jedoch zu einem Zeitpunkt immer nur ein Schlüssel für das jeweilige Key-Kriterium gültig ist.  
Anhand dieser Funktion wird das Zertifikat zum angegebenen Key-Kriterium in der Datenbank nach HM1 gespeichert. Wurde für das angegebene Key-Kriterium bereits ein Schlüssel gespeichert, so wird dieser, bevor der neue Schlüssel gespeichert wird, zuvor historisiert.  
Bevor das Zertifikat in der Datenbank zu den angegebenen Key-Kriterien gespeichert wird, werden folgende Prüfungen durchgeführt:

* Die geforderten Sicherheitsrichtlinien des Zertifikats (RSAwithSHA1) müssen erfüllt sein. Im Fehlerfall wird die Fehlermeldung DOKU-004 in die Ausgangsdaten gestellt.

# \_Toc172189881Vorbedingungen

Das Zertifikat konnte erfolgreich aus der Datei gelesen werden.

# \_Toc172189882Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Auftragstyp | Auftragstyp | CHAR(10) | M |
| Empfängertyp | Empfängertyp | CHAR(4) | M |
| Schlüsseltyp | Schlüsseltyp | CHAR(3) | M |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="d9b5ef59-bffc-43b7-bd92-2338d201193d"><ac:plain-text-body><![CDATA[ | X509Certificate | Zertifikat aus der hochgeladenen Datei | BYTE [] | K | ]]></ac:plain-text-body></ac:structured-macro> |

# \_Toc172189883Ausgangsdaten

Keine.

# \_Toc172189884Meldungen

|  |  |
| --- | --- |
| **MeldungsNr** | **Meldungstext** |
| DOKU-004 | Die Bereitstellung des Schlüssels ist nicht möglich, da das Zertifikat nicht den geforderten Sicherheitsrichtlinien entspricht. |
| BNV-005 | Der Schlüssel wurde erfolgreich zu den gewählten Kriterien (Empfängertyp ‚…', Auftragstyp ‚…', Schlüsseltyp ‚…') bereitgestellt. |