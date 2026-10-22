# GINO-Sperrliste ermitteln (OPC_CLV_FKT_170)_KVW-12096_

# \_Toc172190206Vorbedingungen

Keine

# \_Toc172190207Beschreibung

In der Entität „GINO Produktionsdaten" werden alle Einträge ermittelt, deren Sperrkennzeichen gesetzt ist. Ist der „Filter Seriennummer" gesetzt und enthält von Leerzeichen verschiedene Werte wird die Seriennummer der Produktionsdatensätze damit verglichen und nur Datensätze zurückgeliefert, bei denen die Seriennummer dem Filter entspricht. Ansonsten werden alle gesperrten Produktionsdaten zurückgegeben.Die Einträge werden nach der Seriennummer aufsteigend sortiert zurückgegeben.

# \_Toc172190208Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="3065adf0-6200-4777-ab8c-9a6c1aec7831"><ac:plain-text-body><![CDATA[ | Filter\_SeriennummerFilter Seriennummer | Dabei handelt es sich um einen maximal 10-stelligen String. Dieser kann aus Ziffern [0-9], der Wildcard ? für ein beliebiges Zeichen und der Wildcard \* für beliebig viele Zeichen bestehen. |  | **K** | ]]></ac:plain-text-body></ac:structured-macro> |

# \_Toc172190209Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Produktionsdaten" | Liste von „GINO-Produktionsdaten", kann auch leer sein. |  | **M** |

# \_Toc172190210Meldungen

Keine.