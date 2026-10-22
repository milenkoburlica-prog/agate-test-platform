# Vertragspartner suspendieren (OPC_VPV_FKT_003)

# \_Toc83719398\_Toc172189899Beschreibung

Diese Funktion suspendiert einen Benutzer vom Typ „Vertragspartner".

# \_Toc83719399\_Toc172189900Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc83719400\_Toc172189901Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartnernummer |  | M |
| Suspendierungs-grund | Grund der Suspendierung des Vertragspartners |  | **M** |

# \_Toc83719401\_Toc172189902Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Status | Kennzeichen ob die Suspendierung des Benutzers erfolgreich war. | OK, NOK | M |

# \_Toc83719402\_Toc172189903Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-003 | Der Vertragspartner <VPNR> konnte nicht suspendiert werden | Log | Error |