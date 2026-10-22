# Übersiedlung stornieren (OPC_CLV_FKT_117)

# \_Toc172190044Vorbedingungen

In den Stammdaten ist ein Vorgänger eingetragen und das Kennzeichen KZ\_UEBERNOMMEN ist nicht gesetzt.

# \_Toc172190045Beschreibung

Mit dieser Funktion wird eine gekennzeichnete Übersiedlung rückgängig gemacht. Damit kann abweichend vom Standardverhalten für die übersiedelte Ordination ein neuer Anschluss zugeordnet werden.  
Der Vorgänger wird ausgetragen und die Daten werden gespeichert. In den VP-Stammdaten des Vorgängers wird der Nachfolger ausgetragen.   
Das Flag, das Exportdatum und die Aktion für den Providerexport- und den Callcenterexport werden gelöscht.  
Für den Vorgänger wird für den Provider- und Callcenterexport das Exportdatum gelöscht.

# \_Toc172190046Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gina-Stammdaten | Objekt |  | **M** |

# \_Toc172190047Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Stammdaten | Entität |  | **M** |
| Provider Export | Entität |  | **K** |
| Callcenter Export | Entität |  | **K** |

# \_Ref482195952\_Toc172190048Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-1005 | Die Übersiedlung der Ordination (VPNR: <VPNR>, Ordinationsnummer: <ORDNR>) wurde erfolgreich storniert. | Message | Info |
| GUI-014 | Wollen Sie die Übersiedlung für die Ordination (VPNR: <VPNR>, Ordinationsnummer: <ORDNR>) wirklich stornieren? | Message | Warning |