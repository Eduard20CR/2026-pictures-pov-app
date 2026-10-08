# Registros de decisiones de arquitectura (ADR)

Este directorio recoge las decisiones de arquitectura relevantes del proyecto en formato [MADR](https://adr.github.io/madr/). Cada registro documenta el contexto, las opciones consideradas, la decisión adoptada y sus consecuencias.

## Índice

| Número | Título | Estado | Fecha |
|---|---|---|---|
| [0001](0001-backend-spring-boot-en-lambda.md) | Backend con Spring Boot en AWS Lambda | Aceptado | 2026-10-08 |
| [0002](0002-npm-sin-workspaces.md) | npm sin workspaces | Aceptado | 2026-10-08 |

## Convenciones

- **Nombre del archivo.** `NNNN-titulo-en-minusculas-con-guiones.md`, con numeración correlativa de cuatro dígitos. Un número no se reutiliza.
- **Estados.** Propuesto, Aceptado, Rechazado, Obsoleto o Reemplazado por `NNNN`.
- **Inmutabilidad.** Un ADR aceptado no se modifica en lo sustancial. Si la decisión cambia, se crea un nuevo ADR y el anterior pasa a estado *Reemplazado por `NNNN`*.
- **Fuente única.** La justificación de cada decisión reside únicamente en su ADR; el resto de la documentación enlaza a él.

## Plantilla

```markdown
# NNNN. Título de la decisión

- **Estado:** Propuesto | Aceptado | Rechazado | Obsoleto | Reemplazado por NNNN
- **Fecha:** AAAA-MM-DD

## Contexto y planteamiento del problema

Situación que motiva la decisión y restricciones aplicables.

## Factores de decisión

- Factor 1.
- Factor 2.

## Opciones consideradas

1. Opción A.
2. Opción B.

## Decisión

Opción elegida y motivo principal.

## Ventajas y desventajas de las opciones

### Opción A

- A favor: …
- En contra: …

## Consecuencias

### Positivas

- …

### Negativas y riesgos

- …
```
