# Session beenden (OPC_SEV_FKT_005)

# \_Toc172190294Beschreibung

Diese Funktion ermittelt alle offenen Sessions zu den gegebenen Suchkriterien und versucht diese zu beenden. Das Timeout gibt an wie lange die Sessions noch maximal aktiv sein dürfen. Ist die Änderung erfolgreich propagiert worden, liefert die Funktion „true" (Erfolg) zurück. Es wird für die Rückmeldung nicht das Ende des Timeouts abgewartet.

# \_Toc172190295Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Session ID | Id der zu löschenden Session | Numerisch | M |
| Timeout | Timeout in Minuten bis die Session spätestens beendet wird. | Numerisch | M |

# \_Toc172190296Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Erfolg | Rückgabeflag zeigt mit ‚true' eine erfolgreiche Löschung an. | ‚true', ‚false' | M |