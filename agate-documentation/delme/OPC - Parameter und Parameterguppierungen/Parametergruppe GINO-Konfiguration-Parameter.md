# Parametergruppe GINO-Konfiguration-Parameter

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| ID | Eindeutiger Identifier des Konfigurations-Parameters | Long | K |
| Name | Name der Konfigurations-Parameters | String(100) | M |
| Wert | Wert des Konfigurations-Parameters | String(1000) | M |
| Gruppen-ID | Siehe „GINO-Gruppen-ID". |  | K |
| Default | Flag, gibt an ob der Wert ein Defaultwert ist | Boolean | M |
| Remove | Flag, das angibt, ob der Parameter zum GINO übertragen werden soll.   *Hinweis: Es soll z.B. ein Parameter, der in der allgemeinen Gruppe definiert ist, in der dynamischen Gruppe nicht an den GINO übertragen werden, dann hat der Parameter den Wert „true". In diesem Fall haben „Wert" und „Default" keine Bedeutung.* | Boolean | M |