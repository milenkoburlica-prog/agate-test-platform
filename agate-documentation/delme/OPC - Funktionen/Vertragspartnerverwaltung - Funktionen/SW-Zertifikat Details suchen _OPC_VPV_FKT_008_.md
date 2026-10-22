# SW-Zertifikat Details suchen (OPC_VPV_FKT_008)

# \_Toc172189923Vorbedingungen

Keine

# \_Toc172189924Beschreibung

Diese Funktion ermittelt die Details des gewählten SW-Zertifikats anhand der ID des gewählten SW-Zertifikats aus der Trefferliste (siehe Funktion „SW-Zertifikat suchen (OPC\_VPV\_FKT\_007)").  
Anhand des Aktiv- Kennzeichens wird entweder das Sperren (SW-Zertifikat ist aktiv) oder Entsperren (SW-Zertifikat gesperrt) des SW-Zertifikats zugelassen.

# \_Toc172189925Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| SW-ZertifikatID | Primary-Key der Entität 'SW-Zertifikat' |  | M |

# \_Toc172189926Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| VPNR | Vertragspartnernummer |  | M |
| Zuname | Zuname des Vertragspartner |  | M |
| Seriennummer | Seriennummer |  | M |
| Seriennummer-hex | Hexadezimale Form der Seriennummer |  | M |
| Aussteller | Distinguished Name des Aussteller |  | M |
| SHA1-Fingerprint | SHA1-Fingerprint |  | M |
| SHA256-Fingerprint | SHA256-Fingerprint |  | M |
| Gültigkeitsbeginn | Datum ab dem das SW-Zertifikat gültig ist |  | M |
| Gültigkeitsende | Datum bis zu dem das SW-Zertifikat gültig ist |  | M |
| Aktiv-KZ | Kennzeichen ob SW-Zertifikat gesperrt wurde | 0 = nicht gesperrt1 = gesperrt | M |
| Algorithmus | Signaturalgorithmus, Schlüsselalgorithmus und Schlüssellänge |  | M |
| DN | Distinguished Name des Antragstellers |  | M |

# \_Toc172189927Meldungen

Keine