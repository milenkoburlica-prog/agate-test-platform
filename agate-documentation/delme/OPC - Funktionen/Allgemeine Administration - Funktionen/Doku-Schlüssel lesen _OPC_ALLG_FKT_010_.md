# Doku-Schlüssel lesen (OPC_ALLG_FKT_010)

# \_Toc172189874Beschreibung

Bei der Datei mit dem bereitzustellenden Zertifikat muss es sich um eine CER- oder PEM- Datei handeln. Andernfalls wird die Fehlermeldung DOKU-001 in die Ausgangsdaten gestellt.  
Serverseitig wird das Zertifikat aus der hochgeladene Datei als ‚X509Certificate' gelesen. Kann der Dateninhalt des Zertifikats nicht gelesen werden, so wird die Fehlermeldung DOKU-002 in die Ausgangsdaten gestellt.  
Der angegebene Auftrags-, Empfänger- und Schlüsseltyp wird geprüft. Entspricht die Kombination nicht einer der nachfolgenden, so wird die Fehlermeldung DOKU-003 in die Ausgangsdaten gestellt.

| **Auftragstyp** | **Empfängertyp** | **Schlüsseltyp** |
| --- | --- | --- |
| VUNeu | PST | ID (Identifikator) |
| VUNeu | PST | SO (Sensibles Objekt) |
| VUNeu | OGGH | ID (Identifikator) |
| VUNeu | OGGH | SO (Sensibles Objekt) |
| DMP | PST | ID (Identifikator) |
| DMP | PST | SO (Sensibles Objekt) |
| BKF | PST | ID (Identifikator) |
| BKF | PST | SO (Sensibles Objekt) |
| BKF | PST | ST (Standort) |

Nachdem das Zertifikat aus der Datei erfolgreich gelesen wurde, werden seine Attribute ermittelt und als Ergebnis in die Ausgangsdaten gestellt.

# \_Toc172189875Vorbedingungen

Eine Datei (Dateierweiterung ‚CER' oder ‚PEM') mit dem bereitzustellenden Zertifikat (RSA-Schlüssel) wurde an den Betrieb übergeben und vom Betrieb am definierten Ablageort abgelegt.

# \_Toc172189876Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Datei | Datei mit dem bereitzustellenden Zertifikat |  | M |
| Auftragstyp | Auftragstyp   Mögliche Werte: siehe Beschreibung. | CHAR(10) | M |
| Empfängertyp | Empfängertyp   Mögliche Werte: siehe Beschreibung. | CHAR(4) | M |
| Schlüsseltyp | Schlüsseltyp   Mögliche Werte: siehe Beschreibung. | CHAR(3) | M |

# \_Toc172189877Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Seriennummer | Seriennummer des Zertifikats | CHAR(48) | K |
| Aussteller | DN des Ausstellers | CHAR(256) | K |
| Gültig von | Datum, ab dem das Zertifikat gültig ist | DATE | K |
| Gültig bis | Datum, bis zu dem das Zertifikat gültig ist | DATE | K |
| DN | DN des Antragstellers | CHAR(256) | K |
| Algorithmus | Algorithmus (Signaturalgorithmus, Schlüsselalgorithmus und Schlüssellänge) | CHAR(256) | K |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="bec5ab9a-8433-4302-8c6c-d1c5badae55a"><ac:plain-text-body><![CDATA[ | X509Certificate | Zertifikat aus der hochgeladenen Datei | BYTE [] | K | ]]></ac:plain-text-body></ac:structured-macro> |

# \_Toc172189878Meldungen

|  |  |
| --- | --- |
| **MeldungsNr** | **Meldungstext** |
| DOKU-001 | Die Bereitstellung des Schlüssels ist nicht möglich, da die Dateierweiterung der Zertifikatsdatei nicht erlaubt ist. |
| DOKU-002 | Die Bereitstellung des Schlüssels ist nicht möglich, da das Zertifikat nicht aus der Zertifikatsdatei ermittelt werden kann. |
| DOKU-003 | Die Bereitstellung des Schlüssels ist nicht möglich, da die angegebene Kombination aus Empfänger-, Auftrags- und Schlüsseltyp nicht erlaubt ist. |