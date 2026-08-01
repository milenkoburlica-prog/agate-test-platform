package at.co.svc.open.api.spec.generator;

import java.util.List;

import at.co.svc.open.api.spec.model.EndpointDescription;
import at.co.svc.open.api.spec.model.GeneratedTestCase;

public class AgatePromptBuilder {

    public static String buildPrompt(EndpointDescription endpoint, List<GeneratedTestCase> rawTestCases) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Ti si ekspert za testiranje koji generiše YAML test suite za Agate Test Studio.\n");
        prompt.append("Moraš da vratiš ISKLJUČIVO validan YAML fajl, bez ikakvih dodatnih objašnjenja, uvodnih reči, komentara van YAML-a ili markdown ograda (kao što su ```yaml).\n\n");

        prompt.append("### KRITIČNA PRAVILA:\n");
        prompt.append("1. ZA SVAKI test slučaj iz dolenavedene liste 'ZADATAK', moraš generisati poseban test blok u YAML-u.\n");
        prompt.append("2. Polje `id` u svakom test slučaju MORA biti tačno preuzeto iz 'Naziv' polja tog test slučaja (npr. ako je naziv 'Verify_get_clv_svsc_alive_success', `id` mora biti identičan).\n");
        prompt.append("3. Polje `description` mora odgovarati 'Opis' polju iz zadatka.\n");
        prompt.append("4. Vrednost za očekivani rezultat (npr. HTTP 200, HTTP 503) mora se preslikati u `expected` polje unutar `ASSERT` koraka za STATUS.\n\n");

        prompt.append("### PRAVILA ZA AGATE REST DSL:\n");
        prompt.append("- Svaki test fajl počinje sa korenskim elementom `testCases:`\n");
        prompt.append("- Svaki test slučaj u listi obavezno sadrži: `id`, `description`, `stage: '*'`, `priority: HIGH`, `variables`, i listu `steps`.\n");
        prompt.append("- Koristi `type: REST` za HTTP pozive.\n");
        prompt.append("- Operacije (`op`): \n");
        prompt.append("  * `EXEC`: Za slanje zahteva. Obavezno sadrži `command: rest.<ime_modula>`, `endpoint: \"{B[api.endpoint]}\"`, i `response: ime_odgovora`.\n");
        prompt.append("  * `ASSERT`: Za proveru statusa. Koristi `response`, `source: STATUS`, `action: EQUALS`, i `expected`.\n\n");

        prompt.append("### PRIMER ISPRAVNOG YAML-a:\n");
        prompt.append("testCases:\n");
        prompt.append("  - id: Verify_get_clv_svsc_alive_success\n");
        prompt.append("    description: Verify that service alive endpoint is available\n");
        prompt.append("    stage: '*'\n");
        prompt.append("    priority: HIGH\n");
        prompt.append("    variables:\n");
        prompt.append("      api.endpoint: \"[https://api.example.com](https://api.example.com)\"\n");
        prompt.append("    steps:\n");
        prompt.append("      - type: REST\n");
        prompt.append("        op: EXEC\n");
        prompt.append("        command: rest.get__clv_svsc_alive\n");
        prompt.append("        endpoint: \"{B[api.endpoint]}\"\n");
        prompt.append("        response: response_1\n");
        prompt.append("      - type: REST\n");
        prompt.append("        op: ASSERT\n");
        prompt.append("        response: response_1\n");
        prompt.append("        source: STATUS\n");
        prompt.append("        action: EQUALS\n");
        prompt.append("        expected: 200\n\n");

        prompt.append("### ZADATAK:\n");
        prompt.append("Generiši Agate YAML test suite za sledeći endpoint:\n");
        prompt.append("- Putanja: " + endpoint.getPath() + "\n");
        prompt.append("- Metod: " + endpoint.getMethod() + "\n");
        prompt.append("Pretvori sledeće test slučajeve u YAML korake prateći gorenavedena pravila:\n");

        for (GeneratedTestCase tc : rawTestCases) {
            prompt.append("  * Naziv: " + tc.getName() + " | Opis: " + tc.getDescription() + " | Očekivano: " + tc.getExpectedResult() + "\n");
        }

        return prompt.toString();
    }
}