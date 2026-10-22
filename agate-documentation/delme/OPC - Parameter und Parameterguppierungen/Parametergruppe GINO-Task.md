# Parametergruppe GINO-Task

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| ID | Siehe „GINO-Task-ID" | Long | K |
| Call | Typ der GINO-Resource die in der Antwort eines ALIVE Requests durch den Parameter call zurückgegeben wird, siehe „GINO-Resourcetyp"   Folgende Resourcen können nicht zurückgemeldet werden:   * alive * event * maintenance * scriptresult |  | M |
| Uhrzeit-ab | Uhrzeit, ab der der Task ausgeführt werden kann. |  | M |
| Uhrzeit-bis | Uhrzeit, bis der der Task ausgeführt werden kann. |  | M |
| Intervall | Zeitangabe in Stunden: die eingetragene Resource wird nur dann zurückgegeben, falls die Resource in den letzten x Stunden nicht ausgeführt wurde.   Falls kein Wert eingetragen ist, wird die Resource nur dann zurückkgeben, wenn sie seit dem Startzeitpunkt der Gruppe nicht ausgeführt wurde. | INT | K |

*\*B: muss gesetzt sein falls Repeat=true*