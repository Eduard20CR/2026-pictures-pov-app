# Visión general del proyecto

| Campo | Valor |
|---|---|
| Proyecto | Pictures POV App |
| Documento | Visión general |
| Versión | 0.2 |
| Estado | Borrador |
| Fecha | 2026-10-06 |
| Responsable | Oscar |

## Historial de cambios

| Versión | Fecha | Descripción |
|---|---|---|
| 0.1 | 2026-10-06 | Versión inicial. |
| 0.2 | 2026-10-06 | Base de datos cambiada de Amazon DynamoDB a PostgreSQL. |

---

## 1. Propósito del documento

Este documento ofrece una descripción de alto nivel de Pictures POV App: el problema que resuelve, a quién va dirigida, qué hace y cómo está construida. Es el punto de entrada a la documentación del proyecto; el detalle de cada aspecto se especifica en los documentos referenciados en la sección 9.

## 2. Contexto y problema

En bodas, cumpleaños y fiestas, los asistentes toman una gran cantidad de fotos con sus teléfonos que rara vez llegan a los organizadores. Las alternativas habituales, como grupos de mensajería o carpetas compartidas, exigen que cada invitado instale una aplicación, cree una cuenta o reciba un enlace de forma individual, reducen la calidad de las imágenes y dispersan las fotos en varios canales.

## 3. Descripción de la solución

Pictures POV App es una aplicación web que centraliza las fotos de un evento a partir de un código QR:

1. Un administrador crea el evento y lo asigna al correo del cliente que lo organiza.
2. El organizador obtiene el enlace y el código QR del evento y los coloca en el lugar de la celebración.
3. Los invitados escanean el código, acceden a la página del evento sin crear una cuenta y suben fotos desde la galería de su teléfono o tomándolas con la cámara, hasta un límite por dispositivo (10 por defecto).
4. Todas las fotos se muestran en una galería común del evento.
5. El organizador inicia sesión para consultar las fotos, moderarlas y descargarlas en un archivo ZIP.

## 4. Usuarios

| Rol | Descripción | Capacidades principales |
|---|---|---|
| Invitado | Asistente al evento. No dispone de cuenta. | Acceder al evento mediante el QR o el enlace, subir fotos dentro de su límite, consultar la galería. |
| Organizador | Cliente al que se asignan uno o más eventos. | Consultar sus eventos, obtener el enlace y el QR, personalizar el evento, moderar y descargar las fotos. |
| Administrador | Miembro del equipo de la plataforma. | Crear, asignar y gestionar eventos y usuarios de toda la plataforma. |

## 5. Alcance

### 5.1 Incluido

- Acceso de invitados sin registro mediante código QR o enlace.
- Subida de fotos desde el navegador con un límite por dispositivo y un límite total por evento.
- Galería del evento con visibilidad configurable: pública, protegida con PIN o visible solo para el organizador.
- Inicio de sesión de organizadores y administradores con Google o con correo y contraseña.
- Moderación de fotos: ocultar, volver a mostrar y eliminar.
- Descarga de fotos individuales y del evento completo en un archivo ZIP.
- Panel de administración para la gestión de eventos y usuarios.
- Generación de miniaturas y eliminación automática de fotos tras un plazo de retención.

### 5.2 Excluido de la primera versión

- Subida de videos.
- Pagos y planes de suscripción.
- Registro autónomo de organizadores.
- Comentarios, reacciones y edición de fotos.
- Reconocimiento facial.
- Aplicación móvil nativa.

## 6. Principios del producto

- **Sin fricción para el invitado.** No se exige cuenta, instalación ni datos personales para participar.
- **Control para el organizador.** El organizador decide qué fotos se muestran y quién puede ver la galería.
- **Límite por dispositivo, no por red.** Los asistentes a un evento comparten habitualmente la misma red WiFi, por lo que el límite de subida se aplica por dispositivo y no por dirección IP.
- **Privacidad por defecto.** Las fotos se conservan durante un plazo limitado y los enlaces de descarga son temporales.

