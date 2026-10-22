# Chipkarte-Verwalten löschen (OPC_CKV_GUI_012)

Anzeigename: Karte verwalten (OPC\_CKV\_GUI\_012) [e-card Löschen]

Diese Maske ermöglicht das Löschen einer einzelnen e‑card (CIN).  
Vor dem endgültigen Löschen muss der Benutzer ein Stichwort eingeben und die Aktion ausdrücklich bestätigen.  
  
Je nach Benutzereingabe oder technischem Zustand werden entsprechende Warn‑ oder Fehlermeldungen angezeigt.

**Maskenfelder:**

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Bestätigungsfrage | M | A | „Sind Sie sicher, dass Sie die e-card (CIN: <CIN>) löschen möchten?“ | Text |
| Stichwort | M | E | Eingabe des Bestätigungswortes | alphanumerisch |

**Navigation:**

| **Maskenfeld** | **Beschreibung** | **Format** |
| --- | --- | --- |
| **Löschen** | Validierungs- und Fehlerszenarien:   * Stichwort fehlt oder ist falsch Anzeige der Warnmeldung: *„Eingabe fehlerhaft: Bitte geben Sie das Stichwort ein.“* * Technischer Fehler (z. B. Datenbankfehler) Anzeige der Fehlermeldung: *„Fehler: Es ist ein unerwarteter Fehler aufgetreten!“* * Erfolgreiche Löschung Weiterleitung zur Erfolgsmaske „Karten verwalten (OPC\_CKV\_GUI\_013)“. | Button |
| **Abbrechen** | Bricht den aktuellen Vorgang ab und kehrt zur vorherigen Ansicht zurück: "Karten verwalten (OC\_CKV\_GUI\_004)" | Button |