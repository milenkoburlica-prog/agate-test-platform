# Adressaten ermitteln (OPC_MES_FKT_001)

# \_Toc172190299Beschreibung

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

# \_Toc172190300Eingangsdaten

| **Name** | **Beschreibung** | **Mögliche Werte** | **Muss/ Kann** |
| --- | --- | --- | --- |
| Filter Vertragspartnernummer | Liste mit Vertragspartnernummern |  | K |
| Filter Zuname | Liste mit Zunamen von Vertragspartnern |  | K |

# \_Toc172190301Ausgangsdaten

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

# \_Toc172190302Meldungen / Ausnahmen

|  |
| --- |
| **Ausnahmen** |
| Zu den angegebenen Filterkriterien stehen keine Adressaten zur Verfügung. |