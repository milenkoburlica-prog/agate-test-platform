# Parametergruppe GINO-Resource Status

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| ID | Siehe „GINO-Task-ID" | Long | K |
| Call | Typ der GINO-Resource für die Anzeige siehe „GINO-Resourcetyp |  | M |
| Erster Aufruf | Zeitpunkt des ersten Aufrufs |  | K |
| Letzter Aufruf | Zeitpunkt des letzten Aufrufs |  | K |
| Response Status | http-Response Code |  | K |
| Error Code | Errror Code |  | K |
| Error Text | Error Text |  | K |
| Input Liste | Liste von key/value Paaren, die vom GINO übermittelt wurden.   Die Liste enthält zuerst die Keys mit einzeiligen Values und dann mit mehrzeiligen. Innerhalb dieser 2 Gruppen werden die Keys alphabetisch sortiert.   Um die mehrzeiligen Value voneinander zu trennen wird vor und nach dem Wert eine Zeile mit Bindestrichen angezeigt. In der Zeile über der Value findet man in der Mitte der Beistriche den Key. |  | K |
| Output Liste | Liste von key/value Paaren, die an den GINO übermittelt wurden.   Die Liste enthält zuerst die Keys mit einzeiligen Values und dann mit mehrzeiligen. Innerhalb dieser 2 Gruppen werden die Keys alphabetisch sortiert   Um die mehrzeiligen Value voneinander zu trennen wird vor und nach dem Wert eine Zeile mit Bindestrichen angezeigt. In der Zeile über der Value findet man in der Mitte der Beistriche den Key. |  | K |