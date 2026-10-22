# Bemerkung zur Karte ändern (OPC_CKV_FKT_003)

# \_Toc172190341Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Kartentyp | Kartentyp | e-card, o-card | **M** |
| Karten ID | Eindeutige Identifikation der e-card (e-card ID) oder o-card (o-card ID), abhängig vom Kartentyp |  | **M** |
| Bemerkung | Bemerkung zur Karte |  | **M** |
| Bemerkungsdatum | Datum an dem die Bemerkung geändert wurde.   Falls nicht versorgt, aktuelles Datum |  | **K** |
| Bemerkungsuser | User, der die Bemerkung geändert hat |  | **M** |

# \_Toc172190342Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Prüfergebnis | Prüfergebnis | true, false | **M** |

# \_Toc172190343Beschreibung

Die Bemerkung, das Bemerkungsdatum und der Bemerkungsuser werden zur e-card bzw. o-card gespeichert.

1. Suche der Karte nach folgenden Kriterien:

* Ist der Kartentyp aus den Eingangsdaten gleich „e-card", wird in der Entität „e-card" gesucht. Ist der Kartentyp aus den Eingangsdaten gleich „o-card", wird in der Entität „o-card" gesucht.
* Ist der Kartentyp aus den Eingangsdaten gleich „e-card", muss die e-card ID gleich der Karten ID aus den Eingangsdaten sein.
* Ist der Kartentyp aus den Eingangsdaten gleich „o-card", muss die o-card ID gleich der Karten ID aus den Eingangsdaten sein.

1. Zur so gefundenen Karte wird die Bemerkung, das Bemerkungsdatum und der Bemerkungsuser mit den Werten aus den Eingangsdaten versorgt und gespeichert.