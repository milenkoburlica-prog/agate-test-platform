# GINOs entsperren (OPC_CLV_FKT_172)

# \_Toc172190218Vorbedingungen

Keine

# \_Toc172190219Beschreibung

Falls Geräte ausgewählt wurden, muss die Aktion Bestätigt werden (Ja/Nein).  
Falls ja ausgeählt wurde wird für jedes ausgewählte Gerät in der Entität „GINO Produktionsdaten"anhand der Seriennummer der entsprechende Eintrag ermittelt, das Sperrkennzeichen auf ‚false' gesetzt,das Sperrdatum entfernt und das Sperrkommentar ggf. angepasst oder gelöscht. Im Erfolgsfall wird die Meldung CLV-203 geloggt.  
Die Funktion wird unter Ausgabe der Meldung GUI-014 beendet.  
*Hinweis: Um einen entsperrten GINO wieder im Betrieb zu nehmen ist ein Werksreset des GINO notwendig. Damit wird das gesperrte Zertifikat auch am GINO gelöscht und ein neues Zertifikat angefordert. (Der Reset kann entfallen, wenn die Zertifikate auf Grund der Prüfung des Ablaufdatums sowieso erneuert würden).*

# \_Toc172190220Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Seriennummer | Siehe „GINO-Seriennummer" |  | **M** |
| Kommentar | Kommentar warum die Sperrung/Entsperrung vorgenommen wurde/wird | 100 alphanumerische Zeichen | **K** |

# \_Toc172190221Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Produktionsdaten" | Liste von „GINO-Produktionsdaten", kann auch leer sein. |  | **M** |

# \_Toc172190222Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-035 | Die GINOs mit den Seriennummern <Menge Seriennummern> wurden entsperrt. | Message | Info |
| CLV-203 | Der GINO mit der Seriennummer <Seriennummer> wurde entsperrt. | LOG | Info |