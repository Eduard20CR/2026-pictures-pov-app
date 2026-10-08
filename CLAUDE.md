# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Estado del repositorio

Pictures POV App: aplicación web donde los invitados de un evento suben fotos a una galería común escaneando un QR, sin crear cuenta; los organizadores las moderan y descargan en ZIP.

El proyecto está en fase de documentación. `apps/` e `infra/` existen pero están vacíos: no hay código, build, lint ni tests todavía. No inventes comandos; cuando se añada código, documenta aquí los reales.

## Arquitectura prevista

El panorama completo (diagrama, tabla del stack, árbol de carpetas) está en `README.md`. Puntos que requieren cruzar varios documentos:

- **Dos toolchains independientes, sin workspaces.** `apps/api` es Spring Boot 3 + Java 21 con Gradle; `apps/web` es React + Vite + TypeScript con npm. No hay `packages/shared` ni pnpm.
- **El contrato OpenAPI es la frontera entre ambos.** El backend lo genera desde el código con springdoc-openapi (RNF-OPE-06) y el frontend genera sus tipos con openapi-typescript en `apps/web/src/api/`. Un cambio en la API implica regenerar esos tipos.
- **Backend en AWS Lambda con SnapStart** detrás de API Gateway HTTP API (vía `aws-serverless-java-container-springboot3`). Las conexiones a PostgreSQL deben abrirse después de restaurar el snapshot, nunca durante la inicialización capturada.
- **Las fotos no pasan por el backend.** El navegador sube directo a S3 con URLs prefirmadas; una Lambda disparada por S3 genera miniaturas WebP. Fotos y ZIP solo se exponen con enlaces firmados temporales (RNF-SEG-05).
- **Restricción de costo dominante:** cada entorno sin tráfico debe costar ≤ 25 USD/mes (RNF-COS-01). Descarta servicios con costo fijo en reposo (p. ej. Fargate + ALB).
- Infraestructura con Terraform, entornos dev/prod en cuentas AWS separadas; CI/CD con GitHub Actions + OIDC.

## Documentación

Toda la documentación está en español, con tono formal.

- `docs/01_requisitos_funcionales.md` (`RF-<ÁREA>-<NN>`) y `docs/02_requisitos_no_funcionales.md` (`RNF-<CATEGORÍA>-<NN>`). Los identificadores son estables: no se reutilizan y los eliminados se marcan *Retirado*. Prioridad MoSCoW (M/S/C/W).
- Cada documento de requisitos tiene tabla de metadatos (versión, estado, fecha) e historial de cambios: al modificarlo, sube la versión y añade una fila al historial.
- `docs/adr/` usa MADR con numeración `NNNN-titulo.md`; el índice, las convenciones y la plantilla están en `docs/adr/README.md`. Al crear un ADR, actualiza ese índice.
- **La justificación de una decisión vive solo en su ADR**; README y demás documentos enlazan a él en vez de repetirla. Un ADR aceptado no se reescribe: se crea uno nuevo que lo reemplaza.
- `docs/c4_model.excalidraw` es el diagrama C4 (JSON de Excalidraw). Si cambia el stack, actualiza sus etiquetas además del diagrama Mermaid del README. `docs/plan_maestro.pdf` no es editable desde el repo.
- Los enlaces del README a `docs/requerimientos/requerimientos-funcionales.md` y `docs/requerimientos/reglas-de-negocio.md` están rotos: esos archivos no existen (las reglas de negocio `RN-NN` y parámetros `P-NN` aún no tienen documento).
