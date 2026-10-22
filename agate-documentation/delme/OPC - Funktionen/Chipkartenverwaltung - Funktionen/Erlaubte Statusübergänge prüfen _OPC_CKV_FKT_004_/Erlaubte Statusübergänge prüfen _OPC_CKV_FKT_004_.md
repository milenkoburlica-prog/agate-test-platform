# Erlaubte Statusübergänge prüfen (OPC_CKV_FKT_004)

# \_Toc172190345Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Status | Zu prüfender Kartenstatus |  | **M** |

# \_Toc172190346Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Auftragstyp (1..n) | Liste von Auftragstypen, die sich aus dem eingegebenen Status ableiten lassen |  | **M** |

# \_Toc172190347Beschreibung

Die möglichen Auftragstypen, die laut Chipkartenverwaltung auf den eingegebenen Status folgen können, werden ermittelt und an der Ausgangsschnittstelle bereitgestellt.