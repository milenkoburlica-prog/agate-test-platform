## Demo

Agate AI can generate REST API tests from any OpenAPI specification.

Example:

Swagger Petstore OpenAPI:

https://petstore3.swagger.io/api/v3/openapi.yaml

Run:

java -jar agate-ai.jar \
  --openapi https://petstore3.swagger.io/api/v3/openapi.yaml



## Flow

OpenAPI Spec
      |
      v
OpenAPI Parser
      |
      v
Endpoint Metadata
      |
      v
Ollama / LLM
      |
      v
AGATE YAML Test Suite
