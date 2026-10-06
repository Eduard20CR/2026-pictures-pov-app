# Especificación de requerimientos no funcionales

| Campo | Valor |
|---|---|
| Proyecto | Pictures POV App |
| Documento | Requerimientos no funcionales |
| Versión | 0.2 |
| Estado | Borrador |
| Fecha | 2026-10-06 |
| Responsable | Oscar |

## Historial de cambios

| Versión | Fecha | Descripción |
|---|---|---|
| 0.1 | 2026-10-06 | Versión inicial. |
| 0.2 | 2026-10-06 | Los archivos ZIP se incluyen en RNF-SEG-05 y RNF-REN-06 pasa a medir la generación en segundo plano. |

---

## 1. Introducción

### 1.1 Propósito

Este documento define los atributos de calidad y las restricciones que Pictures POV App debe cumplir: seguridad, privacidad, rendimiento, capacidad, disponibilidad, usabilidad, operación y costo.

### 1.2 Documentos relacionados

| Documento | Descripción |
|---|---|
| [01_requisitos_funcionales.md](01_requisitos_funcionales.md) | Requerimientos funcionales (RF) y glosario. |

## 2. Convenciones

- **Identificador.** Cada requerimiento tiene un identificador estable con el formato `RNF-<CATEGORÍA>-<NN>`. Un identificador no se reutiliza; los requerimientos eliminados se marcan como *Retirado*.
- **Categorías.** `SEG` seguridad, `PRI` privacidad, `REN` rendimiento, `CAP` capacidad, `DIS` disponibilidad, `USA` usabilidad y compatibilidad, `OPE` operación y mantenibilidad, `COS` costo.
- **Medición.** Cada requerimiento incluye un criterio medible. Los percentiles (p95) se calculan sobre una ventana de 30 días en el entorno de producción, salvo que se indique otra cosa.
- **Prioridad.** Se usa la escala MoSCoW definida en los requerimientos funcionales.

---

## 3. Seguridad

| ID | Requerimiento | Prioridad |
|---|---|---|
| RNF-SEG-01 | Toda comunicación entre clientes y sistema debe cifrarse con TLS 1.2 o superior. | M |
| RNF-SEG-02 | La autorización de cada operación debe validarse en el servidor, con independencia de lo que muestre la interfaz (RN-04, RN-05). | M |
| RNF-SEG-03 | El sistema debe validar el tipo real de cada archivo subido a partir de su contenido, no de su extensión ni del tipo declarado por el cliente. Los archivos que no superen la validación no deben publicarse (RF-SIS-01). | M |
| RNF-SEG-04 | El identificador público de cada evento debe tener al menos 128 bits de entropía y no ser secuencial (RF-ORG-02). | M |
| RNF-SEG-05 | Las fotos y los archivos ZIP no deben ser accesibles mediante URLs públicas permanentes; el acceso debe realizarse con enlaces firmados y temporales (RN-08, RF-ORG-14). | M |
| RNF-SEG-06 | Los endpoints accesibles sin autenticación deben limitar la tasa de peticiones por dispositivo y por dirección IP. | S |
| RNF-SEG-07 | Las credenciales y secretos no deben almacenarse en el código fuente ni en el repositorio. | M |

## 4. Privacidad

| ID | Requerimiento | Prioridad |
|---|---|---|
| RNF-PRI-01 | Las imágenes mostradas a invitados no deben contener metadatos de ubicación. | S |
| RNF-PRI-02 | El sistema debe almacenar de los invitados únicamente el identificador de dispositivo y, si lo proporcionan, su nombre. | M |
| RNF-PRI-03 | Las fotos eliminadas por retención o por eliminación definitiva no deben poder recuperarse, incluidas las copias de seguridad, una vez transcurridos 35 días desde su eliminación. | S |

## 5. Rendimiento

