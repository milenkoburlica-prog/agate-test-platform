# Erlaubte Statusübergänge für OPC prüfen (OPC_CKV_FKT_005)

# \_Toc172190349Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Status | Zu prüfender Kartenstatus |  | **M** |

# \_Toc172190350Ausgangsdaten

|  |  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** |  | **mögliche Werte** |  | **Muss / Kann** |  |
| Endstatus setzen erlaubt | Gibt an, ob der Endstatus 05 für diese Karte gesetzt werden darf | true, false |  | **M** |  |

# \_Toc172190351Beschreibung

Für den OPC wird ermittelt, welche Funktion mit dem Kartenstatus durchgeführt werden darf.

1. Die Funktion „Erlaubte Statusübergänge prüfen (OPC\_CKV\_FKT\_004)" wird mit dem Status aus den Eingangsdaten aufgerufen.
2. Wird der Auftragstyp „Endstatus setzen" geliefert, wird das Flag „Endstatus setzen erlaubt" auf true gesetzt