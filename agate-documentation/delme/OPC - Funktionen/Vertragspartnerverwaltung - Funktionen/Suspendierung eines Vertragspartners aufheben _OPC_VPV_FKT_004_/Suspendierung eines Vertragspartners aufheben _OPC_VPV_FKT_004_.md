# Suspendierung eines Vertragspartners aufheben (OPC_VPV_FKT_004)

# \_Toc83719404\_Toc172189905Beschreibung

Diese Funktion hebt die Suspendierung eines Benutzers vom Typ „Vertragspartner" auf.

# \_Toc83719405\_Toc172189906Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# \_Toc83719406\_Toc172189907Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartnernummer |  | M |
| Grund der Aufhebung der Suspendierung | Grund der Aufhebung der Suspendierung des Vertragspartners |  | M |

# \_Toc83719407\_Toc172189908Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Status | Kennzeichen ob die Aufhebung der Suspendierung des Benutzers erfolgreich war. | OK, NOK | M |

# \_Toc83719408\_Toc172189909Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-004 | Die Suspendierung des Vertragspartners <VPNR> konnte nicht aufgehoben werden | Log | Error |