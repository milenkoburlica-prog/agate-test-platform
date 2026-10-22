# SW-Zertifikat suchen (OPC_VPV_FKT_007)

# \_Toc172189917Vorbedingungen

Keine

# \_Toc172189918Beschreibung

Diese Funktion ermittelt die SW-Zertifikate zur eingegebenen Vertragspartnernummer (VPNR). Ausgehend von der eingegebenen VPNR wird zunächst der Vertragspartner ermittelt. Ausgehend vom Vertragspartner werden über die Entität HVB\_BEZIEHUNG alle SW-Zertifikate ermittelt, in der der jeweilige Vertragspartner in einer „Parent-Beziehung" mit einem anderen Vertragspartner steht. Gibt es bei einem Vertragspartner keine „Parent-Beziehung" mehr so ist das Ende der SW-Zertifikats-Hierarchie erreicht.   
Die gefundenen Treffer werden in der VPNR- Hierarchie sortiert ausgegeben (z.B.: SW-Zertifikat VPNR, SW-Zertifikat Parent VPNR, SW-Zertifikat Parent VPNR der Parent VPNR usw.). Sollten zu einer VPNR mehrere SW-Zertifikate vorhanden sein, so werden diese innerhalb der jeweiligen VPNR nicht speziell sortiert.  
Es werden alle SW-Zertifikate unabhängig des Lösch-Kennzeichens ermittelt. Ein als ‚gelöscht' markiertes SW-Zertifikat wurde gesperrt und kann wieder entsperrt werden. Ein aktives SW-Zertifikat (Lösch-Kennzeichen nicht gesetzt) kann gesperrt werden.  
Können keine SW-Zertifikate zur angegebenen Vertragspartnernummer ermittelt werden, so wird die Fehlermeldung ‚BNV-015' ausgegeben.

# \_Toc172189919Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| VPNR | Vertragspartnernummer |  | M |

# \_Toc172189920Ausgangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| 1 bis n Wiederholungen |  |  |  |
| VPNR | Vertragspartnernummer |  | M |  |
| Zuname | Zuname des Vertragspartners |  | M |  |
| SW-ZertifikatID | Primary-Key der Entität 'SW-Zertifikat' |  | M |  |
| Seriennummer | Seriennummer des Zertifikats |  | M |  |
| Seriennummer-hex | Hexadezimale Form der Seriennummer |  | M |  |
| Aktiv-KZ | Kennzeichen ob SW-Zertifikat gesperrt wurde | 0 = nicht gesperrt1 = gesperrt | M |  |

# \_Toc172189921Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-015 | Anhand der Suchparameter ‚Vertragspartnernummer:<VPNR>' wurden keine SW-Zertifikate gefunden. | Message | Error |