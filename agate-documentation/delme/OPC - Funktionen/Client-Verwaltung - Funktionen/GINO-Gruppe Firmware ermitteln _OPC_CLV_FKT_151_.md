# GINO-Gruppe Firmware ermitteln (OPC_CLV_FKT_151)

# \_Toc172190134Vorbedingungen

Keine

# \_Toc172190135Beschreibung

Anhand der übergebenen GINO-Gruppen-ID wird in der Enitiät „GINO-Firmware Zuordnung" ein eventuell auf den Eintrag in „GINO-Gruppe" referenzierender Eintrag ermittelt und aus diesem das Attribut „Software Version" in den Ausgangsdaten zurückgeliefert.

# \_Toc172190136Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gruppen-ID | ID der Gruppe | numerisch | **M** |

# \_Toc172190137Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Firmware Version | Siehe „GINO-Firmware Version". |  | **K** |

# \_Toc172190138Meldungen

Keine.