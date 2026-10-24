# AGATE AI Troubleshooter

**AI-powered Test Failure Analysis & Troubleshooting for AGATE**

## Überblick

Der **AGATE AI Troubleshooter** ist ein KI-gestützter Diagnoseassistent zur Analyse fehlgeschlagener Testausführungen innerhalb der AGATE Testplattform.

Ziel ist es, Testfehler schneller zu verstehen, mögliche Ursachen zu identifizieren und konkrete, überprüfbare Korrekturvorschläge bereitzustellen.

Der Assistent kombiniert die deterministische Analyse von AGATE-Ausführungsprotokollen mit der Unterstützung durch Large Language Models (LLMs).

## Funktionsumfang

- **Automatische Fehleranalyse:** Erkennung fehlgeschlagener Testschritte und relevanter Fehlermeldungen.
- **Multi-Engine-Unterstützung:** Analyse von SOAP, REST, SQL, GUI, CMD, OpenShift und BUFFER.
- **Root Cause Investigation:** Untersuchung von Fehlerursachen anhand tatsächlicher Testausführungen und vorhandener Evidenz.
- **Intelligente Korrekturvorschläge:** Vorschläge zur Fehlerbehebung mit nachvollziehbaren Begründungen und, soweit möglich, technischer Verifikation.
- **Evidence Explorer:** Anzeige relevanter Testschritte und direkter Zugriff auf die entsprechenden Stellen im Originalprotokoll.
- **AI Chat:** Interaktive Fehleranalyse durch Datei-Upload oder direktes Einfügen von AGATE-Protokollen.

## Architektur

Der AGATE AI Troubleshooter basiert auf **Java 21 und Quarkus**.

Die Fehleranalyse erfolgt über spezialisierte Java-Diagnosekomponenten. Für zusätzliche Interpretation und Korrekturvorschläge wird ein LLM über die GroqCloud API eingebunden.

Die Architektur ist darauf ausgelegt, künftig weitere LLM-Anbieter und Diagnosewerkzeuge integrieren zu können.

## Grundprinzip

**Find the failure → Analyze the evidence → Explain the cause → Propose a fix → Verify the correction**

Der Assistent führt keine eigenständigen Änderungen an AGATE-Tests oder produktiven Systemen durch. Vorgeschlagene Korrekturen müssen fachlich und technisch geprüft werden.

## Projektinformationen

- **Projekt:** `agate-ai-troubleshooter-agent`
- **Technologie:** Java 21, Quarkus
- **LLM-Integration:** GroqCloud
- **Benutzeroberfläche:** Webbasierter AI Chat
- **Einsatzgebiet:** AGATE Testdiagnose und Troubleshooting

## Lokaler Start

```bash
mvn clean test
mvn quarkus:dev -Ddebug=5091
```

**Weboberfläche:** http://127.0.0.1:8091/