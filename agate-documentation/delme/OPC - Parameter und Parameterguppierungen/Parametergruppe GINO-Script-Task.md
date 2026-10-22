# Parametergruppe GINO-Script-Task

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| ID | Siehe „GINO-Script-Task-Zuordnung-ID" | Long | K |
| Uhrzeit-ab | Uhrzeit, ab der das Script ausgeführt werden kann. |  | M |
| Uhrzeit-bis | Uhrzeit, bis zu der das Task ausgeführt werden kann. |  | M |
| Gruppen-ID | Siehe „GINO-Gruppen-ID". |  | K |
| Script-ID | Siehe „GINO-Script-ID" |  | M |
| Script | Name des Scripts | String (1000) | M |
| Intervall | Zeitangabe in Stunden: das Skript wird nur dann zurückgegeben, falls das Script in den letzten x Stunden nicht ausgeführt wurde.   Falls kein Wert eingetragen ist, wird das Skript nur dann zurückkgeben, falls es seit dem Startzeitpunkt der Gruppe nicht ausgeführt wurde. | INT | K |
|  |  |  |  |

*\*B: muss gesetzt sein falls Repeat=true*