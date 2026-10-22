# Endstatus setzen (OPC_CKV_FKT_006)

# \_Toc172190353Eingangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** |  | **mögliche Werte** | **Muss / Kann** |
| Kartentyp | Kartentyp | e-card, o-card |  | **M** |
| Karten ID | Eindeutige Identifikation der o-card (o-card ID) oder der e-card (e-card ID), abhängig vom Kartentyp |  |  | **M** |
| Bemerkung | Bemerkung zur Karte |  |  | **M** |
| Bemerkungsdatum | Datum an dem die Bemerkung geändert wurde.   Falls nicht versorgt, aktuelles Datum |  |  | **K** |
| Bemerkungsuser | User, der die Bemerkung geändert hat |  |  | **M** |

# \_Toc172190354Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Kartenstatus | Neuer Kartenstatus in der Entität „e-card" bzw. „o-card" definiert. |  | **K** |

# \_Toc172190355Beschreibung

Die Karte aus den Eingangsdaten wird auf den Endstatus gesetzt.

1. Die Funktion „Kartenstatus mit „<Status>" festlegen (OPC\_CKV\_FKT\_006)" wird mit dem Endstatus 05 und den Kartendaten aus den Eingangsdaten aufgerufen. Falls die Funktion Meldungen liefert, werden diese durchgereicht.
2. Die Funktion Bemerkung zur Karte ändern (OPC\_CKV\_FKT\_003) wird mit den Bemerkungsdaten aus den Eingangsdaten aufgerufen.