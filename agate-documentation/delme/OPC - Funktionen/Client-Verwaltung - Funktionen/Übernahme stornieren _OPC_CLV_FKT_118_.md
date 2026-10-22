# Übernahme stornieren (OPC_CLV_FKT_118)

# \_Toc172190050Vorbedingungen

Das Kennzeichen KZ\_UEBERGEBEN ist gesetzt.

# \_Toc172190051Beschreibung

Mit dieser Funktion wird eine übernommener Stammdatensatz als nicht übernommen gekennzeichnet.

Der Nachfolger wird ausgetragen und das Kennzeichen für KZ\_UEBERGEBEN wird zurückgesetzt.

Beim Nachfolger wird das Kennzeichen für KZ\_UEBERNOMMEN zurückgesetzt.

Hinweis: das bedeutet nicht, dass der Zustand vor der Übernahme wiederhergestellt ist. Die übergebenen Anschlüsse müssen durch eine Übernahme „zurückgeholt" werden, Falls man danach diese Übernahme auch storniert, ist der ursprüngliche Zustand wiederhergestellt.

# \_Toc172190052Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Stammdaten | Objekt |  | **M** |

# \_Toc172190053Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Stammdaten | Entität |  | **M** |

# \_Toc172190054Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-1006 | Die Übernahme der Ordination (VPNR: <VPNR>, Ordinationsnummer: <ORDNR>) wurde erfolgreich storniert. | Message | Info |
| GUI-024 | Wollen Sie die Übernahme für die Ordination (VPNR: <VPNR>, Ordinationsnummer: <ORDNR>) wirklich stornieren? | Message | Warning |