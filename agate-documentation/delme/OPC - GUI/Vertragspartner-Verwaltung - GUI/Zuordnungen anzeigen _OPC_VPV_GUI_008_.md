# Zuordnungen anzeigen (OPC_VPV_GUI_008)

Anzeigename: Zuordnungen anzeigen \[Rechte|Verträge|Rollen|Sperren|Piloten\]
Anzeige der Zuordnungen aus Sicht der gewählten Ordination.
Die folgenden Aktionen sind aktivierbar:

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