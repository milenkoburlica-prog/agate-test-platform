# GINO-Gruppenliste ermitteln (OPC_CLV_FKT_140)

# \_Toc172190068Vorbedingungen

Keine

# \_Toc172190069Beschreibung

Diese Funktion ermittelt für in den Eingangdaten übergebenen GINO-Gruppentyp die aktuellen Gruppen aus der Entität „Gino-Gruppe" eingeschränkt auf die gespeicherten Gültigkeitsdaten.   
Für jeden ermittelten Eintrag in „Gino-Gruppen" werden die entsprechenden Daten in den Ausgangsdaten zurückgeliefert, weiters wird zu jedem Eintrag die Anzahl der aktuellen, referenzierenden Einträge in der Entität „GINO-Gruppen-Mitglieder" ermittelt, und im Parameter „Anzahl Gruppenmitglieder" der Paramtergruppe „GINO-Gruppe" zurückgeliefert.

# \_Toc172190070Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gruppen-Typ | Typ der Gruppe, siehe „GINO-Gruppentyp". |  | **M** |

# \_Toc172190071Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Gruppe"   Gruppen-ID | Siehe „GINO-Gruppe".   Die Liste kann auch leer sein. |  | **M** |

# \_Toc172190072Meldungen

Keine