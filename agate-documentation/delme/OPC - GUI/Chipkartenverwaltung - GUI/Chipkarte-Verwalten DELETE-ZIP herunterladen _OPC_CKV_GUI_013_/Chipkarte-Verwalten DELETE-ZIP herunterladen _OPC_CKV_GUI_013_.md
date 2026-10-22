# Chipkarte-Verwalten DELETE-ZIP herunterladen (OPC_CKV_GUI_013)

Anzeigename: Karte verwalten (OPC\_CKV\_GUI\_013)

Diese Maske wird angezeigt, nachdem eine e‑card erfolgreich gelöscht wurde.  
Alle zur Karte gehörenden Datenbankeinträge wurden im Rahmen des Löschvorgangs ermittelt und in einer ZIP‑Datei exportiert.  
  
Je nach Benutzereingabe oder technischem Zustand werden entsprechende Warn‑ oder Fehlermeldungen angezeigt.

**Maskenfelder:**

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| INFO | M | A | Die e‑card wurde erfolgreich gelöscht. Export erfolgreich „In der Datei ECARD\_DELETE\_<SVNR>\_<CIN>\_<YYYYMMDDHHMSS>.zip wurden betroffene Datenbank Tabellen exportiert.“ | Text |

**Navigation:**

| **Maskenfeld** | **Beschreibung** | **Format** |
| --- | --- | --- |
| **Schließen** | Beendet den Vorgang und kehrt zurück zum „Karten verwalten (OPC\_CKV\_GUI\_002)“. | Button |
| **Herunterladen** | Startet den Download der ZIP‑Datei  Die möglichen exportierten Tabellen:  ECARD\_DELETE\_<SVNR>\_<CIN>\_<YYYYMMDDHHMSS>.zip   * `CARD_ECARD_<CIN>.csv` * `CARD_ESTATUS_<CIN>.csv` * `CARD_EAUFTRAG_<CIN>.csv` * `CARD_EZERTIFIKAT_<CIN>.csv` * `CARD_ECARD_FOTO_<CIN>.csv` * `CARD_ECARD_FOTO_HM13_<CIN>.csv` * `FOTO_INFO_AUSKUNFT_<CIN>.csv` * `FOTO_INFO_AUSKUNFT_HM13_<CIN>.csv` | Button |