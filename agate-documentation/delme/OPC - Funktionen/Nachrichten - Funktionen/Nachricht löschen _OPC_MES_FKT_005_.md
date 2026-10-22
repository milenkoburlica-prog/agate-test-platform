# Nachricht löschen (OPC_MES_FKT_005)

# \_Toc172190322Beschreibung

Diese Funktion dient zum Löschen einer Nachricht. Die Nachricht wird dabei nicht physikalisch gelöscht. Sie wird von allen zugeordneten Vertragspartner-Sessions entfernt und ab diesem Zeitpunkt keinen neuen Vertragspartner-Sessions zugeordnet.  
Zum Löschen der Nachricht wird die Nachricht unter Angabe der eindeutigen Meldungsnummer (eindeutige Identifikation der Nachricht) an das Messaging-System (siehe Funktion „BF\_MS\_FKT\_002") übergeben und gelöscht.

# \_Toc172190323Vorbedingungen

Es ist eine Nachricht vorhanden und der Aufrufer besitzt die eindeutige Identifikation.

# \_Toc172190324Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Identifikation | Eindeutige Identifikation der zu löschenden Nachricht. |  | M |

# \_Toc172190325Meldungen / Ausnahmen

|  |
| --- |
| **Ausnahmen** |
| Wollen sie die Nachricht mit der Meldungsnummer <Eindeutige Identifikation der zu löschenden Nachricht> wirklich löschen? |
| Die Nachricht mit der Meldungsnummer <Eindeutige Identifikation der zu löschenden Nachricht> wurde erfolgreich gelöscht. |