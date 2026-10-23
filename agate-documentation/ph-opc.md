# Pflichtenheft OPC

## Inhaltsverzeichnis

- [Pflichtenheft OPC](#pflichtenheft-opc)
    - [OPC - GUI](#opc-gui)
        - [Allgemeine Ansichten](#allgemeine-ansichten)
            - [Hauptmenü OPC (OPC_GEN_GUI_001)](#hauptmenue-opc-opc-gen-gui-001)
            - [Loginseite (OPC_GEN_GUI_002)](#loginseite-opc-gen-gui-002)
            - [Rückfrage (OPC_GEN_GUI_004)](#rueckfrage-opc-gen-gui-004)
            - [Erfolgsmeldung (OPC_GEN_GUI_005)](#erfolgsmeldung-opc-gen-gui-005)
            - [Fehlerseite (OPC_GEN_GUI_006)](#fehlerseite-opc-gen-gui-006)
        - [Allgemeine Administration - GUI](#allgemeine-administration-gui)
            - [Startdialog Allgemeine Administration (OPC_ALG_GUI_001)](#startdialog-allgemeine-administration-opc-alg-gui-001)
            - [Systemparameter (OPC_ALG_GUI_002)](#systemparameter-opc-alg-gui-002)
            - [Doku-Schlüssel bereitstellen (OPC_ALG_GUI_003)](#doku-schluessel-bereitstellen-opc-alg-gui-003)
            - [Doku-Schüssel prüfen (OPC_ALG_GUI_004)](#doku-schuessel-pruefen-opc-alg-gui-004)
            - [Datenbank-Instanz (OPC_ALG_GUI_005)](#datenbank-instanz-opc-alg-gui-005)
            - [Systemparameter Hilfeseite (OPC_ALG_GUI_005)](#systemparameter-hilfeseite-opc-alg-gui-005)
        - [Vertragspartner-Verwaltung - GUI](#vertragspartner-verwaltung-gui)
            - [Startdialog Vertragspartner Verwaltung (OPC_VPV_GUI_001)](#startdialog-vertragspartner-verwaltung-opc-vpv-gui-001)
            - [Vertragspartner Suchen (OPC_VPV_GUI_002)](#vertragspartner-suchen-opc-vpv-gui-002)
            - [Vertragspartner Liste (OPC_VPV_GUI_003)](#vertragspartner-liste-opc-vpv-gui-003)
            - [Vertragspartner Details (OPC_VPV_GUI_004)](#vertragspartner-details-opc-vpv-gui-004)
            - [Vertragspartner Suspendierung Ändern (OPC_VPV_GUI_005)](#vertragspartner-suspendierung-aendern-opc-vpv-gui-005)
            - [Ordination Suchen (OPC_VPV_GUI_006)](#ordination-suchen-opc-vpv-gui-006)
            - [Ordinationen Liste (OPC_VPV_GUI_007)](#ordinationen-liste-opc-vpv-gui-007)
            - [Zuordnungen anzeigen (OPC_VPV_GUI_008)](#zuordnungen-anzeigen-opc-vpv-gui-008)
            - [Ordinationen Zuordnung ändern (OPC_VPV_GUI_009)](#ordinationen-zuordnung-aendern-opc-vpv-gui-009)
            - [Ordinationen aus Datei lesen (OPC_VPV_GUI_010)](#ordinationen-aus-datei-lesen-opc-vpv-gui-010)
            - [Datei Info anzeigen (OPC_VPV_GUI_011)](#datei-info-anzeigen-opc-vpv-gui-011)
            - [Komponenten Liste (OPC_VPV_GUI_012)](#komponenten-liste-opc-vpv-gui-012)
            - [Komponente ändern (OPC_VPV_GUI_013)](#komponente-aendern-opc-vpv-gui-013)
            - [SW-Zertifikat Hochladen (OPC_VPV_GUI_014)](#sw-zertifikat-hochladen-opc-vpv-gui-014)
            - [SW-Zertifikat Überprüfen (OPC_VPV_GUI_015)](#sw-zertifikat-ueberpruefen-opc-vpv-gui-015)
            - [SW-Zertifikat suchen (OPC_VPV_GUI_016)](#sw-zertifikat-suchen-opc-vpv-gui-016)
            - [SW-Zertifikatsliste (OPC_VPV_GUI_017)](#sw-zertifikatsliste-opc-vpv-gui-017)
            - [SW-Zertifikat Details (OPC_VPV_GUI_018)](#sw-zertifikat-details-opc-vpv-gui-018)
        - [Client-Verwaltung - GUI](#client-verwaltung-gui)
            - [Startdialog Client-Verwaltung (OPC_CLV_GUI_001)](#startdialog-client-verwaltung-opc-clv-gui-001)
            - [Stammdaten suchen (OPC_CLV_GUI_101)](#stammdaten-suchen-opc-clv-gui-101)
            - [Stammdaten Liste (OPC_CLV_GUI_102)](#stammdaten-liste-opc-clv-gui-102)
            - [Stammdaten Detailinformationen (OPC_CLV_GUI_103)](#stammdaten-detailinformationen-opc-clv-gui-103)
            - [Anschluss suchen (OPC_CLV_GUI_104)](#anschluss-suchen-opc-clv-gui-104)
            - [Anschluss Liste (OPC_CLV_GUI_105)](#anschluss-liste-opc-clv-gui-105)
            - [Anschluss Detailinformationen (OPC_CLV_GUI_106)](#anschluss-detailinformationen-opc-clv-gui-106)
            - [Gino suchen (OPC_CLV_GUI_111)](#gino-suchen-opc-clv-gui-111)
            - [Gino Liste (OPC_CLV_GUI_112)](#gino-liste-opc-clv-gui-112)
            - [Gino Detailinformationen (OPC_CLV_GUI_113)](#gino-detailinformationen-opc-clv-gui-113)
            - [Gino Resource Status (OPC_CLV_GUI_114)](#gino-resource-status-opc-clv-gui-114)
            - [Gino Gruppen Zuordnungen (OPC_CLV_GUI_115)](#gino-gruppen-zuordnungen-opc-clv-gui-115)
            - [Gino Kalve Aufruf ”_fwcheck” (OPC_CLV_GUI_116)](#gino-kalve-aufruf-fwcheck-opc-clv-gui-116)
            - [Gino Kalve Aufruf ”_server-config” (OPC_CLV_GUI_117)](#gino-kalve-aufruf-server-config-opc-clv-gui-117)
            - [Gino Kalve Aufruf ”_alive” (OPC_CLV_GUI_118)](#gino-kalve-aufruf-alive-opc-clv-gui-118)
            - [Gino Kalve Aufruf ”_scriptcheck” (OPC_CLV_GUI_119)](#gino-kalve-aufruf-scriptcheck-opc-clv-gui-119)
            - [GINO-Konfigurationsparameter erstellen (OPC_CLV_GUI_135)](#gino-konfigurationsparameter-erstellen-opc-clv-gui-135)
            - [GINO-Konfigurationsparameter löschen (OPC_CLV_GUI_136)](#gino-konfigurationsparameter-loeschen-opc-clv-gui-136)
            - [GINO-Gruppenliste ausgeben (OPC_CLV_GUI_140)](#gino-gruppenliste-ausgeben-opc-clv-gui-140)
            - [GINO-Gruppe Konfiguration anzeigen_erstellen_ändern (OPC_CLV_GUI_142)](#gino-gruppe-konfiguration-anzeigen-erstellen-aendern-opc-clv-gui-142)
            - [GINO-Gruppe Firmware anzeigen_erstellen_ändern (OPC_CLV_GUI_143)](#gino-gruppe-firmware-anzeigen-erstellen-aendern-opc-clv-gui-143)
            - [GINO-Gruppe Task anzeigen_erstellen_ändern (OPC_CLV_GUI_144)](#gino-gruppe-task-anzeigen-erstellen-aendern-opc-clv-gui-144)
            - [GINO-Gruppe Script-Task anzeigen_erstellen_ändern (OPC_CLV_GUI_146)](#gino-gruppe-script-task-anzeigen-erstellen-aendern-opc-clv-gui-146)
            - [GINO Gruppenmitglieder verwalten (OPC_CLV_GUI_145)](#gino-gruppenmitglieder-verwalten-opc-clv-gui-145)
            - [GINO-Firmwareliste ausgeben (OPC_CLV_GUI_150)](#gino-firmwareliste-ausgeben-opc-clv-gui-150)
            - [GINO-Firmware hochladen (OPC_CLV_GUI_151)](#gino-firmware-hochladen-opc-clv-gui-151)
            - [GINO-Scriptliste ausgeben (OPC_CLV_GUI_152)](#gino-scriptliste-ausgeben-opc-clv-gui-152)
            - [GINO-Script hochladen (OPC_CLV_GUI_153)](#gino-script-hochladen-opc-clv-gui-153)
            - [GINO-Sperrliste ausgeben (OPC_CLV_GUI_084)_KVW-12096_](#gino-sperrliste-ausgeben-opc-clv-gui-084-kvw-12096)
            - [GINO mit Seriennummer sperren (OPC_CLV_GUI_085)](#gino-mit-seriennummer-sperren-opc-clv-gui-085)
            - [GINO mit Seriennummer für andere Instanz exportieren (OPC_CLV_GUI_086)](#gino-mit-seriennummer-fuer-andere-instanz-exportieren-opc-clv-gui-086)
            - [Provider (OPC_CLV_GUI_089)](#provider-opc-clv-gui-089)
            - [Provider anlegen (OPC_CLV_GUI_090)](#provider-anlegen-opc-clv-gui-090)
            - [Provider ändern (OPC_CLV_GUI_091)](#provider-aendern-opc-clv-gui-091)
            - [GINO Produktionsdaten importieren (OPC_CLV_GUI_087)](#gino-produktionsdaten-importieren-opc-clv-gui-087)
        - [Session-Verwaltung - GUI](#session-verwaltung-gui)
            - [Startdialog Session-Verwaltung (OPC_SEV_GUI_001)](#startdialog-session-verwaltung-opc-sev-gui-001)
            - [Anmeldung suchen (OPC_SEV_GUI_002)](#anmeldung-suchen-opc-sev-gui-002)
            - [Anmeldungen Liste (OPC_SEV_GUI_003)](#anmeldungen-liste-opc-sev-gui-003)
            - [Anzahl offener VP-Sessions suchen (OPC_SEV_GUI_004)](#anzahl-offener-vp-sessions-suchen-opc-sev-gui-004)
            - [Anzahl offene VP-Session Details (OPC_SEV_GUI_005)](#anzahl-offene-vp-session-details-opc-sev-gui-005)
            - [Session-Informationen suchen (OPC_SEV_GUI_006)](#session-informationen-suchen-opc-sev-gui-006)
            - [Session-Informationen Liste (OPC_SEV_GUI_007)](#session-informationen-liste-opc-sev-gui-007)
            - [Session Details (OPC_SEV_GUI_008)](#session-details-opc-sev-gui-008)
            - [Request Liste (OPC_SEV_GUI_009)](#request-liste-opc-sev-gui-009)
            - [Dialog_Session beenden suchen (OPC_SEV_GUI_010)](#dialog-session-beenden-suchen-opc-sev-gui-010)
            - [Dialog_Session beenden Liste (OPC_SEV_GUI_011)_KVW-12428_](#dialog-session-beenden-liste-opc-sev-gui-011-kvw-12428)
        - [Nachrichten Verwaltung - GUI](#nachrichten-verwaltung-gui)
            - [Startdialog Nachrichten (OPC_MES_GUI_001)](#startdialog-nachrichten-opc-mes-gui-001)
            - [Nachricht erstellen (OPC_MES_GUI_002)](#nachricht-erstellen-opc-mes-gui-002)
            - [Nachricht prüfen (OPC_MES_GUI_003)](#nachricht-pruefen-opc-mes-gui-003)
            - [Nachricht suchen (OPC_MES_GUI_004)](#nachricht-suchen-opc-mes-gui-004)
            - [Nachrichten Liste (OPC_MES_GUI_005)](#nachrichten-liste-opc-mes-gui-005)
            - [Nachrichten Detail (OPC_MES_GUI_006)](#nachrichten-detail-opc-mes-gui-006)
            - [Adressaten Liste (OPC_MES_GUI_007)](#adressaten-liste-opc-mes-gui-007)
        - [Chipkartenverwaltung - GUI](#chipkartenverwaltung-gui)
            - [Startdialog Chipkartenverwaltung (OPC_CKV_GUI_001)](#startdialog-chipkartenverwaltung-opc-ckv-gui-001)
            - [Chipkarten Suchkriterien (OPC_CKV_GUI_002)](#chipkarten-suchkriterien-opc-ckv-gui-002)
            - [Chipkarten Ergebnis Liste (OPC_CKV_GUI_003)](#chipkarten-ergebnis-liste-opc-ckv-gui-003)
            - [Chipkarten Details (OPC_CKV_GUI_004)](#chipkarten-details-opc-ckv-gui-004)
            - [Chipkarte bearbeiten (OPC_CKV_GUI_005)](#chipkarte-bearbeiten-opc-ckv-gui-005)
            - [Chipkarte Endzustand setzen (OPC_CKV_GUI_006)](#chipkarte-endzustand-setzen-opc-ckv-gui-006)
            - [Fotoinformation Suchen (OPC_CKV_GUI_007)](#fotoinformation-suchen-opc-ckv-gui-007)
            - [Fotoinformation Details (OPC_CKV_GUI_008)](#fotoinformation-details-opc-ckv-gui-008)
            - [Fotoinformationen beenden (OPC_CKV_GUI_009)](#fotoinformationen-beenden-opc-ckv-gui-009)
            - [Foto-Avisos anlegen (OPC_CKV_GUI_010)](#foto-avisos-anlegen-opc-ckv-gui-010)
            - [Foto-Metadaten löschen (OPC_CKV_GUI_011)](#foto-metadaten-loeschen-opc-ckv-gui-011)
            - [Chipkarte-Verwalten löschen (OPC_CKV_GUI_012)](#chipkarte-verwalten-loeschen-opc-ckv-gui-012)
            - [Chipkarte-Verwalten DELETE-ZIP herunterladen (OPC_CKV_GUI_013)](#chipkarte-verwalten-delete-zip-herunterladen-opc-ckv-gui-013)
        - [ASWH-Verwaltung - GUI](#aswh-verwaltung-gui)
            - [Startdialog ASWH-Verwaltung (OPC_AHV_GUI_001)](#startdialog-aswh-verwaltung-opc-ahv-gui-001)
            - [Hersteller importieren (OPC_AHV_GUI_001)](#hersteller-importieren-opc-ahv-gui-001)
            - [Hersteller exportieren (OPC_AHV_GUI_002)](#hersteller-exportieren-opc-ahv-gui-002)
    - [OPC - Funktionen](#opc-funktionen)
        - [Anmelden am OPC](#anmelden-am-opc)
            - [Anmelden (BF_ANM_FKT_001)](#anmelden-bf-anm-fkt-001)
            - [Abmelden (BF_ABM_FKT_001)](#abmelden-bf-abm-fkt-001)
            - [Fenster ‚Hauptmenü e-card Betriebs-Client‘ ausgeben (BF_ANM_FKT_006)](#fenster-hauptmenue-e-card-betriebs-client-ausgeben-bf-anm-fkt-006)
        - [Allgemeine Administration - Funktionen](#allgemeine-administration-funktionen)
            - [Systemparameter ausgeben (OPC_ALLG_FKT_001)](#systemparameter-ausgeben-opc-allg-fkt-001)
            - [Systemparameter ändern (OPC_ALLG_FKT_002)](#systemparameter-aendern-opc-allg-fkt-002)
            - [Systemparameter hinzufügen (OPC_ALLG_FKT_003)](#systemparameter-hinzufuegen-opc-allg-fkt-003)
            - [Systemparameter-Metainfo bearbeiten (OPC_ALLG_FKT_004)](#systemparameter-metainfo-bearbeiten-opc-allg-fkt-004)
            - [Systemparameter löschen (OPC_ALLG_FKT_005)](#systemparameter-loeschen-opc-allg-fkt-005)
            - [System-Gruppen abfragen (OPC_ALLG_FKT_006)](#system-gruppen-abfragen-opc-allg-fkt-006)
            - [Doku-Schlüssel lesen (OPC_ALLG_FKT_010)](#doku-schluessel-lesen-opc-allg-fkt-010)
            - [Doku-Schlüssel speichern (OPC_ALLG_FKT_011)](#doku-schluessel-speichern-opc-allg-fkt-011)
        - [Vertragspartnerverwaltung - Funktionen](#vertragspartnerverwaltung-funktionen)
            - [Vertragspartner suchen (OPC_VPV_FKT_001)](#vertragspartner-suchen-opc-vpv-fkt-001)
            - [Vertragspartnerdetails suchen (OPC_VPV_FKT_002)](#vertragspartnerdetails-suchen-opc-vpv-fkt-002)
            - [Vertragspartner suspendieren (OPC_VPV_FKT_003)](#vertragspartner-suspendieren-opc-vpv-fkt-003)
            - [Suspendierung eines Vertragspartners aufheben (OPC_VPV_FKT_004)](#suspendierung-eines-vertragspartners-aufheben-opc-vpv-fkt-004)
            - [SW-Zertifikat bereitstellen (OPC_VPV_FKT_006)](#sw-zertifikat-bereitstellen-opc-vpv-fkt-006)
            - [SW-Zertifikat suchen (OPC_VPV_FKT_007)](#sw-zertifikat-suchen-opc-vpv-fkt-007)
            - [SW-Zertifikat Details suchen (OPC_VPV_FKT_008)](#sw-zertifikat-details-suchen-opc-vpv-fkt-008)
            - [SW-Zertifikat sperren _ entsperren (OPC_VPV_FKT_009)](#sw-zertifikat-sperren-entsperren-opc-vpv-fkt-009)
            - [Rollen ermitteln (OPC_VPV_FKT_010)](#rollen-ermitteln-opc-vpv-fkt-010)
            - [Ausprägungscodes ermitteln (OPC_VPV_FKT_012)](#auspraegungscodes-ermitteln-opc-vpv-fkt-012)
            - [Komponenten ermitteln (OPC_VPV_FKT_013)](#komponenten-ermitteln-opc-vpv-fkt-013)
            - [Komponente ändern (OPC_VPV_FKT_014)](#komponente-aendern-opc-vpv-fkt-014)
            - [Ordinationen suchen (OPC_VPV_FKT_015)](#ordinationen-suchen-opc-vpv-fkt-015)
            - [Ordinationen aus Datei lesen (OPC_VPV_FKT_016)](#ordinationen-aus-datei-lesen-opc-vpv-fkt-016)
            - [Zugeordnete Rollen ermitteln (OPC_VPV_FKT_017)](#zugeordnete-rollen-ermitteln-opc-vpv-fkt-017)
            - [Zugeordnete Sperren ermitteln (OPC_VPV_FKT_018)](#zugeordnete-sperren-ermitteln-opc-vpv-fkt-018)
            - [Zugeordnete Piloten ermitteln (OPC_VPV_FKT_019)](#zugeordnete-piloten-ermitteln-opc-vpv-fkt-019)
            - [Zugeordnete Verträge ermitteln (OPC_VPV_FKT_020)](#zugeordnete-vertraege-ermitteln-opc-vpv-fkt-020)
            - [Rollen zuordnen (OPC_VPV_FKT_021)](#rollen-zuordnen-opc-vpv-fkt-021)
            - [Sperren zuordnen (OPC_VPV_FKT_022)](#sperren-zuordnen-opc-vpv-fkt-022)
            - [Piloten zuordnen (OPC_VPV_FKT_023)](#piloten-zuordnen-opc-vpv-fkt-023)
            - [SW-Zertifikat importieren (OPC_VPV_FKT_024)](#sw-zertifikat-importieren-opc-vpv-fkt-024)
        - [Client-Verwaltung - Funktionen](#client-verwaltung-funktionen)
            - [Detailinformation – GINO ermitteln (OPC_CLV_FKT_026)](#detailinformation-gino-ermitteln-opc-clv-fkt-026)
            - [Report „Prozentuelle Auswertung der am System angemeldeten Vertragspartner“ erstellen (OPC_CLV_FKT_056)](#report-prozentuelle-auswertung-der-am-system-angemeldeten-vertragspartner-erstellen-opc-clv-fkt-056)
            - [Report „Details der am System angemeldeten Vertragspartner“ erstellen (OPC_CLV_FKT_057)](#report-details-der-am-system-angemeldeten-vertragspartner-erstellen-opc-clv-fkt-057)
            - [Client-Stammdaten suchen (OPC_CLV_FKT_109)](#client-stammdaten-suchen-opc-clv-fkt-109)
            - [Übersiedlung stornieren (OPC_CLV_FKT_117)](#uebersiedlung-stornieren-opc-clv-fkt-117)
            - [Übernahme stornieren (OPC_CLV_FKT_118)](#uebernahme-stornieren-opc-clv-fkt-118)
            - [Bereich für IP-Adresse ermitteln (OPC_CLV_FKT_124)](#bereich-fuer-ip-adresse-ermitteln-opc-clv-fkt-124)
            - [IP-Adresse aus Bereich ermitteln (OPC_CLV_FKT_125)](#ip-adresse-aus-bereich-ermitteln-opc-clv-fkt-125)
            - [GINO-Gruppenliste ermitteln (OPC_CLV_FKT_140)](#gino-gruppenliste-ermitteln-opc-clv-fkt-140)
            - [GINO-Gruppe Konfiguration ermitteln (OPC_CLV_FKT_141)](#gino-gruppe-konfiguration-ermitteln-opc-clv-fkt-141)
            - [GINO-Gruppe speichern (OPC_CLV_FKT_142)](#gino-gruppe-speichern-opc-clv-fkt-142)
            - [GINO-Konfiguration Zuordnung speichern (OPC_CLV_FKT_143)](#gino-konfiguration-zuordnung-speichern-opc-clv-fkt-143)
            - [GINO-Firmware Zuordnung speichern (OPC_CLV_FKT_144)](#gino-firmware-zuordnung-speichern-opc-clv-fkt-144)
            - [GINO-Konfigurationsgruppe löschen (OPC_CLV_FKT_145)](#gino-konfigurationsgruppe-loeschen-opc-clv-fkt-145)
            - [GINO Gruppenmitglieder ermitteln (OPC_CLV_FKT_146)](#gino-gruppenmitglieder-ermitteln-opc-clv-fkt-146)
            - [GINO Anschlüsse ermitteln (OPC_CLV_FKT_147)](#gino-anschluesse-ermitteln-opc-clv-fkt-147)
            - [GINO Gruppenmitglieder hinzufügen (OPC_CLV_FKT_148)](#gino-gruppenmitglieder-hinzufuegen-opc-clv-fkt-148)
            - [GINO Gruppenmitglieder löschen (OPC_CLV_FKT_149)](#gino-gruppenmitglieder-loeschen-opc-clv-fkt-149)
            - [GINO-Firmware speichern (OPC_CLV_FKT_150)](#gino-firmware-speichern-opc-clv-fkt-150)
            - [GINO-Gruppe Firmware ermitteln (OPC_CLV_FKT_151)](#gino-gruppe-firmware-ermitteln-opc-clv-fkt-151)
            - [GINO-Firmware Versionen ermitteln (OPC_CLV_FKT_152)](#gino-firmware-versionen-ermitteln-opc-clv-fkt-152)
            - [GINO-Firmwaregruppe löschen (OPC_CLV_FKT_153)](#gino-firmwaregruppe-loeschen-opc-clv-fkt-153)
            - [GINO-Task Gruppe ermitteln (OPC_CLV_FKT_161)](#gino-task-gruppe-ermitteln-opc-clv-fkt-161)
            - [GINO-Task Zuordnung speichern (OPC_CLV_FKT_162)](#gino-task-zuordnung-speichern-opc-clv-fkt-162)
            - [GINO-Task Gruppe löschen (OPC_CLV_FKT_163)](#gino-task-gruppe-loeschen-opc-clv-fkt-163)
            - [GINO-Resource Status ermitteln (OPC_CLV_FKT_164)](#gino-resource-status-ermitteln-opc-clv-fkt-164)
            - [GINO-Script Gruppe ermitteln (OPC_CLV_FKT_165)](#gino-script-gruppe-ermitteln-opc-clv-fkt-165)
            - [GINO-Script Zuordnung speichern (OPC_CLV_FKT_166)](#gino-script-zuordnung-speichern-opc-clv-fkt-166)
            - [GINO-Script Gruppe löschen (OPC_CLV_FKT_167)](#gino-script-gruppe-loeschen-opc-clv-fkt-167)
            - [GINO-Scripte ermitteln (OPC_CLV_FKT_168)](#gino-scripte-ermitteln-opc-clv-fkt-168)
            - [GINO-Script speichern (OPC_CLV_FKT_169)](#gino-script-speichern-opc-clv-fkt-169)
            - [GINO-Sperrliste ermitteln (OPC_CLV_FKT_170)_KVW-12096_](#gino-sperrliste-ermitteln-opc-clv-fkt-170-kvw-12096)
            - [GINO sperren (OPC_CLV_FKT_171)](#gino-sperren-opc-clv-fkt-171)
            - [GINOs entsperren (OPC_CLV_FKT_172)](#ginos-entsperren-opc-clv-fkt-172)
            - [GINO-Resource Historie ermitteln (OPC_CLV_FKT_173)](#gino-resource-historie-ermitteln-opc-clv-fkt-173)
            - [Provider ermitteln (OPC_CLV_FKT_174)](#provider-ermitteln-opc-clv-fkt-174)
            - [Provider anzeigen (OPC_CLV_FKT_175)](#provider-anzeigen-opc-clv-fkt-175)
            - [Provider löschen (OPC_CLV_FKT_176)](#provider-loeschen-opc-clv-fkt-176)
            - [Provider speichern (OPC_CLV_FKT_177)](#provider-speichern-opc-clv-fkt-177)
            - [Provider ändern (OPC_CLV_FKT_178)](#provider-aendern-opc-clv-fkt-178)
            - [GINO Seriennummern aus Datei lesen (OPC_CLV_FKT_179)](#gino-seriennummern-aus-datei-lesen-opc-clv-fkt-179)
            - [GINOs sperren (OPC_CLV_FKT_180)](#ginos-sperren-opc-clv-fkt-180)
            - [GINOs für andere Instanz vorbereiten (OPC_CLV_FKT_181)](#ginos-fuer-andere-instanz-vorbereiten-opc-clv-fkt-181)
            - [GINO Produktionsdaten aus Datei lesen (OPC_CLV_FKT_182)](#gino-produktionsdaten-aus-datei-lesen-opc-clv-fkt-182)
            - [GINO Produktionsdaten importieren (OPC_CLV_FKT_183)](#gino-produktionsdaten-importieren-opc-clv-fkt-183)
            - [GINO-Konfigurationsparameter erstellen (OPC_CLV_FKT_154)](#gino-konfigurationsparameter-erstellen-opc-clv-fkt-154)
            - [GINO-Konfigurationsparameter löschen (OPC_CLV_FKT_155)](#gino-konfigurationsparameter-loeschen-opc-clv-fkt-155)
        - [Session-Verwaltung - Funktionen](#session-verwaltung-funktionen)
            - [Session(s) suchen (OPC_SEV_FKT_001)](#session-s-suchen-opc-sev-fkt-001)
            - [Anzahl offener VP-Sessions ermitteln (OPC_SEV_FKT_002)](#anzahl-offener-vp-sessions-ermitteln-opc-sev-fkt-002)
            - [Anmeldungen ermitteln (OPC_SEV_FKT_003)](#anmeldungen-ermitteln-opc-sev-fkt-003)
            - [Sessiondetails ermitteln (OPC_SEV_FKT_004)](#sessiondetails-ermitteln-opc-sev-fkt-004)
            - [Session beenden (OPC_SEV_FKT_005)](#session-beenden-opc-sev-fkt-005)
        - [Nachrichten - Funktionen](#nachrichten-funktionen)
            - [Adressaten ermitteln (OPC_MES_FKT_001)](#adressaten-ermitteln-opc-mes-fkt-001)
            - [Nachricht speichern (OPC_MES_FKT_002)](#nachricht-speichern-opc-mes-fkt-002)
            - [Nachricht suchen (OPC_MES_FKT_003)](#nachricht-suchen-opc-mes-fkt-003)
            - [Details einer Nachricht ermitteln (OPC_MES_FKT_004)](#details-einer-nachricht-ermitteln-opc-mes-fkt-004)
            - [Adressaten einer Nachricht ermitteln (OPC_MES_FKT_007)](#adressaten-einer-nachricht-ermitteln-opc-mes-fkt-007)
            - [Nachricht löschen (OPC_MES_FKT_005)](#nachricht-loeschen-opc-mes-fkt-005)
        - [Chipkartenverwaltung - Funktionen](#chipkartenverwaltung-funktionen)
            - [Karten suchen (OPC_CKV_FKT_001)](#karten-suchen-opc-ckv-fkt-001)
            - [Fotoinformation suchen (OPC_CKV_FKT_002)](#fotoinformation-suchen-opc-ckv-fkt-002)
            - [Bemerkung zur Karte ändern (OPC_CKV_FKT_003)](#bemerkung-zur-karte-aendern-opc-ckv-fkt-003)
            - [Erlaubte Statusübergänge prüfen (OPC_CKV_FKT_004)](#erlaubte-statusuebergaenge-pruefen-opc-ckv-fkt-004)
            - [Erlaubte Statusübergänge für OPC prüfen (OPC_CKV_FKT_005)](#erlaubte-statusuebergaenge-fuer-opc-pruefen-opc-ckv-fkt-005)
            - [Endstatus setzen (OPC_CKV_FKT_006)](#endstatus-setzen-opc-ckv-fkt-006)
            - [Fotoinformation beenden (OPC_CKV_FKT_007)](#fotoinformation-beenden-opc-ckv-fkt-007)
            - [Fotoinformationen beenden (OPC_CKV_FKT_008)](#fotoinformationen-beenden-opc-ckv-fkt-008)
            - [Foto-Avisos anlegen (OPC_CKV_FKT_009)](#foto-avisos-anlegen-opc-ckv-fkt-009)
            - [VSNRs für Fotoinformation lesen (OPC_CKV_FKT_010)](#vsnrs-fuer-fotoinformation-lesen-opc-ckv-fkt-010)
            - [Foto-Metadaten suchen (OPC_CKV_FKT_011)](#foto-metadaten-suchen-opc-ckv-fkt-011)
            - [Foto-Metadaten löschen (OPC_CKV_FKT_012)](#foto-metadaten-loeschen-opc-ckv-fkt-012)
        - [ASWH-Verwaltung - Funktionen](#aswh-verwaltung-funktionen)
            - [Herstellerdaten importieren (OPC_AHV_FKT_001)](#herstellerdaten-importieren-opc-ahv-fkt-001)
            - [Herstellerdaten exportieren (OPC_AHV_FKT_002)](#herstellerdaten-exportieren-opc-ahv-fkt-002)
    - [OPC - Parameter und Parameterguppierungen](#opc-parameter-und-parameterguppierungen)
        - [Parametergruppe GINO-Gruppe](#parametergruppe-gino-gruppe)
        - [Parametergruppe GINO-Konfiguration-Parameter](#parametergruppe-gino-konfiguration-parameter)
        - [Parametergruppe GINO-Gruppen-Mitglied](#parametergruppe-gino-gruppen-mitglied)
        - [Parametergruppe GINO-Firmware](#parametergruppe-gino-firmware)
        - [Parametergruppe GINO-Produktionsdaten](#parametergruppe-gino-produktionsdaten)
        - [Parametergruppe GINO-Task](#parametergruppe-gino-task)
        - [Parametergruppe GINO-Script-Task](#parametergruppe-gino-script-task)
        - [Parametergruppe GINO-Script](#parametergruppe-gino-script)
        - [Parametergruppe GINO-Resource Status](#parametergruppe-gino-resource-status)

---



# Pflichtenheft OPC

Die Seite und die Unterseiten beschreiben die Funktionen des Operations-Client (OPC) .

Der OPC ist ein GUI zur Verwaltung und Anzeige der für den Betrieb notwendigen Daten.

* Unter [OPC - GUI](#opc-gui) sind die Dialogmasken des OPC beschrieben
* Unter [OPC - Funktionen](#opc-funktionen) sind die Funktionen beschrieben
* Unter [OPC - Parameter und Parameterguppierungen](#opc-parameter-und-parameterguppierungen) sind die von den Funktionen verwendeten Parameter beschrieben.   
  Nur für Teile der Clientverwaltung relevant, ansonsten sind die Parameter direkt bei den Funktionen beschrieben.

Mit der Übernahme nach Confluence wurde das Word-Dokument [PH\_501\_ecard\_OPC.docx](http://ecardcm/svn/ECARD/dokumentation/e-card/technische_spezifikation/pflichtenhefte/Operations-Client/archiv/PH_501_ecard_OPC.docx) abgelöst. (  R24b).  Die Beschreibung der Abläufe wurde  endgültig entfernt, da diese wenig Mehrwert für das Verständnis bringen und schon davor nicht mehr gewartet wurden bzw. für neue Funktionalitäten nicht hinzugefügt wurden.

Mit R14a erfolgte die Zusammenlegung der OPC-Funktionalität in einem PH. Die Kürzel für die Funktionen und Abläufe wurden verändert, die Kürzel für die Meldungen wurden jedoch nicht verändert.  
Aufgrund der späten Zusammenlegung ergeben sich auch Unterschiede in der Struktur der Beschreibungen.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


## OPC - GUI

Für alle Masken dieses Kapitels gilt folgendes.  
 Mögliche Spalten in der Beschreibung:

* Maskenfeld: Name der Maske
* M/K Muss/Kann
* E/A Eingabefeld/Ausgabefeld
* Beschreibung
* Format


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Allgemeine Ansichten




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Hauptmenü OPC (OPC_GEN_GUI_001)

Vor der Anzeige werden die Berechtigungen des Benutzers ermittelt und nur Links zu den Abläufen aktiviert und angezeigt, für die die ermittelten Rechte ausreichend sind.

|  |  |
| --- | --- |
| Allgemeine Administration | Link auf die Maske „Startdialog Allgemeine Administration (OPC\_ALG\_GUI\_001)" |
| Vertragspartner-Verwaltung | Link auf die Maske „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Client-Verwaltung | Link auf die Maske „Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)" |
| Session-Verwaltung | Link auf die Maske „Startdialog Session-Verwaltung" |
| Nachrichten | Link auf die Maske „Startdialog Nachrichten  " |
| Chipkarten-Verwaltung | Link auf die Maske „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |
| ASWH-Verwaltung | Link auf die Maske „Startdialog ASWH-Verwaltung (OPC\_AHV\_GUI\_001)" |

Durch Auswahl des jeweiligen Menüpunktes wird in das entsprechende Untemenü verzweigt. Wird vom Benutzer der Link „Home" gewählt, so wird wieder die Funktionsübersicht ausgegeben.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Loginseite (OPC_GEN_GUI_002)

Diese Maske bindet die Loginmethodik des Servers an und bietet dem Benutzer die Möglichkeit sich zu authentifizieren.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Login | Aufruf von „Anmelden (BF\_ANM\_FKT\_001)" |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Benutzername | - | M | E | Der Name des Benutzers | alphanumerische Zeichen |
| Kennwort | - | M | E | Das Passwort des Benutzers, geschütztes Eingabefeld | alphanumerische Zeichen |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Rückfrage (OPC_GEN_GUI_004)

Es wird eine Frage mit zwei möglichen Antworten eingeblendet, der Benutzer kann Zustimmen oder Ablehnen.   
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Bestätigung | Seitenabhängig, siehe Beschreibung beim Aufruf. |
| Ablehnung | Seitenabhängig, siehe Beschreibung beim Aufruf. |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| ShortText |  | K | A | Title der Rückmeldung |  |
| LongText |  | K | A | Vollständige Meldung |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Erfolgsmeldung (OPC_GEN_GUI_005)

Die Erfolgsmeldung kann aus diversen Abläufen heraus aufgerufen werden.   
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Schließen | zurück zum Startdialog des aktuellen Ablaufes. |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| ShortText |  | K | A | Title der Rückmeldung |  |
| LongText |  | K | A | Vollständige Meldung |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Fehlerseite (OPC_GEN_GUI_006)

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Fehlernummer |  | K | A | Wird angezeigt wenn eine Fehlernummer gesetzt wurde. |  |
| Fehlertyp |  | K | A | Wird angezeigt wenn ein Fehlertyp gesetzt wurde. |  |
| Details |  | K | A | Wird angezeigt wenn Fehlerdetails oder Fehlermeldungen gesetzt wurden. |  |
| Zurück zur Seite |  | K | A | Link zur Seite die den Fehler geworfen hat. Wird angezeigt wenn die FehlerUrl gesetzt wurde. | Link |
| Home |  | M | A | Link auf „Hauptmenü OPC (OPC\_GEN\_GUI\_001)" | Link |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Allgemeine Administration - GUI




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Startdialog Allgemeine Administration (OPC_ALG_GUI_001)

Im Startdialog des Menüpunktes „Allgemeine Administration“ werden die Administrationsfunktionen der allgemeinen Administration ausgegeben.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Systemparameter | Link auf die Maske „“.  Es können die Systemparameter gesammelt, aufgerufen werden oder gruppenweise (für jede Gruppe wird ein Unterpunkt im Menü angelegt). Die bekannten Gruppen können über „“ ermittelt werden. |
| Doku-Schlüssel bereitstellen | Link auf die Maske „“ |
| Datenbank-Instanz | Link auf die Maske „“ |
| Home | Link auf das |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Systemparameter (OPC_ALG_GUI_002)

Anzeigename: Systemparameter konfigurieren [Systemparameter]  
Anzeigename der Unterpunktaufrufe: Systemparameter konfigurieren [<Gruppenname>]

Maske zur Anzeige und Bearbeitung von Systemparametern.

Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Speichern | Es wird die Eingabe auf der Seite deaktiviert und eine Bestätigung durch den Benutzer eingefordert.  Bestätigt der Benutzer die Eingaben, so wird die Funktion „“ aufgerufen  Im Erfolgsfall wird die Maske „“ angezeigt. Im Fehlerfall die Fehlermeldung ausgegeben.  Lehnt der Benutzer die Bestätigung ab, so wird die Seite neu aufgebaut und die Felder sind wieder gemäß ihren Parameter ediertbar. |
| Hinzufügen (nur in den Untergruppen) | Es wird die Liste "Systemparameter" ausgeblendet und stattdessen das Formular für das Hinzufügen "Neuer Systemparameter" angezeigt, welche vom Benutzer wie gewünscht ausgefüllt werden kann.  Durch ein erneutes Auswählen des "Hinzufügen" Buttons wird ein neuer Systemparameter mit den eingegebenen Werten angelegt.  [Systemparameter hinzufügen (OPC\_ALLG\_FKT\_003)](#systemparameter-hinzufuegen-opc-allg-fkt-003)  Im Erfolgsfall wird die Maske „“ angezeigt. Im Fehlerfall die Fehlermeldung ausgegeben. Am Ende des Prozesses wird die Seite neu aufgebaut und die Felder sind wieder gemäß ihren Parameter ediertbar. |

Formular "Systemparameter"

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Parameter Name | M | A | Wurde keine Kategorie ausgewählt, wird hinter dem Namen des Parameters auch die Gruppe angefügt.   Wurde eine Kategorie ausgewählt, wird nur der Name des Parameters ausgegeben, ohne Kategorie. | Formatspezifikation gemäß Rückmeldung |
| Parameter Wert | M | E | Der aktuelle Wert des Parameters.   Das Feld ist editierbar, wenn das „Editable“ Flag des Parameters gesetzt ist, sonst nicht. | Die Validierung erfolgt gegen das Feld „Valid" des Parameters. |
| Hilfe Link | M | A | Öffnet die Hilfeseite für den aktuellen Parameter in einem neuen Fenster/Tab, siehe „“ | Link |

Formular "Neuer Systemparameter"

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Gruppe | M | A | Die Gruppe des neuen Parameters |  |
| Name | M | E | Der Name des neuen Parameters | Max. 100 Zeichen |
| Wert | M | E | Der Standardwert des neuen Parameters | Max. 1000 Zeichen |
| Beschreibung | K | E | Die Beschreibung des neuen Parameters | Max. 1000 Zeichen |
| Regex | K | E | Die "Regex" Regel zur Validierung des Parameters | Max. 1000 Zeichen |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Doku-Schlüssel bereitstellen (OPC_ALG_GUI_003)

|  |  |
| --- | --- |
| Bereitstellen | Aufruf der Funktion „Doku-Schlüssel lesen (OPC\_ALLG\_FKT\_010)".   Im Erfolgsfall, Weiterleitung auf die Maske „Doku-Schüssel prüfen".   Im Fehlerfall, verbleib auf dieser Maske und ausgabe der Fehlermeldung. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Allgemeine Administration (OPC\_ALG\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Dateiname | M | E | Dateiupoad eines validen Zertifikates. | Dateiinput |
| Empfängertyp | M | E | Mögliche Werte:   * Psuedonymisierungsstelle * ÖGGH | Auswahl |
| Auftragstyp | M | E | Mögliche Werte:   * Datenblätter der Vorsorgeuntersuchung * Datenblätter DMP * Dokumentationsblätter BKF | Auswahl |
| Schüsseltyp | M | E | Mögliche Werte:   * Sensibles Objekt * Identifikator * Standort | Auswahl |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Doku-Schüssel prüfen (OPC_ALG_GUI_004)

|  |  |
| --- | --- |
| Speichern | Aufruf der Funktion „Doku-Schlüssel speichern (OPC\_ALLG\_FKT\_011)"   Im Erfolgsfall Weiterleitung auf die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)". |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Allgemeine Administration (OPC\_ALG\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Empfängertyp | M | A | Der gewählte Empfängertyp | Alphanumerisch |
| Auftragstyp | M | A | Der gewählte Auftragstyp | Alphanumerisch |
| Schlüsseltyp | M | A | Der gewählte Schlüsseltyp | Alphanumerisch |
| Seriennummer | M | A | Die ermittelte Seriennummer des hochgeladenen Doku-Schlüssels | Numerisch |
| Aussteller | M | A | Der ermittelte Aussteller des hochgeladenen Doku-Schlüssels | Alphanumerisch |
| Gültigkeitsbeginn | M | A | Der Beginn der ermittelten Gültigkeit des hochgeladenen Doku-Schlüssels | ‚TT.MM.JJJJ' |
| Gültigkeitsende | M | A | Das Ende der ermittelten Gültigkeit des hochgeladenen Doku-Schlüssels | ‚TT.MM.JJJJ' |
| Algorithmus | M | A | Der ermittelte Algorithmus des hochgeladenen Doku-Schlüssels | Alphanumerisch |
| DN | M | A | Die ermittelte DN des hochgeladenen Doku-Schlüssels | Alphanumerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Datenbank-Instanz (OPC_ALG_GUI_005)

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Iteration | M | A | Die in den Parametern gesetzte Entwicklungsiteration. | Alphanumerisch |
| Datenbank URL | M | A | Die URL zur aktuell verbundenen Datenbankinstanz. | Alphanumerisch |
| Schema | M | A | Das aktuell aktive Datenbank Schema. | Alphanumerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Systemparameter Hilfeseite (OPC_ALG_GUI_005)

Anzeigename: Hilfe [<Parametername>]  
Diese Maske stellt die Informationen zu einem Parameter dar und wird nicht innerhalb des OPCs geöffnet, sondern in einem zusätzlichen Tab bzw. Fenster.

Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Bearbeiten | Es werden die Parameter "Beschreibung" und "Regular Expression" zu Eingabefeldern umgeschaltet |
| Speichern (nur nach dem Start von "Bearbeiten") | Es wird die Funktion [Systemparameter bearbeiten (OPC\_ALLG\_FKT\_004)](#systemparameter-metainfo-bearbeiten-opc-allg-fkt-004) mit den geänderten Werten aufgerufen. |
| Löschen | Es wird die Funktion [Systemparameter löschen (OPC\_ALLG\_FKT\_005)](#systemparameter-loeschen-opc-allg-fkt-005) aufgerufen. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Parametername | M | A | Der vollständige Name des Parameters. | Alphanumerisch |
| Defaultwert | K | A | Der Standardwert des Parameters, wenn vorhanden. Es ist jener Wert, der beim Anlegen für diesen Parameter eingetragen wurde. | Alphanumerisch |
| Beschreibung | M | E/A | Die Beschreibung des Parameters. Ausgabe einer entsprechenden Meldung, falls der Parameter leer ist. | Alphanumerisch (max. 1000 Zeichen) |
| Regular Expression | M | E/A | Die Regular Expression, mit der dieser Parameter validiert wird (insofern sie nicht leer gelassen wird).  Ausgabe einer entsprechenden Meldung, falls der Parameter leer ist. | (max. 1000 Zeichen) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Vertragspartner-Verwaltung - GUI




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Startdialog Vertragspartner Verwaltung (OPC_VPV_GUI_001)

Im Startdialog des Menüpunktes „Vertragspartner Verwaltung" werden die Administrationsfunktionen der Vertragspartnerverwaltung ausgegeben.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Vertragspartner Suchen | Link auf die Maske „Vertragspartner Suchen (OPC\_VPV\_GUI\_002)" |
| Ordinationen suchen | Link auf die Maske „Ordination Suchen (OPC\_VPV\_GUI\_006)" |
| Ordinationen aus Datei lesen | Link auf die Maske „Ordinationen aus Datei lesen (OPC\_VPV\_GUI\_010)" |
| Komponenten verwalten | Link auf die Maske „Komponenten Liste (OPC\_VPV\_GUI\_012)" |
| SW-Zertifikat bereitstellen | Link auf die Maske „SW-Zertifikat Hochladen (OPC\_VPV\_GUI\_014)" |
| SW-Zertifikat suchen | Link auf die Maske „SW-Zertifikat suchen (OPC\_VPV\_GUI\_016)" |
| Home | Link auf das Hauptmenü OPC (OPC\_GEN\_GUI\_001) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Vertragspartner Suchen (OPC_VPV_GUI_002)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Suchen | Aufruf der Funktion „Vertragspartner suchen (OPC\_VPV\_FKT\_001)"   Im Erfolgsfall Weiterleitung auf die Maske „Vertragspartner Liste (OPC\_VPV\_GUI\_003)".   Im Fehlerfall: Ausgabe der Fehlermeldung. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | K | E | Die VPNR eines Vertragspartners. | Numerisch |
| Zuname | K | E |  | Alphanumerisch |
| Vorname | K | E |  | Alphanumerisch |
| PLZ | K | E |  | Numerisch |
| Suspendierung | M | E | Mögliche Werte:   * alle * suspendiert * nicht suspendiert | Auswahl |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Vertragspartner Liste (OPC_VPV_GUI_003)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Ausgewählten Vertragspartner anzeigen | Anzeige der Maske: „Vertragspartner Details (OPC\_VPV\_GUI\_004)" für den gewählten Vertragspartner.   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Zurück | Anzeigen der Maske „Vertragspartner Suchen (OPC\_VPV\_GUI\_002) |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Treffer | M | A | Anzahl der Treffer | Numerisch |  |
| Pro Vertragspartner |  |  |  |  |
| Auswahl | M | E | Auswahlelement für die Aktion „Ausgewählten Vertragspartner anzeigen" | RadioButton |  |
| VP Nr. | M | E | Link auf die Maske: „Vertragspartner Details (OPC\_VPV\_GUI\_004)" für diesen Vertragspartner | Link, Numerisch |  |
| Zuname | M | A |  | Alphanumerisch |  |
| Vorname | M | A |  | Alphanumerisch |  |
| Plz | M | A |  | Numerisch |  |
| Ort | M | A |  | Alphanumerisch |  |
| BLD | M | A |  | Numerisch |  |
| suspendiert | M | A | Indikator ob der Vertragspartner suspendiert ist, oder nicht. | Checkbox |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Vertragspartner Details (OPC_VPV_GUI_004)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Suspendierung aktivieren | Anzeige der Maske „Vertragspartner Suspendierung Ändern (OPC\_VPV\_GUI\_005)"   Nur aktiv wenn der Vertragspartner das Flag „suspendiert" nicht gesetzt hat. |
| Suspendierung aufheben | Anzeige der Maske „Vertragspartner Suspendierung Ändern (OPC\_VPV\_GUI\_005)"   Nur aktiv wenn der Vertragspartner das Flag „suspendiert" gesetzt hat. |
| Zurück | Anzeigen der Maske „Vertragspartner Liste (OPC\_VPV\_GUI\_003) |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A | Die VPNR eines Vertragspartners. | Numerisch |
| LEA-ID | M | A |  |  |
| ZPV-ID | M | A |  |  |
| Vorname | M | A |  | Alphanumerisch |
| Zuname | M | A |  | Alphanumerisch |
| Straße | M | A |  | Alphanumerisch |
| PLZ | M | A |  | Alphanumerisch |
| Ort | M | A |  | Alphanumerisch |
| Bundesland | M | A |  | Alphanumerisch |
| Suspendierungsgrund | M | A |  | Alphanumerisch |
| Suspendierungsdatum | M | A |  | ‚TT.MM.JJJJ' |
| Bearbeiter | M | A |  | Alphanumerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Vertragspartner Suspendierung Ändern (OPC_VPV_GUI_005)

|  |  |
| --- | --- |
| Zurück | Anzeigen der Maske „Vertragspartner Details (OPC\_VPV\_GUI\_004)" |
| Suspendierung aktivieren | Aktiv wenn der ausgewählte Vertragspartner nicht suspendiert ist. |
| Suspendierung aufheben | Aktiv wenn der ausgewählte Vertragspartner suspendiert ist. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A | Die VPNR eines Vertragspartners. | Numerisch |
| Suspendierungsgrund | M | E | Der Grund für die Statusänderung. | Textfeld |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Ordination Suchen (OPC_VPV_GUI_006)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Suchen | Aufruf der Funktion: „Ordinationen suchen (OPC\_VPV\_FKT\_015)"   Im Erfolgsfall:Anzeige der Maske „Ordinationen Liste (OPC\_VPV\_GUI\_007)"   Im Fehlerfall: ausgabe der Fehlermeldung. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | K | E | Die VPNR eines Vertragspartners. | Numerisch |
| Ordinationsnummer | K | E |  | Numerisch |
| Ausprägung | K | E |  | Auswahl |
| Rolle | K | E |  | Auswahl |
| Sperre | K | E |  | Auswahl |
| Pilot | K | E |  | Auswahl |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Ordinationen Liste (OPC_VPV_GUI_007)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Alle Auswählen | Wählt alle „Auswahl" Elemente in der Liste aus. |
| Auswahl aufheben | Setzt alle „Auswahl" Elemente in der Liste zurück. |
| Rollen zuordnen | Im Erfolgsfall: Aufruf der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „ROLLE"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Sperren zuordnen | Im Erfolgsfall: Aufruf der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „SPERREN"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Piloten zuordnen | Im Erfolgsfall: Aufruf der Maske: „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „PILOTEN"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Zurück | Anzeigen der Maske „Ordination Suchen (OPC\_VPV\_GUI\_006)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Treffer | M | A | Anzahl der Treffer | Numerisch |  |
| Pro Ordination |  |  |  |  |
| Auswahl | M | E |  | LOV |  |
| VPNR | M | A | Link zur Maske „Zuordnungen anzeigen (OPC\_VPV\_GUI\_008)" für das gewählte Element. | Link, Numerisch |  |
| ORDNR | M | A | Link zur Maske „Zuordnungen anzeigen (OPC\_VPV\_GUI\_008)" für das gewählte Element. | Link, Numerisch |  |
| Ausprägung | M | A |  | Alphanumerisch |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Zuordnungen anzeigen (OPC_VPV_GUI_008)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Zurück | Anzeigen der Maske „Ordinationen Liste (OPC\_VPV\_GUI\_007)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| VPNR | M | A |  | Numerisch |  |
| ORDNR | M | A |  | Numerisch |  |
| Ausprägung | M | A |  | Alphanumerisch |  |
| Rechte | M | E | Anzeigen der „Rechte Liste" in der aktuellen Maske. Standartmäßig aktiv. | Link |  |
| Verträge | M | E | Anzeigen der „Verträge Liste" in der aktuellen Maske. | Link |  |
| Rollen | M | E | Anzeigen der „Rollen Liste" in der aktuellen Maske. | Link |  |
| Sperren | M | E | Anzeigen der „Sperren Liste" in der aktuellen Maske. | Link |  |
| Piloten | M | E | Anzeigen der „Piloten Liste" in der aktuellen Maske. | Link |  |
| Rechte Liste, pro Recht ein Eintrag |  |  |  |  |
| Recht | M | A | Code der Berechtigung. | Numerisch |  |
| Dialog | M | A | Flag, zeigt an ob die Ordination über diese Berechtigung verfügt. | Checkbox |  |
| Beschreibung | M | A | Beschreibung der Berechtigung. | Alphanumerisch |  |
| Verträge Liste, pro Vertrag ein Eintrag |  |  |  |  |
| Typ | M | A |  | Alphanumerisch |  |
| FG | M | A |  | Numerisch |  |
| SVT\_L | M | A |  | Numerisch |  |
| SVT\_V | M | A |  | Numerisch |  |
| Rollen Liste, pro Rolle ein Eintrag |  |  |  |  |
| Indikator | M | A | Zeigt an ob die Ordination über diese Rolle verfügt. | Checkbox |  |
| Rolle | M | A | Name der Rolle | Alphanumerisch |  |
| Beschreibung | M | A | Beschreibung der Rolle | Alphanumerisch |  |
| Sperren Liste, pro Komponente für die Sperren erlaubt sind, ein Eintrag |  |  |  |  |
| Indikator | M | A | Zeigt an ob die Ordination für diese Komponente gesperrt ist. | Checkbox |  |
| Komponente | M | A | Code der Komponente | Alphanumerisch |  |
| Beschreibung | M | A | Beschreibung der Komponente | Alphanumerisch |  |
| Piloten Liste, pro Pilot-Komponente ein Eintrag |  |  |  |  |
| Indikator | M | A | Zeigt an ob die Ordination für den Piloten zugelassen ist. | Checkbox |  |
| Komponente | M | A | Code der Komponente | Alphanumerisch |  |
| Beschreibung | M | A | Beschreibung der Komponente | Alphanumerisch |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Ordinationen Zuordnung ändern (OPC_VPV_GUI_009)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Zuordnung durchführen | Aufruf der Maske: „Rückfrage (OPC\_GEN\_GUI\_004)",   * Bei BestätigungParameter = „ROLLEN": Aufruf der Funktion „Rollen zuordnen (OPC\_VPV\_FKT\_021)" Parameter = „SPERREN": Aufruf der Funktion „Sperren zuordnen (OPC\_VPV\_FKT\_022)"Parameter = „PILOTEN": Aufruf der Funktion „Piloten zuordnen (OPC\_VPV\_FKT\_023)"         Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"     Im Fehlerfall: Ausgabe der Fehlermeldung. * Bei Ablehnung Anzeige der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" |
| Zurück | Wurden die Ordinationen über Suchkriterien ausgewählt: Aufruf der Maske: „Ordinationen Liste (OPC\_VPV\_GUI\_007)"   Wurden die Ordinationen über eine Datei ausgewählt: Aufruf der Maske „Datei Info anzeigen (OPC\_VPV\_GUI\_011)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| pro ZuordnungParameter |  |  |  |  |
| Rolle | M | A | Kurze Bezeichnung der Rolle.     Nur angezeigt wenn die Zuordnungsart auf „ROLLE" gesetzt ist. | Alphanumerisch |  |
| Komponente | M | A | Kurze Bezeichnung der Komponente.     Nur angezeigt wenn die Zuordnungsart nicht auf „ROLLE" gesetzt ist. | Alphanumerisch |  |
| Aktuell | M | A | Aktueller Status der Zuordnung.   Mögliche Werte   * „gesetzt" alle Ordinationen sind zugeordnet * „nicht gesetzt" keine Ordination ist zugeordnet * „unterschiedlich" es existieren sowohl Ordinationen die zugeordnet sind, als auch solche die nicht zugeordent sind. |  |  |
| Zuordnen | M | E | Flag um die Ordinationen dem aktuellen Parameter zuzuordnen.     Eingabe deaktiviert, wenn keine Zuordnung möglich ist (bereits alle zugeordnet) | Checkbox |  |
| Entfernen | M | E | Flag um die Zuordnung der Ordinationen zumaktuellen Parameter aufzuheben.     Eingabe deaktiviert, wenn kein Entfernen möglich ist (es existiert keine Zuordnung) | Checkbox |  |
| Beschreibung | M | A | Lange Beschreibung des Parameters | Alphanumerisch |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Ordinationen aus Datei lesen (OPC_VPV_GUI_010)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Datei lesen | Aufruf der Funktion „Ordinationen aus Datei lesen (OPC\_VPV\_FKT\_016)"   Im Erfolgsfall: Anzeige der Maske „Datei Info anzeigen (OPC\_VPV\_GUI\_011)"   Im Fehlerfall: Ausgabe der Fehlermeldung. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Ordinationsliste | M | E |  | Dateiupload |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Datei Info anzeigen (OPC_VPV_GUI_011)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Rollen zuordnen | Im Erfolgsfall: Aufruf der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „ROLLE"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Sperren zuordnen | Im Erfolgsfall: Aufruf der Maske „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „SPERREN"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Piloten zuordnen | Im Erfolgsfall: Aufruf der Maske: „Ordinationen Zuordnung ändern (OPC\_VPV\_GUI\_009)" mit dem Parameter Aktion = „PILOTEN"   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Auswahl) |
| Zurück | Anzeigen der Maske „Ordination Suchen (OPC\_VPV\_GUI\_006)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Ausgewählte Datei | M | A | Der Dateiname des Uploads. | Alphanumerisch |
| Anzahl Ordinationen | M | A | Anzahl der Ordinationen die in der Datei gefunden wurden. | Numerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Komponenten Liste (OPC_VPV_GUI_012)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Komponente ändern | Im Erfolgsfall: Aufruf der Maske „Komponente ändern (OPC\_VPV\_GUI\_013)" für die durch „Auswahl" festgelegte Komponente.   Im Fehlerfall: Ausgabe der Fehlermeldung (nichts ausgewählt) |
|  |  |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| pro Komponente |  |  |  |  |
| Auswahl | M | E | Listenauswahl Element | RadioButton |  |
| Komponente | M | A | Name der Komponente | Alphanumerisch |  |
| Sperre möglich | M | A | Indikator ob diese Komponente gesperrt ist. | Checkbox |  |
| Pilot möglich | M | A | Indikator ob diese Komponente ein Pilot ist. | Checkbox |  |
| Voll Ab | M | A | Startdatum des Vollbetriebs | ‚TT.MM.JJJJ' |  |
| Pilot Ab | M | A | Startdatum des Pilotbetriebs | ‚TT.MM.JJJJ' |  |
| Beschreibung | M | A | Komponentenbeschreibung | Alphanumerisch |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Komponente ändern (OPC_VPV_GUI_013)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Komponente ändern | Im Erfolgsfall: Aufruf der Maske „Rückfrage (OPC\_GEN\_GUI\_004)"   * Bei BestätigungAufruf der Funktion „Komponente ändern (OPC\_VPV\_FKT\_014)" Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" * Bei Ablehnung: Anzeige der Maske „Komponente ändern (OPC\_VPV\_GUI\_013)"     Im Fehlerfall: Anzeige der Fehlermeldung (nur Validierungsfehler) |
| Zurück | Anzeige der Maske Komponenten Liste (OPC\_VPV\_GUI\_012) |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Komponente | M | A | Name der Komponente | Alphanumerisch |
| Sperren möglich | M | A | Indikator ob eine Sperre für diese Komponente gesetzt werden kann. | Checkbox |
| Pilot möglich | M | A | Indikator ob diese Komponente im Pilotbetrieb ist. | Checkbox |
| Pilot ab | K | E | Startdatum des Pilotbetriebes.   Nur aktiv wenn | ‚TT.MM.JJJJ' |
| Voll ab | M | E |  | ‚TT.MM.JJJJ' |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikat Hochladen (OPC_VPV_GUI_014)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Bereitstellen | Aufruf der Funktion „SW-Zertifikat bereitstellen (OPC\_VPV\_FKT\_006)"   Im Erfolgsfall: Anzeige der Maske „SW-Zertifikat Überprüfen (OPC\_VPV\_GUI\_015)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| SW-Zertifikat | M | E | Datei mit dem Zertifikat. | Dateiupload |
| Vertragspartnernummer | M | E | VPNR eines existierenden Vertragspartners | Numerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikat Überprüfen (OPC_VPV_GUI_015)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Speichern | Aufruf der Funktion „SW-Zertifikat importieren (OPC\_VPV\_FKT\_024)"   Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A |  | Numerisch |
| Zuname | M | A |  | Alphanumerisch |
| Seriennummer | M | A |  | Numerisch |
| Seriennummer (HEX) | M | A |  | Alphanumerisch |
| Aussteller | M | A |  | Alphanumerisch |
| SHA1-Fingerprint | M | A |  | Alphanumerisch |
| SHA256-Fingerprint | M | A |  | Alphanumerisch |
| Gültigkeitsbeginn | M | A |  | ‚TT.MM.JJJJ' |
| Gültigkeitsende | M | A |  | ‚TT.MM.JJJJ' |
| Aktiv-KZ | M | A |  | Checkbox |
| Algorithmus | M | A |  | Alphanumerisch |
| DN | M | A |  | Alphanumerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikat suchen (OPC_VPV_GUI_016)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Suchen | Aufruf der Funktion „SW-Zertifikat suchen (OPC\_VPV\_FKT\_007)"   Im Erfolgsfall: Anzeige des Ergebnisses in der Maske „SW-Zertifikatsliste (OPC\_VPV\_GUI\_017)"   Im Fehlerfall: Ausgabe der Fehlermeldung. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | E | Die Nummer des Vertragspartners | Numerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikatsliste (OPC_VPV_GUI_017)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| SW-Zertifikat Details anzeigen | Im Erfolgsfall: Anzeige der Maske „SW-Zertifikat Details (OPC\_VPV\_GUI\_018)" für das durch „Auswahl" gewählte Zertifikat.   Im Fehlerfall: Ausgabe einer Fehlermeldung (keine Auswahl) |
| Zurück | Anzeigen der Maske „SW-Zertifikat suchen (OPC\_VPV\_GUI\_016)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A | Die Nummer des Vertragspartners | Numerisch |  |
| pro SW-Zertifikat |  |  |  |  |
| Auswahl | M | E | Legt das Zertifikat als ausgewählt fest. | RadioButton |  |
| Seriennummer | M | A | Link zur Maske „SW-Zertifikat Details (OPC\_VPV\_GUI\_018)" für dieses SW-Zertifikat | Numerisch |  |
| HEX | M | A |  | Alphanumerisch |  |
| VP Nr. | M | A |  | Numerisch |  |
| Zuname | M | A |  | Alphanumerisch |  |
| Aktiv-KZ | M | A | Indikator ob das Zertifikat aktiv ist. | Checkbox |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikat Details (OPC_VPV_GUI_018)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Vertragspartner Verwaltung (OPC\_VPV\_GUI\_001)" |
| Sperren | Anzeigen der Maske „Rückfrage (OPC\_GEN\_GUI\_004)"   * Bei BestätigungAusführen der Funktion „SW-Zertifikat sperren / entsperren (OPC\_VPV\_FKT\_009)" mit dem Sperren Kennzeichen.Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" * Bei AblehnungAnzeige der Maske „SW-Zertifikat Details (OPC\_VPV\_GUI\_018)"     Angezeigt wenn das SW-Zertifikat aktiv ist. |
| Entsperren | Anzeigen der Maske „Rückfrage (OPC\_GEN\_GUI\_004)"   * Bei BestätigungAusführen der Funktion „SW-Zertifikat sperren / entsperren (OPC\_VPV\_FKT\_009)" mit dem Entsperren Kennzeichen.Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" * Bei AblehnungAnzeige der Maske „SW-Zertifikat Details (OPC\_VPV\_GUI\_018)"     Angezeigt wenn das SW-Zertifikat nicht aktiv ist. |
| Zurück | Anzeigen der Maske „SW-Zertifikatsliste (OPC\_VPV\_GUI\_017)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A | Die Nummer des Vertragspartners | Numerisch |
| Zuname | M | A |  | Alphanumerisch |
| Seriennummer | M | A |  | Numerisch |
| Seriennummer (HEX) | M | A |  | Alphanumerisch |
| Aussteller | M | A |  | Alphanumerisch |
| SHA1-Fingerprint | M | A |  | Alphanumerisch |
| SHA256-Fingerprint | M | A |  | Alphanumerisch |
| Gültigkeitsbeginn | M | A |  | ‚TT.MM.JJJJ' |
| Gültigkeitsende | M | A |  | ‚TT.MM.JJJJ' |
| Aktiv-KZ | M | A |  | Checkbox |
| Algorithmus | M | A |  | Alphanumerisch |
| DN | M | A |  | Alphanumerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Client-Verwaltung - GUI




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Startdialog Client-Verwaltung (OPC_CLV_GUI_001)

Im Startdialog des Menüpunktes „Client-Verwaltung" werden die Administrationsfunktionen der Client-Verwaltung ausgegeben.  
Allgemeine Beschreibung:   
Für die neuen Masken, (>100) ist folgendes einheitliches Verhalten umgesetzt:

* Alle Masken haben einen Button „Schliessen" (ganz rechts) welcher wieder den Startdialog (ohne Maske) anzeigt.
* Die Masken haben einen Button "Zurück" (zweiter Button von rechts), welche im Normalfall die zuvor aufgerufene Maske anzeigt.
  + Falls der Aufruf direkt vom Menü erfolgt, so fehlt dieser Button.
  + Bei der Erfolgsmeldung fehlt der Button. (Nur „Anzeigen" und „Schliessen" möglich).
  + Die angezeigte Maske ist nicht immer die tatsächlich zuletzt angezeigte Maske, da manchmal intern eine Funktionalität aufgerufen wird, welche dann bei Zurück angezeigt wird. Z.B. wird beim Button Vorgänger der Maske „die Maske „Stammdaten Detailinformationen (OPC\_CLV\_GUI\_103)"" intern die Suche aufgerufen. D.h. der Button Zurück, zeigt dann die die Maske „Stammdaten suchen (OPC\_CLV\_GUI\_101)" an.
* Jede Funktion die den Datenbestand ändert, ist durch eine Abfrage zu bestätigen. Die Abfrage wird oberhalb der Maske mit den Daten angezeigt.
* Nach Ändern von Daten wird eine sinnvolle Erfolgsmeldung (diese wird im PH nicht genau definiert)) angezeigt. Danach kann man wieder zur Anzeige der Daten wechseln („Button Anzeige").

Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Provider | Link auf die Maske „Provider (OPC\_CLV\_GUI\_089)" |
| Client Stammdaten | Nicht aktivierbare Gruppe für folgende 4 Einträge |
| Exot anlegen | Link auf die Maske „Stammdaten Detailinformationen (OPC\_CLV\_GUI\_103)" zur Eingabe von Daten |
| Stammdaten | Link auf die Maske „Stammdaten suchen (OPC\_CLV\_GUI\_101)" |
| Anschluss | Link auf die Maske „Anschluss suchen (OPC\_CLV\_GUI\_104)" |
| GINO | Link auf die Maske „Gino suchen (OPC\_CLV\_GUI\_111)" |
| GINO Gruppe | Nicht aktivierbare Gruppe für folgende 3 Einträge |
| GINO-Konfiguration | Link auf die Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| GINO-Firmware | Link auf die Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| GINO-Task | Link auf die Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| GINO-Script | Link auf die Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| GINO-Firmware | Nicht aktivierbare Gruppe für folgende 2 Einträge |
| Firmwareliste ausgeben | Link auf die Maske „GINO-Firmwareliste ausgeben (OPC\_CLV\_GUI\_150)" |
| Firmware hochladen | Link auf die Maske „GINO-Firmware hochladen (OPC\_CLV\_GUI\_151)" |
| GINO-Script | Nicht aktivierbare Gruppe für folgende 2 Einträge |
| Scriptliste ausgeben | Link auf die Maske „GINO-Scriptliste ausgeben (OPC\_CLV\_GUI\_152)" |
| Script hochladen | Link auf die Makse „GINO-Script hochladen (OPC\_CLV\_GUI\_153)" |
| GINO-Sperre |  |
| Gino Sperrliste ausgeben | Link auf die Maske „GINO-Sperrliste ausgeben (OPC\_CLV\_GUI\_084)" |
| Gino sperren | Link auf die Maske „GINO mit Seriennummer sperren (OPC\_CLV\_GUI\_085)" |
| Gino Gerät exportieren | Link auf die Maske „GINO mit Seriennummer für andere Instanz exportieren (OPC\_CLV\_GUI\_086)" |
| Home | Link auf das Hauptmenü OPC (OPC\_GEN\_GUI\_001) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Stammdaten suchen (OPC_CLV_GUI_101)

Anzeigename: Stammdaten suchen (OPC\_CLV\_GUI\_101)  
Diese Maske dient zum Suchen von Stammdaten.   
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Ausgabe einer Liste der laut Suchkriterien gefundenen Stammdaten.  Aufruf der Funktion „".(Suchebene Stammdaten) Abhänig vom Ergebnis der Suche wird mit folgender Aktion fortgesetzt:   * kein Treffer: Anzeige der Fehlermeldung „Es wurde kein Eintrag gefunden" * genau ein Treffer: Anzeigen der Maske „" * mehr als ein Treffer: Anzeigen der Maske „" |

Folgende Suchkriterien können angegeben werden. (keine Vorbelegung)

| **Suchkriterium** | **Anmerkung** |
| --- | --- |
| Stammdaten-relevante Felder |  |
| Zuordnungstyp | Drop-Down-Liste („alle", „Exot", „Standard") |
| Vertragspartnernummer | Wildcardsuche möglich  Max. 6 alphanumerische Zeichen (0-9a-zA-Z-.\_?\*) |
| Ordinationsnummer | Max 2 numerische Zeichen |
| Zuname | Wildcardsuche möglich  70 alphanumerische Zeichen |
| Vorname | Wildcardsuche möglich  70 alphanumerische Zeichen |
| Anschlussart | Drop-DownListe("alle","GIN","leer") |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Stammdaten Liste (OPC_CLV_GUI_102)

Anzeigename: Stammdaten Suchergebnis (OPC\_CLV\_GUI\_102)  
In dieser Maske wird die Ergebnisliste der Suche auf der Maske „Stammdaten suchen (OPC\_CLV\_GUI\_101)" angezeigt.   
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Vertragspartnernummer | Die Vertragspartnernummer ist als Link ausgeführt. Durch Auswahl einer Vertragspartnernummer werden analog zu „Anzeigen" die Detailinformationen dargestellt.". |
| Anzeigen | Ausgabe von Detailinformationen zu den ausgewählten Stammdaten. Anzeigen der Maske „Stammdaten Detailinformationen (OPC\_CLV\_GUI\_103)". |

Die Liste der gefundenen Einträge enthält folgdene Felder

* Vertragspartnernummer
* Ordinationsnummer


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Stammdaten Detailinformationen (OPC_CLV_GUI_103)

Anzeigename: Stammdaten Detailinformationen (OPC\_CLV\_GUI\_103)  
Auf dieser Maske werden Detailinformationen der Stammdaten angezeigt und geändert.  
Zum Anzeigen der Daten stehen folgende Aktionen zur Verfügung. In diesem Fall sind die Felder nicht editierbar.

|  |  |
| --- | --- |
| Ändern | Anzeigen dieser Maske zum Ändern/Anlegen von Daten.. |
| Ord. Übernehmen | Anzeige dieser Maske für „Ordination übernehmen".  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN nicht gesetzt ist.* |
| Übersiedlung Stornieren | Die vorbereitete Übersiedlung wird stoniert.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten ein Vorgänger eingetragen ist und das Kennzeichen KZ\_UEBERNOMMEN nicht gesetzt ist.*  Aufruf der Funktion „Übersiedlung stornieren (OPC\_CLV\_FKT\_117)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung. |
| Übernahme Stornieren | Die Übernahme wird stoniert.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN gesetzt ist.*  Aufruf der Funktion „Übernahme stornieren (OPC\_CLV\_FKT\_118)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung. |
| Anschluss anzeigen | Die Anschlüsse des Stammdatensatzes werden ermittelt.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN nicht gesetzt ist.*  Aufruf der Funktion „".(Suchebene Anschluss, Stammdaten-ID wird übergeben) Abhänig vom Ergebnis der Suche wird mit folgender Aktion fortgesetzt:   * kein Treffer: Anzeige einer Fehlermeldung „Kein Eintrag gefunden" * genau ein Treffer: Anzeigen der Maske „" * mehr als ein Treffer: Anzeigen der Maske „" (als Suchkriterium werden Vertragspartnernummer und Ordinationsnummer angezeigt) |
| Anschluss ausstatten | Für die Stammdaten wird ein weiterer Anschluss angelegt.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN nicht gesetzt ist.*  Aufruf der Funktion „Anschluss ausstatten (CV\_BAS\_FKT\_102)". und Anzeigen der zurückgelieferten Daten in der Maske „" für das Anlegen.Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Vorgänger | Die Daten des Vorgängers angezeigt.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERNOMMEN gesetzt ist.*  Es werden alle Stammdaten gesucht, welche die aktuellen Stammdaten als Nachfolger eingetragen und das Kennzeichen KZ\_UEBERGEBEN gesetzt haben.  Im Erfolgsfall: Ausgabe der Daten des Vorgängers in dieser Maske oder Ausgabe einer Liste von Stammdaten..Im Fehlerfall (nur bei ungültiger Datenkonstellation): Ausgabe der Fehlermeldung. |
| Nachfolger | Die Daten des Nachfolgers werden angezeigt.  *Hinweis: Der Button wird nur dann angezeigt, falls für die Stammdaten das Kennzeichen KZ\_UEBERGEBEN gesetzt ist und ein Nachfolger eingetragen.*  Im Erfolgsfall: Ausgabe der Daten des Nachfolgers in dieser Maske.Im Fehlerfall ((nur bei ungültiger Datenkonstellation): Ausgabe der Fehlermeldung. |

Für das Ändern/Anlegen der Daten sind folgende Eingaben möglich :

Bei Zurodnungstyp Exot sind alle Maskenfelder bis auf Zuordnungstyp und die Felder für Vorgänger, Nachfolger,"Provider Export Aktion" und „Callcenter Export Aktion" editierbar.

Bei Standard sind folgende weitere Felder nicht editierbar: Ausprägung, Ordination aktiv" (Checkbox), Ordinationsnummer, Anrede, Titel Vorne, Vorname, Zuname, Titel Hinten, Straße, PLZ, Ort, Bezirk, Bundeslandcode.  
Die VPNR sollte nur dann geändert werden, wenn ein Stempelverlust vorangegangen ist. Ein entsprechender Hinweis wird ausgegeben.  
  
Hinweis: Das Ändern der Daten für den Export ist in folgenden 2 Fällen sinnvoll:

* Kennzeichen für den Export deaktivieren, fallls man einen für den Export vorgesehenen Datensatz nicht exportieren möchte. In diesem Fall kann man das Kennzeichen später wieder aktivieren. Das Aktivieren für einen nicht vorgesehenen Datensatz hat keine Auswirkungen (Z:B. wird ein Anschluss im EHI\_Netz auch dann nicht für den Providerexport vorgesehenen, falls man das Kennzeichen aktiviert.)
* Löschen es Exportdatums, um einen Datensatz erneut zu exportieren. Das Setzen oder Ändern des Exportdatums hat keine fachlichen Auswirkungen (bei Änderungen von relevanten Daten, wird das Datum unabhängig von der Eingabe auf leer gesetzt).
* Datum "Providerdaten aktualisiert": Damit könnte man einen Datensatz in die Liste der aktualisierten Daten hinzufügen oder entfernen. Falls man (zusätzlich)  ein für den Export relevantes Datum ändert, wird dieses Datum auf alle Fälle auf den aktuellen Wert gesetzt.

Folgende Aktionen stehen zur Verfügung:

|  |  |
| --- | --- |
| Speichern | Die Aktion muss bestätigt werden. („Wollen Sie die Änderung durchführen"). Die Änderungen werden durch Aufruf der Funktion „Stammdaten speichern (CV\_BAS\_FKT\_101)" gespeichert.  Im Erfolgsfall: Ausgabe der Erfolgsmeldung „Die Daten wurden erfolgreich geändert" Im Fehlerfall: Ausgabe der Fehlermeldung. |

Für die Übernahme einer Ordination. sind die Maskenfelder „Vorgänger VPNR" und „Vorgänger Ordinationsnummer" editierbarFolgende Aktion steht zur Verfügung

|  |  |
| --- | --- |
| Ord. übernehmen | Aufruf der Funktion „Ordination übernehmen(CV\_BAS\_FKT\_113)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung „Die Daten wurden erfolgreich geändert".Im Fehlerfall: Ausgabe der Fehlermeldung. |

Die Maske dient zur Verwaltung der Entität Stammdaten (CL\_STAMMDATEN)

| **Maskenfeld** | **Zuordnung Entität Stammdaten** | **Plausibilität** |
| --- | --- | --- |
| Zuordnungstyp | * KZ\_EXOT==1: Exot * sonst: Standard | „Exot", „Standard" |
| Vertragspartnernummer | VPNR | Max. 6 alphanumerische Zeichen (0-9a-zA-Z-.\_) |
| Ausprägung | Ausprägung Code | alphanumerische Zeichen |
| Ordinationsnummer | Ordinationsnummer | 2 numerische Zeichen |
| Ordinationaktiv? | KZ aktiv | Checkbox Mögliche Werte:  „aktiv", „nicht aktiv" |
| Anrede | Anredecode | „Herr", „Frau", „An" |
| Titel Vorne | Titel vorne | 30 alphanumerische Zeichen |
| Vorname | Vorname | 70 alphanumerische Zeichen |
| Zuname | Zuname | 70 alphanumerische Zeichen |
| Titel Hinten | Titel hinten | 30 alphanumerische Zeichen |
| Straße | Strasse | 60 alphanumerische Zeichen |
| PLZ | PLZ | 9 alphanumerische Zeichen |
| Ort | Ort | 40 alphanumerische Zeichen |
| Bezirk | Politischer Bezirk | 2 alphanumerisches Zeichen |
| Bundeslandcode | Bundeslandcode | 2 alphanumerisches Zeichen |
| Vorgänger VPNR | Vorgänger: Anhand der eingetragenen Id, wird Vertragspartnernummer ermittelt | 6 alpha-numerische Zeichen (0-9a-zA-Z-.\_) |
| Vorgänger Ordinationsnummer | Vorgänger: Anhand der eingetragenen Id, wird Ordinationsnummer ermittelt | 2 numerische Zeichen |
| Vorgänger übernommen | KZ übernommen | „ausgewählt", „nicht ausgewählt" (Checkbox) |
| Nachfolger VPNR | Nachfolger: Anhand der eingetragenen Id, wird Vertragspartnernummer ermittelt | 6 alpha-numerische Zeichen (0-9a-zA-Z-.\_) |
| Nachfolger Ordninationsnummer | Nachfolger: Anhand der eingetragenen Id, wird Ordinationsnummer ermittelt | 2 numerische Zeichen |
| Nachfolger übergeben | KZ übergeben | „ausgewählt", „nicht ausgewählt" (Checkbox) |
| Telefonnummer | Telefonnummer | 60 alphanumerische Zeichen |
| KUMS | KUMS | 100 alphanumerische Zeichen |
| Providerdaten aktualisiert | Provider Export Änderung | Timestamp im Format „DD.M.YYYYY HH:MM" |
| Anschlussart | Anschlussart | "GIN",  "" |
| Provider-Export am | Provider Export am | Datum |
| Provider-Export Aktion | Provider Export Aktion | alphanumerische Zeichen |
| Callcenter Export | Kennzeichen Callcenterexport | Checkbox |
| Callcenter-Export Aktion | Callcenter Export am | alphanumerische Zeichen |
| Callcenter-Export am | Callcenter Export Aktion | Datum |
| Billing-Kennzeichen GINA | Billing KZ Gina | „SV", „VP", „KA" |
| Billing-Kennzeichen Karte | Billing KZ Karte | „SV", „VP", „KA" |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anschluss suchen (OPC_CLV_GUI_104)

Anzeigename: Anschluss suchen (OPC\_CLV\_GUI\_104)  
Diese Maske dient zum Suchen von Einträgen der Entität Anschluss.   
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Ausgabe einer Liste der laut Suchkriterien gefundenen Anschlüsse.   Aufruf der Funktion „Client-Stammdaten suchen (OPC\_CLV\_FKT\_109)".(Suchebene Anschluss) Abhänig vom Ergebnis der Suche wird mit folgender Aktion fortgesetzt:   * kein Anschluss: Anzeige einer Fehlermeldung „Es wurde kein Eintrag gefunden" * genau ein Anschluss: Anzeigen der Maske „"Anschluss Detailinformationen (OPC\_CLV\_GUI\_106) * mehr als ein Anschluss: Anzeigen der Maske „Anschluss Liste (OPC\_CLV\_GUI\_105)" |

Folgende Suchkriterien können angegeben werden. (keine Vorbelegung)

| **Suchkriterium** | **Anmerkung** |
| --- | --- |
| Stammdaten-relevante Felder |  |
| Zuordnungstyp | Drop-Down-Liste („alle", „Exot", „Standard") |
| Vertragspartnernummer | Wildcardsuche möglich   Max. 6 alphanumerische Zeichen (0-9a-zA-Z-.\_?\*) |
| Ordinationsnummer | Max 2 numerische Zeichen |
| Zuname | Wildcardsuche möglich   70 alphanumerische Zeichen |
| Vorname | Wildcardsuche möglich   70 alphanumerische Zeichen |
| Anschluss-relevante Felder |  |
| Client-IP | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |
| Subnetz | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |
| Router-IP | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anschluss Liste (OPC_CLV_GUI_105)

Anzeigename: Anschluss Suchergebnis (OPC\_CLV\_GUI\_105)  
In dieser Maske wird die Ergebnisliste der Suche angezeigt.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Client-IP | Die Client-IP ist als Link ausgeführt. Durch Auswahl werden die Detailinformationen analog zur Aktion „Anzeigen" dargestellt. |
| Anzeigen | Ausgabe von Detailinformationen zum ausgewählten Eintrag. Anzeigen der Maske „Anschluss Detailinformationen (OPC\_CLV\_GUI\_106)". |

Die Liste der gefundenen Einträge enthält folgdene Felder

* Client-IP
* Subnetz
* Vertragspartnernummer
* Ordinationsnummer


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anschluss Detailinformationen (OPC_CLV_GUI_106)

Anzeigename: Anschluss Detailinformationen (OPC\_CLV\_GUI\_106)  
Auf dieser Maske werden Detailinformationen der Anschlüsse angezeigt und geändert.   
Für die Anzeige sind die Felder nicht editierbar.Folgende Aktionen stehen zur Verfügung:

|  |  |
| --- | --- |
| Stammdaten anzeigen | Anzeigen der Maske „" für die dem Anschluss zugeordneten Stammdaten |
| Gino(s) anzeigen | Die Ginos des Anschlusses werden ermittelt.  Aufruf der Funktion „".(Suchebene Gino) Abhänig vom Ergebnis der Suche wird mit folgender Aktion fortgesetzt:   * kein Treffer: Anzeige einer Fehlermeldung „Kein Eintrag gefunden" * genau ein Treffer: Anzeigen der Maske „" * mehr als ein Treffer: Anzeigen der Maske „" (als Suchkriterium werden Name, Gruppe, Vertragspartnernummer und Ordinationsnummer, angezeigt) |
| Gina ausstatten | Für den Anschluss wird eine weitere Gina angelegt.  Aufruf der Funktion „GINA ausstatten (CV\_BAS\_FKT\_107)". und Anzeigen der zurückgelieferten Daten in der Maske „" für das Anlegen.Im Fehlerfall: Ausgabe der Fehlermeldung.  *Hinweis: Der Button wird nur dann angezeigt, falls KZ\_FREIGABE gesetzt ist.* |
| Ändern | Anzeigen dieser Maske zum Ändern von Daten  *Hinweis: Der Button wird nur dann angezeigt, falls KZ\_FREIGABE gesetzt ist.* |
| Freigeben | Aufruf der Funktion „Anschluss freigeben (CV\_BAS\_FKT\_105)"  Das Kennzeichen für die Freigabe wird gesetzt, die restlichen Felder werden unverändert angezeigt.  *Hinweis: Der Button wird nur dann angezeigt, falls KZ\_FREIGABE nicht gesetzt ist.* |
| Sperren | Aufruf der Funktion „Anschluss sperren (CV\_BAS\_FKT\_104)"  Das Kennzeichen für die Freigabe wird entfernt, die restlichen Felder werden unverändert angezeigt.  *Hinweis: Der Button wird nur dann angezeigt, falls KZ\_FREIGABE gesetzt ist.* |
| Löschen | Die Aktion muss bestätigt werden. („Wollen Sie den Eintrag wirklich löschen?"). Der Anschluss wird durch Aufruf der Funktion „Anschluss löschen (CV\_BAS\_FKT\_106)" gelöscht  Im Erfolgsfall: Ausgabe der Erfolgsmeldung und Anzeige der Maske „" .Im Fehlerfall: Ausgabe der Fehlermeldung. |

Für das Ändern/Anlegen der Daten sind bis auf Freigabestatus (immer gesetzt) alle Felder editierbar. Das Netz ist nur im Zuge eine Neuanlage änderbar.  
  
Folgende Aktionen stehen zur Verfügung

|  |  |
| --- | --- |
| Netz ändern | Falls das Netz geändert wird, erfolgt ein Aufruf der Funktion „Anschluss ausstatten (CV\_BAS\_FKT\_102" und die zurückgelieferten Daten werden angezeigt. |
| Speichern | Die Aktion muss bestätigt werden. („Wollen Sie die Änderung durchführen"). Die Änderungen werden durch Aufruf der Funktion „Anschluss speichern (CV\_BAS\_FKT\_103)" gespeichert.  Falls sich der Eintrag in „weitere Client-IPs" ändert, werden durch die Funktion auch Zuordnungen in der Entität „zusammenhängede IPs" angepasst, d.h. neue Ips werden eingetragen und nicht mehr aufgelistete IPs entfernt.  Im Erfolgsfall: Ausgabe der Erfolgsmeldung „Die Daten wurden erfolgreich geändert".Im Fehlerfall: Ausgabe der Fehlermeldung. |

Die Maske dient zur Verwaltung der Entität Stammdaten (CL\_ANSCHLUSS)

| **Maskenfeld** | **Zuordnung Entität Anschluss** | **Plausibilität** |
| --- | --- | --- |
| Netz | Anhand „Netz Id" wird aus der Entität Netz die Bezeichnug ermittelt | Drop-Down Menü im Änderunsmodus, ansonsten max 3 Zeichen |
| Client-IP | Client-IP | Max. 15 Zeichen (0-9.?\*) |
| Router-IP | Router-IP. falls die Router-IP nicht belegt ist, wird das Feld nicht angezeigt. | Max. 15 Zeichen (0-9.?\*) |
| Anlegezeitpunkt | Anlegezeitpunkt | Timestamp im Format „DD.M.YYYYY HH:MM" |
| Anschlussnummer | Anschlussnummer | 60 alphanumerische Zeichen |
| Provider | Provider | Drop-Down Menü im Änderunsmodus, ansonsten max 20 Zeichen |
| Callcentergruppen-Id | Callcenter ID | 2 alphanumerische Zeichen |
| Freigabestatus | KZ Freigabe | Checkbox" |
| Automatisch Löschen | Kennzeichen automatisch löschbar | Checkbox" |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Gino suchen (OPC_CLV_GUI_111)

Anzeigename: GINO suchen (OPC\_CLV\_GUI\_111)  
Diese Maske dient zum Suchen von Einträgen der Entität Gino und zugeordneten Entitäten.   
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Suchen | Ausgabe einer Liste der laut Suchkriterien gefundenen Ginos.   Aufruf der Funktion „Client-Stammdaten suchen (OPC\_CLV\_FKT\_109)".(Suchebene Gino) Abhänig vom Ergebnis der Suche wird mit folgender Aktion fortgesetzt:   * kein Treffer: Anzeige einer Fehlermeldung „Es wurde kein Eintrag gefunden" * genau ein Treffer: Anzeigen der Maske „Gino Detailinformationen (OPC\_CLV\_GUI\_113)" * mehr als ein Treffer: Anzeigen der Maske „Gino Liste (OPC\_CLV\_GUI\_112)" |

Folgende Suchkriterien können angegeben werden. (keine Vorbelegung)

| **Suchkriterium** | **Anmerkung** |
| --- | --- |
| Stammdaten-relevante Felder |  |
| Zuordnungstyp | Drop-Down-Liste („alle", „Exot", „Standard") |
| Vertragspartnernummer | Wildcardsuche möglich   Max. 6 alphanumerische Zeichen (0-9a-zA-Z-.\_?\*) |
| Ordinationsnummer | Max 2 numerische Zeichen |
| Zuname | Wildcardsuche möglich   70 alphanumerische Zeichen |
| Vorname | Wildcardsuche möglich   70 alphanumerische Zeichen |
| Anschluss-relevante Felder |  |
| Client-IP | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |
| Subnetz-IP | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |
| Router-IP | Wildcardsuche möglich   Max. 15 Zeichen (0-9.?\*) |
| GINO-relevante Felder |  |
| GINO-Seriennummer | 10 alphanumerische Zeichen   Wildcardsuche möglich   Max. 10 Zeichen (0-9.?\*) |
| GINO-Firmware | Nach klicken des Buttons „Zeige Firmwarefilter" erscheint eine Mehrfachauswahlliste.   Die Liste besteht aus allen verfügbaren Firmware-Versionen. Zusätlich wird zu jeder Firmware-Version in Klammer die Anzahl der aktiven/Gesamtzahl Ginos angegeben.   Bsp.: 01.01.25 (236)   Wird kein Element ausgewählt, so wird die Firmware Version bei der Suche ignoriert, ansonten werden nur Ginos mit den entsprechend ausgewählten Firmware Version angezeigt. |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Gino Liste (OPC_CLV_GUI_112)

Anzeigename: GINO Suchergebnis (OPC\_CLV\_GUI\_112)  
In dieser Maske wird die Ergebnisliste der Suche angezeigt.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Name | Die GINO-Seriennummer ist als Link ausgeführt. Durch Auswahl werden die Detailinformationen dargestellt (analog „Anzeigen") |
| Anzeigen | Ausgabe von Detailinformationen zum ausgewählten Eintrag in der Maske Gino Detailinformationen (OPC\_CLV\_GUI\_113) |

Die Liste der gefundenen Einträge enthält folgende Felder

* Seriennummer
* Name
* Gruppe
* Checkbox freigegeben
* Checkbox aktiv: selektiert falls letzter Kontakt im dafür vorgesehenen Zeitraum (Konfigurationsparameter clv.gino.kontakt) stattgefunden hat.
* Checkbox Satellit: selektiert falls die Seriennummer des Satelliten in CL\_GINO eingetragen ist
* Vertragspartnernummer
* Ordinationsnummer


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Gino Detailinformationen (OPC_CLV_GUI_113)

Anzeigename: GINO Detailinformationen (OPC\_CLV\_GUI\_113)   
Auf dieser Maske werden Detailinformationen der Gino angezeigt.  
Die neben den Client-Stammdaten notwendigen Daten für die Anzeige, werden durch Aufruf der Funktion „Detailinformation – GINO ermitteln (OPC\_CLV\_FKT\_026)" ermittelt.  
Für die Anzeige sind die Felder nicht editierbar. Ausnahme: der Streuwert kann editiert und gespeichert werdenFolgende Aktion steht zur Verfügung:

|  |  |
| --- | --- |
| Anschluss anzeigen | Anzeigen der Maske „Anschluss Detailinformationen (OPC\_CLV\_GUI\_106)" für den dem Gino zugeordneten Anschluss |
| Speichern | Aktualisert den entpsrechenden Eintrag in der Entität GINO. |
| Zurück | Anzeigen der Maske „Gino Liste (OPC\_CLV\_GUI\_112) |
| Resource Status | Anzeigen der Maske „Gino Resource Status (OPC\_CLV\_GUI\_114)" |
| Aktuelle Gruppen Zuordnung | Anzeigen der Maske „Gino Gruppen Zuordnungen (OPC\_CLV\_GUI\_115)" |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

Die Maske dient zur Anzeige der Entität Gino (CL\_GINO)

| **Maskenfeld** | **Zuordnung Entität** | **Plausibilität** |
| --- | --- | --- |
| Entität GINO |  |  |
| Name | Name |  |
| Gruppe | Gruppe |  |
| Seriennummer | Seriennummer |  |
| Status | „nicht freigegeben" falls KZ\_FREIGABE nicht gesetzt, „nicht aktiv" falls „letzter Kontakt" zu weit zurück", „aktiv" falls letzter Kontakt im dafür vorgesehnen Zeitraum (definiert durchKonfigurationsparameter „clv.gino.kontakt", Default 31 Minuten) stattgefunden hat. |  |
| GINO-IP | IP-Adresse | Max. 15 Zeichen (0-9.?\*) |
| Zugriff NAT IP | ZUGRIFF\_NAT\_IP |  |
| Zugriff DNS | ZUGRIFF\_DNS |  |
| Satellit Seriennummer | Serienummer des Satelliten |  |
| Anlegezeitpunkt | Anlegezeitpunkt im Format „DD.M.YYYYY HH:MM" |  |
| Letzter Kontakt | Letzter Kontakt im Format „DD.M.YYYYY HH:MM" |  |
| Firmware Version | Firmware Version |  |
| Streuwert | Streuwert |  |
| Entität Gino Produktionsdaten |  |  |
| MAC Adresse | MAC-Adresse | 20 alphanumerische Zeichen |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Gino Resource Status (OPC_CLV_GUI_114)

Anzeigename: GINO Resource Status (OPC\_CLV\_GUI\_114)   
Auf dieser Maske werden die Status der Gino-Resource angezeigt. Für GINO-Resources die bereits mehrmals aufgerufen wurden, kann man sich, durch einen Klick auf die jeweilige Resource, die Historie anzeigen lassen. Bei einem Klick auf den Aufruf in der Historie, wird das Ergebnis des Aufrufs angezeigt.  
Die neben den Client-Stammdaten notwendigen Daten für die Anzeige, werden durch Aufruf der Funktion „GINO-Resource Status ermitteln (OPC\_CLV\_FKT\_164)" ermittelt.  
Die Historie der einzelnen Resource-Calls wird, durch die Funktion „GINO-Resource Historie ermitteln (OPC\_CLV\_FKT\_173)", bei einem Klick auf die Ressource ermittelt.  
Folgende Aktionen steht zur Verfügung:

|  |  |
| --- | --- |
| Status Refresh | Erneuert Aufuf von „GINO-Resource Status ermitteln (OPC\_CLV\_FKT\_164)" und Ausgabe auf der aktuellen Maske. |
|  | Aktualisert den entpsrechenden Eintrag in der Entität GINO. |
| Zurück | Anzeigen der Vorgänger Maske   „Gino Detailinformationen (OPC\_CLV\_GUI\_113)". |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Client-IP | Attribut Client-IP aus Entität Anschluss | M | A | Anschluss IP der GINO |  |
| Seriennummer | „Seriennummer" aus Enitiät GINO, | M | A | GINO-Seriennummer |  |
| Name | Name aus Entität GINO | M | A | Name |  |
| GINO-IP | IP-Adresse | M | A | IP-Adresse des GINOS |  |
| Zugriff NAT IP | ZUGRIFF\_NAT\_IP | M | A | Zugriff über NAT IP |  |
| Zugriff DNS | ZUGRIFF\_DNS | M | A | Zugriff über DNS |  |
| List der für die Anzeige definierten Aufrufe/Historie der Aufrufe |  |  |  |  |  |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="8d7cb717-45ec-49a7-9f18-5ff20eb78b34"><ac:plain-text-body><![CDATA[ | Aufruf |  | M | A[0:n] | Call aus „[GINO-Resource Status | Parametergruppe GINO-Resource Status]" |  | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="8c7eaf26-3251-4385-b94f-e50da3374063"><ac:plain-text-body><![CDATA[ | erster Aufruf |  | K | A[0:n] | „erster Aufruf" aus „[GINO-Resource Status | Parametergruppe GINO-Resource Status]" |  | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="a87f1b2b-aac0-41c3-9c6a-533700f5280a"><ac:plain-text-body><![CDATA[ | letzter Aufruf |  | K | A[0:n] | „letzter Aufruf" aus „[GINO-Resource Status | Parametergruppe GINO-Resource Status]" |  | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="98421851-3a79-4cc6-8e06-e8c003966a73"><ac:plain-text-body><![CDATA[ | HTTP-Statuscode |  | K | A[0:n] | Response Status aus „[GINO-Resource Status | Parametergruppe GINO-Resource Status]." |  | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="d87ae07e-7692-4489-857c-ea9005f65296"><ac:plain-text-body><![CDATA[ | Fehlercode |  | K | A[0:n] | Error-Code aus „[GINO-Resource Status | Parametergruppe GINO-Resource Status]." |  | ]]></ac:plain-text-body></ac:structured-macro> |
|  |  |  |  |  |  |
| Fehler |  | K |  | Error-Code und Error-Text aus „GINO-Resource Status.",Wird nur für den per Klick ausgwählten Aufrufl angezeigt falls ein Error-Text vorhanden ist. |  |
| Input |  | K |  | Siehe Input Liste aus „GINO-Resource Status", wird nur für den per Klick ausgwählten Aufrufl angezeigt. |  |
| Output |  | K |  | Siehe Output Liste aus „GINO-Resource Status", wird nur für den per Klick ausgwählten Aufrufl angezeigt. |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Gino Gruppen Zuordnungen (OPC_CLV_GUI_115)

Anzeigename: GINO Gruppen Zuordnungen (OPC\_CLV\_GUI\_115)   
Auf dieser Maske werden alle relevanten Gruppen eines Gino angezeigt (KALVE\_FKT\_011). Dabei werden jeweils alle relevanten Gruppen pro Typ ermittelt. Die Gruppen werden nach Typ gruppiert ausgegeben. Die Sortierung innerhalb einer Typ-Gruppe erfolgt von „am spezifischsten" nach „am allgemeinsten".  
Folgende Aktion steht zur Verfügung:

|  |  |
| --- | --- |
| Aufruf Firmware | Anzeigen der Maske „Gino Kalve Aufruf "/fwcheck" (OPC\_CLV\_GUI\_116) für den entsprechenden GINO. |
| Aufruf Server Konfiguration | Anzeigen der Maske „Gino Kalve Aufruf "/server-config" (OPC\_CLV\_GUI\_117)" für den entsprechenden GINO. |
| Aufruf Alive | Anzeigen der Maske „Gino Kalve Aufruf "/alive" (OPC\_CLV\_GUI\_118)"" für den entsprechenden GINO. |
| Aufruf Script Check | Anzeigen der Maske „Gino Kalve Aufruf "/scriptcheck" (OPC\_CLV\_GUI\_119)" für den entsprechenden GINO. |
| Zurück | Anzeigen der Maske „Gino suchen (OPC\_CLV\_GUI\_111)" |

| **Maskenfeld** | **Zuordnung Entität** | **Plausibilität** |
| --- | --- | --- |
| Entität GINO |  |  |
| Name | Name |  |
| Seriennummer | Seriennummer |  |
| Gruppe | Gruppe |  |
| Firmware Version | Firmware Version |  |
| Entität ANSCHLUSS |  |  |
| Client-IP | Client-IP |  |
| Entität GINO PRODUKTIONSDATEN |  |  |
| Hardware Version | Hardware Version |  |

| **Maskenfeld** | **Zuordnung Entität** | **Plausibilität** |
| --- | --- | --- |
| Entität GINO GRUPPE |  |  |
| Id | Id |  |
| Typ | Typ |  |
| Name | Name |  |
| Mitglieder Typ | Mitglieder Typ |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Gino Kalve Aufruf ”_fwcheck” (OPC_CLV_GUI_116)

Anzeigename: GINO Kalve Aufruf „/fwcheck" (OPC\_CLV\_GUI\_116)   
Auf dieser Maske wird die Rückmeldung des Aufrufes der entsprechenden Kalve Schnittstelle ausgegeben (KALVE\_FKT\_007).  
Folgende Aktion steht zur Verfügung:

|  |  |
| --- | --- |
| Zugeordnete Gruppen | Anzeigen der Maske „Gino Gruppen Zuordnungen (OPC\_CLV\_GUI\_115)" für den entsprechenden GINO. |
| Aufruf Server Konfiguration | Anzeigen der Maske „Gino Kalve Aufruf "/server-config" (OPC\_CLV\_GUI\_117)" für den entsprechenden GINO. |
| Aufruf Alive | Anzeigen der Maske „Gino Kalve Aufruf "/alive" (OPC\_CLV\_GUI\_118)"" für den entsprechenden GINO. |
| Aufruf Script Check | Anzeigen der Maske „Gino Kalve Aufruf "/scriptcheck" (OPC\_CLV\_GUI\_119)" für den entsprechenden GINO. |
| Zurück | Anzeigen der Maske „Gino suchen (OPC\_CLV\_GUI\_111)" |

| **Maskenfeld** | **Zuordnung Entität** | **Plausibilität** |
| --- | --- | --- |
| Entität Gino Firmware |  |  |
| Neue Firmware | FW Version |  |
|  |  |  |
| Fehlercode |  |  |
| Fehlermeldung |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Gino Kalve Aufruf ”_server-config” (OPC_CLV_GUI_117)

Anzeigename: GINO Kalve Aufruf „/server-config" (OPC\_CLV\_GUI\_117)   
Auf dieser Maske wird die Rückmeldung des Aufrufes der entsprechenden Kalve Schnittstelle ausgegeben (KALVE\_FKT\_009).  
Folgende Aktion steht zur Verfügung:

|  |  |
| --- | --- |
| Zugeordnete Gruppen | Anzeigen der Maske „Gino Gruppen Zuordnungen (OPC\_CLV\_GUI\_115)" für den entsprechenden GINO. |
| Aufruf Firmware | Anzeigen der Maske „Gino Kalve Aufruf "/fwcheck" (OPC\_CLV\_GUI\_116)" für den entsprechenden GINO. |
| Aufruf Alive | Anzeigen der Maske „Gino Kalve Aufruf "/alive" (OPC\_CLV\_GUI\_118)" für den entsprechenden GINO. |
| Aufruf Script Check | Anzeigen der Maske „Gino Kalve Aufruf "/scriptcheck" (OPC\_CLV\_GUI\_119)" für den entsprechenden GINO. |
| Zurück | Anzeigen der Maske „Gino suchen (OPC\_CLV\_GUI\_111)" |

| **Maskenfeld** | **Zuordnung Entität** | **Plausibilität** |
| --- | --- | --- |
| Entität Gino Konfiguration |  |  |
| Parameter | Name |  |
| Wert | Value |  |
|  |  |  |
| Fehlercode |  |  |
| Fehlermeldung |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Gino Kalve Aufruf ”_alive” (OPC_CLV_GUI_118)

Anzeigename: GINO Kalve Aufruf „/alive" (OPC\_CLV\_GUI\_118)   
Auf dieser Maske wird die Rückmeldung des Aufrufes der entsprechenden Kalve Schnittstelle ausgegeben (KALVE\_FKT\_014).  
Folgende Aktion steht zur Verfügung:

|  |  |
| --- | --- |
| Zugeordnete Gruppen | Anzeigen der Maske „Gino Gruppen Zuordnungen (OPC\_CLV\_GUI\_115)" für den entsprechenden GINO. |
| Aufruf Firmware | Anzeigen der Maske „Gino Kalve Aufruf "/fwcheck" (OPC\_CLV\_GUI\_116)" für den entsprechenden GINO. |
| Aufruf Server Konfiguration | Anzeigen der Maske „Gino Kalve Aufruf "/server-config" (OPC\_CLV\_GUI\_117)" für den entsprechenden GINO. |
| Aufruf Script Check | Anzeigen der Maske „Gino Kalve Aufruf "/scriptcheck" (OPC\_CLV\_GUI\_119)" für den entsprechenden GINO. |
| Zurück | Anzeigen der Maske „Gino suchen (OPC\_CLV\_GUI\_111)" |

| **Maskenfeld** | **Zuordnung Entität** | **Plausibilität** |
| --- | --- | --- |
| Entität Gino Task Zuordnung |  |  |
| Aktueller Task | Call |  |
|  |  |  |
| Fehlercode |  |  |
| Fehlermeldung |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Gino Kalve Aufruf ”_scriptcheck” (OPC_CLV_GUI_119)

Anzeigename: GINO Kalve Aufruf „/scriptcheck" (OPC\_CLV\_GUI\_119)   
Auf dieser Maske wird die Rückmeldung des Aufrufes der entsprechenden Kalve Schnittstelle ausgegeben (KALVE\_FKT\_015).  
Folgende Aktion steht zur Verfügung:

|  |  |
| --- | --- |
| Zugeordnete Gruppen | Anzeigen der Maske „Gino Gruppen Zuordnungen (OPC\_CLV\_GUI\_115)" für den entsprechenden GINO. |
| Aufruf Firmware | Anzeigen der Maske „Gino Kalve Aufruf "/fwcheck" (OPC\_CLV\_GUI\_116)" für den entsprechenden GINO. |
| Aufruf Server Konfiguration | Anzeigen der Maske „Gino Kalve Aufruf "/server-config" (OPC\_CLV\_GUI\_117)" für den entsprechenden GINO. |
| Aufruf Alive | Anzeigen der Maske „Gino Kalve Aufruf "/alive" (OPC\_CLV\_GUI\_118)" für den entsprechenden GINO. |
| Zurück | Anzeigen der Maske „Gino suchen (OPC\_CLV\_GUI\_111)" |

| **Maskenfeld** | **Zuordnung Entität** | **Plausibilität** |
| --- | --- | --- |
| Entität Gino Task Script |  |  |
| Auszuführendes Script | Scriptname |  |
|  |  |  |
| Fehlercode |  |  |
| Fehlermeldung |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Konfigurationsparameter erstellen (OPC_CLV_GUI_135)

Anzeigename für GINO-Konfigurationsparameter erstellen: GINO-Konfigurationsparameter erstellen (OPC\_CLV\_GUI\_135)

In dieser Maske kann ein neuer GINO-Konfigurationsparameter erstellt werden.

Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Speichern | Der neue Parameter wird mit der Funktion [GINO-Konfigurationsparameter erstellen (OPC\_CLV\_FKT\_154)](#gino-konfigurationsparameter-erstellen-opc-clv-fkt-154) angelegt. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung () |

| **Maskenfeld** | **DB –**  **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Name | NAME | M | E | Der Name des Parameters | (max. 100 Zeichen) |
| Standardwert | VALUE | M | E | Der Standardwert des Parameters | (max. 4000 Zeichen) |
| Beschreibung | BESCHREIBUNG | M | E | Die Beschreibung des Parameters | (max. 1000 Zeichen) |
| Zur allgemeinen Konfiguration hinzufügen |  | M | E | Auswahl, ob der Wert zu den allgemeinen Gruppen hinzugefügt wird oder nicht. | Ja/Nein |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Konfigurationsparameter löschen (OPC_CLV_GUI_136)

Anzeigename für GINO-Konfigurationsparameter erstellen: GINO-Konfigurationsparameter löschen (OPC\_CLV\_GUI\_136)

In dieser Maske kann ein GINO-Konfigurationsparameter gelöscht werden.

Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Löschen | Der ausgewählten Parameter wird mit der Funktion [GINO-Konfigurationsparameter löschen (OPC\_CLV\_FKT\_155)](#gino-konfigurationsparameter-loeschen-opc-clv-fkt-155) gelöscht. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung () |

| **Maskenfeld** | **DB –**  **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Checkbox |  | M | E | Auswahl, ob der Wert zu löschen ist, oder nicht. | Ja/Nein |
| Name | NAME | M | A | Der Name des Parameters | (max. 100 Zeichen) |
| Beschreibung | BESCHREIBUNG | M | A | Die Beschreibung des Parameters | (max. 1000 Zeichen) |
| Standardwert | VALUE | M | A | Der Standardwert des Parameters | (max. 4000 Zeichen) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Gruppenliste ausgeben (OPC_CLV_GUI_140)

Anzeigename für GINO-Gruppenliste Konfiguration: GINO-Gruppenliste Konfiguration [GINO-Konfiguration]  
Anzeigename für GINO-Gruppenliste Firmware: GINO-Gruppenliste Firmware [GINO-Firmware]  
Anzeigename für GINO-Gruppenliste Task: GINO-Gruppenliste Task [GINO-Task]  
Anzeigename für GINO-Gruppenliste Script: GINO-Gruppenlist Script [GINO-Script]  
  
In dieser Maske werden Konfigurations-, Firmware- bzw. Task Gruppen angezeigt.  
Beim Aufruf dieser Maske wird die Funktion “[GINO-Gruppenliste ermitteln (OPC\_CLV\_FKT\_140)](#gino-gruppenliste-ermitteln-opc-clv-fkt-140)“ ausgeführt.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Radio-Button | Markierung der jeweiligen GINO-Konfiguration |
| Gruppen-ID | Die Gruppen-IDs werden als Link ausgeführt. Durch Auswahl des Links werden die Detailinformationen der gewählten Gruppe dargestellt.  Aufruf der Funktion   ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Konfiguration anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_142)“  bzw.  ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Firmware anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_143)“.  bzw.  ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_144)“  bzw.  ““ und anschließend Anzeige der Maske “GINO-Gruppe Script-Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_146)“ |
| Anzeigen | Die Detailinformation der gewählten Gruppe wird dargestellt.  Aufruf der Funktion   ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Konfiguration anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_142)“  bzw.  ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Firmware anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_143)“  bzw.  ““ mit Gruppen-ID und anschließend Anzeige der Maske “GINO-Gruppe Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_144)“  bzw.  ““ und anschließend Anzeige der Maske “GINO-Gruppe Script-Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_146)“ |
| Neue Gruppe anlegen | Anlegen einer neuen Gruppe  Aufruf der Funktion ““ ohne Gruppen-ID und Anzeige der Maske  “GINO-Gruppe Konfiguration anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_142)“ bzw. GINO-Gruppe Firmware anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_143)  bzw. “GINO-Gruppe Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_144)“  bzw. “GINO-Gruppe Script-Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_146)“ |
| Neuen Parameter anlegen (Nur bei Konfiguration) | Anzeige der GUI Oberfläche [GINO-Konfigurationsparameter erstellen (OPC\_CLV\_GUI\_135)](#gino-konfigurationsparameter-erstellen-opc-clv-gui-135) |
| Parameter löschen (Nur bei Konfiguration) | Anzeige der GUI Oberfläche [GINO-Konfigurationsparameter löschen (OPC\_CLV\_GUI\_136)](#gino-konfigurationsparameter-loeschen-opc-clv-gui-136) |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung () |

| **Maskenfeld** | **DB –**  **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| ID |  | M | A[0:n]  Siehe "GINO-Gruppen-ID" | Gruppen ID | numerisch |
| Gruppenname |  | M | A[0:n]  Siehe “[GINO-Gruppen-Name | Gruppenname | 30 alphanumerische Zeichen |
| Anzahl Mitglieder |  | M | A[0:n] | Anzahl der zu dieser Gruppe zugeordneten GINOs | numerisch |
| Mitgliedertyp |  |  |  | Siehe “GINO-Gruppen-Mitgliedertyp“ |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Gruppe Konfiguration anzeigen_erstellen_ändern (OPC_CLV_GUI_142)

|  |  |
| --- | --- |
|  |  |
| Gruppe Speichern | Speichern der neu angelegten Gruppe oder geänderten GruppeAufruf der Funktion „GINO-Gruppe speichern (OPC\_CLV\_FKT\_142)" sowie „GINO-Konfiguration Zuordnung speichern (OPC\_CLV\_FKT\_143)"   Im Fehlerfall: Ausgabe der Fehlermeldung der jeweiligen Funktion.   Ist nur für „Gruppe anlegen" oder „Gruppe ändern" aktiv. |
| Gruppe Bearbeiten | Ist nur für „Gruppe anzeigen" aktiv |
| Gruppe Löschen | Löschen einer Gruppe   Ist nur für „Gruppe anzeigen" aktiv   Aufruf der Funktion „GINO-Konfigurationsgruppe löschen (OPC\_CLV\_FKT\_145)" nach einer Bestätigungsabfrage und anschließend Anzeigen der Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| Gruppenmitglieder verwalten | Gruppenmitglieder verwalten, ist nur aktiv für GINO-Gruppen-Mitgliedertyp „SPEZIELL". Aufruf der Funktion „GINO Gruppenmitglieder ermitteln (OPC\_CLV\_FKT\_146)" und anschließend Anzeige der Maske „GINO Gruppenmitglieder verwalten (OPC\_CLV\_GUI\_145)". |
| Zurück | Anzeige der GruppenlisteAnzeigen der Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Gruppenname |  | M | E | Name der Gruppe | 30 alphanumerische Zeichen |
| Mitgliedertyp |  | M | E/A | Siehe „GINO-Gruppen-Mitgliedertyp" | Drop-Down-Liste |
| Filter Seriennummer | Seriennummer | K | E | Filter für Seriennummer | 1000 alphanumerische Zeichen |
| Filter Firmware | Firmware | K | E | Filter für Firmware | 1000 alphanumerische Zeichen |
| Anzahl Mitglieder |  | M | A | Anzahl der Mitglieder der Gruppe |  |
| Start |  | M | E/A | Start des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| Ende |  | M | E/A | Ende des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="d5b433f9-d4c7-4798-a3cb-2d89c53efa41"><ac:plain-text-body><![CDATA[ | Auswahl |  | M | E[0:n] | Kennzeichen ob der gewählte Parameter in die Gruppe aufgenommen werden soll | Checkbox | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="18129d14-8858-4269-9c1e-29fd93ab0f18"><ac:plain-text-body><![CDATA[ | Name |  | M | A[0:n] | Name des Parameters | 50 alphanumerische Zeichen | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="619489cc-970b-46d4-b0fe-ce21b7b1c3d2"><ac:plain-text-body><![CDATA[ | Wert |  | M | E[0:n] | Wert des Parameters  ]]></ac:plain-text-body></ac:structured-macro>   Das Feld bleibt ausgegraut wenn:   * Auswahl = false oder * default = true oder * Entfernen = true             Kann ein Wert eingegeben werden, so wird dieser beim Speichern übernommen (auch wenn das Feld leer bleibt) | 4000 alphanumerische Zeichen (Feldlänge 50) |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="45e73885-e3da-4bea-93bc-927ce02bf291"><ac:plain-text-body><![CDATA[ | default |  | M | E[0:n] | Das Feld bleibt ausgegraut wenn  ]]></ac:plain-text-body></ac:structured-macro>   * Auswahl = false oder * Entfernen = true         Wird das Feld selektiert, so wird * der Default-Wert angezeigt und * das Feld Wert ausgegraut     Wird das Feld deselektiert, so wird das Feld Wert aktiv. | Checkbox |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="918f40ee-9f01-4b98-9777-499cd61354b1"><ac:plain-text-body><![CDATA[ | Entfernen |  | M | E[0:n] | Das Feld bleibt ausgegraut wenn Auswahl = false.  ]]></ac:plain-text-body></ac:structured-macro>   Wird das Feld selektiert, so wird   * Default ausgegraut * Kein Parameter-Wert angezeit und * das Feld Wert ausgegraut | Checkbox |

*Hinweise:*   
*Für den Fall GINO-Konfiguration-Gruppe anzeigen sind alle Felder als reine Ausgabefelder ausgegraut dargestellt.*   
*Für den Fall der Neuerstellung der Gruppe ist der Mitgliedertyp auf ‚SPEZIELL' oder ‚DYNAMISCH' eingeschränkt*


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Gruppe Firmware anzeigen_erstellen_ändern (OPC_CLV_GUI_143)

|  |  |
| --- | --- |
|  |  |
| Gruppe Speichern | Speichern der neu angelegten Gruppe oder geänderten GruppeAufruf der Funktion „" sowie „". Im Fehlerfall: Ausgabe der Fehlermeldung.  Ist nur für „Gruppe anlegen" oder „Gruppe ändern" sichtbar. |
| Gruppe Bearbeiten | Ist nur für „Gruppe anzeigen" sichtbar.  Eine allgemeine Gruppe mit "Gültig Bis" in der Vergangenheit kann nicht verändert werden. |
| Gruppe Löschen | Löschen einer Gruppe,  Ist nur für „Gruppe anzeigen" sichtbar.  Aufruf der Funktion „" nach einer Bestätigungsabfrage und anschließend Anzeigen der Maske  „".  Eine allgemeine Gruppe kann nur gelöscht werden, wenn "Gültig bis" in der Vergangenheit liegt. |
|  |  |
| Gruppenmitglieder verwalten | Gruppenmitglieder verwalten, ist nur aktiv für GINO-Gruppen-Mitgliedertyp „SPEZIELL".Aufruf der Funktion „" und anschließend Anzeige der Maske „". |
| Zurück | Anzeige der GruppenlisteAnzeigen der Maske „" |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung () |

| **Maskenfeld** | **DB –**  **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Gruppenname |  | M | E/A | Name der Gruppe | 30 alphanumerische Zeichen |
| Mitgliedertyp |  | M | E/A | Siehe „GINO-Gruppen-Mitgliedertyp" | Drop-Down-Liste |
| Filter Seriennummer | Seriennummer | K | E | Filter für Seriennummer | 1000 alphanumerische Zeichen |
| Filter Firmware | Firmware | K | E | Filter für Firmware | 1000 alphanumerische Zeichen |
| Anzahl Mitglieder |  | M | A | Anzahl der Mitglieder der Gruppe |  |
| Gültig ab |  | M | E/A | Start des Gültigkeitszeitraums im Format  ‚TT.MM.JJJJ HH:mm'  *Hinweis: für eine allgemeine Gruppe kann das Datum nur beim Neuanlegen oder beim Ändern der zweiten, bis "31.12.3999" gültigen, Gruppe eingegeben werden*. *Dieses Datum wird dann wird dann für die erste relevante allgemeine Gruppe als "Gültig bis" gesetzt.* |  |
| Gültig bis |  | M | E/A | Ende des Gültigkeitszeitraums im Format  ‚TT.MM.JJJJ HH:mm' |  |
| Firmware Version | Firmware-Version | M | E/A | Auswahl an bekannten Software Versionen durch Aufruf von „" ermittelt. | Drop-Down-Liste |

*Hinweise:*   
*Für den Fall „*GINO-Gruppe Firmware anzeigen" *sind alle Felder als reine Ausgabefelder ausgegraut dargestellt.*   
*Eine allgemeine Gruppe kann nur dann ausgestellt werden, falls es nur eine relevante (d.h. Startdatum nach aktuellem Datum) allgemeine Gruppe gibt, in diesem Fall ist "Gültig bis" mit "31.12.3999" vorgegeben und das eingegebene Startdatum (muss in Zukunft liegen) wird als "Gültig bis" der bestehenden relevanten Gruppe gesetzt. Mit dieser Einschränkung und den Einschränkungen bei "Gruppe bearbeiten" und "Gruppe löschen" kann man im Zuge des ROW bereits vorab die neue Defaultfirmware-Version festlegen, man kann aber nicht mehrere (gleichzeitig gültige) Defaultfirmware-Versionen setzen.*


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Gruppe Task anzeigen_erstellen_ändern (OPC_CLV_GUI_144)

|  |  |
| --- | --- |
|  |  |
| Gruppe Speichern | Speichern der neu angelegten Gruppe oder geänderten GruppeAufruf der Funktion „GINO-Gruppe speichern (OPC\_CLV\_FKT\_142)" sowie „GINO-Task Zuordnung speichern (OPC\_CLV\_FKT\_162)". Im Fehlerfall: Ausgabe der Fehlermeldung.   Ist nur für „Gruppe anlegen" oder „Gruppe ändern" sichtbar. |
| Gruppe Bearbeiten | Ist nur für „Gruppe anzeigen" sichtbar |
| Gruppe Löschen | Löschen einer Gruppe,   Ist nur für „Gruppe anzeigen" sichtbar.   Aufruf der Funktion „GINO-Task Gruppe löschen (OPC\_CLV\_FKT\_163)" nach einer Bestätigungsabfrage und anschließend Anzeigen der Maske   „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)". |
|  |  |
| Gruppenmitglieder verwalten | Gruppenmitglieder verwalten, ist nur aktiv für GINO-Gruppen-Mitgliedertyp „SPEZIELL".Aufruf der Funktion „GINO Gruppenmitglieder ermitteln (OPC\_CLV\_FKT\_146)" und anschließend Anzeige der Maske „GINO Gruppenmitglieder verwalten (OPC\_CLV\_GUI\_145)". |
| Zurück | Anzeige der GruppenlisteAnzeigen der Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Gruppenname |  | M | E/A | Name der Gruppe | 30 alphanumerische Zeichen |
| Mitgliedertyp |  | M | E/A | Siehe „GINO-Gruppen-Mitgliedertyp" | Drop-Down-Liste |
| Filter Seriennummer | Seriennummer | K | E | Filter für Seriennummer | 1000 alphanumerische Zeichen |
| Filter Firmware | Firmware | K | E | Filter für Firmware | 1000 alphanumerische Zeichen |
| Anzahl Mitglieder |  | M | A | Anzahl der Mitglieder der Gruppe |  |
| Start |  | M | E/A | Start des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| Ende |  | M | E/A | Ende des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="4d92d9dd-1875-4bd7-8737-99b4239dfb0d"><ac:plain-text-body><![CDATA[ | Auswahl |  | M | E[0:n] | Kennzeichen ob der gewählte Parameter in die Gruppe aufgenommen werden soll | Checkbox | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="4bf9c9cd-e667-4466-b607-10f42399e319"><ac:plain-text-body><![CDATA[ | Call |  | M | A[0:n] | Siehe „[GINO-Task | Parametergruppe GINO-Task]", Attribut „call" | Drop-Down-Liste | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="73c7abdb-d685-4e5e-978f-b517f1b292be"><ac:plain-text-body><![CDATA[ | Uhrzeit-Ab |  | M | E[0:n] | Siehe „[GINO-Task | Parametergruppe GINO-Task]", Attribut „Uhrzeit-Ab"  ]]></ac:plain-text-body></ac:structured-macro>  Vorbelegung „00:00"   Das Feld bleibt ausgegraut wenn Auswahl = false | Gültige Uhrzeit |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="5f43f6cc-0930-47ea-86a2-77ef049c7147"><ac:plain-text-body><![CDATA[ | Uhrzeit-Bis |  | M | E[0:n] | Siehe „[GINO-Task | Parametergruppe GINO-Task]", Attribut „Uhrzeit-Bis"  ]]></ac:plain-text-body></ac:structured-macro>  Vorbelegung „00:00"   Das Feld bleibt ausgegraut wenn Auswahl = false | Gültige Uhrzeit |
|  |  |  |  |  |  |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="896bc5ae-18ba-4f9e-92e5-a1cc10e1dc98"><ac:plain-text-body><![CDATA[ | Intervall |  | K | E[0:n] | Siehe „[GINO-Task | Parametergruppe GINO-Task]", Attribut „Intervall"  ]]></ac:plain-text-body></ac:structured-macro>  Das Feld bleibt ausgegraut wenn Auswahl = false |  |

*Hinweis: für den Fall „*GINO-Gruppe Task anzeigen" *sind alle Felder als reine Ausgabefelder ausgegraut dargestellt.*


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Gruppe Script-Task anzeigen_erstellen_ändern (OPC_CLV_GUI_146)

|  |  |
| --- | --- |
|  |  |
| Gruppe Speichern | Speichern der neu angelegten Gruppe oder geänderten GruppeAufruf der Funktion „GINO-Gruppe speichern (OPC\_CLV\_FKT\_142)" sowie „GINO-Script Zuordnung speichern (OPC\_CLV\_FKT\_166)".   Im Fehlerfall: Ausgabe der Fehlermeldung.   Ist nur für „Gruppe anlegen" oder „Gruppe ändern" sichtbar. |
| Gruppe Bearbeiten | Ist nur für „Gruppe anzeigen" sichtbar |
| Gruppe Löschen | Löschen einer Gruppe,   Ist nur für „Gruppe anzeigen" sichtbar.   Aufruf der Funktion „GINO-Script Gruppe löschen (OPC\_CLV\_FKT\_167)" nach einer Bestätigungsabfrage und anschließend Anzeigen der Maske   „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)". |
|  |  |
| Gruppenmitglieder verwalten | Gruppenmitglieder verwalten, ist nur aktiv für GINO-Gruppen-Mitgliedertyp „SPEZIELL".Aufruf der Funktion „GINO Gruppenmitglieder ermitteln (OPC\_CLV\_FKT\_146)" und anschließend Anzeige der Maske „GINO Gruppenmitglieder verwalten (OPC\_CLV\_GUI\_145)". |
| Zurück | Anzeige der GruppenlisteAnzeigen der Maske „GINO-Gruppenliste ausgeben (OPC\_CLV\_GUI\_140)" |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Gruppenname |  | M | E/A | Name der Gruppe | 30 alphanumerische Zeichen |
| Mitgliedertyp |  | M | E/A | Siehe „GINO-Gruppen-Mitgliedertyp" | Drop-Down-Liste |
| Filter Seriennummer | Seriennummer | K | E | Filter für Seriennummer | 1000 alphanumerische Zeichen |
| Filter Firmware | Firmware | K | E | Filter für Firmware | 1000 alphanumerische Zeichen |
| Anzahl Mitglieder |  | M | A | Anzahl der Mitglieder der Gruppe |  |
| Start |  | M | E/A | Start des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| Ende |  | M | E/A | Ende des Gültigkeitszeitraums im Format   ‚TT.MM.JJJJ HH:mm' |  |
| Skript |  | M | E/A | Siehe „GINO-Script-Task", Attribut „Skript"   Die Auswahl an bekannten Scripten wird durch den Aufruf von „GINO-Scripte ermitteln (OPC\_CLV\_FKT\_168)" ermittelt.     Das Feld bleibt ausgegraut wenn Auswahl = false | Drop-Down-Liste |
| Uhrzeit-Ab |  | M | E/A | Siehe „GINO-Script-Task"", Attribut „Uhrzeit-Ab"   Vorbelegung „00:00"   Das Feld bleibt ausgegraut wenn Auswahl = false | Gültige Uhrzeit |
| Uhrzeit-Bis |  | M | E/A | Siehe „GINO-Script-Task"", Attribut „Uhrzeit-Bis"   Vorbelegung „00:00"   Das Feld bleibt ausgegraut wenn Auswahl = false | Gültige Uhrzeit |
| Intervall |  | K | E/A | Siehe „GINO-Script-Task"", Attribut „Intervall"   Das Feld bleibt ausgegraut wenn Auswahl = false |  |

*Hinweis: für den Fall „*GINO-Gruppe Script-Task anzeigen" *sind alle Felder als reine Ausgabefelder ausgegraut dargestellt.*


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO Gruppenmitglieder verwalten (OPC_CLV_GUI_145)

|  |  |
| --- | --- |
| Mitglieder neu zuordnen | Erstellen neuer Gruppenmitglieder   Aufruf der Funktion „GINO Gruppenmitglieder löschen (OPC\_CLV\_FKT\_149) und anschließend GINO Gruppenmitglieder hinzufügen (OPC\_CLV\_FKT\_148)   Im Erfolgsfall: Aktualisierung der Maske.   Im Fehlerfall: Ausgabe der Fehlermeldung der jeweiligen Funktion. |
| Zurück | Anzeigen der Maske   * „GINO-Gruppe Konfiguration anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_142)" * GINO-Gruppe Firmware anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_143) * GINO-Gruppe Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_144) bzw.     -GINO-Gruppe Script-Task anzeigen/erstellen/ändern (OPC\_CLV\_GUI\_146)     im Anzeigemodus |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Gruppen-ID |  | M | A | ID der Gruppe | numerisch |
| Gruppenname |  | M | A | Name der Gruppe | 30 alphanumerische Zeichen |
| Gruppentyp |  | M | A | Siehe „GINO-Gruppentyp" |  |
| Mitgliedertyp |  | M | A | Siehe „GINO-Gruppen-Mitgliedertyp" |  |
| Anschluss-IP |  | B\* | E | Liste mit per Komma getrennten IP-Adressen | Max 50 alphanumerische Zeichen |
| Dateiname |  | B\* | E | Name der CSV-Datei welche   IP-Adressen beinhaltet. Dient als Input für die Anschluss-IPs (In jeder Zeile der Datei wird genau eine IP-Adresse erwartet - keine weiteren Trennzeichen.) | Max 15 alphanumerische Zeichen pro Zeile, keine weitern Trennzeichen zwischen den IP-Adressen. |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="f9e8deaa-8f9a-4de5-975e-1acda74c2a88"><ac:plain-text-body><![CDATA[ | IP-Adresse |  | M | A[1:n] | IP-Adresse  ]]></ac:plain-text-body></ac:structured-macro>   Hinweis: wird nur angezeigt wenn es < 10 Einträge gibt | 15 alphanumerische Zeichen |

*B\* Ist eine Datei ausgewählt, so wird das Eingabefeld ignoriert.*


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Firmwareliste ausgeben (OPC_CLV_GUI_150)

|  |  |
| --- | --- |
| Ausgewählte Löschen | Die ausgewählten Versionen werden gelöscht. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="22275ebe-cc75-4caa-b5c5-3255b2f020df"><ac:plain-text-body><![CDATA[ | Auswahl |  | M | E[1..n] | Checkbox zur Auswahl der zu löschenden Firmware |  | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="32fb14e6-21df-4a37-943f-97fa63a6b4e2"><ac:plain-text-body><![CDATA[ | Firmware Version |  | M | A[1..n] | Version der Firmware | Regulärer Ausdruck „^d{2}\.d{2}\.d{2}$" | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="bb9040f6-2866-4b27-b54b-f5e4593abf8f"><ac:plain-text-body><![CDATA[ | Hardware Version |  | M | A[1..n] | Hardware Version der Firmware | Regulärer Ausdruck „^d{2}\.d{2}$" | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="f28c10aa-0a9f-42a3-b090-40207534951d"><ac:plain-text-body><![CDATA[ | Letzte kompatilble Version |  | M | A[1..n] | Letzte kompatilble Version der Firmware | Regulärer Ausdruck „^d{2}\.d{2}\.d{2}$" | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="f503b40d-56c1-48ab-8959-846de72a46a0"><ac:plain-text-body><![CDATA[ | Dateiname |  | M | A[1..n] | Name der Firmware Datei | 255 alphanumerische Zeichen (keine Überprüfung) | ]]></ac:plain-text-body></ac:structured-macro> |
| Letzte Änderung | LASTCHANGE | M |  | Zeitpunkt der letzten Änderung | Formattiertes Datum |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Firmware hochladen (OPC_CLV_GUI_151)

|  |  |
| --- | --- |
| Speichern | Sichtbar nachdem eine Datei ausgewählt wurde.Aufruf der Funktion „GINO-Firmware speichern (OPC\_CLV\_FKT\_150)".   Im Gutfall Ausgabe ein Gutmeldung Anzeige der Maske mit dem Button „Weitere Firmware hochladen" und nicht änderbaren Feldern.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Durchsuchen (Datei auswählen/ andere Datei auswählen) | Hinweis: „andere Datei auswählen" wird angezeigt, falls bereits eine Datei ausgewählt wurde.   Öffnet ein Dateimenü zum Selektieren der Firmware-Datei   Bei Auswahl einer Datei, werden aus dem Dateinamen   * Die Softwareversion und * die Hardware Version         ermittelt und in die Eingabedatenfelder übernommen, diese können bei Bedarf überschrieben werden.     Für die Übernahme der Versionsfelder wird ein Dateinamensschema     ginofirmware\_<hwversion>\_<dd.dd.dd>.tar.ogz vorausgesetzt, wobei ‚d' für eine dezimale Ziffer steht.         Folgt die Datei einem anderen Schema, so erfolgt keine Übernahme der Versiondaten in den Eingabefeldern. |
| Weitere Firmware hochladen | Zeigt wieder Initialzustand der Maske zur Auswahl einer weiteren Datei. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Software Version |  | M | E/A | Software Version der Firmware | 3 Zahlen durch „." getrennt, z.B. 1.20.3 |
| Hardware Version |  | M | E/A | Hardware Version der Firmware | 100 Zeichen |
| Letzte kompatilble Version |  | M | E/A | Letzte kompatilble Version der Firmware | 3 Zahlen durch „." getrennt, z.B. 1.20.1 |
| Dateiname |  | M | A | Name der Firmware Datei, wird durch Dateiauswahl mit Button „Durchsuchen" befüllt. | 255 alphanumerische Zeichen (keine Überprüfung) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Scriptliste ausgeben (OPC_CLV_GUI_152)

|  |  |
| --- | --- |
| Ausgewählte Löschen | Die ausgewählten Scripte werden gelöscht. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="6c53004c-45e2-4fa4-a485-caf87cc45888"><ac:plain-text-body><![CDATA[ | Auswahl |  | M | E[1..n] | Checkbox zur Auswahl der zu löschenden Scripte |  | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="df191cb6-37bd-482c-ac36-cbd1354c62a0"><ac:plain-text-body><![CDATA[ | Scriptname |  | M | A[1..n] | Name des Scripts |  | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="8080a3af-2b64-413f-9e08-ae40bf7ed18a"><ac:plain-text-body><![CDATA[ | Anlegedatum |  | M | A[1..n] | Datum zu dem das Script gespeichert wurde.  ]]></ac:plain-text-body></ac:structured-macro>  ‚TT.MM.JJJJ HH:mm' |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Script hochladen (OPC_CLV_GUI_153)

|  |  |
| --- | --- |
| Speichern | Aufruf der Funktion „GINO-Script speichern (OPC\_CLV\_FKT\_169)".   Die selektierten Dateinamen werden übergeben.Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Durchsuchen (Dateiname) | Öffnet ein Dateimenü zum Selektieren der Skript-Datei   Bei Auswahl einer Datei wird der Dateinamen ermittelt. |
| Durchsuchen (Signatur-Dateiname) | Öffnet ein Dateimenü zum Selektieren der Skript-Datei   Bei Auswahl einer Datei wird der Dateinamen ermittelt |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Sperrliste ausgeben (OPC_CLV_GUI_084)_KVW-12096_

|  |  |
| --- | --- |
| OK | Funktion „GINO-Sperrliste ermitteln (OPC\_CLV\_FKT\_170)" wird erneut mit dem Eingangsparameter „Filter Seriennummer" aufgerufen und die Liste der gesperrten GINO-Produktionsdaten mit den neuen Werten ersetzt. |
| Check-Box | Markierung des jeweiligen Gerätes |
| Gerät entsperren | Ausgewählte Geräte werden wieder entsperrt: Aufruf der Funktion GINOs entsperren (OPC\_CLV\_FKT\_172)   Im Erfolgsfall: Ausgabe der Erfolgsmeldung.Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| **Filter Seriennummer** | **Seriennummer** | **K** | **E** | **Eingangswert für den Aufruf der Funktion „****GINO-Sperrliste ermitteln (OPC\_CLV\_FKT\_170)****"** | **10 Zeichen**   <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="9eb45735-2204-44ad-bda5-9548e9c62824"><ac:plain-text-body><![CDATA[**[0-9?**]\* | ]]></ac:plain-text-body></ac:structured-macro> |
| Seriennummer |  | M | A | Seriennummer des (aktuell) gesperrten Gerätes | 50 alphanumerische Zeichen |
| Sperrdatum |  | M | A | Datum und Uhrzeit an dem das Gerät gesperrt wurde | Datum |
| Kommentar | KOMMENTAR\_SPERRE | K | A/E | Kommentar warum die Sperrung/Entsperrung vorgenommen wurde/wird. | 100 alphanumerische Zeichen |
| Bearbeiter |  | M | A | Benutzerkennung, der die Gerätsperre gesetzt hat | 20 alphanumerische Zeichen |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO mit Seriennummer sperren (OPC_CLV_GUI_085)

|  |  |
| --- | --- |
| Sperren | Die Funktion „GINOs sperren (OPC\_CLV\_FKT\_180)" wird aufgerufen. Als Seriennummern werden entweder die mit „Seriennummern importieren" hinzugefügten, oder falls keine importiert wurden, die vom Eingabefeld Seriennummer verwendet. Im Erfolgsfall: Ausgabe der Erfolgsmeldung, sowie Freigabe der Aktion „Weitere GINOs sperren".Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Seriennummern importieren | Es wird die Funktion „GINO Seriennummern aus Datei lesen (OPC\_CLV\_FKT\_179)" ausgeführt und die ausgelesenen Seriennummern dem Feld „Ausgewählte Seriennummern" hinzugefügt. Nach einem erfolgreichen Import, wird diese Aktion gesperrt und es kann keine Seriennummer mehr angegeben werden. |
| Weitere GINOs sperren | Die Maske wird zurück gesetzt. Erst nach einem erfolgreichem Ausführen der Aktion Sperren aktivierbar. |
| Zurück | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| Seriennummer | SERIENNUMMER | K | E | Seriennummer des zu sperrenden Gerätes | 50 alphanumerische Zeichen |
| Kommentar | KOMMENTAR\_SPERRE | K | E | Kommentar warum die Sperrung vorgenommen wurde | 100 alphanumerische Zeichen |
| Datei auswählen |  | K | E | Datei mit Seriennummern | Dateiupload |
| Ausgewählte Seriennummern |  | K | A | Liste mit Seriennummern | Durch „Seriennummern importieren" ausgefüllt |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO mit Seriennummer für andere Instanz exportieren (OPC_CLV_GUI_086)

|  |  |
| --- | --- |
| Die folgenden Aktionen sind aktivierbar:Exportieren | Die Funktion „GINOs für andere Instanz vorbereiten (OPC\_CLV\_FKT\_181)" wird aufgerufen. Als Seriennummern werden entweder die mit „Seriennummern importieren" hinzugefügten, oder falls keine importiert wurden, die vom Eingabefeld Seriennummer verwendet. Im Erfolgsfall: Ausgabe der Erfolgsmeldung sowie Freigabe der Aktionen „Download" und „Weitere Gino exportieren". Die Aktion „Seriennummer importieren" wird gesperrt.Im Fehlerfall: Ausgabe der entsprechenden Fehlermeldungen. |
| Seriennummern importieren | Es wird die Funktion „GINO Seriennummern aus Datei lesen (OPC\_CLV\_FKT\_179)" ausgeführt und die ausgelesenen Seriennummern dem Feld „Ausgewählte Seriennummern" hinzugefügt. Nach einem erfolgreichen Import, wird diese Aktion gesperrt und es kann keine Seriennummer mehr angegeben werden. |
| Download | SS51 konforme .csv Datei mit den Herstellerdaten wird heruntergeladen um den GINO in der ASWH/Test-Instanz einspielen zu können. Erst nach erfolgreichem Ausführen der Aktion Exportieren aktivierbar. |
| Weitere GINOs exportieren | Die Maske wird zurück gesetzt. Erst nach einem erfolgreichem Ausführen der Aktion Exportieren aktivierbar. |
| Zurück | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| Seriennummer | SERIENNUMMER | K | E | Seriennummer des zu exportierenden Gerätes | 50 alphanumerische Zeichen |
| Datei auswählen |  | K | E | Datei mit Seriennummern | Dateiupload |
| Ausgewählte Seriennummern |  | K | A | Liste mit Seriennummern | Durch „Seriennummern importieren" ausgefüllt |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Provider (OPC_CLV_GUI_089)

|  |  |
| --- | --- |
| Radio-Button | Markierung des jeweiligen Provider |
| Provider anlegen | Anlegen eines neuen Provider. Anzeigen der Maske „Provider anlegen (OPC\_CLV\_GUI\_090)". |
| Provider löschen | Löschen eines Provider.Aufruf der Funktion „Provider löschen (OPC\_CLV\_FKT\_176)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung.Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Provider ändern | Ändern eines Provider.   Anzeigen der Maske „Provider ändern (OPC\_CLV\_GUI\_091)". |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung (Startdialog Client-Verwaltung (OPC\_CLV\_GUI\_001)) |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="fc7b21a5-e154-4754-a432-3bfeee3b9b53"><ac:plain-text-body><![CDATA[ | Provider-ID |  | M | A[1:n] | Liste mit den gültigen Provider-IDs | alphanumerische Zeichen | ]]></ac:plain-text-body></ac:structured-macro> |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="18e4862d-861c-4033-b6f1-f1cb44387192"><ac:plain-text-body><![CDATA[ | Providername |  | M | A[1:n] | Liste mit den Namen der Provider | 30 alphanumerische Zeichen | ]]></ac:plain-text-body></ac:structured-macro> |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Provider anlegen (OPC_CLV_GUI_090)

|  |  |
| --- | --- |
| Speichern | Speichern des neuen Provider. Aufruf der Funktion „Provider speichern (OPC\_CLV\_FKT\_177)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung.Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Zurück | Anzeige der ProviderAnzeigen der Maske „Provider (OPC\_CLV\_GUI\_089)" |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Providername |  | M | E | Name des Provider | 30 alphanumerische Zeichen |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Provider ändern (OPC_CLV_GUI_091)

|  |  |
| --- | --- |
| Ändern | Ändern des Provider.Aufruf der Funktion „Provider ändern (OPC\_CLV\_FKT\_178)". Im Erfolgsfall: Ausgabe der Erfolgsmeldung.Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Zurück | Anzeigen der Maske „Provider (OPC\_CLV\_GUI\_089)". |

| **Maskenfeld** | **DB –**   **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| --- | --- | --- | --- | --- | --- |
| Provider-ID |  | M | A | Provider-ID | alphanumerische Zeichen |
| Providername |  | M | E | Name des Provider | 30 alphanumerische Zeichen |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO Produktionsdaten importieren (OPC_CLV_GUI_087)

Die folgenden Aktionen sind aktivierbar

|  |  |
| --- | --- |
| Importieren | Die Funktion „“ wird aufgerufen. Als Produktionsdaten wird der Inhalt des unter "Produktionsdaten laden" geladenen Files verwendet.  Die Aktion „Produktionsdaten laden“ wird gesperrt.  Im Fehlerfall: Ausgabe der entsprechenden Fehlermeldungen.  Im Fall, dass Produktionsdaten schon vorhanden waren: Ausgabe der Warnung und Abfrage der Bestätigung "Import erzwingen".  Im Erfolgsfall: Ausgabe der Erfolgsmeldung sowie Freigabe der Aktionen „Weitere GINOs importieren“. |
| Produktionsdaten laden | Es wird die Funktion „“ mit der ausgewählten Datei ausgeführt und der Inhalt in „Ausgewählte Produktionsdaten“ hinzugefügt. Nach einem erfolgreichen Import wird diese Aktion gesperrt und es kann kein weiteres File angegeben werden. |
| Weitere GINOs importieren | Die Maske wird zurückgesetzt. Erst nach einem erfolgreichen Ausführen der Aktion Importieren aktivierbar. |
| Schließen | Anzeigen Funktionsauswahl Client-Verwaltung () |

|  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- |
| **Maskenfeld** | **DB –**  **Feldname** | **M/K** | **E/A** | **Beschreibung** | **Plausibilität** |
| Datei auswählen |  | M | E | Datei mit Produktionsdaten | Dateiupload |
| Ausgewählte Produktionsdaten |  | M | A | Produktionsdaten | Durch „Produktionsdaten laden“ ausgefüllt |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Session-Verwaltung - GUI




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Startdialog Session-Verwaltung (OPC_SEV_GUI_001)

Im Startdialog des Menüpunktes „Session-Verwaltung" werden die Abläufe als Menüpunkte angeboten.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Anmeldungen | Link auf die Maske „Anmeldung suchen (OPC\_SEV\_GUI\_002)" |
| Anzahl offener VP-Sessions anzeigen | Link auf die Maske „Anzahl offener VP-Sessions suchen (OPC\_SEV\_GUI\_004)" |
| Session Information | Link auf die Maske „Session-Informationen suchen (OPC\_SEV\_GUI\_006)" |
| Dialog/Session beenden | Link auf die Maske „Dialog/Session beenden suchen" |
| Home | Link auf das Hauptmenü OPC (OPC\_GEN\_GUI\_001) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anmeldung suchen (OPC_SEV_GUI_002)

|  |  |
| --- | --- |
| Suchen | Aufruf der Funktion „Anmeldungen ermitteln (OPC\_SEV\_FKT\_003)"   Im Erfolgsfall: Anzeige der Maske „Anmeldungen Liste (OPC\_SEV\_GUI\_003)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | E |  | Numerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anmeldungen Liste (OPC_SEV_GUI_003)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Session-Verwaltung (OPC\_SEV\_GUI\_001)" |
| Zurück | Anzeige der Maske „Anmeldung suchen (OPC\_SEV\_GUI\_002)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A | Die VPNR nach der gesucht wurde | Numerisch |  |
| pro Anmeldung |  |  |  |  |
| o-card ID / Seriennummer | M | A |  | Numerisch |  |
| Anmeldetyp | M | A | Art der Anmeldung | „O", „S" |  |
| Zeitpunkt | M | A | Zeitpunkt der Anmeldung | ‚TT.MM.JJJJ HH:mm:SS' |  |
| o-card Laufnummer | M | A | Laufnummer | Numerisch |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anzahl offener VP-Sessions suchen (OPC_SEV_GUI_004)

|  |  |
| --- | --- |
| Suchen | Aufruf der Funktion „Anzahl offener VP-Sessions ermitteln (OPC\_SEV\_FKT\_002)"   Im Erfolgsfall: Anzeige der Maske „Anzahl offene VP-Session Details (OPC\_SEV\_GUI\_005)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | K | E | Die VPNR nach der gesucht wurde | Numerisch |
| GINA-IP | K | E |  | IP4 / IP6 |
| Session offen seit | M | E | Mögliche Werte:   * 1 min * 5 min * 10 min * 20 min * 30 min * 60 min * 2 Std * 1 Tag | Auswahl |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anzahl offene VP-Session Details (OPC_SEV_GUI_005)

|  |  |
| --- | --- |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Session-Verwaltung (OPC\_SEV\_GUI\_001)" |
| Zurück | Anzeige der Maske „Anmeldung suchen (OPC\_SEV\_GUI\_002)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A | Vertragspartnernummer nach der gesucht wurde, oder leer | Numerisch |
| GINA-IP | M | A | GINA-IP nach der gesucht wurde, oder leer | IP4 / IP6 |
| Gesamtzahl offener VP-Dialoge/Sessions | M | A |  | Alphanumerisch <#Dialoge>/<#Sessions> |
| Anzal der VP-Dialoge/Sessions die länger als 1 min offen sind | M | A |  | Alphanumerisch<#Dialoge>/<#Sessions> |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Session-Informationen suchen (OPC_SEV_GUI_006)

|  |  |
| --- | --- |
| Suchen | Aufruf der Funktion „Session(s) suchen (OPC\_SEV\_FKT\_001)"   Im Erfolgsfall: Anzeige der Maske „Session-Informationen Liste (OPC\_SEV\_GUI\_007)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | K | E |  | Numerisch |
| Ordinationsnummer | K | E |  | Numerisch |
| GINA-IP | K | E |  | IP4 / IP6 |
| Terminal-ID | K | E |  | Alphanumerisch |
| Externe User-ID | K | E |  | Alphanumerisch |
| Datum von | K | E |  | ‚TT.MM.JJJJ' |
| Datum bis | K | E |  | ‚TT.MM.JJJJ' |
| Feld 1 | M | E | Mögliche Werte   * Ordinationsnummer * Datum von * Dialog-ID | Auswahl |
| Reihenfolge | M | E |  | „aufsteigend", „absteigend" |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Session-Informationen Liste (OPC_SEV_GUI_007)

|  |  |
| --- | --- |
| Ausgewählte Sessioninformation anzeigen | Aufruf der Funktion „Sessiondetails ermitteln (OPC\_SEV\_FKT\_004)" für das durch „Auswahl" bestimmte Element.   Im Erfolgsfall: Anzeige der Maske „Session Details (OPC\_SEV\_GUI\_008)"   Im Fehlerfall: Ausgabe der Fehlermeldung |
| Zurück | Anzeige der Maske „Session-Informationen suchen (OPC\_SEV\_GUI\_006)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A | Vertragspartnernummer nach der gesucht wurde, oder leer | Numerisch |  |
| Ord.Nr. | M | A | Ordinationsnummer nach der gesucht wurde, oder leer | Numerisch |  |
| GINA-IP | M | A | GINA-IP nach der gesucht wurde, oder leer | IP4 / IP6 |  |
| Terminal-ID | M | A | Id nach der gesucht wurde, oder leer | Alphanumerisch |  |
| Externe User-Id | M | A | User-Id nach der gesucht wurde, oder leer | Alphanumerisch |  |
| von | M | A | „Datum von" nach dem gesucht wurde, oder leer | ‚TT.MM.JJJJ' |  |
| bis | M | A | „Datum bis" nach dem gesucht wurde, oder leer | ‚TT.MM.JJJJ' |  |
| pro Session Information |  |  |  |  |
| Auswahl | M | E |  | RadioButton |  |
| Ordination | M | A | Link auf die Aktion „Ausgewählte Sessioninformation anzeigen" mit dem aktuellen Element als „Auswahl". | „Straße Haus/Tür, PLZ Ort" |  |
| Ord. Nr. | M | A |  | Numerisch |  |
| Dialog-ID | M | A |  | Numerisch |  |
| GINA-IP | M | A |  | IP4 / IP6 |  |
| Terminal-ID | M | A |  | Alphanumerisch |  |
| Externe User-ID | M | A |  | Alphanumerisch |  |
| von | M | A |  | ‚TT.MM.JJJJ HH:mm:SS' |  |
| bis | M | A |  | ‚TT.MM.JJJJ HH:mm:SS' |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Session Details (OPC_SEV_GUI_008)

|  |  |
| --- | --- |
| Liste der Requests anzeigen | Anzeigen der Maske „Request Liste (OPC\_SEV\_GUI\_009)" für die angezeigte Session. |
| Zurück | Anzeigen der Maske „Session-Informationen Liste (OPC\_SEV\_GUI\_007)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Ordination | M | A | Anschrift der Ordination | Alphanumerisch   "<Straße> <Hausnummer>/<Tür>, <PLZ> <Ort>" |  |
| Ord.Nr. | M | A |  | Numerisch |  |
| Gina-IP | M | A |  | IP4 / IP6 |  |
| Terminal-ID | M | A |  | Alphanumerisch |  |
| ASW Produkt-ID | M | A |  | Numerisch |  |
| ASW Produkt-Version | M | A |  | Numerisch |  |
| ASW-Hersteller-ID | M | A |  | Alphanumerisch |  |
| **<KVW-12428>ASW-Hersteller-Name** | K | A |  | Alphanumerisch |  |
| Externe User-ID | M | A |  | Numerisch |  |
| GINA SW-Information | M | A |  | Alphanumerisch |  |
| von | M | A |  | ‚TT.MM.JJJJ HH:mm' |  |
| bis | M | A |  | ‚TT.MM.JJJJ HH:mm' |  |
| Beendigungsgrund | M | A |  | Alphanumerisch |  |
| Anmeldetyp | M | A |  | Alphanumerisch |  |
| ocard | M | A |  | Numerisch"<CardNr> <Laufnr>" |  |
| SW-Zertifikat | M | A |  | Numerisch |  |
| Activity timeout | M | A |  | Numerisch |  |
| Inactivity timeout | M | A |  | Numerisch |  |
| Letzter Zugriff | M | A |  | 'TT.MM.JJJJ HH:mm' |  |
| Dialog-ID | M | A |  | Alphanumerisch |  |
| GINA-Host | M | A |  | IP4 / IP6 |  |
| User Agend | M | A |  | Alphanumerisch |  |
| Rolle | M | A | Elga Rolle | Alphanumerisch |  |
| GDAMA | M | A | Elga GDMA | Alphanumerisch |  |
| Servicename | M | A | Elga Servicename | Alphanumerisch |  |
| pro Applikationsserver |  |  |  |  |
| Kennung | M | A | Applikationsname | Alphanumerisch |  |
| URL | M | A | URL auf den Contextpfad (kein Link) | URL |  |
| Servername | M | A | Name des Servers | Alphanumerisch |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Request Liste (OPC_SEV_GUI_009)

|  |  |
| --- | --- |
| Schließen | Anzeige der Maske „Startdialog Session-Verwaltung (OPC\_SEV\_GUI\_001)" |
| Zurück | Anzeigen der Maske „Session Details (OPC\_SEV\_GUI\_008)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| GINA-IP | M | A |  |  |  |
| Terminal-ID | M | A |  |  |  |
| von | M | A |  |  |  |
| bis | M | A |  |  |  |
| pro Request |  |  |  |  |
| Datum | M | A |  |  |  |
| Applikationsserver | M | A |  |  |  |
| Applikation | M | A |  |  |  |
| Methode | M | A |  |  |  |
| Interface-Name | M | A |  |  |  |
| Interface-Version | M | A |  |  |  |
| Dauer | M | A |  |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Dialog_Session beenden suchen (OPC_SEV_GUI_010)

|  |  |
| --- | --- |
| Suchen | Aufruf der Funktion: „Session(s) suchen (OPC\_SEV\_FKT\_001)"   Im Erfolgsfall: Anzeige der Maske „Dialog/Session beenden Liste (OPC\_SEV\_GUI\_011)"   Im Fehlerfall: Ausgabe der Fehlermeldung |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Vertragspartnernummer | K | E |  | Numerisch |
| Ordinationsnummer | K | E |  | Numerisch |
| GINA-IP | K | E |  | IP4 / IP 6 |
| Terminal-ID | K | E |  | Numerisch |
| Feld 1 | M | E | Mögliche Werte   * Ordinationsnummer * Datum von * Dialog-ID | Auswahl |
| Reihenfolge | M | E |  | „aufsteigend", „absteigend" |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Dialog_Session beenden Liste (OPC_SEV_GUI_011)_KVW-12428_

|  |  |
| --- | --- |
| Alle Sessions auswählen | Aktiviert alle „Auswahl" Felder in der Liste. |
| Ausgewählte Dialoge/Sessions beenden | **Maske wird neuerlich angezeigt:**   * Die Eingabe für „Auswahl" und „Zeitpunkt" sind deaktiviert. * Button „Alle Sessions auswählen" wird nicht angezeigt. * Ausblenden aller nicht ausgewählten Elemente. * Ausgabe der Rückfragemeldung * Bei Bestätigung Aufruf der Funktion „Session beenden (OPC\_SEV\_FKT\_005)" für alle gewählten Elemente Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"         Im Fehlerfall: Ausgabe der Fehlermeldung * Bei Ablehnung Anzeige der Maske „Dialog/Session beenden Liste (OPC\_SEV\_GUI\_011)" (Rücksetzten aller Änderungen) |
| Zurück | Anzeige der Maske „Dialog/Session beenden suchen (OPC\_SEV\_GUI\_010)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Vertragspartnernummer | M | A |  |  |  |
| Ord.Nr. | M | A |  |  |  |
| GINA-IP | M | A |  |  |  |
| Terminal-ID | M | A |  |  |  |
| Zeitpunkt | M | E | Mögliche Werte:   * sofort * 10 min * 60 min | Auswahl |  |
| pro Dialog/Session |  |  |  |  |
| Auswahl | M | E | Mehrfachauswahl | Checkbox |  |
| Ordination | M | A |  |  |  |
| Ord.Nr. | M | A |  |  |  |
| Dialog-ID | M | A |  |  |  |
| GINA-IP | M | A |  |  |  |
| Terminal-ID | M | A |  |  |  |
| Hersteller-ID | K | A | Die ID des Herstellers |  |  |
| Hersteller-Name | K | A | Der Firmenname des Herstellers (SYS\_ASWH) |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Nachrichten Verwaltung - GUI




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Startdialog Nachrichten (OPC_MES_GUI_001)

Im Startdialog des Menüpunktes „Nachrichten" werden die Abläufe als Menüpunkte angeboten.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Nachricht erstellen | Link auf die Maske „Nachricht erstellen (OPC\_MES\_GUI\_002)" |
| Nachricht suchen | Link auf die Maske „Nachricht suchen (OPC\_MES\_GUI\_004)" |
| Home | Link auf das Hauptmenü OPC (OPC\_GEN\_GUI\_001) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Nachricht erstellen (OPC_MES_GUI_002)

|  |  |
| --- | --- |
| Prüfen | Aufruf der Funktion „"  Im Erfolgsfall: Anzeige der Maske „"  Im Fehlerfall: Ausgabe der Fehlermeldung. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Meldungsname | M | E | Bezeichner der Meldung   Angezeigter Hinweis: „*Achtung: Diese Information wird beim GDA ausgegeben!"* | alphanumerisch |
| Meldungstext | M | E | Vollständiger Text der Meldung | Alphanumerisch, Mehrzeilig |
| Gültig von | M | E |  | ‚TT.MM.JJJJ'  ‚TT.MM.JJJJ HH:mm' |
| Gültig bis | M | E |  | ‚TT.MM.JJJJ'  ‚TT.MM.JJJJ HH:mm' |
| Nachricht ungefiltert an alle Vertragspartner versenden? | M | E | Bei Aktivierung werden alle Filter-Eingaben deaktiviert und für die weitere Verarbeitung ignoriert. | Checkbox |
| Nachricht nicht an aktive Sessions versenden? | M | E |  | Checkbox |
| Vertragspartner-Filter |  |  | Anzeigeblock, optisch abgegrenzt |  |
| Vertragspartnernummer | K | E |  | Numerisch |
| Dateiname | K | E | Dateiupload für eine Auswahlliste an VP Nummern im CSV Format. | Dateiupload |
| Zuname | K | E |  | Alphanumerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Nachricht prüfen (OPC_MES_GUI_003)

|  |  |
| --- | --- |
| Speichern | Aufruf der Funktion „"  Im Erfolgsfall: Anzeige der Maske „"  Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Zurück | Anzeige der Maske „" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Meldungsname | M | A | Bezeichner der Meldung   Angezeigter Hinweis: „*Achtung: Diese Information wird beim GDA ausgegeben!"* | Alphanumerisch |
| Meldungstext | M | A | Vollständiger Text der Meldung | Alphanumerisch |
| Gültig von | M | A |  | ‚TT.MM.JJJJ'  ‚TT.MM.JJJJ HH:mm' |
| Gültig bis | M | A |  | ‚TT.MM.JJJJ'  ‚TT.MM.JJJJ HH:mm' |
| Nachricht ungefiltert an alle Vertragspartner versenden? | M | A |  | Checkbox |
| Nachricht nicht an aktive Sessions versenden? | M | A |  | Checkbox |
| Anzahl Adressaten | M | A |  | Numerisch |
| Filter |  |  | Anzeigeblock, optisch abgegrenzt |  |
| Vertragspartnernummer | M | A |  | Alphanumerisch |
| Zuname | M | A |  | Alphanumerisch |
| Adressaten |  |  | Anzeigeblock, optisch abgegrenzt |  |
| Vertragspartner | M | A |  | Alphanumerisch |
| Zuname | M | A |  | Alphanumerisch |
| Ordinationsnummer | M | A |  | Alphanumerisch |
| Bundesland | M | A |  | Alphanumerisch |
| PLZ | M | A |  | Alphanumerisch |
| Ort | M | A |  | Alphanumerisch |
| Strasse | M | A |  | Alphanumerisch |
| Besitzverhältniscode | M | A |  | Numerisch |
| VP Session ID | M | A |  | Numerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Nachricht suchen (OPC_MES_GUI_004)

|  |  |
| --- | --- |
| Suchen | Aufruf der Funktion „Nachricht suchen (OPC\_MES\_FKT\_003)"   Im Erfolgsfall: Anzeige der Maske „Nachrichten Liste (OPC\_MES\_GUI\_005)"   Im Fehlerfall: Ausgabe der Fehlermeldung. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Meldungsnummer | K | E |  | Numerisch |
| Meldungsname | K | E |  | Alphanumerisch |
| Gültig von | K | E |  | ‚TT.MM.JJJJ'   ‚TT.MM.JJJJ HH:mm' |
| Gültig bis | K | E |  | ‚TT.MM.JJJJ'   ‚TT.MM.JJJJ HH:mm' |
| Nachrichten | K | E |  | Mögliche Werte:   * alle * aktiv * inaktiv |
| Ersteller | K | E |  | Alphanumerisch |
| Erstelldatum | K | E |  | ‚TT.MM.JJJJ HH:mm |
| Vertragspartnernummer | K | E |  | Numerisch |
| Feld1 | K | E | Sortierungsfeld | Mögliche Werte:   * Meldungsnummer * Meldungsname * Gültig Von * Gültig Bis |
| Reihenfolge | K | E | Sortierungsreihenfolge | Mögliche Werte:   * aufsteigend * absteigend |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Nachrichten Liste (OPC_MES_GUI_005)

|  |  |
| --- | --- |
| Schließen | Anzeige der Maske „Startdialog Nachrichten (OPC\_MES\_GUI\_001)" |
| Details anzeigen | Aufruf der Funktion „Details einer Nachricht ermitteln (OPC\_MES\_FKT\_004)" für die gewählte Nachricht.   Anzeige der Maske „Nachrichten Detail (OPC\_MES\_GUI\_006)" |
| Zurück | Anzeige der Maske „Nachricht suchen (OPC\_MES\_GUI\_004)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Meldungsnummer | M | A |  |  |  |
| Meldungsname | M | A |  |  |  |
| Gültig von | M | A |  |  |  |
| Gültig bis | M | A |  |  |  |
| Nachrichten | M | A |  |  |  |
| Ersteller | M | A |  |  |  |
| Erstelldatum | M | A |  |  |  |
| Vertragspartnernummer | M | A |  |  |  |
| pro Nachricht |  |  |  |  |
| Auswahl | M | E |  | RadioButton |  |
| Meldungnummer | M | A |  | Link |  |
| Meldungname | M | A |  |  |  |
| Gültig von | M | A |  |  |  |
| Gültig bis | M | A |  |  |  |
| Nachricht aktiv | M | A |  |  |  |
| Nachricht gelöscht | M | A |  |  |  |
| Ersteller | M | A |  |  |  |
| Erstelldatum | M | A |  |  |  |
| Anzahl Adressaten | M | A |  |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Nachrichten Detail (OPC_MES_GUI_006)

|  |  |
| --- | --- |
| Schließen | Anzeige der Maske „Startdialog Nachrichten (OPC\_MES\_GUI\_001)" |
| Nachricht löschen | Anzeige der Maske „Rückfrage (OPC\_GEN\_GUI\_004)"   * Bei Bestätigung         Aufruf der Funktion „Nachricht löschen (OPC\_MES\_FKT\_005)"     Im Erfolgsfall: Anzeige der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"     Im Fehlerfall: Ausgabe der Fehlermeldung * Bei Ablehnung     Anzeige der Maske „Nachrichten Detail (OPC\_MES\_GUI\_006)" |
| Adressaten Anzeigen | Aufruf der Funktion „Adressaten einer Nachricht ermitteln (OPC\_MES\_FKT\_007)"   Im Erfolgsfall: Anzeige der Maske „Adressaten Liste (OPC\_MES\_GUI\_007)"   Im Fehlerfall: Ausgabe der Fehlermeldung |
| Zurück | Anzeige der Maske „Nachrichten Liste (OPC\_MES\_GUI\_005)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Meldungsname |  | A |  |  |
| Meldungstext |  | A |  |  |
| Gültig von |  | A |  | ‚TT.MM.JJJJ'   ‚TT.MM.JJJJ HH:mm' |
| Gültig bis |  | A |  | ‚TT.MM.JJJJ'   ‚TT.MM.JJJJ HH:mm' |
| Nachricht aktiv? |  | A |  |  |
| Ersteller |  | A |  |  |
| Erstelldatum |  | A |  |  |
| Löschdatum |  | A |  |  |
| Anzahl Adressaten |  | A |  |  |
| Filter |  |  | Anzeigeblock, optisch abgegrenzt |  |
| Vertragsparternummern |  | A |  |  |
| Zuname |  | A |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Adressaten Liste (OPC_MES_GUI_007)

|  |  |
| --- | --- |
| Zurück | Anzeige der Maske „Nachrichten Detail (OPC\_MES\_GUI\_006)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Pro Adressat |  |  |  |  |
| Vertragspartner | M | A |  |  |  |
| Zuname | M | A |  |  |  |
| Ordinationsnummer | K | A |  |  |  |
| Bundesland | K | A |  |  |  |
| PLZ | K | A |  |  |  |
| Ort | K | A |  |  |  |
| Strasse | K | A |  |  |  |
| Besitzverhältnis Code | M | A |  |  |  |
| VP Session ID | K | A |  |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Chipkartenverwaltung - GUI




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Startdialog Chipkartenverwaltung (OPC_CKV_GUI_001)

Im Startdialog des Menüpunktes „Chipkarten-Verwaltung" werden die Administrationsfunktionen der Chipkartenverwaltung ausgegeben.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Karten verwalten | Link auf die Maske „Chipkarten Suchkriterien (OPC\_CKV\_GUI\_002)" |
| Fotoinformation suchen | Link auf die Maske „Fotoinformation Suchen (OPC\_CKV\_GUI\_007)" |
| Fotoinformationen beenden | Link auf die Maske „Fotoinformationen beenden (OPC\_CKV\_GUI\_009)" |
| Foto-Avisos anlegen | Link auf die Maske „Foto-Avisos anlegen (OPC\_CKV\_GUI\_010)" |
| Home | Link auf das Hauptmenü OPC (OPC\_GEN\_GUI\_001) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Chipkarten Suchkriterien (OPC_CKV_GUI_002)

|  |  |
| --- | --- |
| Suchen | Führt die Funktion „Karten suchen (OPC\_CKV\_FKT\_001)" aus.   Im Erfolgsfall: Anzeige der Maske „Chipkarten Ergebnis Liste (OPC\_CKV\_GUI\_003)"   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Karte | M | E | Kartentyp nach dem gesucht werden soll. | „e-card", „o-card" |
| Card-ID | K | E | Die Id der Karte. | Numerisch |
| SV-/VP-Nummer | K | E | Nimmt entweder eine SV-Nummer oder eine VP-Nummer als Eingabe an. Das gewählte Format hängt von der Einstellung unter dem Feld Karte ab. „e-card" erlaubt nur SV-Nummern, „o-card" VP-Nummern. | Numerisch |
| Kartenfolgenummer | K | E |  | Numerisch |
| CIN | K | E |  | Numerisch |
| Status | K | E | Mögliche Werte   * 04 Produktion fehlgeschlagen * 12 Endgültige Sperre fehlerhaft * 23 Vorläufige Sperre fehlerhaft * 33 Sperre storniert fehlerhaft | Auswahl |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Chipkarten Ergebnis Liste (OPC_CKV_GUI_003)

|  |  |
| --- | --- |
| Details ausgeben | Anzeige der Maske „Chipkarten Details (OPC\_CKV\_GUI\_004)" für die gewählte Karte an.   Im Fehlerfall: Ausgabe der Fehlermeldung (keine Karte ausgewählt). |
| Zurück | Anzeige der Maske „Chipkarten Suchkriterien (OPC\_CKV\_GUI\_002)" |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Treffer | M | A | Anzahl der gefundenen Karten | Numerisch |  |
| Karte | M | A | Ausgabe des Suchparameters „Karte", bezeichnet den Kartentyp nach dem gesucht wurde. | „e-card", „o-card" |  |
| Card-ID | K | A | Ausgabe des Suchparameters „Card-ID" | Numerisch |  |
| SV-/VP-Nummer | K | A | Ausgabe des Suchparameters „SV-/VP-Nummer) | Numerisch |  |
| Kartenfolgenummer | K | A | Ausgabe des Suchparameters „Kartenfolgenummer" | Numerisch |  |
| CIN | K | A | Ausgabe des Suchparameters „CIN" | Numerisch |  |
| Status | K | A | Ausgabe des Suchparameters „Status" | Numerisch |  |
| Pro gefundener Karte. |  |  |  |  |
| Auswahlfeld | M | E | Legt die Karte fest auf die Button-Aktionen wirken. | RadioButton |  |
| SV-/VP-Nummer | M | A | Link zu „Chipkarten Details (OPC\_CKV\_GUI\_004)" | Numerisch |  |
| Kartenfolgenummer | M | A |  | Numerisch |  |
| Card-ID | M | A |  | Numerisch |  |
| CIN | M | A |  | Numerisch |  |
| Status | M | A |  | Numerisch |  |
| Datum | M | A |  | ‚TT.MM.JJJJ HH:mm' |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Chipkarten Details (OPC_CKV_GUI_004)

|  |  |
| --- | --- |
| Bemerkung erfassen | Anzeige der Maske „". |
| Endzustand setzen | Anzeige der Maske „".  Ist nur aktiv wenn die Karte das Flag EndStatusAllowed besitzt. |
| Zurück | Anzeige der Maske „" |
| Löschen | Anzeige der Maske "" wird nur für die e-cards mit Status 13 ("Quittung Endgültige Sperre OK") aktiv. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |  |
| --- | --- | --- | --- | --- | --- |
| Treffer | M | A | Anzahl der gefundenen Karten | Numerisch |  |
| Karte | M | A | Kartentyp | „e-card", „o-card" |  |
| Card-ID | M | A | Karten ID | Numerisch |  |
| CIN | M | A |  | Numerisch |  |
| SV-/VP-Nummer | M | A | VPNR/VSNR | Numerisch |  |
| Kartenfolgenummer | M | A |  | Numerisch |  |
| Bemerkung | M | A |  | Alphanumerisch |  |
| Bemerkungsbearbeiter | M | A | Bemerkungsuser | Alphanumerisch |  |
| Bemerkungsdatum | M | A |  | ‚TT.MM.JJJJ HH:mm' |  |
| pro Statuseintrag |  |  |  |  |
| Statuscode | M | A | Status | Numerisch |  |
| Grundcode | K | A | Grund Code | Numerisch |  |
| Grundtext | K | A | Bemerkung | Alphanumerisch |  |
| Datum | M | A |  | ‚TT.MM.JJJJ HH:mm' |  |
| Bearbeiter | K | A | User | Alphanumerisch |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Chipkarte bearbeiten (OPC_CKV_GUI_005)

|  |  |
| --- | --- |
| Speichern | Aufruf der Funktion: „Bemerkung zur Karte ändern (OPC\_CKV\_FKT\_003)"   Im Erfolgsfall: Aufruf der Maske „Chipkarten Details (OPC\_CKV\_GUI\_004)" mit der aktualisierten Karte.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Zurück | führt zur Maske „Chipkarten Ergebnis Liste (OPC\_CKV\_GUI\_003)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Bemerkung | M | E | Bemerkungstext des Benutzers | Alphanumerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Chipkarte Endzustand setzen (OPC_CKV_GUI_006)

|  |  |
| --- | --- |
| Speichern | Aufruf der Funktion „Endstatus setzen (OPC\_CKV\_FKT\_006)"   Im Erfolgsfall: Aufruf der Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)"   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Zurück | führt zur Maske „Chipkarten Ergebnis Liste (OPC\_CKV\_GUI\_003)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Hinweis | M | A | „Wird der Endstatus gesetzt, so muss zwingend eine Bemerkung angegeben werden" |  |
| Bemerkung | M | E | Bemerkungstext des Benutzers | Alphanumerisch |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Fotoinformation Suchen (OPC_CKV_GUI_007)

|  |  |
| --- | --- |
| Suchen | Führt die Funktion „Fotoinformation suchen (OPC\_CKV\_FKT\_002)" aus.   Im Erfolgsfall: Anzeige der Maske „Fotoinformation Details (OPC\_CKV\_GUI\_008)"   Im Fehlerfall Ausgabe der Fehlermeldung. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| SV-Nummer | M | E | SV-Nummer | Numerisch |
| Typ | K | E | Mögliche Werte   * FA - Foto-Aviso * FZ – Foto zwingend notwendig | Auswahl |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Fotoinformation Details (OPC_CKV_GUI_008)

|  |  |
| --- | --- |
| Fotoinformation beenden | Führt die Funktion „Fotoinformation beenden (OPC\_CKV\_FKT\_007)" aus.   Im Erfolgsfall wird die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" angezeigt.   Im Fehlerfall Ausgabe der Fehlermeldung.. |
| Zurück | Anzeige der Maske „Fotoinformation Suchen (OPC\_CKV\_GUI\_007)" |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| VSNR | M | A | VSNR | Numerisch |
| Typ | M | A | Typ der Fotoinformation | „FA - Foto-Aviso"„FZ – Foto zwingend notwendig" |
| Einmeldezeitpunkt | M | A | Beginn der Fotoinformation | TT.MM.JJJJ |
| Sperre ab | K | A | Datum der KSE-Sperre | TT.MM.JJJJ |
| Sperre durch | K | A | Kennzeichen des Akteurs, der die Sperre getriggert hat. | „VP – Vertragspartner"„SD – Sachbearbeiter"„CK - CKV" |
| Meldung durch | K | A | Kennzeichen des Akteurs der Meldung | „SD – Sachbearbeiter"„CK – CKV"„OP – OPC" |
| ID Meldung | K | A | ID des Akteurs der Meldung | alphanumerisch |
| Hinweis | K | E/A | Freitextfeld   Hinweis: ein bestehender Eintrag kann beim Löschen überschrieben werden. |  |
| Neuer Ausstellungsantrag angefordert | M | A | Flag | Checkboc |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Fotoinformationen beenden (OPC_CKV_GUI_009)

|  |  |
| --- | --- |
| Fotoinformationen beenden | Führt die Funktion „Fotoinformationen beenden (OPC\_CKV\_FKT\_008)" aus.   Im Erfolgsfall wird die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" angezeigt.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| VSNR-Datei | M | E | Datei mit Versicherungsnummern | Dateiupload |
| Typ | K | E | Mögliche Werte   * FA - Foto-Aviso * FZ – Foto zwingend notwendig | Auswahl |
| Hinweis | K | E |  | Freitextfeld |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Foto-Avisos anlegen (OPC_CKV_GUI_010)

|  |  |
| --- | --- |
| Foto-Avisos anlegen | Führt die Funktion „Foto-Avisos anlegen (OPC\_CKV\_FKT\_009)" aus.   Im Erfolgsfall wird die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" angezeigt.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog Chipkartenverwaltung (OPC\_CKV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| VSNR-Datei | M | E | Datei mit Versicherungsnummern | Dateiupload |
| Hinweis | K | E |  | Freitextfeld |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Foto-Metadaten löschen (OPC_CKV_GUI_011)

|  |  |  |
| --- | --- | --- |
| Foto-Metadaten suchen | Führt die Funktion „[ Foto-Metadaten suchen (OPC\_CKV\_FKT\_011) | Foto-Metadaten suchen (OPC\_CKV\_FKT\_011)]" aus.   Falls Metadaten gefunden wurden, werden die entsprechenden Register in einer Liste mit Checkbox zur Auswahl ausgegeben und das Eingabefeld, sowie die Aktion „Foto-Metadaten suchen" gesperrt und die Aktionen „Foto-Metadaten löschen" und „Abbrechen" freigegeben.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Foto-Metadaten löschen   (Initial gesperrt) | Führt die Funktion „[ Foto-Metadaten löschen (OPC\_CKV\_FKT\_012) | Foto-Metadaten löschen (OPC\_CKV\_FKT\_012)]" aus.   Im Erfolgsfall wird die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" angezeigt und die Aktion „Foto-Metadaten suchen" wieder freigeschalten, sowie die Aktionen „Foto-Metadaten löschen" und „Abbrechen" wieder gesperrt.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Abbrechen   (Initial gesperrt) | Setzt die Seite auf den Startzustand zurück. |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| SV-Nummer | M | E | Sozialversicherungsnummer | Numerisch |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Checkbox | M | E | Auswahl zum Löschen | Checkbox |
| Register Kennzeichen |  | A | Kennzeichen des Registers in dem das Foto vorhanden ist. | Text |
| Ermittlungszeitpunkt |  | A | Zeitpunkt der Aktualisierung oder der Speicherung dieses Eintrags. | Text |
| Dokumenten Ausstellungsdatum |  | A | Ausstellungsdatum des Dokuments, aus dem das Foto verwendet wird. | Text |
| Beauskunftungs Enddatum |  | A | Datum bis zu dem das Foto beauskunftet wird. | Text |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Chipkarte-Verwalten löschen (OPC_CKV_GUI_012)

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


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Chipkarte-Verwalten DELETE-ZIP herunterladen (OPC_CKV_GUI_013)

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


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### ASWH-Verwaltung - GUI




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Startdialog ASWH-Verwaltung (OPC_AHV_GUI_001)

Im Startdialog des Menüpunktes „ASWH-Verwaltung" werden die Administrationsfunktionen der ASWH-Verwaltung ausgegeben.  
Die folgenden Aktionen sind aktivierbar:

|  |  |
| --- | --- |
| Hersteller importieren | Link auf die Maske „Hersteller importieren (OPC\_AHV\_GUI\_001)" |
| Hersteller exportieren | Link auf die Maske „Hersteller exportieren (OPC\_AHV\_GUI\_002)" |
| Home | Link auf das Hauptmenü OPC (OPC\_GEN\_GUI\_001) |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Hersteller importieren (OPC_AHV_GUI_001)

|  |  |
| --- | --- |
| Importieren | Führt die Funktion „Herstellerdaten importieren (OPC\_AHV\_FKT\_001)" aus.   Im Erfolgsfall wird die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" angezeigt.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog ASWH-Verwaltung (OPC\_AHV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Hersteller-Datei | M | E | Datei mit Herstellerdaten | Dateiupload |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Hersteller exportieren (OPC_AHV_GUI_002)

|  |  |
| --- | --- |
| Exportieren | Führt die Funktion „Herstellerdaten exportieren (OPC\_AHV\_FKT\_002)" aus.   Im Erfolgsfall wird die Maske „Erfolgsmeldung (OPC\_GEN\_GUI\_005)" angezeigt.Neben dem Button „Schließen" wird hier zusätzlich der Button „Download" angezeigt, welches es ermöglicht die bereitgestellte Datei über einen Standarddialog herunter zu laden.   Im Fehlerfall: Ausgabe der Fehlermeldung. |
| Schließen | Schließt den aktuellen Ablauf und kehrt zurück in die Ansicht „Startdialog ASWH-Verwaltung (OPC\_AHV\_GUI\_001)" |

| **Maskenfeld** | **M/K** | **E/A** | **Beschreibung** | **Format** |
| --- | --- | --- | --- | --- |
| Hersteller-Datei | M | A | Name der Datei, die für den Dowload erzeugt wird   Vorbelegt mit „ASWH\_IDS\_<Datum>.csv" |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


## OPC - Funktionen

Meldungsnummern die mit „GUI" beginnen (z.B. GUI-001) sind nicht Teil der Meldung und werden nicht ausgegeben. Sie wurden eingeführt um auf die Meldung referenzieren zu können und werden auserhalb dieses Dokuments nicht verwendet.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Anmelden am OPC




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anmelden (BF_ANM_FKT_001)

# Beschreibung

Die übergebene Benutzerkennung (User) und das übergebene Passwort werden der serverseitig konfigurierten Authentifizierungsstelle übermittelt und die Rückmeldung verarbeitet.

# Vorbedinungen

Der Benutzer ist nicht authentifiziert.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Kennung | Die Kennung des Benutzers |  | M |
| Passwort | Das Passwort des Benutzers |  | M |

# Ausgangsdaten

Die Ausgangsdaten stehen der aktiven Session zur Verfügung.

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Kennung | Die Kennung des Benutzers. |  | M |
| Benutzerrollen | Liste aller Rollen die dem Benutzer zugeordnet sind. |  | M |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Abmelden (BF_ABM_FKT_001)

# Beschreibung

Die aktive Session des Benutzers wird beendet.

# Vorbedingungen

Der Benutzer ist authentifizeirt.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Fenster ‚Hauptmenü e-card Betriebs-Client‘ ausgeben (BF_ANM_FKT_006)

# Beschreibung

Die Anmeldung an den OPC ist abgeschlossen und die Maske ‚Hauptmenü OPC' wird angezeigt.

# Vorbedingungen

Die Anmeldung war erfolgreich.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Kennung | Die Kennung des Benutzers |  | M |
| Berechtigungsstring | Beschreibung aller Rollen des Benutzers |  | M |

# Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Maskentexte | Alle Maskentexte werden aus den Properties-Dateien gelesen und die Maske angezeigt. |  | M |
| Funktionen | Liste der für den Benutzer erlaubten Funktionen |  | M |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Allgemeine Administration - Funktionen




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Systemparameter ausgeben (OPC_ALLG_FKT_001)

# Beschreibung

Es werden in der Datenbank alle Systemparameter durchsucht und jene retourniert, die der Gruppe in den Eingangsdaten entspricht, oder alle, wenn keine Gruppe angegeben wurde (Die Gruppen mit den Suffixen ".regex" und ".desc" werden ignoriert).  
Zu jedem ermittelten Parameter werden die Regeln zur Validierung für gültige Eingaben geladen und wenn vorhanden in das Ausgabefeld „Valid“ gespeichert.  
Zu jedem ermittelten Parameter wird versucht eine Beschreibung aus der Datenbank zu laden und bei Erfolg wird der gefundene Wert in „Description“ gesetzt.

# Vorbedingungen

Der aufrufende Benutzer hat eine Rolle, die ihn berechtigt, diese Systemparameter zu verwalten.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Gruppe | Filterkriterium, wenn gesetzt, werden nur Parameter dieser Gruppe ausgelesen. |  | K |

# Ausgangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |  |
| --- | --- | --- | --- | --- |
| pro Parameter |  |  |  |  |
| Name | Die Menge der eingerichteten Systemparameter wird aus der Datenbank ermittelt und in den entsprechenden Feldern angezeigt. |  | **M** |  |
| Name | Der Name des Parameters |  | **M** |  |
| Gruppe | Die Gruppe zu der dieser Parameter gehört |  | **M** |  |
| Wert | Der aktuelle Wert des Parameters |  | **M** |  |
| Default Wert | Der Standardwert für den Parameter |  | **K** |  |
| Valid | REGEX zur Beschreibung der gültigen Wertebereiche. |  | **K** |  |
| Editierbar | Flag zur Indikation, ob Änderungen am Wert des Parameters zulässig sind oder nicht. | ‚true', ‚false' | **M** |  |
| Unsichtbar | Flag zur Indikation, ob der Parameter angezeigt werden soll oder nicht. | ‚true', ‚false' | **M** |  |
| Description | Beschreibung zu dem Parameter |  | **K** |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Systemparameter ändern (OPC_ALLG_FKT_002)

# Beschreibung

Mittels dieser Funktion werden ein oder mehrere Systemparameter geändert. Die Änderungen werden protokolliert.

Dabei werden die Werte gegen den Regex-Wert aus der Datenbank validiert, insofern einer existiert und der Wert tatsächlich geändert wurde.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
|  | Alle Systemparameter |  | **M** |

# Ausgangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Return | Quittung OK oder Fehlernummer |  | **M** |

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
|  | Parameter "<PARAMETER\_NAME>" hat einen ungültigen Wert (Regex: <REGEX>). | Message | Error |
|  | Die Parameter <LISTE\_PARAMETER> wurden gespeichert. | Message | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Systemparameter hinzufügen (OPC_ALLG_FKT_003)

# Beschreibung

Es wird ein neuer Systemparameter mit den übergebenen Werten angelegt.

Dabei werden folgende Einträge in der Entität SYS\_PARAMETER erstellt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <WERT> | <GRUPPE> | 1 | <AKTUELLES\_DATUM> | <ID> | 1 | 0 |

Falls er Wert "Beschreibung" nicht leer ist, wird ein weiterer Parameter angelegt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <BESCHREIBUNG> | <GRUPPE>.desc | 1 | <AKTUELLES\_DATUM> | <ID> | 0 | 1 |

Falls der Wert "Regex" nicht leer ist, wird ein weiterer Parameter angelegt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <REGEX> | <GRUPPE>.regex | 1 | <AKTUELLES\_DATUM> | <ID> | 0 | 1 |

# Vorbedingungen

Ausfüllen des Formulars zum Hinzufügen neuer Systemparameter und betätigen von „Hinzufügen“.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des neuen Parameters | Max. 100 Zeichen | M |
| Wert | Der Standardwert des neuen Parameters | Max. 1000 Zeichen | M |
| Gruppe | Die Gruppe des neuen Parameters | Max. 44 Zeichen (damit .regex und .desc noch angehängt werden können) | M |
| Beschreibung | Die Beschreibung des neuen Parameters | Max. 1000 Zeichen | K |
| Regex | Die "Regex" Regel zur Validierung des Parameters | Max. 1000 Zeichen | K |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-070 | Der Systemparameter <PARAMETER\_NAME> wurde erfolgreich angelegt. | Message | Info |
| GUI-071 | Der Systemparameter <PARAMETER\_NAME> konnte nicht angelegt werden (<FEHLERMELDUNG>) | Message | Error |
| GUI-072 | Der Systemparameter <PARAMETER\_NAME> wurde vom Nutzer <USER\_ID> angelegt. | Log | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Systemparameter-Metainfo bearbeiten (OPC_ALLG_FKT_004)

# Beschreibung

Es wird der übergebene Systemparameter bearbeitet.

Dabei werden folgende Einträge in der Entität SYS\_PARAMETER erstellt:

Falls der Wert "Beschreibung" sich geändert hat, wird der Bestehende mit Name <NAME> und Gruppe <GRUPPE>.desc mit Aktuell <1> auf nicht aktuell gesetzt und ein weiterer Parameter angelegt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <BESCHREIBUNG> | <GRUPPE>.desc | 1 | <AKTUELLES\_DATUM> | <ID> | 0 | 1 |

Falls der Wert "Regex" sich geändert hat, wird der Bestehende mit Name <NAME> und Gruppe <GRUPPE>.regex mit Aktuell <1> auf nicht aktuell gesetzt und ein weiterer Parameter angelegt:

| Name | Wert | Gruppe | Aktuell | Lastchange | UserId | Editierbar | Unsichtbar |
| --- | --- | --- | --- | --- | --- | --- | --- |
| <NAME> | <REGEX> | <GRUPPE>.regex | 1 | <AKTUELLES\_DATUM> | <ID> | 0 | 1 |

# Vorbedingungen

Änderung eines Systemparameters und betätigen von „Ändern“.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des Parameters | Max. 100 Zeichen | M |
| Beschreibung | Die neue Beschreibung des neuen Parameters | Max. 1000 Zeichen | K |
| Regular Expression | Die Regular Expression zur Validierung des Parameters | Max. 1000 Zeichen | K |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-073 | Der Systemparameter <PARAMETER\_NAME> wurde erfolgreich editiert. | Message | Info |
| GUI-074 | Der Systemparameter <PARAMETER\_NAME> konnte nicht editiert werden (<FEHLERMELDUNG>) | Message | Error |
| GUI-075 | Der Systemparameter <PARAMETER\_NAME> wurde vom Nutzer <USER\_ID> bearbeitet. | Log | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Systemparameter löschen (OPC_ALLG_FKT_005)

# Beschreibung

Es wird der übergebene Systemparameter gelöscht.

Dabei wird die Historisierung verwendet und die Spalte "Aktuell" auf "0" gesetzt. Dies wird sowohl für den Parameter mit dem entsprechenden Namen an sich, als auch für die Beschreibung sowie den Regex durchgeführt.

# Vorbedingungen

Keine.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des Parameters | Max. 100 Zeichen | M |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-076 | Der Systemparameter <PARAMETER\_NAME> wurde erfolgreich gelöscht | Message | Info |
| GUI-077 | Der Systemparameter <PARAMETER\_NAME> konnte nicht gelöscht werden (<FEHLERMELDUNG>) | Message | Error |
| GUI-078 | Der Systemparameter <PARAMETER\_NAME> wurde vom Nutzer <USER\_ID> entfernt. | Log | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### System-Gruppen abfragen (OPC_ALLG_FKT_006)

# Beschreibung

Liest und liefert für jede Gruppe in den Systemparametern aus der Datenbank einen Eintrag.

# Ausgangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Gruppennamen | Eine Liste der Namen aller gefundenen Gruppen. Die Liste ist alphabetisch sortiert. |  | M |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Doku-Schlüssel lesen (OPC_ALLG_FKT_010)

# Beschreibung

Bei der Datei mit dem bereitzustellenden Zertifikat muss es sich um eine CER- oder PEM- Datei handeln. Andernfalls wird die Fehlermeldung DOKU-001 in die Ausgangsdaten gestellt.  
Serverseitig wird das Zertifikat aus der hochgeladene Datei als ‚X509Certificate' gelesen. Kann der Dateninhalt des Zertifikats nicht gelesen werden, so wird die Fehlermeldung DOKU-002 in die Ausgangsdaten gestellt.  
Der angegebene Auftrags-, Empfänger- und Schlüsseltyp wird geprüft. Entspricht die Kombination nicht einer der nachfolgenden, so wird die Fehlermeldung DOKU-003 in die Ausgangsdaten gestellt.

| **Auftragstyp** | **Empfängertyp** | **Schlüsseltyp** |
| --- | --- | --- |
| VUNeu | PST | ID (Identifikator) |
| VUNeu | PST | SO (Sensibles Objekt) |
| VUNeu | OGGH | ID (Identifikator) |
| VUNeu | OGGH | SO (Sensibles Objekt) |
| DMP | PST | ID (Identifikator) |
| DMP | PST | SO (Sensibles Objekt) |
| BKF | PST | ID (Identifikator) |
| BKF | PST | SO (Sensibles Objekt) |
| BKF | PST | ST (Standort) |

Nachdem das Zertifikat aus der Datei erfolgreich gelesen wurde, werden seine Attribute ermittelt und als Ergebnis in die Ausgangsdaten gestellt.

# Vorbedingungen

Eine Datei (Dateierweiterung ‚CER' oder ‚PEM') mit dem bereitzustellenden Zertifikat (RSA-Schlüssel) wurde an den Betrieb übergeben und vom Betrieb am definierten Ablageort abgelegt.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Datei | Datei mit dem bereitzustellenden Zertifikat |  | M |
| Auftragstyp | Auftragstyp   Mögliche Werte: siehe Beschreibung. | CHAR(10) | M |
| Empfängertyp | Empfängertyp   Mögliche Werte: siehe Beschreibung. | CHAR(4) | M |
| Schlüsseltyp | Schlüsseltyp   Mögliche Werte: siehe Beschreibung. | CHAR(3) | M |

# Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Seriennummer | Seriennummer des Zertifikats | CHAR(48) | K |
| Aussteller | DN des Ausstellers | CHAR(256) | K |
| Gültig von | Datum, ab dem das Zertifikat gültig ist | DATE | K |
| Gültig bis | Datum, bis zu dem das Zertifikat gültig ist | DATE | K |
| DN | DN des Antragstellers | CHAR(256) | K |
| Algorithmus | Algorithmus (Signaturalgorithmus, Schlüsselalgorithmus und Schlüssellänge) | CHAR(256) | K |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="bec5ab9a-8433-4302-8c6c-d1c5badae55a"><ac:plain-text-body><![CDATA[ | X509Certificate | Zertifikat aus der hochgeladenen Datei | BYTE [] | K | ]]></ac:plain-text-body></ac:structured-macro> |

# Meldungen

|  |  |
| --- | --- |
| **MeldungsNr** | **Meldungstext** |
| DOKU-001 | Die Bereitstellung des Schlüssels ist nicht möglich, da die Dateierweiterung der Zertifikatsdatei nicht erlaubt ist. |
| DOKU-002 | Die Bereitstellung des Schlüssels ist nicht möglich, da das Zertifikat nicht aus der Zertifikatsdatei ermittelt werden kann. |
| DOKU-003 | Die Bereitstellung des Schlüssels ist nicht möglich, da die angegebene Kombination aus Empfänger-, Auftrags- und Schlüsseltyp nicht erlaubt ist. |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Doku-Schlüssel speichern (OPC_ALLG_FKT_011)

# Beschreibung

Das Zertifikat wird je Auftragstyp, Empfängertyp und Schlüsseltyp (diese Kriterien werden in weiterer Folge als Key-Kriterium bezeichnet) in der DB gespeichert.  
Je Key-Kriterium kann mehrmals ein Schlüssel bereitgestellt werden, wobei jedoch zu einem Zeitpunkt immer nur ein Schlüssel für das jeweilige Key-Kriterium gültig ist.  
Anhand dieser Funktion wird das Zertifikat zum angegebenen Key-Kriterium in der Datenbank nach HM1 gespeichert. Wurde für das angegebene Key-Kriterium bereits ein Schlüssel gespeichert, so wird dieser, bevor der neue Schlüssel gespeichert wird, zuvor historisiert.  
Bevor das Zertifikat in der Datenbank zu den angegebenen Key-Kriterien gespeichert wird, werden folgende Prüfungen durchgeführt:

* Die geforderten Sicherheitsrichtlinien des Zertifikats (RSAwithSHA1) müssen erfüllt sein. Im Fehlerfall wird die Fehlermeldung DOKU-004 in die Ausgangsdaten gestellt.

# Vorbedingungen

Das Zertifikat konnte erfolgreich aus der Datei gelesen werden.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Auftragstyp | Auftragstyp | CHAR(10) | M |
| Empfängertyp | Empfängertyp | CHAR(4) | M |
| Schlüsseltyp | Schlüsseltyp | CHAR(3) | M |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="d9b5ef59-bffc-43b7-bd92-2338d201193d"><ac:plain-text-body><![CDATA[ | X509Certificate | Zertifikat aus der hochgeladenen Datei | BYTE [] | K | ]]></ac:plain-text-body></ac:structured-macro> |

# Ausgangsdaten

Keine.

# Meldungen

|  |  |
| --- | --- |
| **MeldungsNr** | **Meldungstext** |
| DOKU-004 | Die Bereitstellung des Schlüssels ist nicht möglich, da das Zertifikat nicht den geforderten Sicherheitsrichtlinien entspricht. |
| BNV-005 | Der Schlüssel wurde erfolgreich zu den gewählten Kriterien (Empfängertyp ‚…', Auftragstyp ‚…', Schlüsseltyp ‚…') bereitgestellt. |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Vertragspartnerverwaltung - Funktionen

Die folgenden Funktionen dienen der Suspendierung eines Vertragspartners.

* + Vertragspartner suchen (OPC\_VPV\_FKT\_001)
  + Vertragspartnerdetails suchen (OPC\_VPV\_FKT\_002)
  + Vertragspartner suspendieren (OPC\_VPV\_FKT\_003)
  + Suspendierung eines Vertragspartners aufheben (OPC\_VPV\_FKT\_004)


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Vertragspartner suchen (OPC_VPV_FKT_001)

# Beschreibung

Diese Funktion sucht Vertragspartner anhand der in Folge angegebenen Suchparameter und zeigt sie in Form einer Vertragspartnerliste an. Jeder einzelne Listeneintrag enthält Attribute laut Ausgangsdaten.

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  | **K** |
| Zuname | Zuname des Vertragspartners. |  | **K** |
| Vorname | Vorname des Vertragspartners. |  | **K** |
| PLZ | Postleitzahl der Zustelladresse der Ordination des Vertragspartners |  | **K** |
| Suspendiert | Suspendierungskennzeichen des Vertragspartners | alle / suspendiert / nicht suspendiert | **M** |

# Ausgangsdaten

| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| --- | --- | --- | --- |
| 0 bis n Wiederholungen |  |  |  |
| Kennung | Userid des Vertragspartners. |  | **M** |
| Zuname | Zuname des Vertragspartners. |  | **M** |
| Vorname | Vorname des Vertragspartners. |  | **K** |
| PLZ | Postleitzahl der Zustelladresse der Ordination des Vertragspartners |  | **M** |
| Ort | Ort der Zustelladresse der Ordination des Vertragspartners |  | **K** |
| BLD | Bundesland der Zustelladresse der Ordination des Vertragspartners |  | **M** |
| VP Nr. | Vertragspartnernummer |  | **M** |
| Suspendiert | Suspendierungskennzeichen des Vertragspartners | J/N (Checkbox – nicht überschreibbar) | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-001 | Vertragspartner anhand der Suchparameter <Suchparameter> nicht gefunden. | Log, Message | Warning |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Vertragspartnerdetails suchen (OPC_VPV_FKT_002)

# Beschreibung

Diese Funktion zeigt sämtliche im Folgenden angeführten Details eines Vertragspartners an:

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** |  | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  |  | **M** |

# Ausgangsdaten

| **Name** | **Beschreibung** |  | **mögliche Werte** |  | **Muss/Kann** |  |
| --- | --- | --- | --- | --- | --- | --- |
| Karteikarte ‚Vertragspartner' |  |  |  |  |  |  |
| Vertragspartner-nummer |  | Vertragspartnernummer |  |  |  | **M** |  |
| LEA Partner-ID |  | LEA Partner-Identifikation |  |  |  | **M** |  |
| ZPV Partner-ID |  | ZPV Partner-Identifikation |  |  |  | **M** |  |
| Zuname |  | Zuname des Vertragspartners. |  |  |  | **M** |  |
| Vorname |  | Vorname des Vertragspartners. |  |  |  | **K** |  |
| Straße |  | Straße und Hausnummer der Zustelladresse der Ordination des Vertragspartners |  |  |  | **M** |  |
| PLZ/Ort |  | Postleitzahl und Ort der Zustelladresse der Ordination des Vertragspartners |  |  |  | **K** |  |
| Bundesland |  | Bundesland der Zustelladresse der Ordination des Vertragspartners |  |  |  | **M** |  |
| Suspendierungs-grund |  | Grund der Suspendierung des Vertragspartners |  |  |  | **M** |  |
| Suspendierungs-datum |  | Datum und Zeitpunkt der Suspendierung des Vertragspartners |  |  |  | **M** |  |
| Bearbeiter |  | Userid des eingeloggten Systembenutzers. |  |  |  | **M** |  |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-002 | Es wurden keine Detailinformationen zum Vertragspartner <VPNR> konnte gefunden | Log | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Vertragspartner suspendieren (OPC_VPV_FKT_003)

# Beschreibung

Diese Funktion suspendiert einen Benutzer vom Typ „Vertragspartner".

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartnernummer |  | M |
| Suspendierungs-grund | Grund der Suspendierung des Vertragspartners |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Status | Kennzeichen ob die Suspendierung des Benutzers erfolgreich war. | OK, NOK | M |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-003 | Der Vertragspartner <VPNR> konnte nicht suspendiert werden | Log | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Suspendierung eines Vertragspartners aufheben (OPC_VPV_FKT_004)

# Beschreibung

Diese Funktion hebt die Suspendierung eines Benutzers vom Typ „Vertragspartner" auf.

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartnernummer |  | M |
| Grund der Aufhebung der Suspendierung | Grund der Aufhebung der Suspendierung des Vertragspartners |  | M |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Status | Kennzeichen ob die Aufhebung der Suspendierung des Benutzers erfolgreich war. | OK, NOK | M |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-004 | Die Suspendierung des Vertragspartners <VPNR> konnte nicht aufgehoben werden | Log | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikat bereitstellen (OPC_VPV_FKT_006)

# Vorbedingungen

Eine Datei mit der Dateierweiterung ‚CER' oder ‚PEM' wurde am definierten Ablageort für die Bereitstellung eines SW-Zertifikats abgelegt. Das Zertifikat wurde noch nicht für einen Vertragspartner bereitgestellt.   
Die Vertragspartnernummern (VPNR) wurden durch den HVB-Import übernommen.  
Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Beschreibung

Anhand dieser Funktion wird ein SW-Zertifikat aus einer Datei extrahiert und für das System zur weiteren Verarbeitung bereitgestellt.  
Zu einer VPNR können mehrere SW-Zertifikate bereitgestellt werden. Ein SW-Zertifikat selbst kann jedoch nur einmalig für eine VPNR bereitgestellt werden. Über die Beziehungsentität „HVB\_Beziehung" ist allerdings auch die Zuordnung von SW-Zertifikaten für ganze Vertragspartnergruppen möglich (z.B. mehrere Vertragspartner stehen in Beziehung zu einem übergeordneten Vertragspartner, der das SW-Zertifikat bereitstellt).  
Die eingegebene VPNR darf nur aus numerischen Zeichen bestehen. Bei Eingabe von ungültigen Zeichen wird die allgemein gültige Fehlermeldung ‚Der Wert ‚<Eingabe>' für das Feld ‚<VPNR>' ist nicht korrekt.' Ausgegeben.  
Bei der Datei mit dem bereitzustellenden SW-Zertifikats muss es sich um eine CER- oder PEM- Datei handeln. Andernfalls wird die Fehlermeldung ‚BNV-010' ausgegeben.  
Serverseitig wird das SW-Zertifikat aus der hochgeladene Datei als ‚X509Certificate' gelesen. Kann der Dateninhalt des SW-Zertifikats nicht gelesen werden, so wird die Fehlermeldung ‚BNV-011' ausgegeben.  
Nachdem das SW-Zertifikat erfolgreich gelesen wurde, werden die folgenden Prüfungen durchgeführt:

* Die angegebene Vertragspartnernummer muss existieren. Im Fehlerfall wird die Fehlermeldung ‚BNV-012' ausgegeben.
* Das SW-Zertifikat darf in der Entität ‚SW-Zertifikat' noch nicht bereitgestellt worden sein. Die Prüfung erfolgt anhand der Seriennummer und dem Aussteller des SW-Zertifikats. Im Fehlerfall wird die Fehlermeldung ‚BNV-013' ausgegeben.
* Die geforderten Sicherheitsrichtlinien eines SW-Zertifikats müssen erfüllt sein. Diese sind:
  + Priorität- oder RSA-Schlüssel, andernfalls Fehlermeldung BNV-022
  + Schlüssellänge in Bit entsprechend der Systemparameter *SWZ\_Import\_Keylength\_Min/Max* (Gruppe *Schnittstellenparameter*), andernfalls Fehlermeldung BNV-022
  + Zertifikatssignatur nicht unter Verwendung eines gesperrten Algorithmus, andernfalls Fehlermeldung BNV-021. Hierzu wird folgendes Stringformat gegen den im Systemparameter *SWZ\_Import\_Algorithm\_Blacklist* (Gruppe *Schnittstellenparameter*) hinterlegten regulären Ausdruck geprüft: *<Digest>/<Verschlüsselung>,<Schlüsseltyp>-<Schlüssellänge> (z.B. SHA/RSA,RSA-1024)*

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Datei | Datei mit dem bereitzustellenden SW-Zertifikat |  | M |
| VPNR | Vertragspartnernummer zu der das SW-Zertifikat bereitgestellt werden soll |  | M |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Entität ‚SW-Zertifikat' | Neues SW-Zertifikat zur angegebenen Vertragspartnernummer |  | M |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-010 | Die Bereitstellung des SW-Zertifikats aus der Datei ‚<Dateiname>' ist nicht möglich, da die Dateierweiterung der Datei nicht erlaubt ist. | Message | Error |
| BNV-011 | Die Bereitstellung des SW-Zertifikats aus der Datei ‚<Dateiname>' ist nicht möglich, da das SW-Zertifikat nicht aus der Datei ermittelt werden kann. | Message | Error |
| BNV-012 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' konnte nicht bereitgestellt werden, da die Vertragspartnernummer ‚<VPNR>' nicht existiert. | Message | Error |
| BNV-013 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wurde bereits bereitgestellt. | Message | Error |
| BNV-022 | Die Bereitstellung des angegebenen SW-Zertifikats ist nicht möglich, da das SW-Zertifikat nicht den geforderten SicherheitsrichtliniePrioritätDSA oder RSA mit Schlüssellänge <min. Schlüssellänge> - <max. Schlüssellänge>) entspricht. | Message | Error |
| BNV-021 | Die Bereitstellung des angegebenen SW-Zertifikats ist nicht möglich, da der verwendete Signaturalgorithmus <Algorithmus> nicht den geforderten Sicherheitsrichtlinien entspricht. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikat suchen (OPC_VPV_FKT_007)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion ermittelt die SW-Zertifikate zur eingegebenen Vertragspartnernummer (VPNR). Ausgehend von der eingegebenen VPNR wird zunächst der Vertragspartner ermittelt. Ausgehend vom Vertragspartner werden über die Entität HVB\_BEZIEHUNG alle SW-Zertifikate ermittelt, in der der jeweilige Vertragspartner in einer „Parent-Beziehung" mit einem anderen Vertragspartner steht. Gibt es bei einem Vertragspartner keine „Parent-Beziehung" mehr so ist das Ende der SW-Zertifikats-Hierarchie erreicht.   
Die gefundenen Treffer werden in der VPNR- Hierarchie sortiert ausgegeben (z.B.: SW-Zertifikat VPNR, SW-Zertifikat Parent VPNR, SW-Zertifikat Parent VPNR der Parent VPNR usw.). Sollten zu einer VPNR mehrere SW-Zertifikate vorhanden sein, so werden diese innerhalb der jeweiligen VPNR nicht speziell sortiert.  
Es werden alle SW-Zertifikate unabhängig des Lösch-Kennzeichens ermittelt. Ein als ‚gelöscht' markiertes SW-Zertifikat wurde gesperrt und kann wieder entsperrt werden. Ein aktives SW-Zertifikat (Lösch-Kennzeichen nicht gesetzt) kann gesperrt werden.  
Können keine SW-Zertifikate zur angegebenen Vertragspartnernummer ermittelt werden, so wird die Fehlermeldung ‚BNV-015' ausgegeben.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| VPNR | Vertragspartnernummer |  | M |

# Ausgangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| 1 bis n Wiederholungen |  |  |  |
| VPNR | Vertragspartnernummer |  | M |  |
| Zuname | Zuname des Vertragspartners |  | M |  |
| SW-ZertifikatID | Primary-Key der Entität 'SW-Zertifikat' |  | M |  |
| Seriennummer | Seriennummer des Zertifikats |  | M |  |
| Seriennummer-hex | Hexadezimale Form der Seriennummer |  | M |  |
| Aktiv-KZ | Kennzeichen ob SW-Zertifikat gesperrt wurde | 0 = nicht gesperrt1 = gesperrt | M |  |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-015 | Anhand der Suchparameter ‚Vertragspartnernummer:<VPNR>' wurden keine SW-Zertifikate gefunden. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikat Details suchen (OPC_VPV_FKT_008)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion ermittelt die Details des gewählten SW-Zertifikats anhand der ID des gewählten SW-Zertifikats aus der Trefferliste (siehe Funktion „SW-Zertifikat suchen (OPC\_VPV\_FKT\_007)").  
Anhand des Aktiv- Kennzeichens wird entweder das Sperren (SW-Zertifikat ist aktiv) oder Entsperren (SW-Zertifikat gesperrt) des SW-Zertifikats zugelassen.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| SW-ZertifikatID | Primary-Key der Entität 'SW-Zertifikat' |  | M |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| VPNR | Vertragspartnernummer |  | M |
| Zuname | Zuname des Vertragspartner |  | M |
| Seriennummer | Seriennummer |  | M |
| Seriennummer-hex | Hexadezimale Form der Seriennummer |  | M |
| Aussteller | Distinguished Name des Aussteller |  | M |
| SHA1-Fingerprint | SHA1-Fingerprint |  | M |
| SHA256-Fingerprint | SHA256-Fingerprint |  | M |
| Gültigkeitsbeginn | Datum ab dem das SW-Zertifikat gültig ist |  | M |
| Gültigkeitsende | Datum bis zu dem das SW-Zertifikat gültig ist |  | M |
| Aktiv-KZ | Kennzeichen ob SW-Zertifikat gesperrt wurde | 0 = nicht gesperrt1 = gesperrt | M |
| Algorithmus | Signaturalgorithmus, Schlüsselalgorithmus und Schlüssellänge |  | M |
| DN | Distinguished Name des Antragstellers |  | M |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikat sperren _ entsperren (OPC_VPV_FKT_009)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion sperrt bzw. entsperrt das gewählte SW-Zertifikat. Vor dem Sperren bzw. Entsperren wird eine Bestätigungsmeldung ausgegeben.  
Beim Sperren des SW-Zertifikats wird das gewählte SW-Zertifikat nach HM1 gelöscht. Beim Entsperren eines SW-Zertifikats wird das Lösch-Kennzeichen wieder aufgehoben. In beiden Fällen wird der ausführende Benutzer und das Änderungsdatum in der Entität ‚SW-Zertifikat' gemäß HM1 protokolliert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| SW-ZertifikatID | Primary-Key der Entität 'SW-Zertifikat' |  | M |
| KZ | Kennzeichen zum Sperren bzw. Entsperren des SW-Zertifikats | 0 = entsperren1 = sperren | M |

# Ausgangsdaten

Keine

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-016 | Wollen Sie das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wirklich sperren? | Message | Warning |
| BNV-017 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wurde erfolgreich gesperrt. | Log, Message | Info   Info |
| BNV-018 | Wollen Sie das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wirklich entsperren? | Message | Warning |
| BNV-019 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wurde erfolgreich entsperrt. | Log, Message | InfoInfo |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Rollen ermitteln (OPC_VPV_FKT_010)

# Beschreibung

Die Funktion ermittelt die in eCS verfügbaren Rollen, bei denen die Zuordnung von Vertragspartnern über den OPC zulässig ist.  
Dies sind alle Einträge der Entität „Rolle" für die das Kennzeichen „Zuordnung editierbar" gesetzt ist.

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

Keine

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Rollen (0-n), zu denen über den OPC Vertragspartner zugeordnet werden können. |  |  |  |  |
| Rollen ID | Künstlicher Schlüssel der Rolle |  | **M** |
| Bezeichnung | Bezeichnung der Rolle | z.B. BKF-AssessmentVDAS-Stichtagabfrage | **M** |
| Beschreibung | Beschreibung der Rolle (Feld Beschreibung\_Vergabe\_durch) |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Ausprägungscodes ermitteln (OPC_VPV_FKT_012)

# Beschreibung

Die Funktion ermittelt die in ecS verfügbaren Ausprägungen.

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

Keine

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Ausprägungen (1-n) |  |  |  |  |
| Ausprägungs Code | Code der Ausprägung |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Komponenten ermitteln (OPC_VPV_FKT_013)

# Beschreibung

Die Funktion ermittelt die in eCS verfügbaren Komponenten, bei denen eine Zuordnung über den OPC zulässig ist.  
Dies sind alle gültigen Einträge der Entität „Komponente".

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

Keine

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Komponenten (0-n) |  |  |  |  |
| KomponentenCode | Code der Komponete |  | **M** |
| Beschreibung | Beschreibung der Komponente |  | **M** |
| SperreFlag | True, falls die Zuordnung als Sperre möglich ist. |  | **M** |
| PilotFlag | True, falls die Zuordnung als Pilot möglich ist |  | **M** |
| Pilot ab | Zeitpunkt ab dem Pilotbetrieb gestartet werden soll. |  | **K** |
| Vollbetrieb ab | Zeitpunkt ab dem Vollbetrieb gestartet werden soll. |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Komponente ändern (OPC_VPV_FKT_014)

# Beschreibung

Die Funktion ändert die Datumswerte für eine Komponente in der Entität „Komponente".

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| KomponentenCode | Code der Komponente |  | **M** |
| Voll Ab | Datum, ab wann die Komponente in den Vollbetrieb startet. | Datum | **K** |
| Pilot Ab | Datum, ab wann die Komponente in den Pilotbetrieb startet. | Datum | **K** |

# Ausgangsdaten

Keine

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Ordinationen suchen (OPC_VPV_FKT_015)

# Beschreibung

Diese Funktion sucht aktive Ordinationen anhand der in Folge angegebenen Suchparameter und retourniert sie in Form einer Ordinationsliste.
  
Falls angegeben wird die Zuordnung der Rolle anhand der Entität „Systembenutzer Rolle", die Zuordnung der Sperre anhand der Entität „Systembenutzer Komponente" mit gesetztem Flag „Sperre" und die Zuordnung des Piloten ebenfalls anhand der Entität „Systembenutzer Komponente" mit gesetztem Flag „Pilot" geprüft.

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  | **K** |
| Ordinationsnummer | Nummer der Ordination. |  | **K** |
| Ausprägung | Ausprägung der Ordination. |  | **K** |
| Rolle | Zugeordnete Rolle. |  | **K** |
| Sperre | Zugeordnete Sperre. |  | **K** |
| Pilot | Zugeordneter Pilot. |  | **K** |

# Ausgangsdaten

| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| --- | --- | --- | --- | --- |
| 0 bis n Wiederholungen |  |  |  |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  | **M** |  |
| Ordinationsnummer | Nummer der Ordination. |  | **M** |  |
| Ausprägungscode | Ausprägung der Ordination. |  | **M** |  |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |  |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |  |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-023 | Ordination anhand der Suchparameter <Suchparameter> nicht gefunden. | Message | Warning |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Ordinationen aus Datei lesen (OPC_VPV_FKT_016)

# Beschreibung

Diese Funktion prüft die in einer Datei angegebenen Ordinationen und retourniert diese als Liste.  
Die Ordinationen werden im folgendenen Format in der angegebenen Datei erwartet:

* Pro Zeile eine Ordination
* Vertragspartnernummer, Ordinationsnummer und Ausprägungscode durch Strichpunkt getrennt. Es müssen alle 3 Parameter angegeben sein

Falls die Datei nicht gelesen werden kann, wird die Funktion mit der Meldung BNV-024 abgebrochen.  
Falls die Datei nicht dem vorgesehenen Format entspricht, wird die Funktion mit der Meldung BNV-025 abgebrochen.  
Für jeden einzelnen Eintrag wird geprüft, ob dafür eine aktive Ordination existiert. Ist dies nicht der Fall wird die Funktion mit der Meldung BNV-026 abgebrochen.  
*Hinweis: Die Funktion wird beim ersten Fehler abgebrochen, d.h. es werden keine weiteren Zeilen geprüft.*

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Datei | Name der Datei |  | **M** |

# Ausgangsdaten

| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| --- | --- | --- | --- | --- |
| 0 bis n Wiederholungen |  |  |  |
| Vertragspartnernummer | Eindeutige Identifikationsnummer des Vertragspartners. |  | **M** |  |
| Ordinationsnummer | Nummer der Ordination. |  | **M** |  |
| Ausprägung | Ausprägung der Ordination. |  | **M** |  |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |  |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |  |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-024 | Fehler beim Lesen der Datei <Datei>. | MessageLog | Error |
| BNV-025 | Die <Zeilennummer>. Zeile der Datei <Datei> entspricht nicht dem vorgesehenen Format. | MessageLog | Error |
| BNV-026 | Die in der <Zeilennummer>. Zeile der Datei <Datei> eingetragene Ordination (<Vertragspartnernummer>, <Ordinationsnummer>, <Ausprägungscode>) ist nicht gültig. | MessageLog | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Zugeordnete Rollen ermitteln (OPC_VPV_FKT_017)

# Beschreibung

Die Funktion ermittelt die der angegebenen Ordination zugeordneten Rollen anhand der Einträge in der Entität „Systembenutzer Rolle".  
Es werden nur Einträge der Entität „Rolle", für die das Kennzeichen „Zuordnung editierbar" gesetzt ist, berücksichtigt.

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Rollen (0-n), die der Ordination zugeordnet sind |  |  |  |  |
| Rollen ID | Künstlicher Schlüssel der Rolle |  | **M** |
| Bezeichnung | Bezeichnung der Rolle | z.B. BKF-AssessmentVDAS-Stichtagabfrage | **M** |
| Beschreibung | Beschreibung der Rolle |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Zugeordnete Sperren ermitteln (OPC_VPV_FKT_018)

# Beschreibung

Die Funktion ermittelt die der angegebenen Ordination zugeordneten Sperren anhand der Einträge in der Entität „Systembenutzer Komponente" mit gesetztem Flag „Sperre möglich".

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Komponenten (0-n), die der Ordination als Sperre zugeordnet sind. |  |  |  |  |
| KomponentenCode | Code der Komponente |  | **M** |
| Beschreibung | Beschreibung der Komponente |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Zugeordnete Piloten ermitteln (OPC_VPV_FKT_019)

# Beschreibung

Die Funktion ermittelt die der angegebenen Ordination zugeordneten Piloten anhand der Einträge in der Entität „Systembenutzer Komponente" mit gesetztem Flag „Pilot möglich".

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der Komponenten (0-n), die der Ordination als Pilot zugeordnet sind. |  |  |  |  |
| KomponentenCode | Code der Komponente |  | **M** |
| Beschreibung | Beschreibung der Komponente |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Zugeordnete Verträge ermitteln (OPC_VPV_FKT_020)

# Beschreibung

Die Funktion ermittelt die der angegebenen Ordination zugeordneten gültigen Verträge anhand der Einträge in der Entität „VP Vertrag".

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste der gültigen Verträge (0-n), die der Ordination zugeordnet sind. |  |  |  |  |
| Vertragstypcode | Code des Vertragstyps |  | **M** |
| Fachgebietscode | Code des Fachgebiets |  | **M** |
| SVT\_L | Leistungszuständiger SV-Träger |  | **M** |
| SVT\_V | Verrechnungsszuständiger SV-Träger |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Rollen zuordnen (OPC_VPV_FKT_021)

# Beschreibung

Die Funktion ordnet der angegebenen Ordination die angegebenen Rollen durch Einträge in der Entität „Systembenutzer Rolle" zu. Existierende Zuordnungen, die nicht in der Liste enthalten sind, werden historisiert.

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |
| ListeRollen | Liste von 0-n RollenIds |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Sperren zuordnen (OPC_VPV_FKT_022)

# Beschreibung

Die Funktion ordnet der angegebenen Ordination die angegebenen Sperren durch Einträge in der Entität „Systembenutzer Komponente" mit Flag „Sperre" zu. Existierende Zuordnungen, die nicht in der Liste enthalten sind, werden historisiert.

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |
| ListeKomponenten | Liste von 0-n KomponentenCodes |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Piloten zuordnen (OPC_VPV_FKT_023)

# Beschreibung

Die Funktion ordnet der angegebenen Ordination die angegebenen Piloten durch Einträge in der Entität „Systembenutzer Komponente" mit Flag „Pilot" zu. Existierende Zuordnungen, die nicht in der Liste enthalten sind, werden historisiert.

# Vorbedingungen

Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ordinationsid | ID der Ordination (PK der Entität Ordination) |  | **M** |
| Ausprägungsid | ID der Ausprägung(PK der Entität Ausprägung) |  | **M** |
| ListeKomponenten | Liste von 0-n KomponentenCodes |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### SW-Zertifikat importieren (OPC_VPV_FKT_024)

# Vorbedingungen

Das SW-Zertifikat wurde mittels „SW-Zertifikat bereitstellen (OPC\_VPV\_FKT\_006)" eingelesen, und ist noch nicht in der Datenbank vorhanden.  
Die Vertragspartnernummern (VPNR) wurden durch den HVB-Import übernommen.  
Der Systembenutzer ist zur Durchführung des Ablaufes und im Speziellen zum Aufruf dieser Funktion autorisiert.

# Beschreibung

Anhand dieser Funktion wird ein SW-Zertifikat zu der angegebenen Vertragspartnernummer (VPNR) in der Entität ‚SW-Zertifikat' nach HM1 gespeichert.  
Zu einer VPNR können mehrere SW-Zertifikate bereitgestellt werden. Ein SW-Zertifikat selbst kann jedoch nur einmalig für eine VPNR bereitgestellt werden. Über die Beziehungsentität „HVB\_Beziehung" ist allerdings auch die Zuordnung von SW-Zertifikaten für ganze Vertragspartnergruppen möglich (z.B. mehrere Vertragspartner stehen in Beziehung zu einem übergeordneten Vertragspartner, der das SW-Zertifikat bereitstellt).  
In der DB wird als Fingerprint der SHA1-Wert gespeichert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| SwZertifikat | SW-Zertifikat |  | M |
| VPNR | Vertragspartnernummer zu der das SW-Zertifikat bereitgestellt werden soll |  | M |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Entität ‚SW-Zertifikat' | Neues SW-Zertifikat zur angegebenen Vertragspartnernummer |  | M |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| BNV-014 | Das SW-Zertifikat mit der Seriennummer ‚<Seriennummer>' (HEX:<Seriennummer,hexadezimal>) und dem Aussteller ‚<Aussteller>' wurde erfolgreich für den Vertragspartner <VPNR>' bereitgestellt. | Log, Message | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Client-Verwaltung - Funktionen

Hinweis: Jene Funktion der Clientverwaltung die neben dem OPC auch von anderen Batches und Services genutzt werden sind haben als Prefix CV\_BAS\_FKT\_. Und sind bei den CLV Basisfunktionen beschrieben.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Detailinformation – GINO ermitteln (OPC_CLV_FKT_026)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion ermittelt die Details eines GINO aus der Entität „Gino", die Mac-Adresse wird aus der Entität „Gino Produktionsdaten" ermittelt.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gino-Seriennummer | Suchkriterium für Seriennummer des Ginos |  | **M** |
| Gina | Entität |  | **M** |
| Gina Produktionsdaten | Entität |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gino-Stammdaten | Objekt |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Report „Prozentuelle Auswertung der am System angemeldeten Vertragspartner“ erstellen (OPC_CLV_FKT_056)

# Vorbedingungen

Der Instant-Report „Rollout – Prozentuelle Auswertung der am System angemeldeten Vertragspartner" wurde im Report-Framework eingebunden.

# Beschreibung

In dieser Funktion wird der Report „Rollout – Prozentuelle Auswertung der am System angemeldeten Vertragspartner" erstellt. Dabei werden die Vertragspartner, die sich jemals am System angemeldet haben, ermittelt.  
Die Auswertung wird, falls als Filterkriterium ein Postleitzahlenbereich angegeben wurde, entsprechend gefiltert. Dabei werden die Vertragspartner, auch wenn diese mehreren Ordinationen zugeordnet sind (Vertragspartner hat mehrere Ordinationen im angegebenen Filterbereich) nur einmal angegeben. Wird kein Postleitzahlenbereich angegeben, so wird eine Gesamtauswertung aller Vertragspartner (gültige Vertragspartnernummer) durchgeführt, die im aktuellen Datenbestand zumindest eine gültige Ordination haben.  
Bei diesem Report werden immer alle zum Zeitpunkt der Erstellung dieses Reports aktuellen Vertragspartner bewertet.  
Die Vertragspartner werden aus der Entität „Vertragspartner" ermittelt. Über die Entität „Ordination" werden jedoch nur jene Vertragspartner ausgewertet, die einer Ordination im angegebenen Postleitzahlenbereich zugeordnet sind.  
Die sich tatsächlich angemeldeten Vertragspartner werden anhand der protokollierten o-card ID ermittelt. Über die dazu protokollierte Ordinations-ID kann der jeweilige Vertragspartner dem Filterbereich (Postleitzahl) zugeordnet werden. Auch hier wird der Vertragspartner nur einmal bewertet unabhängig in welchen Ordinationen die Anmeldung erfolgte.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Filterkriterium | Postleitzahlenbereich. Wurde kein Filterkriterium angegeben, so wird eine Auswertung aller Vertragspartner durchgeführt |  | **K** |
| Vertragspartner | Entität |  | **M** |
| Ordination | Entität |  | **M** |
| VP Dialog | Entität |  | **M** |
| o-card | Entität |  | **M** |
| Vertragspartnernummer | Entität |  | **M** |

# Ausgangsdaten

| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| --- | --- | --- | --- |
| Reporterstellung | Zeitstempel der Erstellung dieses Reports |  | **M** |
| Filterkriterium | Postleitzahlenbereich. Wurde kein Filterkriterium angegeben, so ist eine Auswertung aller Vertragspartner gefordert |  | **M** |
| Wirksystem | Kennzeichen, ob es sich um das Wirk- (Dispositiv-DB) oder VPSWH (Vertragspartnerhersteller) DB Schema handelt | True (Wirksystem), False (VPSWH) | **M** |
| Anzahl der Vertragspartner insgesamt | Anzahl der Vertragspartner zum jeweiligen Filterkriterium |  | **M** |
| Anzahl der angemeldeten Vertragspartner | Anzahl der Vertragspartner zum jeweiligen Filterkriterium, die sich am jeweiligen System (Wirk- bzw. VPSWH) angemeldet haben |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Report „Details der am System angemeldeten Vertragspartner“ erstellen (OPC_CLV_FKT_057)

# Vorbedingungen

Der Instant-Report „Rollout – Details der am System angemeldeten Vertragspartner" wurde im Report-Framework eingebunden.

# Beschreibung

In dieser Funktion wird der Report „Rollout – Details der am System angemeldeten Vertragspartner" erstellt. Dabei werden die Vertragspartner, die sich jemals bereits am System angemeldet oder nicht angemeldet haben, ermittelt.  
Je Vertragspartner werden folgende Informationen ausgegeben:

* + Vertragspartnernummer
  + Status der Anmeldung

Bei diesem Report werden immer alle zum Zeitpunkt der Erstellung dieses Reports aktuellen Vertragspartner bewertet.  
Zur Ermittlung der Vertragspartner, die sich noch nicht am System gemeldet haben, werden die Vertragspartner zum angegebenen Filterkriterium (Postleitzahl) ermittelt. Dabei werden die Vertragspartner, auch wenn diese mehreren Ordinationen zugeordnet sind (Vertragspartner hat mehrere Ordinationen im angegebenen Filterbereich) nur einmal bewertet.  
Wird kein Postleitzahlenbereich angegeben, so werden alle Vertragspartner berücksichtigt.  
Die sich tatsächlich angemeldeten Vertragspartner werden anhand der protokollierten o-card ID ermittelt. Über die dazu protokollierte Ordinations-ID kann der jeweilige Vertragspartner dem Filterbereich (Postleitzahl) zugeordnet werden. Auch hier wird der Vertragspartner nur einmal im angegebenen Filterbereich bewertet, unabhängig in welchen Ordinationen die Anmeldung erfolgte.  
Zu den Vertragspartnern, die sich bereits angemeldet haben, wird die Vertragspartnernummer und zusätzlich das Kennzeichen „Vertragspartner angemeldet"  für die durchgeführte Anmeldung ausgegeben. Hat sich ein Vertragspartner noch nicht angemeldet, so wird das Kennzeichen „Vertragspartner nicht angemeldet"  ausgegeben.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Filterkriterium | Postleitzahlenbereich. Wurde kein Filterkriterium angegeben, so wird eine Auswertung aller Vertragspartner durchgeführt |  | **K** |
| Vertragspartner | Entität |  | **M** |
| Ordination | Entität |  | **M** |
| VP Dialog | Entität |  | **M** |
| o-card | Entität |  | **M** |
| Vertragspartnernummer | Entität |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Reporterstellung | Zeitstempel der Erstellung dieses Reports |  | **M** |
| Filterkriterium | Postleitzahlenbereich. Wurde kein Filterkriterium angegeben, so wird eine Auswertung aller Vertragspartner durchgeführt |  | **M** |
| VPNR | Liste mit Vertragspartnernummern |  | **M** |
| Status Anmeldung | Status der Anmeldung, ob sich der Vertragspartner bereits angemeldet hat oder nicht | True (Vertragspartner bereits angemeldet)   False (Vertragspartner noch nicht angemeldet) | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Client-Stammdaten suchen (OPC_CLV_FKT_109)

# Vorbedingungen

Keine.

# Beschreibung

Die Funktion sucht in der Entität Stammdaten, Anschluss und Gino nach Client-Stammdaten die den Eingangsdaten entsprechen.  
Je nach Angabe der Suchebene werden entweder nur die VP-Stammdaten, die VP-Stammdaten und Anschlüsse, die VP-Stammdaten, Anschlüsse und Ginas oder oder die VP-Stammdaten, Anschlüsse und Ginos ermittelt und retourniert.  
Wird ein für die VP-Stammdaten relevantes Eingangsdatum übergeben so wird zuerst in den VP-Stammdaten gesucht.   
Falls auch die Anschlüsse ermittelt werden sollen, werden die ermittelten IDs der VP-Stammdaten zu den für die Entität Anschluss relevanten Eingangsdaten hinzugefügt. Mit diesen Daten werden dann die relevanten Datensätze in der Entität Anschluss gesucht.  
Falls auch die Ginos ermittelt werden sollen, werden die ermittelte IDs der Anschlüsse zu den für die Entität Gino relevanten Eingangsdaten hinzugefügt. Mit diesen Daten werden dann die relevanten Datensätze in der Entität gesucht.   
Für das Stats-Logging wird die Dauer des Funktionsaufrufs mit den nachfolgenden Parametern geloggt

* Komponente: Client-Verwaltung – Client-Stammdaten
* Operation: Suchen

# Eingangsdaten

| **Suchkriterium** | **Anmerkung** |
| --- | --- |
| Suchebene | „Stammdaten", „Anschluss", „GINO" |
| Stammdaten-relevante Felder |  |
| Zuordnungstyp | „alle", „Exot", „Standard" |
| Vertragspartnernummer |  |
| Ordinationsnummer |  |
| Zuname | Wildcardsuche möglich |
| Vorname | Wildcardsuche möglich |
| Anschlussart | "alle", "GIN", "GIN\_CONNECT", "leer" |
| Anschluss-relevante Felder |  |
| Stammdaten-ID | Nur anstatt stammdatenrelevanten Feldern möglich |
| Client-IP | Wildcardsuche möglich |
| Subnetz | Wildcardsuche möglich |
| Router-IP | Wildcardsuche möglich |
| GINO-relevante Felder |  |
| GINO-Seriennummer | Wildcardsuche möglich |
| GINO-Firmware | Mehrfachsuche mittels Liste |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Client-Stammdaten | Listen von Stammdaten, Anschlüsse, Ginos oder Ginas, gegebenenfalls erweitert um Gerät Info (HW-Typ, SW-Distribution)   Sortierung ist abhängig von der Art der zurückgegebenen Objekte:   * Stammdaten: Vertragspartnernummer, Ordinationsnummer * Anschluss: Client-IP * GINO: Seriennummer |  | **K** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-015 | Zum angegebenen Filter konnten keine Einträge ermittelt werden. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Übersiedlung stornieren (OPC_CLV_FKT_117)

# Vorbedingungen

In den Stammdaten ist ein Vorgänger eingetragen und das Kennzeichen KZ\_UEBERNOMMEN ist nicht gesetzt.

# Beschreibung

Mit dieser Funktion wird eine gekennzeichnete Übersiedlung rückgängig gemacht. Damit kann abweichend vom Standardverhalten für die übersiedelte Ordination ein neuer Anschluss zugeordnet werden.  
Der Vorgänger wird ausgetragen und die Daten werden gespeichert. In den VP-Stammdaten des Vorgängers wird der Nachfolger ausgetragen.   
Das Flag, das Exportdatum und die Aktion für den Providerexport- und den Callcenterexport werden gelöscht.  
Für den Vorgänger wird für den Provider- und Callcenterexport das Exportdatum gelöscht.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gina-Stammdaten | Objekt |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Stammdaten | Entität |  | **M** |
| Provider Export | Entität |  | **K** |
| Callcenter Export | Entität |  | **K** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-1005 | Die Übersiedlung der Ordination (VPNR: <VPNR>, Ordinationsnummer: <ORDNR>) wurde erfolgreich storniert. | Message | Info |
| GUI-014 | Wollen Sie die Übersiedlung für die Ordination (VPNR: <VPNR>, Ordinationsnummer: <ORDNR>) wirklich stornieren? | Message | Warning |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Übernahme stornieren (OPC_CLV_FKT_118)

# Vorbedingungen

Das Kennzeichen KZ\_UEBERGEBEN ist gesetzt.

# Beschreibung

Mit dieser Funktion wird eine übernommener Stammdatensatz als nicht übernommen gekennzeichnet.

Der Nachfolger wird ausgetragen und das Kennzeichen für KZ\_UEBERGEBEN wird zurückgesetzt.

Beim Nachfolger wird das Kennzeichen für KZ\_UEBERNOMMEN zurückgesetzt.

Hinweis: das bedeutet nicht, dass der Zustand vor der Übernahme wiederhergestellt ist. Die übergebenen Anschlüsse müssen durch eine Übernahme „zurückgeholt" werden, Falls man danach diese Übernahme auch storniert, ist der ursprüngliche Zustand wiederhergestellt.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Stammdaten | Objekt |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Stammdaten | Entität |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-1006 | Die Übernahme der Ordination (VPNR: <VPNR>, Ordinationsnummer: <ORDNR>) wurde erfolgreich storniert. | Message | Info |
| GUI-024 | Wollen Sie die Übernahme für die Ordination (VPNR: <VPNR>, Ordinationsnummer: <ORDNR>) wirklich stornieren? | Message | Warning |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Bereich für IP-Adresse ermitteln (OPC_CLV_FKT_124)

# Vorbedingungen

Keine

# Beschreibung

Der Bereich für die IP Adresse wird ermittelt. Nur für das GIN.Netz möglich  
Anhand der übergebenen Daten wird die „IP Range" aufgrund der Einträge in der Entität „IP Ranges" ermittelt.Wurde kein Bundesländercode übergeben, werden nur jene Einträge in der Entität „IP Ranges" berücksichtigt, bei denen ebenfalls kein Bundesländercode gesetzt ist.  
Kann kein Ergebnis ermittelt werden, wird die Meldung CLV-1080 ausgegeben.  
Ansonsten wird die „IP Range" zurückgegeben und die Funktion beendet.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Ausprägungscode | Ausprägungscode der Gina |  | **M** |
| Bundesländercode | Bundesländercode |  | **K** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Bereichs-Id | Ermittelter Bereich. |  | **K** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-1080 | Für die Ausprägung <Ausprägung> und den Bundesländercode <Bundesländercode> konnte keine IP-Range ermittelt werden. | Log   Message | Warning   Error |

Die Meldungen werden nur dann in die Eventlog-Datei geschrieben, falls die Funktion von einem Batch aufgerufen wird.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### IP-Adresse aus Bereich ermitteln (OPC_CLV_FKT_125)

# Vorbedingungen

Keine

# Beschreibung

Für den übergebenen Bereich wird in der Entität „IP Frei" eine als frei markierte IP Adresse gewählt.   
Zuerst werden IP-Adressen gesucht die noch nie verwendet wurden, danach jene die am längsten frei sind.  
Falls keine freie IP-Adresse existiert, werden auch die gesperrten Adressen berücksichtigt, jene die am Längsten gesperrt sind zuerst.  
Gibst es mehrere gleichwertige Einträge wird die numerisch kleinste IP-Adresse gewählt.   
Anmerkung: Die gewählte IP Adresse wird nicht als „belegt" markiert. Dies erfolgt erst beim Speichern der Ginadaten.  
Wird keine IP-Adresse gefunden wird die Meldung CLV-1090 ausgegeben.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Bereichs-Id | Zuvor ermittelter Bereich. |  | **M** |
| Ausprägungscode | Nur für Meldung relevant. |  | **M** |
| Bundesländercode | Nur für Meldung relevant. |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| IP-Adresse |  |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-1090 | Für die Ausprägung <Ausprägung> und den Bundesländercode <Bundesländercode> konnte keine IP-Adresse ermittelt werden. | MessageLog | Error |

Die Meldungen werden nur dann in die Eventlog-Datei geschrieben, falls die Funktion von einem Batch aufgerufen wird.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Gruppenliste ermitteln (OPC_CLV_FKT_140)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion ermittelt für in den Eingangdaten übergebenen GINO-Gruppentyp die aktuellen Gruppen aus der Entität „Gino-Gruppe" eingeschränkt auf die gespeicherten Gültigkeitsdaten.   
Für jeden ermittelten Eintrag in „Gino-Gruppen" werden die entsprechenden Daten in den Ausgangsdaten zurückgeliefert, weiters wird zu jedem Eintrag die Anzahl der aktuellen, referenzierenden Einträge in der Entität „GINO-Gruppen-Mitglieder" ermittelt, und im Parameter „Anzahl Gruppenmitglieder" der Paramtergruppe „GINO-Gruppe" zurückgeliefert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gruppen-Typ | Typ der Gruppe, siehe „GINO-Gruppentyp". |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Gruppe"   Gruppen-ID | Siehe „GINO-Gruppe".   Die Liste kann auch leer sein. |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Gruppe Konfiguration ermitteln (OPC_CLV_FKT_141)

# Vorbedingungen

Keine

# Beschreibung

Anhand der übergebenen GINO-Gruppen-ID werden in der Enitiät „GINO-Konfiguration-Zuordnung" die auf den Eintrag in „GINO-Gruppe" referenzierenden Einträge ermittelt und in den Ausgangsdaten zurückgeliefert.
  
Wird in den Eingangsdaten keine GINO-Gruppen-ID gesetzt, so werden in der Entität „GINO-Konfiguration" alle aktuellen Einträge ermittelt und in den Ausgangsdaten zurückgeliefert, dies sind also alle Parameter die dem System bekannt sind.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gruppen-ID | ID der Gruppe | numerisch | **K** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Konfiguration-Parameter" | Liste von „GINO-Konfiguration-Parameter", kann auch leer sein. |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-006 | Bitte wählen Sie einen Eintrag aus der Liste aus. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Gruppe speichern (OPC_CLV_FKT_142)

# Vorbedingungen

Keine

# Beschreibung

Wird in der Eingangsdaten eine GINO-Gruppen-ID übergeben, so handelt es sich um eine Änderung einer bestehenden Gruppe, ansonsten um wird eine neue Gruppe angelegt.   
Für eine neue Gruppe wird wird in der Entität „GINO-Gruppe" geprüft, ob bereits eine Gruppe mit dem übergebenen GINO-Gruppen-Namen existiert, falls ja, wird die Funktion unter Ausgabe der unten angebebenen GUI-Meldung beendet.
  
Der ermittelte Eintrag in „GINO-Gruppe" wird mit den übergebenen Eingangsdaten aktualisiert, bzw. wird ein neuer Eintrag angelegt:

* TYP wird mit GINO-Gruppentyp
* MITGLIEDER\_TYP wird mit „GINO-Gruppen-Mitgliedertyp"
* NAME wird mit GINO-Gruppen-Name
* FILTER\_SERIENNUMMER wird mit dem Filter Seriennummer und
* FILTER\_FIRMWARE wird mit dem Filter Firmware

jeweils aus den Eingangsdaten versorgt.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **K** |
| GINO-Gruppen-Name | Siehe „GINO-Gruppen-Name". |  | **M** |
| GINO-Gruppentyp | Siehe „GINO-Gruppentyp" |  | **M** |
| GINO-Gruppen-Mitgliedertyp | Siehe „GINO-Gruppen-Mitgliedertyp". |  | **M** |
| Filter Seriennummer | Filter für GINO-Seriennummer | String(1000) | **K** |
| Filter Firmware | Filter für Firmware | String(1000) | **K** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-007 | Eine Gruppe mit diesem Namen existiert bereits. | Message | Error |
| GUI-008 | Eine allgemeine Gruppe existiert bereits. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Konfiguration Zuordnung speichern (OPC_CLV_FKT_143)

# Vorbedingungen

Keine

# Beschreibung

Für Konfigurationsparameter mit crontab Einträgen (Parameter die mit START\_ oder CHECK\_ beginnen) wird die Syntax der übergebenen Parameterwerte überprüft. Die Einträge müssen die Form, wie in KALVE\_FKT\_009 beschrieben, haben. Sollten diese ungültig sein, wird mit der Fehlermeldung GUI-025 abgebrochen.
  
Anhand der in den Eingangsdaten übergebenen GINO-Gruppen-ID werden in der Entität „GINO-Konfiguration-Zuordnung" gespeicherten Einträge mit Referenzen auf den entsprechenden Eintrag in „GINO-Gruppe" ermittelt, und aus der Datenbank gelöscht.
  
Für jeden in den Eingangsdaten übergebenen Eintrag in der „GINO-Konfiguration-Parameter" Liste wird ein neuer Eintrag in „GINO-Konfiguration-Zuordnung" mit Refernzen auf den Eintrag in „GINO-Gruppe" (via GINO-Gruppen-ID) und auf den Eintrag in „GINO-Konfiguration" angelegt:

* Die Attribute „USE\_DEFAULT" und „REMOVE" werden entsprechend den Eingangsdaten gesetzt

Ist „Default" in den Eingangsdaten („GINO-Konfiguration-Parameter") auf „true" gesetzt, dann wird das Attribut „VALUE" nicht versorgt ansonsten wird der „VALUE" auf den übergebe

* nen „Wert" gesetzt

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| „GINO-Konfiguration-Parameter" Liste | Siehe „GINO-Konfiguration-Parameter", Liste kann auch leer sein. |  | **M** |

# Ausgangsdaten

Keine.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-025 | Die eingegebenen Parameter <Parameter1, Parameter2, …> sind ungültig. Die Konfiguration wurde nicht übernommen. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Firmware Zuordnung speichern (OPC_CLV_FKT_144)

# Vorbedingungen

Keine

# Beschreibung

Anhand der in den Eingangsdaten übergebenen GINO-Gruppen-ID werden in der Entität „GINO-Firmware Zuordnung" gespeicherte Einträge mit Referenzen auf den entsprechenden Eintrag in „GINO-Gruppe" ermittelt. Kann ein Eintrag ermittelt werden, und die Daten unterscheiden sich nicht, so wird die Funkion unter Rücklieferung der entsprechenden Ausgangsdaten erfolgreich beendet. Unterscheiden sich die Daten, so wird der Eintrag aus der Datenbank gelöscht.
  
In der Enitiät „GINO-Firmware Zuordnung" wird ein neuer Eintrag mit Referenz auf die GINO-Gruppe (Parameter „GINO-Gruppen-ID" aus den Eingangsdaten) und der „GINO-Firmware Version" aus den Eingangsdaten gespeichert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **K** |
| GINO-Firmware Version | Siehe „GINO-Firmware Version". |  | **M** |

# Ausgangsdaten

Keine.

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Firmware Zuordnung-ID | Siehe „GINO-Firmware Zuordnung-ID". |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-009 | Es existiert keine Firmware Version <Version>. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Konfigurationsgruppe löschen (OPC_CLV_FKT_145)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion löscht die angegebene Gruppe physikalisch aus der Entität „GINO-Gruppe", sowie die referenzierenden Einträge in „GINO-Konfiguration-Zuordnung" und „GINO-Gruppe-Mitglieder"

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-085 | Wollen sie die Gruppe <Gruppenname> wirklich löschen? Ja/Nein | Message | Warning |
| CLV-086 | Die Gruppe <Gruppenname> wurde erfolgreich gelöscht" | Message   Log | Info   Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO Gruppenmitglieder ermitteln (OPC_CLV_FKT_146)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion ermittelt für Konfigurationsgruppen, Firmwaregruppen, Task-Gruppen oder Script Gruppen anhand der übergegenen Gruppen-ID die Mitglieder aus der Entität „GINO-Gruppe-Mitglieder", reichert die Daten anhand der ermittelten Anschluss-ID um die Client-IP aus der Entität „Anschluss" an und liefert sie in den Ausgangsdaten zurück.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Gruppen-Mitglied" | Siehe „GINO-Gruppen-Mitglied".   Die Liste kann auch leer sein. |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO Anschlüsse ermitteln (OPC_CLV_FKT_147)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion ermittelt anhand der übergegenen IP-Adressen die entsprechenden Einträge in der Entität „Anschluss". Für jeden ermittelten Anschluss wird ein neues Element von GINO-Gruppen-Mitglied erzeugt und mit Anschluss-ID und IP-Adresse in den Ausgangsdaten zurückgeliefert.  
IP-Adressen für die kein Anschluss ermittelt werden konnte, werden ebenfalls in der Liste der IP-Adresse der Ausgangsdaten zurückgeliefert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von IP-Adressen | Liste der Client-IP-Adressen für jeweils einen Anschluss |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Gruppen-Mitglied" | Siehe „GINO-Gruppen-Mitglied".   Die Liste kann auch leer sein. |  | **M** |
| Liste von IP-Adressen | IP-Adressen die keinem Anschluss zugeordnet werden konnten.   Die Liste kann auch leer sein. |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO Gruppenmitglieder hinzufügen (OPC_CLV_FKT_148)

# Vorbedingungen

Keine

# Beschreibung

Die Funktion entfernt ermittelt sowohl für Konfigurations- als auch Firmwaregruppen aus der in den Eingangsdaten übergebenen Liste von IP-Adresse Duplikate und ruft mit der in den Eingangsdaten übergebenen Liste von IP-Adresse die Funktion GINO Anschlüsse ermitteln (OPC\_CLV\_FKT\_147) auf.   
Liefert die Funktion in den Ausgangsdaten IP-Adressen zurück, die nicht zugeordent werden konnten, so wird diese in den Ausgangsdaten zurückgeliefert und die Funkton wird fehlerhaft beendet.  
Es wird geprüft ob die ermittelten Anschlüsse bereits der Gruppe mit der GINO-Gruppen-ID aus den Eingangsdaten zugeordnet sind, in diesem Fall werden die entsprechenden IP-Adressen in der Ausgangsdatenliste „Duplikat Liste von IP-Adressen" zurückgeliefert und die Funktion fehlerhaft beendet.
  
Andernfalls wird für jeden Eintrag der von der obigen Funktion zurückgelieferten Liste von GINO-Gruppen-Mitglied ein Eintrag in „GINO-Gruppe-Mitglieder" gespeichert. Die Liste wird mit den neuen Anschluss-IDs angereichtert und in den Ausgangsdaten zurückgeliefert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| Liste von IP-Adressen | Liste der Client-IP-Adressen für jeweils einen Anschluss |  |  |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Gruppen-Mitglied" | Siehe „GINO-Gruppen-Mitglied".   Die Liste kann auch leer sein. |  | **M** |
| Liste von IP-Adressen | IP-Adressen die keinem Anschluss zugeordnet werden konnten.   Die Liste kann auch leer sein. |  | **M** |
| Duplikat Liste von IP-Adressen | IP-Adressen die der Gruppe bereits zugeordnet sind.   Die Liste kann auch leer sein. |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
|  | Anschluss-IP unbekannt <IP> | Message | Warning |
|  |  |  |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO Gruppenmitglieder löschen (OPC_CLV_FKT_149)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion ermittelt sowohl für Konfigurations- als auch Firmwaregruppen anhand der übergegenen Gruppen-ID die Mitglieder aus der Entität „GINO-Gruppe-Mitglieder" und löscht diese.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# Ausgangsdaten

Keine.

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Firmware speichern (OPC_CLV_FKT_150)

# Vorbedingungen

Keine

# Beschreibung

Die Funktion prüft ob in der Entität „GINO Firmware" bereits ein Eintrag mit den in den Eingangsdaten übergebenen Parametern „Software Version" und „Hardware Version" gespeichert ist.  
Falls ja, wird die Funktion fehlerhaft beendet.   
Falls in den Eingangdaten der Parameter „Letzte kompatible Version" übergeben wird, wird geprüft, ob die übergebene Parameter kleiner als der in „GINO-Firmware Version" Wert ist, falls nicht, wird die Funktion fehlerhaft beendet.   
Falls nein, wird in „GINO Firmware" ein neuer Eintrag mit „Software Version", „Hardware Version" und der übergebenen Firmware in der Datenbank gespeichert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Firmware Version | Siehe „GINO-Firmware Version". |  | **M** |
| GINO-Hardware Version | Siehe „GINO-Hardware Version" |  | **M** |
| Letzte kompatible Version | Letzte kompatible GINO-Firmware Version, siehe „GINO-Firmware Version" |  | **K** |
| Datei | Die Firmware. |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Firmware-ID | Siehe „GINO-Firmware-ID". |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-008 | Eine Firmware mit dieser Hardware- und Softwareversion existert bereits. | Message | Error |
| GUI-016 | Die letzte kompatible Version muss kleiner sein, als die Software Version | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Gruppe Firmware ermitteln (OPC_CLV_FKT_151)

# Vorbedingungen

Keine

# Beschreibung

Anhand der übergebenen GINO-Gruppen-ID wird in der Enitiät „GINO-Firmware Zuordnung" ein eventuell auf den Eintrag in „GINO-Gruppe" referenzierender Eintrag ermittelt und aus diesem das Attribut „Software Version" in den Ausgangsdaten zurückgeliefert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gruppen-ID | ID der Gruppe | numerisch | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Firmware Version | Siehe „GINO-Firmware Version". |  | **K** |

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Firmware Versionen ermitteln (OPC_CLV_FKT_152)

# Vorbedingungen

Keine

# Beschreibung

Aus wird aus der Entität „GINO-Firmware" wird die Liste aller GINO-Firmware Versionen ermittelt und in den Ausgangsdaten zurückgeliefert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gruppen-ID | ID der Gruppe | numerisch | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von GINO-Firmware | Siehe „GINO-Firmware". |  | **M** |

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Firmwaregruppe löschen (OPC_CLV_FKT_153)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion löscht die angegebene Gruppe physikalisch aus der Entität „GINO-Gruppe", sowie die referenzierenden Einträge in „GINO-Firmware-Zuordnung" und „GINO-Gruppe-Mitglieder"

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-085 | Wollen sie die Gruppe <Gruppenname> wirklich löschen? Ja/Nein | Message | Warning |
| CLV-086 | Die Gruppe <Gruppenname> wurde erfolgreich gelöscht" | Message   Log | Info   Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Task Gruppe ermitteln (OPC_CLV_FKT_161)

# Vorbedingungen

Keine

# Beschreibung

Anhand der übergebenen GINO-Gruppen-ID werden in der Enitiät „GINO-Task-Zuordnung" die auf den Eintrag in „GINO-Gruppe" referenzierenden Einträge ermittelt und in den Ausgangsdaten zurückgeliefert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gruppen-ID | ID der Gruppe | numerisch | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Task" | Liste von „GINO-Task", kann auch leer sein. |  | **M** |

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Task Zuordnung speichern (OPC_CLV_FKT_162)

# Vorbedingungen

Keine

# Beschreibung

Anhand der in den Eingangsdaten übergebenen GINO-Gruppen-ID werden in der Entität „GINO-Task-Zuordnung" gespeicherten Einträge mit Referenzen auf den entsprechenden Eintrag in „GINO-Gruppe" ermittelt, und aus der Datenbank gelöscht.
  
Für jeden in den Eingangsdaten übergebenen Eintrag in der „GINO-Konfiguration-Parameter" Liste wird ein neuer Eintrag in „GINO-Task-Zuordnung" mit Refernzen auf den Eintrag in „GINO-Gruppe" (via GINO-Gruppen-ID) angelegt:

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| „GINO-Task" Liste | Siehe „GINO-Task", Liste kann auch leer sein. |  | **M** |

# Ausgangsdaten

Keine.

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Task Gruppe löschen (OPC_CLV_FKT_163)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion löscht die angegebene Gruppe physikalisch aus der Entität „GINO-Gruppe", sowie die referenzierenden Einträge in „GINO-Task-Zuordnung" und „GINO-Gruppe-Mitglieder"

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-085 | Wollen sie die Gruppe <Gruppenname> wirklich löschen? Ja/Nein | Message | Warning |
| CLV-086 | Die Gruppe <Gruppenname> wurde erfolgreich gelöscht" | Message   Log | Info   Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Resource Status ermitteln (OPC_CLV_FKT_164)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion ermittelt anhand der übergebenen Eingangsdaten für jede Resource aus „GINO-Resourcetyp" den aktuellsten Status aus der Entität „GINO-Resource" samt referenzierenden Einträgen in „GINO-Resouce Data". Letztere werden in den Attributen „Input-Liste"/"Output-Liste"" von „GINO-Resource Status" in den Ausgangsdaten zurückgeliefert.   
\_Hinweis: Sortierung siehe  „**2.4.35* *GINO-Resource Historie ermitteln (OPC\_CLV\_FKT\_173)**"\_

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| Seriennummer | GINO-Seriennummer |  |  |
| Client-IP | Client-IP der GINO | String | M |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Resource Status." | Siehe „GINO-Resource Status". |  | **M** |

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Script Gruppe ermitteln (OPC_CLV_FKT_165)

# Vorbedingungen

Keine

# Beschreibung

Anhand der übergebenen GINO-Gruppen-ID wird in der Enitiät „GINO-Script-Zuordnung" ein eventuell auf den Eintrag in „GINO-Gruppe" referenzierender Eintrag ermittelt und in den Ausgangsdaten zurückgeliefert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Gruppen-ID | ID der Gruppe | numerisch | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| „GINO-Script-Task" | Siehe „GINO-Script-Task. |  | **K** |

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Script Zuordnung speichern (OPC_CLV_FKT_166)

# Vorbedingungen

Keine

# Beschreibung

Anhand der in den Eingangsdaten übergebenen GINO-Gruppen-ID wird ein eventuell in der Entität „GINO-Script-Zuordnung" gespeicherter Eintrag mit Referenz auf den entsprechenden Eintrag in „GINO-Gruppe" ermittelt, und aus der Datenbank gelöscht.
  
Für den in den Eingangsdaten übergebenen Parameter „GINO-Script-Task" wird ein neuer Eintrag in „GINO-Script-Zuordnung" mit Refernz auf den Eintrag in „GINO-Gruppe" (via GINO-Gruppen-ID) angelegt:

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |
| GINO-Script-Task | Siehe „GINO-Script-Task" |  | **M** |

# Ausgangsdaten

Keine.

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Script Gruppe löschen (OPC_CLV_FKT_167)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion löscht die angegebene Gruppe physikalisch aus der Entität „GINO-Gruppe", sowie die referenzierenden Einträge in „GINO-Script-Zuordnung" und „GINO-Gruppe-Mitglieder"

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Gruppen-ID | Siehe „GINO-Gruppen-ID" |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-085 | Wollen sie die Gruppe <Gruppenname> wirklich löschen? Ja/Nein | Message | Warning |
| CLV-086 | Die Gruppe <Gruppenname> wurde erfolgreich gelöscht" | Message   Log | Info   Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Scripte ermitteln (OPC_CLV_FKT_168)

# Vorbedingungen

Keine

# Beschreibung

Aus wird aus der Entität „GINO-Script" wird die Liste aller gültigen Einträge ermittelt und in den Ausgangsdaten zurückgeliefert.

# Eingangsdaten

Keine.

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Script" | Siehe „GINO-Script". Liste kann auch leer sein. |  | **M** |

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Script speichern (OPC_CLV_FKT_169)

# Vorbedingungen

Keine

# Beschreibung

Die Funktion prüft ob in der Entität „GINO Script" bereits ein Eintrag mit dem übergebenen Namen der Datei als „Scriptname" gespeichert ist. Falls ja, wird die Funktion fehlerhaft beendet und die Meldung GUI-011 ausgegeben.
  
Es werden folgende Prüfungen durchgeführt:

* Die übergebene Datei darf nicht leer sein.
* Die übergebene Signatur darf nicht leer sein.
* Die übergebene Signatur muss eine ASN1-Struktur sein.

*Hinweis: Die Prüfung sind keine exakten Prüfung, ob das Skript bzw. die Signatur gültig ist und ausgeführt werden kann. Es ist nur sichergestellt, dass die Daten gespeichert werden können und als Signaturdatei eine Datei mit dem richtigen Format angegeben wurde.*  
Im Fehlerfall wird die Meldung GUI-010 ausgegeben.  
Ansonsten wird der Name der Skriptdatei als Skriptname sowie die Daten des Skript und der Signatur gespeichert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Datei | Das Script. |  | **M** |
| Signatur | Signatur |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-010 | Das Script {0} konnte nicht gespeichert werden. | Message | Error |
| GUI-011 | Es existiert bereits ein Script mit dem Namen {0}. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Sperrliste ermitteln (OPC_CLV_FKT_170)_KVW-12096_

# Vorbedingungen

Keine

# Beschreibung

In der Entität „GINO Produktionsdaten" werden alle Einträge ermittelt, deren Sperrkennzeichen gesetzt ist. Ist der „Filter Seriennummer" gesetzt und enthält von Leerzeichen verschiedene Werte wird die Seriennummer der Produktionsdatensätze damit verglichen und nur Datensätze zurückgeliefert, bei denen die Seriennummer dem Filter entspricht. Ansonsten werden alle gesperrten Produktionsdaten zurückgegeben.Die Einträge werden nach der Seriennummer aufsteigend sortiert zurückgegeben.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| <ac:structured-macro ac:name="unmigrated-wiki-markup" ac:schema-version="1" ac:macro-id="3065adf0-6200-4777-ab8c-9a6c1aec7831"><ac:plain-text-body><![CDATA[ | Filter Seriennummer | Dabei handelt es sich um einen maximal 10-stelligen String. Dieser kann aus Ziffern [0-9], der Wildcard ? für ein beliebiges Zeichen und der Wildcard \* für beliebig viele Zeichen bestehen. |  | **K** | ]]></ac:plain-text-body></ac:structured-macro> |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Produktionsdaten" | Liste von „GINO-Produktionsdaten", kann auch leer sein. |  | **M** |

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO sperren (OPC_CLV_FKT_171)

# Vorbedingungen

Keine

# Beschreibung

In der Entität „GINO Produktionsdaten" wird anhand der Seriennummer in den Eingangsdaten der entsprechende Eintrag ermittelt. Kann kein Eintrag ermittelt werden, so wird die Funktion unter Ausgabe der Fehlermeldung GUI-012 abgebrochen.
  
Ansonsten wird geprüft ob im „GINO Produktionsdaten"-Datensatz das Sperrkennzeichen bereits gesetzt ist. Falls nicht, werden die Attribute

* „Gesperrt" auf ‚true'
* „Sperrdatum" auf das aktuelle Datum
* „Sperrkommentar" optional auf den in den Eingangsdaten übergebenen Wert und
* „Bearbeiter" auf den in den Eingangsdaten übergebenen Wert

gesetzt.  
In der Entität „GINO" wird nach einem Eintrag gesucht, der auf den oben ermittelten Eintrag in „GINO Produktionsdaten" referenziert. Kann ein Eintrag ermittelt werden, so werden folgende Schritte durchgeführt.

* Die noch gültigen TLS-Server-Zertifikate und TLS-Client-Zertifikate werden widerrufen.
  + In der Entität „Gino Resource" werden für die SeriennPrioritätr Einträge mit HTTP\_STATUS\_CODE=200 und GINO\_RESOURCE== „tls-certs-server" oder „tls-cert-client" ermittelt, für welche der zugeordnete Datensatz in der Entität „Gino Resource Data" mit KEY=„gueltig-bis" im Feld VALUE ein Datum eingetragen hat, welches grösser gleich dem aktuellen Datum ist. (Stringvergleich mit YYYYMMDD)
  + Für jedes gefundene Zertifikat wird die Widerrufsfunktion des PKI-Service aufgerufen, wobei für die Parameter Antragsteller und Seriennummer der VALUE des zugeordneten Datensatz der Entität „GINO Resource Data" mit KEY==„issuer" bzw. „seriennummer" verwendet wird.Der Widerruf wird mit der Meldung CLV-201 gelogged.Bei einem Fehler wird die Funktion unter Ausgabe der Fehlermeldung CLV-200 abgebrochen.
* Der Datensatz in der Entität GINO wird historisiert.

Im Erfolgsfall wird die Meldung CLV-202 geloggt.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Seriennummer | Siehe „GINO-Seriennummer" |  | **M** |
| Kommentar | Kommentar warum das Gerät gesperrt wurde (max. 100 Zeichen) |  | **K** |

# Ausgangsdaten

Keine.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-012 | Es konnte kein GINO Produktionsdatensatz mit der Seriennummer <Seriennummer> ermittelt werden. | Message | Error |
| CLV-200 | Das Zertifkat (Seriennummer=<Serinnummer Zertifikat>, Antragsteller=<Antragsteller>) zum GINO mit der Seriennummer <Seriennummer GINO> konnte nicht gesperrt werden. | MessageLOG | Error |
| CLV-201 | Das Zertifkat (Seriennummer=<Seriennummer Zertifikat>, Antragsteller=<Antragsteller>) zum GINO mit der Seriennummer <Seriennummer GINO> wurde gesperrt. | LOG | Info |
| CLV-202 | Der GINO mit der Seriennummer ‚<Seriennummer>' wurde gesperrt | LOG | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINOs entsperren (OPC_CLV_FKT_172)

# Vorbedingungen

Keine

# Beschreibung

Falls Geräte ausgewählt wurden, muss die Aktion Bestätigt werden (Ja/Nein).  
Falls ja ausgeählt wurde wird für jedes ausgewählte Gerät in der Entität „GINO Produktionsdaten"anhand der Seriennummer der entsprechende Eintrag ermittelt, das Sperrkennzeichen auf ‚false' gesetzt,das Sperrdatum entfernt und das Sperrkommentar ggf. angepasst oder gelöscht. Im Erfolgsfall wird die Meldung CLV-203 geloggt.  
Die Funktion wird unter Ausgabe der Meldung GUI-014 beendet.  
*Hinweis: Um einen entsperrten GINO wieder im Betrieb zu nehmen ist ein Werksreset des GINO notwendig. Damit wird das gesperrte Zertifikat auch am GINO gelöscht und ein neues Zertifikat angefordert. (Der Reset kann entfallen, wenn die Zertifikate auf Grund der Prüfung des Ablaufdatums sowieso erneuert würden).*

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| GINO-Seriennummer | Siehe „GINO-Seriennummer" |  | **M** |
| Kommentar | Kommentar warum die Sperrung/Entsperrung vorgenommen wurde/wird | 100 alphanumerische Zeichen | **K** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Produktionsdaten" | Liste von „GINO-Produktionsdaten", kann auch leer sein. |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-035 | Die GINOs mit den Seriennummern <Menge Seriennummern> wurden entsperrt. | Message | Info |
| CLV-203 | Der GINO mit der Seriennummer <Seriennummer> wurde entsperrt. | LOG | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Resource Historie ermitteln (OPC_CLV_FKT_173)

# Vorbedingungen

Der Benutzer hat in der Liste der Resource-Calls einen Eintrag ausgewählt.

# Beschreibung

Es wird in der der Entität GinoResource nach dem ausgewählten Resource Call gesucht und für die Ergebnisse dann in der Entität GinoResourceData die Daten geholt. Die Anzahl der Resource Calls die angezeigt werden kann mittels Konfigurationsparameter limitiert werden.  
Die Sortierung erfolgt absteigend nach dem Zeitstempel „letzter Aufruf", d.h. der Eintrag mit dem spätestens Datum wird zuerst angezeigt.  
*Hinweis: Dies entspricht auch der Sortierung nach „erster Aufruf", Ausnahme davon sind Einträge der Resource alive. Hier wird der Eintrag der die Verfügbarkeit des GINOS anzeigen soll (Zeitraum „erster Eintrag" – „letzter Eintrag") den Einträgen mit Statuscode 200 vorgereiht.*

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Client-IP | Client-IP der GINO |  | **M** |
| Resource-Call | Siehe „GINO-Resourcetyp" |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Liste von „GINO-Resource Status" | Liste von „GINO-Resource Status", kann auch leer sein. |  | **M** |

# Meldungen

Keine.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Provider ermitteln (OPC_CLV_FKT_174)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion ermittelt die aktuellen Provider aus der Entität „Provider". Dabei werden die Provider-ID und der Providername ermittelt.

# Eingangsdaten

Keine

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Liste mit den gültigen Providern |  | **M** |

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Provider anzeigen (OPC_CLV_FKT_175)

# Vorbedingungen

Es ist zumindest ein Provider vorhanden.

# Beschreibung

Diese Funktion zeigt die Liste der vorhandenen Providern auf der Maske an. Dabei wird die Provider-ID und der Providername dargestellt.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Liste mit den gültigen Providern |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

Keine


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Provider löschen (OPC_CLV_FKT_176)

# Vorbedingungen

Zum angegebenen Provider ist keine Zuordnung zu einem Anschluss vorhanden.

# Beschreibung

Diese Funktion löscht den angegebenen Provider aus der Entität „Provider".  
Vor dem Löschen werden folgende Prüfungen durchgeführt:

* + Zum Provider darf keine Zuordnung zu einem Anschluss existieren

Sind zum angegebenen Provider noch Zuordnungen zu einem Anschluss vorhanden, so wird das Löschen nicht zugelassen und eine Fehlermeldung ausgegeben und im Ereignis-Log protokolliert.  
Vor dem Löschen muss der Benutzer, falls die Prüfungen erfolgreich waren, das Löschen des Provider bestätigen.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Entität |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Entität |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-210 | Der Provider <Provider> kann nicht gelöscht werden, da noch <Anzahl der Zuordnungen> Anschlüsse diesem Provider zugeordnet sind. | Log   Message | Error   Error |
| CLV-211 | Wollen sie den Provider <Provider> wirklich löschen (Ja/Nein)? | Message | Warning |
| CLV-212 | Der Provider <Provider> wurde erfolgreich gelöscht. | MessageLog | InfoInfo |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Provider speichern (OPC_CLV_FKT_177)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion speichert den neuen Provider in der Entität „Provider".  
Vor dem Speichern wird folgende Prüfung durchgeführt:

* + Der angegebene Provider darf noch nicht existieren (Providername darf nur einmal vorhanden sein)

Im Fehlerfall wird das Speichern des Provider nicht zugelassen und eine Fehlermeldung ausgegeben und im Ereignis-Log protokolliert.  
Vor dem Anlegen muss der Benutzer, falls die Prüfung erfolgreich war, das Anlegen des neu erfassten Provider bestätigen.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Provider |  | **M** |
| Provider | Entität |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Entität |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-213 | Der Provider <Provider> konnte nicht angelegt werden, da der Provider bereits existiert. | Log   Message | Error   Error |
| CLV-214 | Wollen Sie den Provider anlegen? (Ja/Nein) | Message | Warning |
| CLV-215 | Der Provider <Provider> wurde erfolgreich angelegt. | MessageLog | InfoInfo |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Provider ändern (OPC_CLV_FKT_178)

# Vorbedingungen

Es wurde bereits ein Provider angelegt. Dieser Provider wird geändert.

# Beschreibung

Diese Funktion speichert den Providername zu einem bestehenden Provider.   
Hierzu werden folgende Schritte durchgeführt:

1. Aus der Entität „Provider" wird der Provider über die Provider-ID ermittelt
2. Der übergebene Providername (neu) und der Providername aus der Datenbank (alt) werden miteinander verglichen.
   1. Sind die Namen dieselben, wird das Speichern der Änderung nicht zugelassen und eine Fehlermeldung ausgegeben.
   2. Unterscheiden sich die Namen, aber der neue Providername existiert bereits in der Datenbank, wird das Speichern der Änderung nicht zugelassen und eine Fehlermeldung ausgegeben.
   3. Unterscheiden sich die beiden Namen, wird der Eintrag in der Datenbank geändert. (Keine Historisierung)

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Provider |  | **M** |
| Provider | Entität |  | **M** |
| Providername | Name der dem Provider zugeordnet werden sollen. |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Provider | Entität |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CLV-216 | Der Provider <Provider> konnte nicht aktualisiert werden, da der Providername bereits vorhanden ist. | Message/Log | Error |
| CLV-217 | Wollen sie den Provider aktualisieren? (Ja/Nein) | Message | Warning |
| CLV-218 | Der Provider <Provider> wurde erfolgreich geändert. | Message/Log | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO Seriennummern aus Datei lesen (OPC_CLV_FKT_179)

# Vorbedingungen

Keine

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Datei | Datei mit Seriennummern |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Seriennummern | Menge von Seriennummern |  | **M** |

# Beschreibung

Falls keine Datei übermittelt wurde, wird mit der Meldung GUI-030 abgebrochen.  
Die übergebene Datei wird zeilenweise gelesen.  
Tritt dabei ein Fehler auf, wird die Funktion mit der Meldung GUI-026 abgebrochen.  
Falls die Datei leer ist, wird die Funktion mit der Meldung GUI-027 abgebrochen.  
Für jede Zeile werden folgende Schritte durchgeführt:

* In jeder Zeile wird genau eine Seriennummer erwartet. Es wird daher geprüft, ob es sich um einen String bestehend aus Ziffern und Buchstaben handelt.
* Ist dies nicht der Fall wird die Funktion mit der Meldung GUI-028 abgebrochen.
* Ansonsten wird die Seriennummer in die ausgehende Liste aufgenommen.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-026 | Fehler beim Lesen der Datei <Dateiname>. | Message | Error |
| GUI-027 | Die Datei <Dateiname> enthält keine Einträge. | Message | Error |
| GUI-028 | Die <Zeilennummer>. Zeile der Datei <Dateiname> enthält keine Seriennummer. | Message | Error |
| GUI-030 | Es wurde keine Datei angegeben. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINOs sperren (OPC_CLV_FKT_180)

# Vorbedingungen

Keine

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Seriennummern | Menge an Seriennummern | Möglicherweise die leere Menge | **M** |
| Kommentar | Kommentar warum die Geräte gesperrt werden (max. 100 Zeichen) |  | **K** |

# Beschreibung

Falls die Menge mit Seriennummern leer ist, wird mit der Meldung GUI-031 abgebrochen.   
Falls zu den angegebenen Seriennummern bereits ein freigegebener GINO existiert, muss die Aktion mit Meldung GUI-034 bestätigt oder abgebrochen werden (Ja/Nein) und die Aktion wird erst nach einer Bestätigung fortgesetzt.  
Für alle Seriennummern wird die Funktion „GINO sperren (OPC\_CLV\_FKT\_171)" mit dem angegebenen Kommentar aufgerufen.  
Im Fehlerfall wird die gesamte Aktion abgebrochen und die entsprechende Fehlermeldung ausgegeben.  
Die Funktion wird unter Ausgabe der Meldung GUI-013 beendet.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-031 | Es wurde keine Seriennummer ausgewählt. | Message | Error |
| GUI-034 | Die ausgewählten GINOs <Menge Seriennummern> wurden bereits freigegeben, wirklich sperren? | Message | Warning |
| GUI-013 | Die GINOs mit den Seriennummern <Menge Seriennummern> wurden gesperrt. | Message | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINOs für andere Instanz vorbereiten (OPC_CLV_FKT_181)

# Vorbedingungen

Keine

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Seriennummern | Menge an Seriennummern | Möglicherweise die leere Menge | **M** |

# Beschreibung

Falls die Menge mit Seriennummern leer ist, wird mit der Meldung GUI-031 abgebrochen.   
Falls zu den angegebenen Seriennummern bereits ein freigegebener GINO existiert, muss die Aktion mit Meldung GUI-032 bestätigt oder abgebrochen werden (Ja/Nein) und die Aktion wird erst nach einer Bestätigung fortgesetzt.  
Für alle Seriennummern wird die Funktion „GINO sperren (OPC\_CLV\_FKT\_171)" mit dem Kommentar „Testgerät" aufgerufen.  
Im Fehlerfall wird die gesamte Aktion abgebrochen und die entsprechende Fehlermeldung ausgegeben.  
Die Funktion wird unter Ausgabe der Meldung GUI-033 beendet.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-031 | Es wurde keine Seriennummer ausgewählt. | Message | Error |
| GUI-032 | Die ausgewählten GINOs <Menge Seriennummern> wurden bereits freigegeben, wirklich in anderer Instanz verwenden? | Message | Warnung |
| GUI-033 | Die GINOs mit den Seriennummern <Menge Seriennummern> wurden gesperrt. Das Exportfile kann mit „Download" heruntergeladen werden. | Message | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO Produktionsdaten aus Datei lesen (OPC_CLV_FKT_182)

# Vorbedingungen

Keine

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Datei | Datei mit Produktionsdaten |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| String | Produktionsdaten |  | **M** |

# Beschreibung

Falls keine Datei übermittelt wurde, wird mit der Meldung GUI-030 abgebrochen.  
Tritt beim Lesen ein Fehler auf, wird die Funktion mit der Meldung GUI-026 abgebrochen.  
Falls die Datei leer ist, wird die Funktion mit der Meldung GUI-027 abgebrochen.

Der Inhalt der Datei wird zurückgegeben.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-026 | Fehler beim Lesen der Datei <Dateiname>. | Message | Error |
| GUI-027 | Die Datei <Dateiname> enthält keine Einträge. | Message | Error |
| GUI-030 | Es wurde keine Datei angegeben. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO Produktionsdaten importieren (OPC_CLV_FKT_183)

# Vorbedingungen

Das clv-Backend wurde konfiguriert.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| String | Produktionsdaten | .csv Datei nach im SS51-Format | **M** |
|  | Import erzwingen | Ob Daten trotz Duplikaten importiert werden. | **K** |

# Beschreibung

Falls die Produktionsdaten leer sind, wird mit der Fehlermeldung GUI-036 abgebrochen.

Der Import wird über die Schnittstelle "Import Produktionsdaten" vom clv-Backend durchgeführt.

Falls Felder in den Produktionsdaten nicht valide sind, wird mit der Fehlermeldung GUI-037 abgebrochen.

Falls ein sonstiger Fehler auftritt, wird mit der Fehlermeldung GUI-038 bzw. GUI-039 abgebrochen.

Falls vom Backend zurückgemeldet wurde, dass Daten bereits in der DB vorhanden sind, bzw. Daten im File doppelt vorhanden sind, wird die Warnung GUI-040 ausgegeben, für welche der Nutzer bestätigen kann, die Daten trotzdem zu importieren (Import erzwingen). Falls "ja" ausgewählt wurde, wir diese Funktion mit "Import erzwingen" aufgerufen.

Die Funktion wird unter Ausgabe der Meldung GUI-041 beendet.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-036 | Es wurde keine Daten ausgewählt. | Message | Error |
| GUI-037 | Fehler bei Validation von <NAME> (<WERT>): <GRUND>. | Message | Error |
| GUI-038 | Beim Aufruf vom CLV trat der folgende Fehler auf: <NAME> (<STATUS\_CODE>) - <DETAILS>. | Message | Error |
| GUI-039 | Beim Aufruf vom CLV trat der folgende Fehler auf: <DETAILS> | Message | Error |
| GUI-040 | Die Produktionsdaten mit den Seriennummern <LISTE SERIENNUMMERN> sind bereits in der Datenbank vorhanden. <OPTIONAL "Die Produktionsdaten mit den Seriennummern <LISTE SERIENNUMMERN> sind doppelt im angegebenen File vorhanden. ">Trotzdem importieren? | Message | Warnung |
| GUI-041 | Die Seriennummern <LISTE SERIENNUMMERN> wurden erfolgreich importiert. | Message | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Konfigurationsparameter erstellen (OPC_CLV_FKT_154)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion legt einen neuen GINO-Konfigurationsparameter an, indem ein neuer Eintrag in der Entität "CL\_GINO\_CONFIG" angelegt wird. Die Werte Name, Standardwert, Beschreibung werden übernommen. Als "last Change" wird das aktuelle Datum verwendet. Als User-ID die ID des angemeldeten Users.

Sollte der Parameter "Zur allgemeinen Konfiguration hinzufügen" ausgewählt sein, wird dieser Parameter mit dem Standardwert zu jeder allgemeinen Konfigurationsgruppe hinzugefügt. Eintrag in der Entität "CL\_GINO\_KONFIG\_ZUORDNUNG"

Sollte ein Fehler auftreten, wird die Meldung GUI-061 ausgegeben, ansonsten die Meldung GUI-060.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des Parameters | (max. 100 Zeichen) | **M** |
| Standardwert | Der Name des Parameters | (max. 4000 Zeichen) | **M** |
| Beschreibung | Die Beschreibung des Parameters | (max. 1000 Zeichen) | **M** |
| Zur allgemeinen Konfiguration hinzufügen | Auswahl, ob der Wert zu den allgemeinen Gruppen hinzugefügt wird oder nicht. | Ja/Nein | **M** |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-060 | Der GINO-Konfigurationsparameter <PARAMETER> wurde erfolgreich gespeichert. | Message | Info |
| GUI-061 | Der Gino GINO-Konfigurationsparameter <PARAMETER> konnte nicht gespeichert werden. (<GRUND>) | Message | Error |
| CLV-050 | Der Gino Parameter <PARAMETER> wurde vom Nutzer <USER\_ID> erstellt. | Log | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### GINO-Konfigurationsparameter löschen (OPC_CLV_FKT_155)

# Vorbedingungen

Keine

# Beschreibung

Diese Funktion löscht einen GINO-Konfigurationsparameter. Der Löschvorgang wird geloggt (CLV-051).

Sollte ein Fehler auftreten, wird die Meldung GUI-012 ausgegeben, ansonsten die Meldung GUI-013.

# Eingangsdaten

| Name | Beschreibung | mögliche Werte | Muss/Kann |
| --- | --- | --- | --- |
| Name | Der Name des Parameters |  | **M** |

# Ausgangsdaten

Keine

# Meldungen

| MeldungsNr | Meldungstext | Typ | Level |
| --- | --- | --- | --- |
| GUI-062 | Der GINO-Konfigurationsparameter <PARAMETER> wurde erfolgreich gelöscht. | Message | Info |
| GUI-063 | Der Gino GINO-Konfigurationsparameter <PARAMETER> konnte nicht gelöscht werden. | Message | Error |
| CLV-051 | Der Gino Parameter <PARAMETER> wurde vom Nutzer <USER\_ID> gelöscht. | Log | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Session-Verwaltung - Funktionen




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Session(s) suchen (OPC_SEV_FKT_001)

# Beschreibung

Diese Funktion sucht eine oder mehrere Sessions.

# Eingangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| Session Information („Datum von" oder „Datum bis" muss angegeben werden) |  |  |  |
| Vertragspartner-nummer | Vertragspartner |  | **K** |  |
| Ordinationsnummer | Ordinationsnummer |  | **K** |  |
| GinaID | Identifikation der Gina |  | **K** |  |
| TerminalID | Identifikation des Terminals |  | **K** |  |
| ExtUID\_ASW | Externe UserId (z.B. aus ASW/KIS) |  | **K** |  |
| Datum von | Beginn des Beobachtungszeitraumes   Bedingt: Mindestens eines der beiden Felder „Datum bis" und „Datum von" muss angegeben werden.   Das eingebene Datum muss kleiner gleich dem Startdatum der Session sein. |  | **B** |  |
| Datum bis | Ende des Beobachtungszeitraumes   Bedingt: Mindestens eines der beiden Felder „Datum bis" und „Datum von" muss angegeben werden.   Das eingegebene Datum (Uhrzeit „23:59:59") muss grösser oder gleich dem Endedatum der session sein. |  | **B** |  |
| Sortierung – Feld1 | Sortierfeld 1 | Ordinationsnummer, Datum,Dialog-ID | **M** |  |
| Sortierung – Reihenfolge | Sortierreihenfolge | aufsteigend oder   absteigend | **M** |  |
| Dialog/Session beenden |  |  |  |
| Vertragspartner-nummer | Vertragspartner |  | **K** |  |
| Ordinationsnummer | Ordinationsnummer |  | **K** |  |
| GinaID | Identifikation der Gina |  | **K** |  |
| TerminalID | Identifikation des Terminals |  | **K** |  |
| Sortierung – Feld1 | Sortierfeld 1 | Ordinationsnummer oder Datum | **M** |  |
| Sortierung – Reihenfolge | Sortierreihenfolge | aufsteigend oder   absteigend | **M** |  |

# Ausgangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| 0 bis n Wiederholungen |  |  |  |
| Vertragspartner-session ID | Eindeutiger Schlüssel der Vertragspa‚tnersessio' (Attribut 'Sess‚on ID' in'der Entität 'VP\_Dialog') |  | **M** |  |
| Ordination | Adresse der Ordination (PLZ, Ort und Straße) |  | **M** |  |
| Ordinationsnummer | Ordinationsnummer |  | **M** |  |
| DialogID | Identifikation des Dialoges |  | **K** |  |
| GinaID | Identifikation der Gina |  | **M** |  |
| TerminalID | Identifikation des Terminals |  | **M** |  |
| ExtUID\_ASW | Externe UserId |  | **K** |  |
| Von | Sessionbeginn |  | **M** |  |
| Bis | Sessionende |  | **K** |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anzahl offener VP-Sessions ermitteln (OPC_SEV_FKT_002)

# Beschreibung

Es wird die Anzahl der zum aktuellen Zeitpunkt offenen Dialoge und Sessions ermittelt. Die Ausgabeinformation kann durch die Angabe einer Zeitspanne eingeschränkt werden. Die Dauer der Session wird vom Zeitpunkt der Abfrage betrachtet.  
Beim Aufbau einer Session wird ein eindeutiger Schlüssel (DialogID) zur Session gespeichert, falls diese vom Ordinationsclient generiert und an den e-card Server übergeben wurde. Anhand dieser eindeutigen DialogID werden Sessions zusammengefasst. Wird bei einer Session keine DialogID gespeichert, so werden diese Sessions als separate Dialoge gewertet.  
Bei der Ermittlung der Anzahl der offenen Sessions über das Suchkriterium ‚GinaIP' (IP-Adresse), erfolgt keine Überprüfung, ob es sich bei der angegebene IP-Adresse um eine gültige IP-Adresse handelt.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartner |  | **K** Entweder Vertragspartnernummer oder IP-Adresse muss gegeben sein |
| GinaIP | IP-Adresse der GINA |  | **K1** |
| Zeitspanne | Zeit seit der die Sessions offen sein sollen |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartner |  | **K1** |
| GinaIP | IP-Adresse der GINA |  | **K1** |
| OffeneDialoge | Gesamte Anzahl an offenen VP-Dialogen zum Abfragezeitpunkt |  | **M** |
| OffeneSessions | Gesamte Anzahl an offenen VP-Sessions zum Abfragezeitpunkt |  | **M** |
| Zeitspanne | Angegebene Zeit seit der die Sessions schon offen sein sollen |  | **M** |
| OffeneDialogeZeitspanne | Anzahl der offenen VP-Dialoge zum Abfragezeitpunkt, die länger als die angegebene Zeitspanne offen sind |  | **M** |
| OffeneSessionsZeitspanne | Anzahl der offenen VP-Sessions zum Abfragezeitpunkt, die länger als die angegebene Zeitspanne offen sind |  | **M** |

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| SES-130 | Es wurden beide Suchkriterien eingegeben, die Bearbeitung wird abgebrochen. | Log | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Anmeldungen ermitteln (OPC_SEV_FKT_003)

# Beschreibung

Diese Funktion ermittelt die letzte erfolgreiche Anmeldung mittels Ordinationskarten bzw. SW-Zertifikate eines Vertragspartners.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-nummer | Vertragspartnernummer |  | **M** |

# Ausgangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| 0 bis n Wiederholungen (zuerst werden die Anmeldungen mittels Ordinationskarte und dann die Anmeldungen mittels SW-Zertifikat ausgegeben) |  |  |  |
| Identifikation | Anmeldung mittels Ordinationskarte:   o-card ID |  | **M** |  |
|  | Anmeldung mittels SW-Zertifikat:   Seriennummer des SW-Zertifikats |  |  |  |
| Anmeldetyp | „O" bei Anmeldung mittels Ordinationskarte oder „S" bei Anmeldung mittels SW-Zertifikat | „O" bzw. „S" | **M** |  |
| Zeitpunkt | Zeitpunkt der letzten erfolgreichen Anmeldung | Datum/Uhrzeit | **M** |  |
| Laufnummer | Die Laufnummer bei Anmeldungen mittels Ordinationskarte |  | **K** |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Sessiondetails ermitteln (OPC_SEV_FKT_004)

# Beschreibung

Diese Funktion ermittelt Detailinformationen zu einer Vertragspartnersession.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Vertragspartner-session ID | Eindeutiger Schlüssel der Vertragspa‚tnersessio' (Attribut 'Sess‚on ID' in'der Entität 'VP\_Dialog') |  | **M** |

# Ausgangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |  |
| Ordination | Adresse der Ordination (PLZ, Ort und Straße) |  | **M** |  |
| Ordinationsnummer | Ordinationsnummer |  | **M** |  |
| AusprägungsCode | Code der Ausprägung |  | **M** |  |
| DialogID | Identifikation des Dialoges | Zeichenkette | **K** |  |
| GinaID | Identifikation der Gina |  | **M** |  |
| TerminalID | Identifikation des Terminals |  | **K** |  |
| ProduktId\_ASW | Produkt Id des ASW/KIS-Herstellers | Zeichenkette | **K** |  |
| ProduktVersion\_ASW | Produkt Version des ASW/KIS-Herstellers | Zeichenkette | **K** |  |
| ExtUID\_ASW | Externe UserId (z.B. aus ASW/KIS) |  | **K** |  |
| Distribution | Versionsnummer der Softwaredistribution |  | **M** |  |
| von | Startzeitpunkt der Session |  | **M** |  |
| bis | Endzeitpunkt der Session |  | **K** |  |
| Beendigungsgrund | Grund zur Beendigung der Session |  |  |  |
| Anmeldetyp | „O" bei Anmeldung mittels Ordinationskarte oder „S" bei Anmeldung mittels SW-Zertifikat | „O" bzw. „S" | **M** |  |
| Kartennummer | Vertragspartnernummer und laufende Nummer der Karte (Anmeldung mittels Ordinationskarte) | Zeichenkette | **K** |  |
| SW-Zertifikat | Vertragspartnernummer und Seriennummer des SW-Zertifikats (Anmeldung mittels SW-Zertifikat) | Zeichenkette | **K** |  |
| Applikationsserver | Liste der Server zu denen der Request gesendet wurde. Im Normalfall ein Server, im Fall von Load Balancing oder Failover kann es mehr als einer sein |  | **M** |  |
| User Agent | Kennung des User Agents, von dem der Request abgeschickt wurde (z.B. Browser des Benutzers). | ZeiPrioritätkette | **K** |  |
| GINA Host | HTTP-Host-Header des Requests an der GINA. | Zeichenkette | **K** |  |
| Rolle | ELGA-Rolle die bei der Anmeldung angegeben wurde | Zeichenkette | **K** |  |
| GDAMA | GDA Mitarbeiter (nur möglich falls ELGA-Rolle gesetzt ist)   Setzt sich aus den Komponenten „Titel vorne", Vorname, Zuname, Titel hinten" und Zusatzinformation zusammen | Zeichenkette | **K** |  |
| Servicename | Kann anstelle des GDA-Mitarbeiters angegeben werden. | Zeichenkette | **K** |  |
| 0 bis n Wiederholungen der Requestinformationen |  |  |  |
| Datum | Zeitpunkt der Absendung des Requests vom Client |  | **K** |  |
| Applikationsserver | Name des Applikationsserver, auf dem der Request abgearbeitet wird bzw. wurde |  | **K** |  |
| Applikation | am Server aufgerufene Applikation |  | **K** |  |
| Methode | von der Applikation aufgerufene Methode |  | **K** |  |
| Dauer | Dauer der Abarbeitung des Requests (Client – Client) |  | **K** |  |
| InterfaceName | Schnittstellenname des SS12-Funktionsaufrufes |  | **K** |  |
| InterfaceVersion | Schnittstellenversion des SS12-Funktionsaufrufes |  | **K** |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Session beenden (OPC_SEV_FKT_005)

# Beschreibung

Diese Funktion ermittelt alle offenen Sessions zu den gegebenen Suchkriterien und versucht diese zu beenden. Das Timeout gibt an wie lange die Sessions noch maximal aktiv sein dürfen. Ist die Änderung erfolgreich propagiert worden, liefert die Funktion „true" (Erfolg) zurück. Es wird für die Rückmeldung nicht das Ende des Timeouts abgewartet.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Session ID | Id der zu löschenden Session | Numerisch | M |
| Timeout | Timeout in Minuten bis die Session spätestens beendet wird. | Numerisch | M |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss/Kann** |
| Erfolg | Rückgabeflag zeigt mit ‚true' eine erfolgreiche Löschung an. | ‚true', ‚false' | M |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Nachrichten - Funktionen




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Adressaten ermitteln (OPC_MES_FKT_001)

# Beschreibung

In Abhängigkeit der übergebenen Filterkriterien werden in dieser Funktion die Adressaten der Meldung ermittelt. Dabei wird der Adressen-Typ „Vertragspartnernummer" festgelegt. Wird kein Filter angegeben, so werden alle Vertragspartner adressiert und es handelt sich um den Adressen-Typ ‚Vertragspartnernummer'.  
Für den Adressen-Typ stehen folgende Filterkriterien (AufPrioritätefiniert auch die Priorität der Filterkriterien) zur Verfügung:  
Vertragspartnernummer:Vertragspartnernummer und Zuname  
Bei Angabe mehrerer Filter des Adress-Typs wird die Schnittmenge der jeweiligen Filterergebnisse adressiert.  
Innerhalb eines Filterkriteriums (z.B. Vertragspartnernummer: 111222, 111333, 111444) erfolgt eine OR-Verknüpfung. Innerhalb einer Filtergruppe (z.B. Vertragspartnernummern-Filter: Vertragspartnernummer und Zuname) erfolgt eine AND-Verknüpfung analog der Filterfunktion ‚Verteilauftrag erstellen'.  
Beim Filter „Postleitzahl" kann ein Wertebereich (von/bis) angegeben werden (z.B.: 1000-1010). Soll eine Filterung nach bestimmten Postleitzahlen erfolgen, so muss im VON-Bereich ein entsprechender Eintrag vorgenommen werden (z.B.: 10\*).  
Für die Filterkriterien ‚Vertragspartnernummer' und ‚Zuname' wird für Einzelwerte (z.B. bei Vertragspartner: ‚100\*') als auch für Listenwerte (z.B. bei Vertragspartner: „100\*,200\*") die Wildcard Eingabe unterstützt.  
Für Vertragspartnernummern besteht zusätzlich die Möglichkeit, diese aus einer Datei zu laden. Die Datei muss einer Vertragspartnernummer je Zeile enthalten.  
Bei der Eingabe von Listen in den Filterkriterien ‚Vertragspartnernummer' und ‚Zuname', müssen die einzelnen Listenelemente mit Komma voneinander getrennt sein.  
Die Ausgangsdaten (Adressaten) selbst werden in Abhängigkeit des Filterkriteriums erstellt.  
***Vertragspartner-Filter:***  
Bei diesem Filter werden die Vertragspartnernummer und der Zuname des Vertragspartners ermittelt und in den Ausgangsdaten eingetragen.  
Die Information zur Ordination bzw. Session steht bei diesem Filter nicht zur Verfügung.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Filter Vertragspartnernummer | Liste mit Vertragspartnernummern |  | K |
| Filter Zuname | Liste mit Zunamen von Vertragspartnern |  | K |

# Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| VPNR | Vertragspartnernummer |  | K |
| Zuname | Zuname des Vertragspartners |  | K |
| Ordinationsnummer | Ordinationsnummer |  | K |
| Bundesland | Bundeslandcode der Ordination |  | K |
| PLZ | PLZ der Ordination |  | K |
| Ort | Ort der Ordination |  | K |
| Strasse | Strasse der Ordination |  | K |
| Besitzverhältnis | Besitzverhältniscode des Vertragspartners |  | K |
| Session ID | Session ID |  | K |

# Meldungen / Ausnahmen

|  |
| --- |
| **Ausnahmen** |
| Zu den angegebenen Filterkriterien stehen keine Adressaten zur Verfügung. |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Nachricht speichern (OPC_MES_FKT_002)

# Beschreibung

Die Eingabe des Gültigkeitsintervalls ist optional (Kann-Feld). Wird kein Gültigkeitsintervall ‚BIS' erfasst, ist die Nachricht gültig, bis diese gelöscht wird (Gültigkeitintervall ‚VON' muss angegeben werden).  
Zum Speichern der Nachricht wird die erstellte Nachricht (inklusive Filterkriterien und Ersteller) mit den zugehörigen Adressaten und der System-Identifikation ‚SYSTEM\_OPC' an das Messaging-System (siehe Funktion „BF\_MS\_FKT\_001") zum Speichern der Nachricht übergeben. Bei erfolgreicher Speicherung wird eine eindeutige Identifikation für die Nachricht (Meldungsnummer) zurückgeliefert.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Nachricht (Message) | Nachricht |  | M |
| Kategorie | Meldungsname |  | M |
| Text | Meldungstext |  | M |
| Gültigkeitsintervall | Gültigkeitsintervall. |  | M |
| Von | Startzeitpunkt der Gültigkeit der Nachricht |  | M |
| Bis | Endzeitpunkt der Gültigkeit der Nachricht |  | K |
| Filter | Filterkriterien |  | M |
| VPNR | Liste mit Vertragspartnernummern |  | K |
| Zuname | Liste mit Zunamen von Vertragspartnern |  | K |
| Adressaten | Angabe von 1 bis n Adressaten. Ein Adressat kann dabei eine Vertragspartnernummer, Ordination oder eine einzelne Vertragspartner-Session sein. |  | M |

# Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Identifikationen | Eindeutige Identifikation der Nachrichten. |  | M |

# Meldungen / Ausnahmen

|  |
| --- |
| **Ausnahmen** |
| Die Nachricht mit der Meldungsnummer <eindeutige Identifikation für die Nachricht> wurde gespeichert. |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Nachricht suchen (OPC_MES_FKT_003)

# Beschreibung

Über diese Funktion werden die Nachrichten zu einem angegebenen Suchkriterium ermittelt.  
Anhand der Suchkriterien für die Nachrichtenliste wird, falls ein Vertragspartner (Vertragspartnernummer) angegeben wurde, die Meldungsliste zum angegebenen Vertragspartner eingeschränkt.  
Eine Wildcard-Angabe für den Vertragspartner (Vertragspartnernummer) und dem Ersteller wird nicht unterstützt.  
Wird ein Vertragspartner angegeben, so werden die Nachrichten mit dem Adressaten-Typ „Session" nicht bei der Filterung berücksichtigt und diese Meldungen auch nicht dargestellt.  
Das Suchkriterium ‚Nachricht aktiv' bezieht sich immer auf das aktuelle Datum. Wurde eine Nachricht gelöscht, so wird diese Nachricht ebenfalls als ‚nicht aktiv' ausgegeben.  
Die Suchkriterien ‚VON-Datum' und ‚BIS-Datum' beziehen sich immer auf den Gültigkeitsbereich der Meldung (sprich ob die Meldung im angegebenen Zeitraum gültig war).  
***Eingabe ‚VON-Datum':***

* Meldungen, bei denen das eingegeben VON-Datum im Meldungs-Gültigkeitszeitraum liegt

***Eingabe ‚BIS-Datum':***

* Meldungen, bei denen das eingegeben BIS-Datum im Meldungs-Gültigkeitszeitraum liegt (eingegebenes BIS-Datum <= Gültigkeitswert ‚Gültig bis' und eingegebenes BIS-Datum > Gültigkeitswert ‚Gültig von').

***Eingabe ‚VON-Datum' und ‚BIS-Datum':***

* Meldungen, bei denen das eingegebene VON- und BIS-Datum im Meldungs-Gültigkeitszeitraum liegt.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | K |
| Meldungsname | Name der Nachricht |  | K |
| VON-Datum | Startzeitpunkt der Gültigkeit der Nachricht |  | K |
| BIS-Datum | Endzeitpunkt der Gültigkeit der Nachricht |  | K |
| Meldungsstatus | Status der Nachricht | aktiv/inaktiv/alle | K |
| Erfasser | Ersteller der Nachricht |  | K |
| Erfassdatum | Erstelldatum der Nachricht |  | K |
| VPNR | Vertragspartnernummer |  | K |

# Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | M |
| Meldungsname | Name der Meldung |  | M |
| Gültigkeitsintervall | Gültigkeitsintervall |  | M |
| Von | Startzeitpunkt der Gültigkeit der Nachricht |  |  |
| Bis | Endzeitpunkt der Gültigkeit der Nachricht |  |  |
| Meldungsstatus | Status ob Meldung noch aktiv ist | aktiv/inaktiv | M |
| Löschstatus | Status ob Meldung gelöscht wurde | nicht gelöscht/gelöscht | M |
| Ersteller | Ersteller der Nachricht |  | M |
| Erstelldatum | Erstelldatum der Nachricht |  | M |

# Meldungen / Ausnahmen

|  |
| --- |
| **Ausnahmen** |
| Zum angegebenen Suchkriterium stehen keine Nachrichten zur Verfügung |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Details einer Nachricht ermitteln (OPC_MES_FKT_004)

# Beschreibung

Über diese Funktion werden die Details einer Nachricht (siehe Ausgangsdaten) zur angegebenen Meldungsnummer ermittelt. In den Ausgangsdaten werden für das ‚Bundesland' und das ‚Besitzverhältnis' die Codes ausgegeben.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | K |

# Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | M |
| Meldungsname | Name der Meldung |  | M |
| Gültigkeitsintervall | Gültigkeitsintervall |  | M |
| Von | Startzeitpunkt der Gültigkeit der Nachricht |  | M |
| Bis | Endzeitpunkt der Gültigkeit der Nachricht |  | K |
| Meldungsstatus | Status ob Meldung noch aktiv ist |  | M |
| Ersteller | Ersteller der Nachricht |  | M |
| Erstelldatum | Erstelldatum der Nachricht |  | M |
| Löschdatum | Löschdatum der Nachricht |  | M |
| Filter | Filterkriterien |  | M |
| VPNR | Liste mit Vertragspartnernummern |  | K |
| Zuname | Liste mit Zunamen von Vertragspartnern |  | K |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Adressaten einer Nachricht ermitteln (OPC_MES_FKT_007)

# Beschreibung

Über diese Funktion werden die Adressaten einer Nachricht (siehe Ausgangsdaten) zur angegebenen Meldungsnummer in Abhängigkeit des Adressaten-Typs (Vertragspartnernummer Ordination, Session) ermittelt.  
Die Ausgangsdaten (Adressaten) selbst werden dabei in Abhängigkeit des Adressen-Typs erstellt.  
***Session (Session-ID):***  
Es werden alle Ausgangsdaten, falls die erforderlichen Daten zur Ermittlung der Ordination und Vertragspartner zur angegebenen Session-ID gespeichert wurden, ermittelt.  
Können aus den in der Session gespeicherten Informationen die Vertragspartnernummer bzw. Ordinationsnummer nicht ermittelt werden, so wird lediglich die Session-ID ausgegeben. Andernfalls werden anhand der gespeicherten Informationen die Daten aus der Entität „Ordination" bzw. „Vertragspartner" ermittelt und entsprechend eingetragen.  
***Ordination (Ordinations-ID):***  
Anhand der Odinations-ID werden bis auf die Session-ID alle Informationen ermittelt und in den Ausgangsdaten eingetragen.  
***Vertragspartnernummer:***  
Anhand der bei diesem Adressen-Typ eingetragenen Vertragspartnernummer werden die Vertragspartnernummer und der Nachname des Vertragspartners selbst ermittelt und in den Ausgangsdaten eingetragen.   
Die Informationen zur Ordination und Session stehen nicht zur Verfügung.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Meldungsnummer | Eindeutige Identifikation der Nachricht |  | K |

# Ausgangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Adressaten | Liste von 1-n Adressaten |  | M |
| VPNR | Vertragspartnernummer |  | K |
| Zuname | Zuname des Vertragspartners |  | K |
| Ordinationsnummer | Ordinationsnummer |  | K |
| Bundesland | Bundesland der Ordination |  | K |
| PLZ | PLZ der Ordination |  | K |
| Ort | Ort der Ordination |  | K |
| Strasse | Strasse der Ordination |  | K |
| Besitzverhältnis | Besitzverhältnis des Vertragspartners |  | K |
| Session ID | Session ID |  | K |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Nachricht löschen (OPC_MES_FKT_005)

# Beschreibung

Diese Funktion dient zum Löschen einer Nachricht. Die Nachricht wird dabei nicht physikalisch gelöscht. Sie wird von allen zugeordneten Vertragspartner-Sessions entfernt und ab diesem Zeitpunkt keinen neuen Vertragspartner-Sessions zugeordnet.  
Zum Löschen der Nachricht wird die Nachricht unter Angabe der eindeutigen Meldungsnummer (eindeutige Identifikation der Nachricht) an das Messaging-System (siehe Funktion „BF\_MS\_FKT\_002") übergeben und gelöscht.

# Vorbedingungen

Es ist eine Nachricht vorhanden und der Aufrufer besitzt die eindeutige Identifikation.

# Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Identifikation | Eindeutige Identifikation der zu löschenden Nachricht. |  | M |

# Meldungen / Ausnahmen

|  |
| --- |
| **Ausnahmen** |
| Wollen sie die Nachricht mit der Meldungsnummer <Eindeutige Identifikation der zu löschenden Nachricht> wirklich löschen? |
| Die Nachricht mit der Meldungsnummer <Eindeutige Identifikation der zu löschenden Nachricht> wurde erfolgreich gelöscht. |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Chipkartenverwaltung - Funktionen




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Karten suchen (OPC_CKV_FKT_001)

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Kartentyp | Kartentyp | e-card, o-card | **M** |
| CIN | CIN der Karte |  | **K** |
| Karten ID | Eindeutige Identifikation der o-card (o-card ID) oder der e-card (e-card ID), abhängig vom Kartentyp |  | **K** |
| VPNR/VSNR | Vertragspartnernummer oder Versichertennummer, abhängig vom Kartentyp |  | **B** |
| Kartenfolgenummer | Kartenfolgenummer |  | **K** |
| Status | Status der Karte | eingeschränkt auf:   04, 12, 23 und 33 | **K** |

*Hinweis:Wildcards werden nicht unterstützt.Wenn die Kartenfolgenummer eingegeben wird, muss auch die VPNR/VSNR erfasst werden.Alle Eingabefelder werden mit UND verknüpft.Eines der Eingabefelder CIN, VPNR/VSNR, Karten ID, Kartenfolgenummer oder Status muss versorgt sein.*

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| pro gefundener Karte |  |  |  |
| Kartentyp | Kartentyp | e-card, o-card | **M** |
| CIN | CIN der Karte |  | **M** |
| VPNR/VSNR | Vertragspartnernummer oder Versichertennummer, abhängig vom Kartentyp |  | **M** |
| Kartenfolgenummer | Kartenfolgenummer |  | **M** |
| Karten ID | Eindeutige Identifikation der o-card (o-card ID) oder der e-card (e-card ID), abhängig vom Kartentyp |  | **M** |
| Bemerkung | Bemerkung zum Kartenstatus |  | **K** |
| Bemerkungsdatum | Datum an dem die Bemerkung geändert wurde |  | **K** |
| Bemerkungsuser | User, der die Bemerkung geändert hat |  | **K** |
| Status | Aktueller Status der Karte |  | **M** |
| EndStatusAllowed |  |  | **M** |
| pro Status pro gefundener Karte |  |  |  |
| Status | Historischer Status der Karte |  | **K** |
| Grund Code | Code für den Grund der Änderung. |  | **K** |
| Bemerkung | Bemerkung zum Kartenstatus |  | **K** |
| Datum | Änderungsdatum auf diesen Status |  | **K** |
| User | User, der den Status geändert hat. |  | **K** |

*Hinweis:Das Ergebnis wird aufsteigend sortiert nach dem Änderungsdatum des aktuellen Kartenstatus. Danach nach VPNR/VSNR.*

# Beschreibung

Die Funktion sucht alle Karten, die zu den Kriterien aus den Eingangsdaten passen und liefert diese zurück.

1. Die Karten werden nach folgenden Kriterien eingeschränkt:

* Ist der Kartentyp aus den Eingangsdaten gleich „e-card", wird in der Entität „e-card" gesucht. Ist der Kartentyp aus den Eingangsdaten gleich „o-card", wird in der Entität „o-card" gesucht.
* Die CIN muss gleich der aus den Eingangsdaten sein.
* Die Karten ID muss gleich der aus den Eingangsdaten sein.
* Ist der Kartentyp aus den Eingangsdaten gleich „e-card", muss VSNR gleich VPNR/VSNR aus den Eingangsdaten sein.
* Ist der Kartentyp aus den Eingangsdaten gleich „o-card", muss VPNR gleich VPNR/VSNR aus den Eingangsdaten sein.
* Die Kartenfolgenummer muss gleich der aus den Eingangsdaten sein.
* Der aktuelle Status muss gleich dem aus den Eingangsdaten sein.

1. Für alle gefundenen Karten wird die Funktion „Erlaubte Statusübergänge für OPC prüfen (OPC\_CKV\_FKT\_005)" aufgerufen und das Ergebnis in „EndStatusAllowed" gespeichert.
2. Die so ermittelten Daten werden an der Ausgangsschnittstelle bereitgestellt.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
|  | Zum angegeben Suchkriterium stehen keine Karten zur Verfügung. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Fotoinformation suchen (OPC_CKV_FKT_002)

# Vorbedingungen

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR | Versichertennummer |  | **M** |
| Typ | Typ der Fotoinformation | „FZ" oder „FA" | **K** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Fotoinformation | Fotoinformationsobjekt, d.h. alle Daten aus der Entität Foto\_Info. |  | **K** |

# Beschreibung

Die Funktion sucht zu den übergebenen Eingangsdaten einen aktuellen Eintrag in der Entität Foto\_Info, d.h. einen Eintrag bei dem für das Attribut InformationsendeAm das Defaultdatum (31.12.3999) eingetragen ist.  
Falls kein Typ angegeben wird, wird unabhängig vom Typ gesucht.  
*Hinweis: Für eine VSNR kann es zu einem Zeitpunkt nur einen aktuellen Eintrag geben, entweder des Typs „FZ" oder des Typs „FA".*  
Falls der Aufruf über die Maske „Fotoinformation Suchen (OPC\_CKV\_GUI\_007)" erfolgt und kein Eintrag gefunden wurde, wird die Meldung GUI-801 ausgegeben.  
Falls ein Eintrag gefunden wurde, wird dieser retourniert.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-801 | Zu den angegebenen Suchkriterien existiert keine aktuelle Fotoinformation. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Bemerkung zur Karte ändern (OPC_CKV_FKT_003)

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Kartentyp | Kartentyp | e-card, o-card | **M** |
| Karten ID | Eindeutige Identifikation der e-card (e-card ID) oder o-card (o-card ID), abhängig vom Kartentyp |  | **M** |
| Bemerkung | Bemerkung zur Karte |  | **M** |
| Bemerkungsdatum | Datum an dem die Bemerkung geändert wurde.   Falls nicht versorgt, aktuelles Datum |  | **K** |
| Bemerkungsuser | User, der die Bemerkung geändert hat |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Prüfergebnis | Prüfergebnis | true, false | **M** |

# Beschreibung

Die Bemerkung, das Bemerkungsdatum und der Bemerkungsuser werden zur e-card bzw. o-card gespeichert.

1. Suche der Karte nach folgenden Kriterien:

* Ist der Kartentyp aus den Eingangsdaten gleich „e-card", wird in der Entität „e-card" gesucht. Ist der Kartentyp aus den Eingangsdaten gleich „o-card", wird in der Entität „o-card" gesucht.
* Ist der Kartentyp aus den Eingangsdaten gleich „e-card", muss die e-card ID gleich der Karten ID aus den Eingangsdaten sein.
* Ist der Kartentyp aus den Eingangsdaten gleich „o-card", muss die o-card ID gleich der Karten ID aus den Eingangsdaten sein.

1. Zur so gefundenen Karte wird die Bemerkung, das Bemerkungsdatum und der Bemerkungsuser mit den Werten aus den Eingangsdaten versorgt und gespeichert.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Erlaubte Statusübergänge prüfen (OPC_CKV_FKT_004)

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Status | Zu prüfender Kartenstatus |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Auftragstyp (1..n) | Liste von Auftragstypen, die sich aus dem eingegebenen Status ableiten lassen |  | **M** |

# Beschreibung

Die möglichen Auftragstypen, die laut Chipkartenverwaltung auf den eingegebenen Status folgen können, werden ermittelt und an der Ausgangsschnittstelle bereitgestellt.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Erlaubte Statusübergänge für OPC prüfen (OPC_CKV_FKT_005)

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Status | Zu prüfender Kartenstatus |  | **M** |

# Ausgangsdaten

|  |  |  |  |  |  |  |
| --- | --- | --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** |  | **mögliche Werte** |  | **Muss / Kann** |  |
| Endstatus setzen erlaubt | Gibt an, ob der Endstatus 05 für diese Karte gesetzt werden darf | true, false |  | **M** |  |

# Beschreibung

Für den OPC wird ermittelt, welche Funktion mit dem Kartenstatus durchgeführt werden darf.

1. Die Funktion „Erlaubte Statusübergänge prüfen (OPC\_CKV\_FKT\_004)" wird mit dem Status aus den Eingangsdaten aufgerufen.
2. Wird der Auftragstyp „Endstatus setzen" geliefert, wird das Flag „Endstatus setzen erlaubt" auf true gesetzt


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Endstatus setzen (OPC_CKV_FKT_006)

# Eingangsdaten

|  |  |  |  |  |
| --- | --- | --- | --- | --- |
| **Name** | **Beschreibung** |  | **mögliche Werte** | **Muss / Kann** |
| Kartentyp | Kartentyp | e-card, o-card |  | **M** |
| Karten ID | Eindeutige Identifikation der o-card (o-card ID) oder der e-card (e-card ID), abhängig vom Kartentyp |  |  | **M** |
| Bemerkung | Bemerkung zur Karte |  |  | **M** |
| Bemerkungsdatum | Datum an dem die Bemerkung geändert wurde.   Falls nicht versorgt, aktuelles Datum |  |  | **K** |
| Bemerkungsuser | User, der die Bemerkung geändert hat |  |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Kartenstatus | Neuer Kartenstatus in der Entität „e-card" bzw. „o-card" definiert. |  | **K** |

# Beschreibung

Die Karte aus den Eingangsdaten wird auf den Endstatus gesetzt.

1. Die Funktion „Kartenstatus mit „<Status>" festlegen (OPC\_CKV\_FKT\_006)" wird mit dem Endstatus 05 und den Kartendaten aus den Eingangsdaten aufgerufen. Falls die Funktion Meldungen liefert, werden diese durchgereicht.
2. Die Funktion Bemerkung zur Karte ändern (OPC\_CKV\_FKT\_003) wird mit den Bemerkungsdaten aus den Eingangsdaten aufgerufen.


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Fotoinformation beenden (OPC_CKV_FKT_007)

# Vorbedingungen

Es wurde bereits geprüft, ob es sich um eine zu beendende Fotoinformation handelt.

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Fotoinformation | Fotoinformationsobjekt |  | **M** |
| Hinweis | Hinweis zur besseren Nachvollziebarkeit. | Freitextfeld | **K** |

# Ausgangsdaten

Keine

# Beschreibung

Falls der Aufruf über die Maske „Fotoinformation Details (OPC\_CKV\_GUI\_008)" erfolgt muss die Aktion mit der Meldung GUI-802 bestätigt werden.  
Falls ein Hinweis übergeben wurde, so wird dieser im übergebenen Fotoinformationsobjekt gesetzt.  
Das Attribut InformationsendeAm wird auf das aktuelle Datum gesetzt und der Eintrag in der Entität „Fotoinformation" historisiert (LOESCH\_KZ = 1).  
Es wird die Meldung CKV-101 ausgegeben. (An der GUI nur falls der Aufruf über die Maske „Fotoinformation Details (OPC\_CKV\_GUI\_008)" erfolgt.)

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-802 | Wollen Sie die Fotoinformation wirklich beenden? | Message | Info |
| CKV-101 | Die Fotoinformation (VSNR=<VSNR>, Typ=<Typ>, Einmeldezeitpunkt=<Einmeldezeitpunkt>) wurde gelöscht. | MessageLog | InfoInfo |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Fotoinformationen beenden (OPC_CKV_FKT_008)

# Vorbedingungen

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR-Datei | Datei mit VSNRs |  | **M** |
| Typ | Typ der Fotoinformation | „FZ" oder „FA" | **K** |
| Hinweis | Hinweis zur besseren Nachvollziebarkeit. | Freitextfeld | **K** |

# Ausgangsdaten

Keine

# Beschreibung

Mit Hilfe der Funktion „VSNRs für Fotoinformation lesen (OPC\_CKV\_FKT\_010)" wird die Liste von VSNRs ermittelt. Liefert die Funktion einen Fehler wird auch diese Funktion abgebrochen.  
Die weiteren Schritte müssen mit der Meldung GUI-803 bestätigt werden.  
Für jede VSNR werden folgende Schritte durchgeführt:

* Es wird die Funktion „Fotoinformation suchen (OPC\_CKV\_FKT\_002)" mit dem Typ laut Eingangsdaten aufgerufen.
* Falls keine Fotoinformation gefunden wurde, wird die Meldung CKV-102 gelogged und mit der nächsten VSNR fortgesetzt.
* Die Funktion „Fotoinformation beenden (OPC\_CKV\_FKT\_007)" wird mit der gefundenen Fotoinformationen und den Hinweis aus den Eingangsdaten aufgerufen.

Die Meldung CKV-103 wird ausgegeben.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-803 | Wollen Sie wirklich die Fotoinformation für <Anzahl der VSNR in Liste> VSNRs beenden? | Message | Info |
| CKV-102 | Für die VSNR <VSNR> und den Typ <Typ, falls nicht gesetzt „*alle*"> ist keine aktuelle Fotoinformation vorhanden. | Log | Info |
| CKV-103 | Für <Anzahl der durchgeführten Beendigungen> von <Anzahl VSNR insgesamt> VSNRs wurde eine Fotoinformation beendet. | MessageLog | InfoInfo |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Foto-Avisos anlegen (OPC_CKV_FKT_009)

# Vorbedingungen

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR-Datei | Datei mit VSNRs |  | **M** |
| Hinweis | Hinweis zur besseren Nachvollziebarkeit. | Freitextfeld | **K** |

# Ausgangsdaten

Keine

# Beschreibung

Mit Hilfe der Funktion „VSNRs für Fotoinformation lesen (OPC\_CKV\_FKT\_010)" wird die Liste von VSNRs ermittelt. Liefert die Funktion einen Fehler wird auch diese Funktion abgebrochen.  
Die weiteren Schritte müssen mit der Meldung GUI-804 bestätigt werden.  
Für jede VSNR werden folgende Schritte durchgeführt:

* Es wird geprüft, ob in der Entität HVB\_SVPERSON ein aktueller Eintrag für die VSNR existiert.
* Konnte keine SV-Person gefunden werden, wird die Meldung CKV-104 gelogged und mit der nächsten VSNR fortgesetzt.
* Ist für die SV-Person das Flag „Todesdatum bestätigt" gesetzt so wird die Meldung CKV-108 gelogged und mit der nächsten VSNR fortgesetzt.
* Es wird die Funktion „Fotoinformation suchen (OPC\_CKV\_FKT\_002)" mit nicht gesetzen Typ aufgerufen.
* Falls bereits eine Fotoinformation existiert, wird die Meldung CKV-105 gelogged und mit der nächsten VSNR fortgesetzt.
* Für die VSNR wird ein Fotoinformationsobjekt mit folgenden weiteren Daten erzeugt.
  + Typ=FA
  + Einmeldezeitpunkt=aktuelles Datum
  + InformationsendeAm=31.12.3999
  + Meldung durch=OP
  + Hinweis=falls vorhanden Hinweis aus den Eingangsdaten, sonst leer
* Die Fotoinformation wird in der Entitöt Foto\_Info gespeichert und die Meldung CKV-106 gelogged.

Die Meldung CKV-107 wird ausgegeben.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-804 | Wollen Sie wirklich für <Anzahl der VSNR in Liste> VSNRs ein Foto-Aviso anlegen? | Message | Info |
| CKV-104 | Die VSNR <VSNR> ist im System nicht bekannt. | Log | Info |
| CKV-105 | Es existiert bereits eine aktuelle Fotoinformation (VSNR=<VSNR>, Typ=<Typ>, Einmeldezeitpunkt=<Einmeldezeitpunkt>). | Log | Info |
| CKV-106 | Für die VSNR <VSNR> wurde ein Foto-Aviso angelegt. | Log | Info |
| CKV-107 | Für <Anzahl der angelegten Avisos> von <Anzahl VSNR insgesamt> VSNRs wurde eine Foto-Aviso angelegt. | MessageLog | InfoInfo |
| CKV-108 | Die Person mit der VSNR <VSNR> ist bereits verstorben. | Log | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### VSNRs für Fotoinformation lesen (OPC_CKV_FKT_010)

# Vorbedingungen

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR-Datei | Datei mit VSNRs |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| VSNR-Liste | Liste mit VSNRs |  | **M** |

# Beschreibung

Die übergebene Datei wird zeilenweise gelesen.  
Tritt dabei ein Fehler auf, wird die Funktion mit der Meldung CKV-110 abgebrochen.  
Falls die Datei leer ist, wird die Funktion mit der Meldung CKV-111 abgebrochen   
Für jede Zeile werden folgende Schritte durchgeführt:

* In jeder Zeile wird genau eine VSNR erwartet. Es wird daher geprüft, ob sich um eine 10-stellige Zahl handelt.
* Ist dies nicht der Falls wird die Funktion mit der Meldung CKV-112 abgebrochen.
* Falls die ermittelte VSNR bereits in der ausgehenden Liste eingetragen ist,wird die Meldung CKV-113 gelogged und mit der nächsten Zeile fortgesetzt.
* Ansonsten wird die VSNR in die ausgehende Liste aufgenommen.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| CKV-110 | Fehler beim Lesen der Datei <Dateiname>. | MessageLog | ErrorError |
| CKV-111 | Die Datei <Dateiname> enthält keine Einträge. | MessageLog | ErrorError |
| CKV-112 | Die <Zeilennummer>. Zeile der Datei <Dateiname> enthält keine VSNR. | MessageLog | ErrorError |
| CKV-113 | Die VSNR <VSNR> ist mehrfach vorhanden | Log | Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Foto-Metadaten suchen (OPC_CKV_FKT_011)

# Vorbedingungen

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| SVNR | Texteingabe der SVNR |  | **M** |

# Ausgangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Liste Register | Liste mit Registern für welche in der Tabelle FOTO\_METADATEN mit dem BPK der SVNR ein Eintrag vorhanden ist. |  | **M** |
| BPK | BPK zur Angegebenen SVNR |  | **M** |

# Beschreibung

Die übergebene SVNR wird auf Plausibilität überprüpft.  
Mit der SVNR wird die ZPV (bpk-Lesen) mit dem Bereich SV aufgerufen. Falls das ZPV einen Fehler liefert wird mit der Meldung GUI-806 abgebrochen.  
Ansonsten werden alle nicht gelöschten Einträge in FOTO\_METADATEN mit dem zurückgelieferten BPK und allen möglichen Registern gesucht. Die gefundenen Einträge (BPK, Register) werden zurückgegeben.  
Falls keine Metadaten eingetragen sind, wird mit der Meldung GUI-807 abgebrochen.  
Tritt dabei ein allgemeiner Fehler auf, wird die Funktion mit der Meldung GUI-805 abgebrochen.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-805 | Ein Unbekannter Fehler ist aufgetreten. Bitte erneut versuchen. | Message | Error |
| GUI-806 | ZPV liefert Fehler <Fehlernummer ZPV> - „<Fehlerbeschreibung ZPV>". | Message | Error |
| GUI-807 | Es wurden keine Metadaten zur angegebenen SVNR gefunden. | Message | Error |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Foto-Metadaten löschen (OPC_CKV_FKT_012)

# Vorbedingungen

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Liste Register | Ausgewählt durch Checkboxen |  | **M** |
| BPK | Text |  | **M** |

# Ausgangsdaten

# Beschreibung

Wenn die Liste der Ausgewählten Register leer ist, dann wird mit der Fehlermeldung GUI-808 abgebrochen.  
Ansonsten werden alle nicht gelöschten Einträge in FOTO\_METADATEN der entsprechenden BPK und den ausgewählten Registern gelöscht. Die User-ID wird entsprechend angepasst. Die Historisierung nach HM13 wird dabei berücksichtigt  
Es wird die Erfolgsmeldung CKV-114 ausgegeben.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-808 | Wähle zumindest ein Register aus um die Metadaten zu löschen. | Message | Error |
| CKV-114 | Die Foto-Metadaten für SVNR <SVNR> wurde in den Registern <Liste Register> gelöscht. | MessageLog | Info   Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### ASWH-Verwaltung - Funktionen




> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Herstellerdaten importieren (OPC_AHV_FKT_001)

# Vorbedingungen

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Datei | Datei mit Herstellerdaten |  | **M** |

# Ausgangsdaten

keine

# Beschreibung

Die übergebene Datei wird im Format UTF8 zeilenweise gelesen.  
Tritt dabei ein Fehler auf, wird die Funktion mit der Meldung AHV-001 abgebrochen.  
Die erste Zeile der Datei muss dem definierten Header entsprechen.  
Hersteller-ID;Firmenname;Ablaufdatum;gesperrt.  
Ist dies nicht der Fall, wird die Funktion mit dem Fehler AHV-002 abgebrochen.  
Danach werden die weiteren Zeilen gelesen und folgende Prüfungen durchgeführt.   
Die Anzahl der Datenfelder (getrennt durch  muss 4 sein. Im Fehlerfall Abbruch mit AHV-003  
Für die einzelnen Felder werden folgende Prüfungen durchgeführt. Im Fehlerfall Abbruch mit AHV-004.   
Vor den Prüfungen werden führende und nachfolgende Whitespace entfernt (trim), bis auf die Hersteller-Id dürfen die Felder nicht leer sein.

* Hersteller-ID: leer oder eine 10-stellige alphanumerische Zeichenkette ohne die „leicht verwechselbaren" Zeichen „0", „O", „I", „l" *d.h. es stehen 58 Zeichen zur Verfügung: 9 Ziffern (1-9), 24 Grossbuchstaben(A-H, J-N, P-Z), 25 Kleinbuchstaben* (a-k, m-z)Falls das Feld nicht leer ist, wird es auf Eindeutigkeit geprüft. Ist die ID mehrfach vorhanden, so wird mit dem Fehler AHV-005 abgebrochen.
* Firmenname: beliebige Zeichenkette, Länge kleiner 100
* Ablaufdatum: gültiges Datum im Format „JJJJMMTT", kann auch in Vergangenheit liegen
* gesperrt: „J" oder „N"

Die Liste der gültigen eingelesenen Hersteller-IDs wird gegen die derzeit aktiven (d.h. nicht gesperrten) Hersteller-IDs aus der Entität „ASWH" geprüft. Falls einer der aktiven Einträge nicht in der Liste der eingelesenen Datensätze enthalten ist, wird die Funktion mit der Meldung SVH-007 abgebrochen.  
*Hinweis: Der Partnersupport sollte immer eine komplette Liste der Hersteller liefern. Bereits gesperrt hersteller müssen jedoch nicht enthalten sein.*  
Der Benutzer muss die weiteren Schritte mit der Meldung GUI-901 bestätigen. Ansonsten wird die Funktion abgebrochen.  
Die geprüften Daten werden folgendermassen in die Entität „ASWH" übernommen.

* Falls die Hersteller-ID leer ist, wird aus den zur Verfügung stehenden Zeichen eine zufällig noch nicht existierende ID generiert und diese mit den anderen Werten eigetragen.

Ansonsten wird geprüft, ob die Hersteller-ID bereits in der Entität vorhanden ist.

* Ist dies nicht der Fall, so wird ein neuer Datensatz mit den übergebenen Werten angelegt.

Ist die Hersteller-ID bereits vorhanden, so wird geprüft, ob sich die übergebenen Werte von den bereits eingetragenen Werten unterscheiden.

* Falls ja, werden die Daten aktualisiert.
* Falls nein werden keine DB-Aktualisierungen vorgenommen.

Zum Abschluss wird die Meldung AHV-006 (dabei wird auch die Ursache eines Neueintrag/einer Änderung protokolliert) ausgegeben und die Funktion beendet.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| GUI-901 | Wollen Sie wirklich für <Anzahl der Hersteller in Datei> Hersteller die Daten aktualisieren? | Message | Info |
| AHV-001 | Fehler beim Lesen der Datei <Dateiname>. | MessageLog | ErrorError |
| AHV-002 | Die Datei <Dateiname> enthält nicht den erwarteten Header. | MessageLog | ErrorError |
| AHV-003 | Die <Zeilennummer>. Zeile der Datei <Dateiname> enthält eine ungültige Anzahl von Feldern | MessageLog | ErrorError |
| AHV-004 | Die <Zeilennummer>. Zeile der Datei<Dateiname> enthält im Feld <Feldname> einen ungültigen Eintrag | MessageLog | ErrorError |
| AHV-005 | Die Datei<Dateiname> enthält die Hersteller-ID <Hersteller-ID> mehrmals. | MessageLog | ErrorError |
| AHV-006 | Die Daten der Datei <Dateiname> wurden erfolgreich importiert.   (Datensätze gesamt: <Anzahl>, davon neu generierte IDs: <Anzahl>, neu übernommene IDs: <Anzahl>, gesperrte IDs: <Anzahl>. entsperrte IDs: <Anzahl>, geänderte Firmennamen: <Anzahl>, unveränderte Datensätze: <Anzahl>) | MessageLog | InfoInfo |
| AHV-007 | Die Datei <Dateiname> enthält nicht alle aktiven Hersteller.   (<Lister der nicht enthaltenen Hersteller-IDs durch Beistrich getrennt, maximal 10 Einträge>) | MessageLog | ErrorError |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


#### Herstellerdaten exportieren (OPC_AHV_FKT_002)

# Vorbedingungen

# Eingangsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **mögliche Werte** | **Muss / Kann** |
| Datei | Datei mit Herstellerdaten |  | **M** |

# Ausgangsdaten

keine

# Beschreibung

Aus der Entität „ASWH" werden alle Daten ermittelt.  
Falls keine Daten zur Verfügung stehen, wird mit der Fehlermeldung AHV-011 abgebrochen,  
Eine Ausgabedatei mit dem Header Hersteller-ID;Firmenname;Ablaufdatum;gesperrt und den ermittelten Daten wird erzeugt.  
Die Datei wird mit dem Dateinamen aus den Eingangsdaten für den Download zur Verfügung gestellt.  
Zum Abschluss wird die Meldung AHV-012 ausgegeben und die Funktion beendet.

# Meldungen

|  |  |  |  |
| --- | --- | --- | --- |
| **MeldungsNr** | **Meldungstext** | **Typ** | **Level** |
| AHV-011 | Es stehen keine Herstellerdaten für den Exportzur Verfügung. | MessageLog | ErrorError |
| AHV-012 | In der Datei <Dateiname> wurden <Anzahl> Hersteller exportiert. | MessageLog | Info Info |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


## OPC - Parameter und Parameterguppierungen

*Hinweis: Hier sind nur Parameter bzw. Parametergruppen angeführt, die die GINO Verwaltung betreffen.*

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **Länge** |
| GINO-Anschluss-ID | Künstlicher Schlüssel eines Anschlusses | Long |  |
| GINO-Firmware-ID | Künstlicher Schlüssel einer Firmware | Long |  |
| GINO-Firmware Zuordnung-ID | Künstlicher Schlüssel der Zuordnung einer GINO-Gruppe zu einer Firmware | Long |  |
| GINO-Script-Task-Zuordnung-ID | Künstlicher Schlüssel der Zuordnung einer GINO-Gruppe zu einem Script-Task | Long |  |
| GINO-Gruppentyp | Typ der GINO-Gruppe.   Mögliche Werte:   * KONFIGURATION * FIRMWARE * TASK | String |  |
| GINO-Gruppen-Mitgliedertyp | Typ der GINO-Gruppen Mitglieder.   Mögliche Werte:   * SPEZIELL * DYNAMSICH * ALLGEMEIN |  |  |
| GINO-Gruppen-ID | Künstlicher Schlüssel einer GINO-Gruppe | Long |  |
| GINO-Gruppen-Name | Name der GINO-Gruppe | String | 30 |
| GINO-Firmware Version | Version der GINO Software im Format   dd.dd.dd (‚d' ist jeweils eine dezimale Ziffer)   Hinweis: zu einer Software Version kann es mehre Hardware Versionen geben. | String | 8 |
| GINO-Hardware Version | Version der GINO Hardware im Format dd.dd | String | 5 |
| GINO-Resourcetyp | Typ der GINO Resource. Mögliche Werte:   * alive * fwcheck * event * tls-certs-client * tls-certs-server * cvc-certs * local-config * server-config * diagnosis * statistic * logs * maintenance * scriptcheck * scriptresult | String |  |
| GINO-Script-ID | Künstlicher Schlüssel eines GINO-Scripts | Long |  |
| GINO-Seriennummer | Eindeutige Seriennummer eines GINOs | String | 50 |
| GINO-Task-ID | Künstlicher Schlüssel eines GINO-Tasks | Long |  |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Parametergruppe GINO-Gruppe

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| ID | Siehe „GINO-Gruppen-ID". |  | M |
| Name | Siehe „GINO-Gruppen-Name". |  | M |
| Anzahl Gruppenmitglieder | Anzahl der GINOs die der Gruppe zugeordnet sind |  | B\* |
| Gruppen-Mitglieder-Typ | Siehe „GINO-Gruppen-Mitgliedertyp" |  |  |
| Start | Zeitstempel, ab dem die Gruppe gültig ist.   Hinweis: das Attribut ist nicht das Historisierungsattribut nach HM3 | Date | M |
| Ende | Zeitstempel, bis zu dem dem Gruppe gültig ist   Hinweis: das Attribut ist nicht das Historisierungsattribut nach HM3 | Date | M |

*\*B: ist der Typ der GINO-Konfiguration „ALLGEMEIN", so wird die Anzahl nicht versorgt, ansonsten ist das Feld ein Pflichtfeld*


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Parametergruppe GINO-Konfiguration-Parameter

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| ID | Eindeutiger Identifier des Konfigurations-Parameters | Long | K |
| Name | Name der Konfigurations-Parameters | String(100) | M |
| Wert | Wert des Konfigurations-Parameters | String(1000) | M |
| Gruppen-ID | Siehe „GINO-Gruppen-ID". |  | K |
| Default | Flag, gibt an ob der Wert ein Defaultwert ist | Boolean | M |
| Remove | Flag, das angibt, ob der Parameter zum GINO übertragen werden soll.   *Hinweis: Es soll z.B. ein Parameter, der in der allgemeinen Gruppe definiert ist, in der dynamischen Gruppe nicht an den GINO übertragen werden, dann hat der Parameter den Wert „true". In diesem Fall haben „Wert" und „Default" keine Bedeutung.* | Boolean | M |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Parametergruppe GINO-Gruppen-Mitglied

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| ID | Eindeutiger Identifier des GINO-Gruppen-Mitglieds | Long | K |
| Anschluss-ID | Eindeutiger Identifier des Anschlusses | Long | K |
| Anschluss-IP | Client-IP des Anschlusses Adresse | String | M |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Parametergruppe GINO-Firmware

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| ID | Eindeutiger Identifier der GINO-Firmware | Long | K |
| GINO-Firmware Version | Siehe „GINO-Firmware Version". |  | M |
| GINO-Hardware Version | Siehe „GINO-Hardware Version" |  | M |
| Letzte kompatible Version | Letzte kompatible GINO-Firmware Version, siehe „GINO-Firmware Version" |  | K |
| Letzte Änderung | Zeitstempel der letzten Änderung | Date | M |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Parametergruppe GINO-Produktionsdaten

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| Seriennummer | Siehe „GINO-Seriennummer" |  | M |
| Gesperrt | True wenn der GINO gesperrt ist | Boolean | M |
| Sperrdatum | Datum, zu dem GNO gesperrt wurde | Date | K |
| User-ID | ID des Benutzers, der die letzte Änderung durchgeführt hat | Long | M |
| Letzte Änderung | Zeitstempel der letzten Änderung | Date | M |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Parametergruppe GINO-Task

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| ID | Siehe „GINO-Task-ID" | Long | K |
| Call | Typ der GINO-Resource die in der Antwort eines ALIVE Requests durch den Parameter call zurückgegeben wird, siehe „GINO-Resourcetyp"   Folgende Resourcen können nicht zurückgemeldet werden:   * alive * event * maintenance * scriptresult |  | M |
| Uhrzeit-ab | Uhrzeit, ab der der Task ausgeführt werden kann. |  | M |
| Uhrzeit-bis | Uhrzeit, bis der der Task ausgeführt werden kann. |  | M |
| Intervall | Zeitangabe in Stunden: die eingetragene Resource wird nur dann zurückgegeben, falls die Resource in den letzten x Stunden nicht ausgeführt wurde.   Falls kein Wert eingetragen ist, wird die Resource nur dann zurückkgeben, wenn sie seit dem Startzeitpunkt der Gruppe nicht ausgeführt wurde. | INT | K |

*\*B: muss gesetzt sein falls Repeat=true*


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Parametergruppe GINO-Script-Task

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


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Parametergruppe GINO-Script

|  |  |  |  |
| --- | --- | --- | --- |
| **Name** | **Beschreibung** | **Datentyp** | **K/M/B** |
| Script-ID | Siehe „GINO-Script-ID" |  | M |
| Name | Scriptname | String(1000) | M |
| Signatur | Signatur vom Hochladen des Scripts | BLOB | K |


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


### Parametergruppe GINO-Resource Status

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


> *[⬆️ Zurück nach oben](#inhaltsverzeichnis)*


---
*Dieses Dokument wurde automatisch aus Confluence exportiert und synchronisiert.*  
*Datum: 2026-10-23 | Autor: burlmi | Quelle: [Confluence Root Page](https://confluence.tools.svc.co.at/pages/viewpage.action?pageId=193679716)*
