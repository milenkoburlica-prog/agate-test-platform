# Anmelden (BF_ANM_FKT_001)

# \_Toc172189842Beschreibung

Die übergebene Benutzerkennung (User) und das übergebene Passwort werden der serverseitig konfigurierten Authentifizierungsstelle übermittelt und die Rückmeldung verarbeitet.

# \_Toc172189843Vorbedinungen

Der Benutzer ist nicht authentifiziert.

# \_Toc172189844Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Kennung | Die Kennung des Benutzers |  | M |
| Passwort | Das Passwort des Benutzers |  | M |

# \_Toc172189845Ausgangsdaten

Die Ausgangsdaten stehen der aktiven Session zur Verfügung.

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Kennung | Die Kennung des Benutzers. |  | M |
| Benutzerrollen | Liste aller Rollen die dem Benutzer zugeordnet sind. |  | M |