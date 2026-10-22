# Foto-Metadaten löschen (OPC_CKV_FKT_012)

# \_Toc172190387Vorbedingungen

# \_Toc172190388Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Liste Register | Ausgewählt durch Checkboxen |  | **M** |
| BPK | Text |  | **M** |

# \_Toc172190389Ausgangsdaten

# \_Toc172190390Beschreibung

Wenn die Liste der Ausgewählten Register leer ist, dann wird mit der Fehlermeldung GUI-808 abgebrochen.  
Ansonsten werden alle nicht gelöschten Einträge in FOTO\_METADATEN der entsprechenden BPK und den ausgewählten Registern gelöscht. Die User-ID wird entsprechend angepasst. Die Historisierung nach HM13 wird dabei berücksichtigt  
Es wird die Erfolgsmeldung CKV-114 ausgegeben.

# \_Toc172190391Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI\_Message\_808GUI-808 | Wähle zumindest ein Register aus um die Metadaten zu löschen. | Message | Error |
| CKV\_Message\_114CKV-114 | Die Foto-Metadaten für SVNR <SVNR> wurde in den Registern <Liste Register> gelöscht. | MessageLog | Info   Info |