| ID | Requerimiento | Prioridad |
|---|---|---|
| RNF-REN-01 | La página del evento debe mostrar las primeras 30 miniaturas de la galería en menos de 3 segundos (p95) en una conexión móvil 4G. | M |
| RNF-REN-02 | Cada miniatura debe ocupar como máximo 100 KB. | S |
| RNF-REN-03 | Una foto subida debe estar disponible en la galería en menos de 10 segundos (p95) desde que finaliza la subida. | S |
| RNF-REN-04 | Las operaciones de la API, excluida la transferencia de archivos, deben responder en menos de 500 ms (p95) y en menos de 1,5 s (p99). | S |
| RNF-REN-05 | La galería abierta debe reflejar las fotos nuevas en un plazo máximo de 30 segundos (RF-SIS-07). | C |
| RNF-REN-06 | La generación en segundo plano del archivo ZIP de un evento de 2.000 fotos, desde la solicitud hasta que el enlace de descarga está disponible en la página del evento, debe completarse en menos de 15 minutos (RF-ORG-14). | S |

## 6. Capacidad

| ID | Requerimiento | Prioridad |
|---|---|---|
| RNF-CAP-01 | El sistema debe soportar en un mismo evento 300 dispositivos subiendo fotos simultáneamente, con una tasa de error inferior al 1 %. | M |
| RNF-CAP-02 | El sistema debe soportar 20 eventos en estado Activo de forma simultánea sin degradar los objetivos de rendimiento de la sección 5. | S |

## 7. Disponibilidad y recuperación

| ID | Requerimiento | Prioridad |
|---|---|---|
| RNF-DIS-01 | La disponibilidad mensual de la subida de fotos y de la galería debe ser igual o superior al 99,5 %. | S |
| RNF-DIS-02 | Reintentar una subida interrumpida no debe consumir cupo adicional ni duplicar la foto (RF-INV-05). | M |
| RNF-DIS-03 | Ante la pérdida de la base de datos, el punto de recuperación (RPO) no debe superar las 24 horas y el tiempo de recuperación (RTO) no debe superar las 4 horas. | S |
| RNF-DIS-04 | Las fotos deben almacenarse en un servicio con redundancia en al menos tres zonas de disponibilidad. | M |

## 8. Usabilidad y compatibilidad

| ID | Requerimiento | Prioridad |
|---|---|---|
| RNF-USA-01 | El sistema debe funcionar en las dos últimas versiones principales de Safari para iOS, Chrome para Android y Chrome, Safari, Firefox y Edge de escritorio. | M |
| RNF-USA-02 | La interfaz debe ser utilizable en pantallas desde 360 px de ancho. | M |
| RNF-USA-03 | Un invitado debe poder iniciar su primera subida en un máximo de tres interacciones desde que abre el enlace del evento. | S |
| RNF-USA-04 | La interfaz debe cumplir las pautas WCAG 2.1 nivel AA. | S |
| RNF-USA-05 | La interfaz debe estar en español e ingles. | M |

## 9. Operación y mantenibilidad

| ID | Requerimiento | Prioridad |
|---|---|---|
| RNF-OPE-01 | Toda la infraestructura debe definirse como código y desplegarse sin cambios manuales. | M |
| RNF-OPE-02 | El sistema debe contar con dos entornos aislados, desarrollo y producción. | M |
| RNF-OPE-03 | Todo cambio debe desplegarse mediante un proceso automatizado que ejecute pruebas antes de publicar. | M |
| RNF-OPE-04 | El sistema debe emitir registros estructurados con un identificador de correlación por petición, conservados durante 30 días. | S |
| RNF-OPE-05 | El sistema debe alertar cuando la tasa de errores de la API supere el 5 % durante 5 minutos consecutivos. | S |
| RNF-OPE-06 | El contrato de la API debe estar documentado en formato OpenAPI y generarse a partir del código. | S |

## 10. Costo

| ID | Requerimiento | Prioridad |
|---|---|---|
| RNF-COS-01 | El costo de infraestructura de cada entorno sin tráfico no debe superar los 25 USD mensuales. | S |
| RNF-COS-02 | El costo de infraestructura atribuible a un evento de 300 fotos, durante todo su ciclo de vida, no debe superar los 2 USD. | S |
