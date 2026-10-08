# 0001. Backend con Spring Boot en AWS Lambda

- **Estado:** Aceptado
- **Fecha:** 2026-10-08

## Contexto y planteamiento del problema

El backend de la aplicación (`apps/api`) estaba previsto con NestJS ejecutado en AWS Lambda detrás de Amazon API Gateway (HTTP API). Se ha decidido reconsiderar el framework del backend, ya que uno de los objetivos del proyecto es que la experiencia adquirida tenga el mayor valor posible en el mercado laboral.

La nueva solución debe respetar las restricciones vigentes de la arquitectura:

- El costo de infraestructura de cada entorno sin tráfico no debe superar los 25 USD mensuales (RNF-COS-01).
- Las operaciones de la API deben responder en menos de 500 ms (p95) y en menos de 1,5 s (p99) (RNF-REN-04).
- El contrato de la API debe documentarse en formato OpenAPI y generarse a partir del código (RNF-OPE-06).

## Factores de decisión

- Demanda laboral de la tecnología en Costa Rica y a nivel internacional.
- Costo en reposo compatible con RNF-COS-01.
- Latencia de arranque en frío compatible con RNF-REN-04.
- Complejidad del proceso de compilación y despliegue.
- Generación del contrato OpenAPI a partir del código.

## Opciones consideradas

1. Mantener NestJS en AWS Lambda.
2. Spring Boot 3 en AWS Lambda con SnapStart.
3. Spring Boot 3 en AWS Fargate detrás de un Application Load Balancer.
4. Spring Boot 3 compilado como imagen nativa con GraalVM en AWS Lambda.

## Decisión

Se elige la **opción 2: Spring Boot 3 con Java 21 en AWS Lambda**, detrás de Amazon API Gateway (HTTP API).

- La integración entre Lambda y Spring se realiza con `aws-serverless-java-container-springboot3`.
- Se activa Lambda SnapStart para reducir el tiempo de arranque en frío.
- `apps/api` se construye con Gradle.
- El contrato OpenAPI se genera a partir del código con springdoc-openapi.

El motivo principal es que Spring Boot tiene una demanda laboral considerablemente mayor que NestJS, tanto en Costa Rica como a nivel internacional. La ejecución en Lambda conserva el modelo de pago por uso y, por tanto, el cumplimiento de RNF-COS-01.

## Ventajas y desventajas de las opciones

### Opción 1: NestJS en AWS Lambda

- A favor: no requiere cambios en la arquitectura y comparte lenguaje con el frontend.
- En contra: menor demanda laboral que Spring Boot, que es el factor determinante de esta decisión.

### Opción 2: Spring Boot 3 en AWS Lambda con SnapStart

- A favor: alta demanda laboral; costo nulo en reposo; SnapStart mitiga el arranque en frío de la JVM; springdoc-openapi cubre RNF-OPE-06.
- En contra: requiere gestionar correctamente el ciclo de vida de SnapStart, más memoria por función y un segundo toolchain en CI.

### Opción 3: Spring Boot 3 en AWS Fargate con Application Load Balancer

- A favor: modelo de ejecución convencional, sin arranques en frío ni restricciones propias de Lambda.
- En contra: el entorno cuesta aproximadamente 25 USD mensuales solo por permanecer encendido, por lo que incumple RNF-COS-01.

### Opción 4: Spring Boot 3 con imagen nativa GraalVM en AWS Lambda

- A favor: arranque muy rápido y menor consumo de memoria.
- En contra: el proceso de compilación es complejo, lento y exige configuración adicional para reflexión y proxies dinámicos.

## Consecuencias

### Positivas

- El backend se desarrolla con una tecnología de alta demanda laboral.
- Se mantiene una arquitectura sin servidores con costo nulo en reposo, conforme a RNF-COS-01.
- El contrato OpenAPI se genera a partir del código, conforme a RNF-OPE-06.

### Negativas y riesgos

- **Conexiones tras la restauración de SnapStart.** Las conexiones a PostgreSQL no deben abrirse durante la inicialización capturada en el snapshot, sino después de su restauración. De lo contrario, las instancias restauradas reutilizarían conexiones inválidas o compartidas.
- **Memoria de la función.** La función Lambda de la API requiere entre 1 y 2 GB de memoria, lo que incrementa el costo por invocación respecto a un runtime de Node.js.
- **Dos toolchains en CI.** El pipeline de integración continua debe gestionar Java 21 con Gradle para `apps/api` y Node.js con npm para `apps/web`.

## Decisiones relacionadas

- [0002. npm sin workspaces](0002-npm-sin-workspaces.md)
