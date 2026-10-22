# Foto-Metadaten suchen (OPC_CKV_FKT_011)

# \_Toc172190381Vorbedingungen

# \_Toc172190382Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| SVNR | Texteingabe der SVNR |  | **M** |

# \_Toc172190383Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Liste Register | Liste mit Registern für welche in der Tabelle FOTO\_METADATEN mit dem BPK der SVNR ein Eintrag vorhanden ist. |  | **M** |
| BPK | BPK zur Angegebenen SVNR |  | **M** |

# \_Toc172190384Beschreibung

Die übergebene SVNR wird auf Plausibilität überprüpft.  
Mit der SVNR wird die ZPV (bpk-Lesen) mit dem Bereich SV aufgerufen. Falls das ZPV einen Fehler liefert wird mit der Meldung GUI-806 abgebrochen.  
Ansonsten werden alle nicht gelöschten Einträge in FOTO\_METADATEN mit dem zurückgelieferten BPK und allen möglichen Registern gesucht. Die gefundenen Einträge (BPK, Register) werden zurückgegeben.  
Falls keine Metadaten eingetragen sind, wird mit der Meldung GUI-807 abgebrochen.  
Tritt dabei ein allgemeiner Fehler auf, wird die Funktion mit der Meldung GUI-805 abgebrochen.

# \_Toc172190385Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI\_Message\_805GUI-805 | Ein Unbekannter Fehler ist aufgetreten. Bitte erneut versuchen. | Message | Error |
| GUI\_Message\_806GUI-806 | ZPV liefert Fehler <Fehlernummer ZPV> - „<Fehlerbeschreibung ZPV>". | Message | Error |
| GUI\_Message\_807GUI-807 | Es wurden keine Metadaten zur angegebenen SVNR gefunden. | Message | Error |