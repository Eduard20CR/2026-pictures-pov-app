# 0002. npm sin workspaces

- **Estado:** Aceptado
- **Fecha:** 2026-10-08

## Contexto y planteamiento del problema

El repositorio estaba previsto como un monorepo gestionado con pnpm y workspaces, con un paquete `packages/shared` que contendría los tipos generados desde el contrato OpenAPI para compartirlos entre el frontend y el backend.

A raíz de la [decisión 0001](0001-backend-spring-boot-en-lambda.md), el backend pasa a desarrollarse con Spring Boot y se construye con Gradle. En consecuencia, `apps/web` queda como el único proyecto JavaScript del repositorio y el backend ya no consume tipos de TypeScript.

## Factores de decisión

- Número de proyectos JavaScript en el repositorio.
- Simplicidad de la configuración local y de CI.
- Mantenimiento de un único punto de generación de los tipos del cliente de la API.

## Opciones consideradas

1. Mantener pnpm con workspaces y el paquete `packages/shared`.
2. Usar npm sin workspaces y generar los tipos dentro de `apps/web`.

## Decisión

Se elige la **opción 2: npm sin workspaces**.

- `apps/web` se gestiona con npm.
- Se elimina el paquete `packages/shared`.
- Los tipos del cliente de la API se generan con openapi-typescript a partir del contrato OpenAPI y se ubican en `apps/web/src/api/`.

## Ventajas y desventajas de las opciones

### Opción 1: pnpm con workspaces y `packages/shared`

- A favor: facilita compartir código si en el futuro se incorporan más proyectos JavaScript.
- En contra: añade una herramienta y una configuración de workspaces que no aportan valor con un único proyecto JavaScript; el paquete compartido no tiene más consumidor que el frontend.

### Opción 2: npm sin workspaces

- A favor: utiliza el gestor incluido con Node.js, sin instalación adicional; reduce la configuración del repositorio y de CI; los tipos se ubican junto al único código que los consume.
- En contra: si en el futuro se añade otro proyecto JavaScript que necesite los mismos tipos, será necesario reconsiderar la estructura.

## Consecuencias

### Positivas

- La configuración del repositorio y del pipeline de CI para el frontend es más sencilla.
- Los tipos de la API se mantienen junto al código que los utiliza.

### Negativas y riesgos

- Los tipos generados en `apps/web/src/api/` deben regenerarse cada vez que cambie el contrato OpenAPI producido por el backend; el proceso de CI debe detectar desfases entre ambos.
- La incorporación de un nuevo proyecto JavaScript obligaría a revisar esta decisión.

## Decisiones relacionadas

- [0001. Backend con Spring Boot en AWS Lambda](0001-backend-spring-boot-en-lambda.md)
