AGATE OpenAPI – Quick How-To
`startOpenAPI.bat` is the command-line entry point for the AGATE OpenAPI tools.
Build
Build the project before using the CLI:
```bat
mvn clean package -DskipTests
```
Usage
```bat
startOpenAPI.bat COMMAND [arguments]
```
Examples
Show the OpenAPI model
```bat
startOpenAPI.bat model resources\petstore3\openapi.yaml
```
Show the OpenAPI model as JSON
```bat
startOpenAPI.bat model-json resources\petstore3\openapi.yaml
```
Phase 1 – Build the operation/request model
```bat
startOpenAPI.bat phase1 resources\petstore3\openapi.yaml GET "/pet/{petId}" --path-param petId=1
```
Phase 2 – Generate deterministic test cases
```bat
startOpenAPI.bat phase2 resources\petstore3\openapi.yaml GET "/pet/{petId}"
```
Phase 3 – Build the executable AGATE test plan
```bat
startOpenAPI.bat phase3 resources\petstore3\openapi.yaml GET "/pet/{petId}"
```
List generated tests
```bat
startOpenAPI.bat list resources\petstore3\openapi.yaml GET "/pet/{petId}"
```
Show one generated test
First use `list` to get the technical test name, then:
```bat
startOpenAPI.bat test TC_GET_PET_VALID resources\petstore3\openapi.yaml GET "/pet/{petId}"
```
Generate AGATE DSL for one test
```bat
startOpenAPI.bat dsl TC_GET_PET_VALID resources\petstore3\openapi.yaml GET "/pet/{petId}"
```
Generate CSV test data
```bat
startOpenAPI.bat csv resources\petstore3\openapi.yaml POST /pet
```
Generate an AGATE YAML template
```bat
startOpenAPI.bat yaml resources\petstore3\openapi.yaml POST /pet
```
Generate a complete AGATE application
This processes all operations in the OpenAPI document:
```bat
startOpenAPI.bat generate petstore3 resources\petstore3\openapi.yaml
```
Compare two OpenAPI contracts
```bat
startOpenAPI.bat changes resources\v1\openapi.yaml resources\v2\openapi.yaml
```
Analyze the impact of API changes
```bat
startOpenAPI.bat impact resources\v1\openapi.yaml resources\v2\openapi.yaml data\petstore3
```
Help
```bat
startOpenAPI.bat help
```