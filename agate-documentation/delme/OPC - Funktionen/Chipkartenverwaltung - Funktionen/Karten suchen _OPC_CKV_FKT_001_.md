# Karten suchen (OPC_CKV_FKT_001)

# \_Toc172190329Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Kartentyp | Kartentyp | e-card, o-card | **M** |
| CIN | CIN der Karte |  | **K** |
| Karten ID | Eindeutige Identifikation der o-card (o-card ID) oder der e-card (e-card ID), abhängig vom Kartentyp |  | **K** |
| VPNR/VSNR | Vertragspartnernummer oder Versichertennummer, abhängig vom Kartentyp |  | **B** |
| Kartenfolgenummer | Kartenfolgenummer |  | **K** |
| Status | Status der Karte | eingeschränkt auf:   04, 12, 23 und 33 | **K** |

*Hinweis:Wildcards werden nicht unterstützt.Wenn die Kartenfolgenummer eingegeben wird, muss auch die VPNR/VSNR erfasst werden.Alle Eingabefelder werden mit UND verknüpft.Eines der Eingabefelder CIN, VPNR/VSNR, Karten ID, Kartenfolgenummer oder Status muss versorgt sein.*

# \_Toc172190330Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| pro gefundener Karte |  |  |  |
| Kartentyp | Kartentyp | e-card, o-card | **M** |
| CIN | CIN der Karte |  | **M** |
| VPNR/VSNR | Vertragspartnernummer oder Versichertennummer, abhängig vom Kartentyp |  | **M** |
| Kartenfolgenummer | Kartenfolgenummer |  | **M** |
| Karten ID | Eindeutige Identifikation der o-card (o-card ID) oder der e-card (e-card ID), abhängig vom Kartentyp |  | **M** |
| Bemerkung | Bemerkung zum Kartenstatus |  | **K** |
| Bemerkungsdatum | Datum an dem die Bemerkung geändert wurde |  | **K** |
| Bemerkungsuser | User, der die Bemerkung geändert hat |  | **K** |
| \_Hlk119827377Status | Aktueller Status der Karte |  | **M** |
| EndStatusAllowed |  |  | **M** |
| pro Status pro gefundener Karte |  |  |  |
| Status | Historischer Status der Karte |  | **K** |
| Grund Code | Code für den Grund der Änderung. |  | **K** |
| Bemerkung | Bemerkung zum Kartenstatus |  | **K** |
| Datum | Änderungsdatum auf diesen Status |  | **K** |
| User | User, der den Status geändert hat. |  | **K** |

*Hinweis:Das Ergebnis wird aufsteigend sortiert nach dem Änderungsdatum des aktuellen Kartenstatus. Danach nach VPNR/VSNR.*

# \_Toc172190331Beschreibung

Die Funktion sucht alle Karten, die zu den Kriterien aus den Eingangsdaten passen und liefert diese zurück.

1. Die Karten werden nach folgenden Kriterien eingeschränkt:

* Ist der Kartentyp aus den Eingangsdaten gleich „e-card", wird in der Entität „e-card" gesucht. Ist der Kartentyp aus den Eingangsdaten gleich „o-card", wird in der Entität „o-card" gesucht.
* Die CIN muss gleich der aus den Eingangsdaten sein.
* Die Karten ID muss gleich der aus den Eingangsdaten sein.
* Ist der Kartentyp aus den Eingangsdaten gleich „e-card", muss VSNR gleich VPNR/VSNR aus den Eingangsdaten sein.
* Ist der Kartentyp aus den Eingangsdaten gleich „o-card", muss VPNR gleich VPNR/VSNR aus den Eingangsdaten sein.
* Die Kartenfolgenummer muss gleich der aus den Eingangsdaten sein.
* Der aktuelle Status muss gleich dem aus den Eingangsdaten sein.

1. Für alle gefundenen Karten wird die Funktion „Erlaubte Statusübergänge für OPC prüfen (OPC\_CKV\_FKT\_005)" aufgerufen und das Ergebnis in „EndStatusAllowed" gespeichert.
2. Die so ermittelten Daten werden an der Ausgangsschnittstelle bereitgestellt.

# \_Toc172190332Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
|  | Zum angegeben Suchkriterium stehen keine Karten zur Verfügung. | Message | Error |