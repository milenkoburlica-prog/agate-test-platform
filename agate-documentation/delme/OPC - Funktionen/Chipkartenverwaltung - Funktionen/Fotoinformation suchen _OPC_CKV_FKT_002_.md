# Fotoinformation suchen (OPC_CKV_FKT_002)

# \_Toc172190334Vorbedingungen

# \_Toc172190335Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR | Versichertennummer |  | **M** |
| Typ | Typ der Fotoinformation | „FZ" oder „FA" | **K** |

# \_Toc172190336Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Fotoinformation | Fotoinformationsobjekt, d.h. alle Daten aus der Entität Foto\_Info. |  | **K** |

# \_Toc172190337Beschreibung

Die Funktion sucht zu den übergebenen Eingangsdaten einen aktuellen Eintrag in der Entität Foto\_Info, d.h. einen Eintrag bei dem für das Attribut InformationsendeAm das Defaultdatum (31.12.3999) eingetragen ist.  
Falls kein Typ angegeben wird, wird unabhängig vom Typ gesucht.  
*Hinweis: Für eine VSNR kann es zu einem Zeitpunkt nur einen aktuellen Eintrag geben, entweder des Typs „FZ" oder des Typs „FA".*  
Falls der Aufruf über die Maske „Fotoinformation Suchen (OPC\_CKV\_GUI\_007)" erfolgt und kein Eintrag gefunden wurde, wird die Meldung GUI-801 ausgegeben.  
Falls ein Eintrag gefunden wurde, wird dieser retourniert.

# \_Toc172190338Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-801 | Zu den angegebenen Suchkriterien existiert keine aktuelle Fotoinformation. | Message | Error |