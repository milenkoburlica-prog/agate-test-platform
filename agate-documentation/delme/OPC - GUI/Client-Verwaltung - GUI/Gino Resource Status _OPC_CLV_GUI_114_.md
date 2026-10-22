# Gino Resource Status (OPC_CLV_GUI_114)

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