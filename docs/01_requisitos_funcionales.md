# Especificación de requerimientos funcionales

| Campo | Valor |
|---|---|
| Proyecto | Pictures POV App |
| Documento | Requerimientos funcionales |
| Versión | 0.1 |
| Estado | Listo |
| Fecha | 2026-10-06 |
| Responsable | Oscar |

## Historial de cambios

| Versión | Fecha | Descripción |
|---|---|---|
| 0.1 | 2026-10-06 | Versión inicial. |
---

## 1. Introducción

### 1.1 Propósito

Este documento define las capacidades funcionales de Pictures POV App, una aplicación web que permite a los invitados de un evento subir fotos mediante un código QR, sin crear una cuenta, y a los organizadores consultar, moderar y descargar dichas fotos.

### 1.2 Alcance

El documento cubre los requerimientos funcionales de los tres roles del sistema (administrador, organizador e invitado) y el comportamiento automático del sistema. Los requerimientos no funcionales y las reglas de negocio se especifican en documentos separados.

### 1.3 Documentos relacionados

| Documento | Descripción |
|---|---|
| [02_requisitos_no_funcionales.md](02_requisitos_no_funcionales.md) | Requerimientos no funcionales (RNF) |

---

## 2. Convenciones

- **Identificador.** Cada requerimiento tiene un identificador estable con el formato `RF-<ÁREA>-<NN>`. Un identificador no se reutiliza; los requerimientos eliminados se marcan como *Retirado*.
- **Áreas.** `AUT` autenticación, `ADM` administrador, `ORG` organizador, `INV` invitado, `SIS` sistema.
- **Redacción.** Cada requerimiento describe una única capacidad verificable con la forma "El sistema debe permitir que el [rol] …" o "El sistema debe …".
- **Prioridad (MoSCoW).** **M** (Must): obligatorio para la primera versión. **S** (Should): importante, no bloqueante. **C** (Could): deseable. **W** (Won't): fuera del alcance de esta versión.
- **Criterios de aceptación (CA).** Se incluyen en los requerimientos que requieren precisión adicional para ser verificables.
- **Referencias.** `RN-NN` remite a una regla de negocio y `P-NN` a un parámetro configurable, ambos definidos en [reglas-de-negocio.md](reglas-de-negocio.md).

## 3. Glosario

| Término | Definición |
|---|---|
| Administrador | Miembro del equipo de la plataforma con permisos sobre todos los eventos y usuarios. |
| Organizador | Cliente al que se asignan uno o más eventos. También denominado dueño del evento. |
| Invitado | Asistente a un evento. No dispone de cuenta en el sistema. |
| Dispositivo | Navegador identificado mediante un identificador único almacenado en una cookie, emitido por evento. No constituye una identidad verificada (RN-02). |
| Evento | Celebración para la que se recopilan fotos. Tiene un enlace público, un código QR y un ciclo de vida (RN-06). |
| Galería | Conjunto de fotos visibles de un evento. |
| Foto oculta | Foto que no se muestra en la galería pero se conserva y puede volver a mostrarse. |
| Foto eliminada | Foto retirada de forma permanente para los usuarios (RN-07). |
| Ventana de subida | Intervalo de fechas y horas en el que los invitados pueden subir fotos a un evento. |

---

## 4. Autenticación

Aplica a administradores y organizadores. Los invitados no se autentican.

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-AUT-01 | El sistema debe permitir que administradores y organizadores **inicien sesión** con una cuenta de Google o con correo y contraseña. | M |
| RF-AUT-02 | El sistema debe permitir que administradores y organizadores **cierren sesión**. | M |
| RF-AUT-03 | El sistema debe permitir que un usuario con correo y contraseña **recupere su contraseña**. | M |
| RF-AUT-04 | El sistema debe exigir **autenticación multifactor** a los administradores. | S |

---

## 5. Administrador

### 5.1 Gestión de eventos

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-ADM-01 | El sistema debe permitir que el administrador **cree un evento** indicando nombre, fecha del evento y correo del organizador. | M |
| RF-ADM-02 | El sistema debe permitir que el administrador **asigne un evento a un correo**, aunque el organizador aún no tenga cuenta. | M |
| RF-ADM-03 | El sistema debe permitir que el administrador **edite** los datos de un evento y **lo reasigne** a otro correo. | S |
| RF-ADM-04 | El sistema debe permitir que el administrador **elimine un evento**. | M |
| RF-ADM-05 | El sistema debe permitir que el administrador **consulte todos los eventos**, con paginación y ordenados por fecha. | M |
| RF-ADM-06 | El sistema debe permitir que el administrador **busque eventos por correo del organizador**. | M |
| RF-ADM-07 | El sistema debe permitir que el administrador **configure por evento** el límite de fotos por dispositivo y la ventana de subida. | S |
| RF-ADM-08 | El sistema debe mostrar al administrador, por evento, el **número de fotos y el almacenamiento utilizado**. | S |

**CA RF-ADM-02**
1. Un usuario que inicia sesión con un correo verificado igual al correo asignado visualiza el evento en su lista de eventos.
2. Un usuario cuyo correo no está verificado no visualiza el evento (RN-04).
3. Al asignar el evento, el sistema notifica por correo al organizador (RF-SIS-06).

**CA RF-ADM-04**
1. El sistema solicita confirmación mediante la escritura del nombre del evento.
2. El evento deja de ser accesible de inmediato y sus fotos se eliminan conforme a RN-07.

**CA RF-ADM-07**
1. Si no se configura un límite, se aplica el valor por defecto P-01.
2. Si no se configura una ventana de subida, las subidas permanecen abiertas mientras el evento esté en estado Activo (RN-06).

### 5.2 Gestión de usuarios

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-ADM-09 | El sistema debe permitir que el administrador **busque organizadores** por correo. | M |
| RF-ADM-10 | El sistema debe permitir que el administrador **bloquee y desbloquee** a un organizador. | M |
| RF-ADM-11 | El sistema debe permitir que el administrador **elimine** a un organizador. | S |
| RF-ADM-12 | El sistema debe permitir que el administrador **otorgue y revoque el rol de administrador** a otro usuario. | S |

**CA RF-ADM-10**
1. Un usuario bloqueado no puede iniciar sesión.
2. Las sesiones activas de un usuario bloqueado se invalidan en un plazo no superior a P-09.
3. Los eventos de un organizador bloqueado permanecen accesibles para los invitados (RN-09).

**CA RF-ADM-11**
1. Antes de eliminar, el sistema muestra los eventos asignados al organizador.
2. El sistema no permite eliminar a un organizador con eventos asignados hasta que estos se reasignen o eliminen.

**CA RF-ADM-12**
1. El sistema no permite revocar el rol al último administrador.
2. El primer administrador se aprovisiona fuera de la aplicación, como parte de la infraestructura.

### 5.3 Auditoría

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-ADM-13 | El sistema debe **registrar** el autor y la fecha de la creación, eliminación y reasignación de eventos, del bloqueo y eliminación de usuarios y de los cambios de rol, y permitir que el administrador consulte dicho registro. | C |

---

## 6. Organizador

### 6.1 Gestión de sus eventos

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-ORG-01 | El sistema debe permitir que el organizador **consulte la lista de sus eventos**. | M |
| RF-ORG-02 | El sistema debe permitir que el organizador **obtenga el enlace del evento** para compartirlo con los invitados. | M |
| RF-ORG-03 | El sistema debe permitir que el organizador **descargue el código QR** del evento en formato imprimible (PNG y PDF). | M |
| RF-ORG-04 | El sistema debe permitir que el organizador **abra o cierre las subidas** manualmente, con independencia de la ventana de subida. | S |
| RF-ORG-05 | El sistema debe mostrar al organizador **estadísticas del evento**: fotos subidas, dispositivos participantes y fotos ocultas. | C |

**CA RF-ORG-01:** la lista contiene únicamente los eventos asignados al correo verificado del organizador (RN-04).

### 6.2 Personalización

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-ORG-06 | El sistema debe permitir que el organizador **cambie el nombre** del evento. | M |
| RF-ORG-07 | El sistema debe permitir que el organizador **suba o cambie la foto de portada** del evento. | S |
| RF-ORG-08 | El sistema debe permitir que el organizador **elija el color principal** del evento. | S |
| RF-ORG-09 | El sistema debe permitir que el organizador **defina un mensaje de bienvenida** para los invitados. | C |
| RF-ORG-21 | El sistema debe permitir que el organizador **configure la visibilidad de la galería**: pública mediante el enlace, protegida con PIN o visible solo para el organizador. | S |

**CA RF-ORG-08:** el color se selecciona de una paleta predefinida cuyos colores cumplen un contraste mínimo de 4.5:1 con el texto (WCAG 2.1 nivel AA).

**CA RF-ORG-21**
1. La visibilidad por defecto es pública mediante el enlace.
2. La configuración de visibilidad no afecta a la subida de fotos.
3. El organizador puede cambiar el PIN en cualquier momento.

### 6.3 Consulta y descarga de fotos

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-ORG-10 | El sistema debe permitir que el organizador **consulte todas las fotos** de su evento, incluidas las ocultas. | M |
| RF-ORG-11 | El sistema debe permitir que el organizador **vea cada foto en tamaño completo** y navegue entre ellas. | M |
| RF-ORG-12 | El sistema debe permitir que el organizador **filtre y agrupe las fotos por quien las subió**. | S |
| RF-ORG-13 | El sistema debe permitir que el organizador **descargue una foto individual** en su resolución original. | M |
| RF-ORG-14 | El sistema debe permitir que el organizador **descargue todas las fotos del evento** en un archivo ZIP. | M |
| RF-ORG-15 | El sistema debe permitir que el organizador **seleccione varias fotos** y las descargue en un archivo ZIP. | C |

**CA RF-ORG-10:** las fotos ocultas se distinguen visualmente de las visibles.

**CA RF-ORG-11:** la vista de cuadrícula muestra miniaturas; la imagen original se solicita únicamente al abrir una foto.

**CA RF-ORG-12**
1. Las fotos se agrupan por dispositivo.
2. Cada grupo muestra el nombre indicado por el invitado; si no lo indicó, se muestra una etiqueta genérica.
3. Dos dispositivos que indiquen el mismo nombre se presentan como grupos distintos.

**CA RF-ORG-14**
1. El archivo se genera de forma asíncrona.
2. Al finalizar la generación, el sistema notifica al organizador por correo con un enlace de descarga (RF-SIS-06) y muestra el mismo enlace en la página del evento.
3. El enlace de descarga caduca conforme a P-07.
4. Si las fotos del evento no han cambiado desde la última generación, el sistema reutiliza el archivo existente.
5. El número de generaciones por evento está limitado por P-08 (RN-08).

### 6.4 Moderación

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-ORG-16 | El sistema debe permitir que el organizador **oculte** una foto de la galería. | M |
| RF-ORG-17 | El sistema debe permitir que el organizador **vuelva a mostrar** una foto oculta. | M |
| RF-ORG-18 | El sistema debe permitir que el organizador **elimine** una foto. | M |
| RF-ORG-19 | El sistema debe permitir que el organizador **oculte o elimine todas las fotos de un dispositivo** en una sola acción. | S |
| RF-ORG-20 | El sistema debe permitir que el organizador elija entre **moderación posterior**, en la que las fotos se publican al subirse, y **moderación previa**, en la que se publican tras su aprobación. | C |

**CA RF-ORG-18**
1. El sistema solicita confirmación antes de eliminar.
2. La foto deja de ser visible de inmediato y se elimina conforme a RN-07.
3. La eliminación no restituye el cupo del invitado (RN-03).

**CA RF-ORG-20:** el modo por defecto es moderación posterior.

---

## 7. Invitado

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-INV-01 | El sistema debe permitir que el invitado **acceda a la página del evento** desde el código QR o el enlace **sin iniciar sesión**. | M |
| RF-INV-02 | El sistema debe permitir que el invitado **suba fotos desde la galería de su dispositivo o tomándolas con la cámara**. | M |
| RF-INV-03 | El sistema debe **limitar el número de fotos** que un dispositivo puede subir a un evento (RN-01). | M |
| RF-INV-04 | El sistema debe mostrar al invitado **el número de fotos que le quedan** por subir. | M |
| RF-INV-05 | El sistema debe permitir que el invitado **seleccione varias fotos a la vez**, debe mostrar el **progreso** de cada subida y debe permitir **reintentar** una subida fallida. | M |
| RF-INV-06 | El sistema debe permitir que el invitado **indique su nombre** para identificar sus fotos. | S |
| RF-INV-07 | El sistema debe permitir que el invitado **consulte la galería del evento**. | M |
| RF-INV-08 | El sistema debe permitir que el invitado **consulte las fotos que subió** desde su dispositivo. | S |
| RF-INV-09 | *Retirado.* | — |
| RF-INV-10 | El sistema debe permitir que el invitado **reporte una foto** como inapropiada al organizador. | C |
| RF-INV-11 | El sistema debe informar al invitado cuando el evento **no existe**, **está cerrado** o **la ventana de subida ha finalizado**. | M |
| RF-INV-12 | El sistema debe solicitar al invitado la **aceptación de los términos de uso** antes de su primera subida. | S |

**CA RF-INV-03**
1. El límite se valida en el servidor; el sistema no autoriza nuevas subidas a un dispositivo que alcanzó el límite.
2. Si el invitado selecciona más fotos de las que le quedan, el sistema se lo informa antes de iniciar la subida.
3. El cupo se reserva al autorizar cada subida. Si la subida no se completa en el plazo P-10, el cupo reservado se libera.

**CA RF-INV-04:** el número se muestra antes de seleccionar fotos y se actualiza tras cada subida completada.

**CA RF-INV-06**
1. El nombre es opcional y admite hasta 40 caracteres.
2. El nombre se recuerda en el dispositivo para el mismo evento y puede modificarse.

**CA RF-INV-07**
1. La galería muestra únicamente fotos visibles, ni ocultas ni eliminadas.
2. La galería muestra miniaturas y carga las fotos de forma progresiva.
3. Si la galería está protegida con PIN (RF-ORG-21), el sistema solicita el PIN antes de mostrar las fotos y limita los intentos fallidos conforme a P-11.
4. Si la galería es visible solo para el organizador, el invitado no puede consultarla.

**CA RF-INV-11:** cuando la ventana de subida ha finalizado y el evento está en estado Cerrado, la galería continúa disponible según su configuración de visibilidad.

---

## 8. Sistema

Comportamiento que el sistema ejecuta sin intervención directa de un usuario.

| ID | Requerimiento | Prioridad |
|---|---|---|
| RF-SIS-01 | El sistema debe **aceptar únicamente imágenes** en formato JPEG, PNG, WebP o HEIC, con un tamaño no superior a P-02. | M |
| RF-SIS-02 | El sistema debe **generar una miniatura** en formato WebP de cada foto subida. | M |
| RF-SIS-03 | El sistema debe **corregir la orientación** de las imágenes según sus metadatos EXIF y **eliminar los metadatos de ubicación** de las imágenes mostradas a terceros. | S |
| RF-SIS-04 | El sistema debe generar el enlace de cada evento con un **identificador no secuencial y no predecible**. | M |
| RF-SIS-05 | El sistema debe **eliminar automáticamente las fotos** de un evento una vez transcurrido el plazo de retención P-05 desde la fecha del evento. | S |
| RF-SIS-06 | El sistema debe **notificar por correo al organizador** cuando se le asigna un evento, cuando su archivo ZIP está disponible y antes de la eliminación automática de sus fotos. | S |
| RF-SIS-07 | El sistema debe **mostrar las fotos nuevas** en la galería sin que el invitado recargue la página manualmente. | C |

**CA RF-SIS-01**
1. El sistema valida el tipo real del contenido, no solo la extensión o el tipo declarado por el cliente.
2. Los archivos que no cumplen la validación se descartan y no se muestran en la galería.

**CA RF-SIS-05:** el aviso previo al organizador se envía con la antelación P-06.

**CA RF-SIS-07:** la galería se actualiza al menos cada 30 segundos mientras está abierta.

---

## 9. Fuera de alcance

Las siguientes capacidades no forman parte de esta versión (prioridad W):

| Capacidad | Observación |
|---|---|
| Subida de videos | — |
| Pagos y planes de suscripción | — |
| Registro autónomo de organizadores | Los eventos se asignan únicamente por un administrador. |
| Comentarios y reacciones en las fotos | — |
| Edición de fotos | Incluye filtros y recortes. |
| Reconocimiento facial y agrupación automática por persona | — |
| Aplicación móvil nativa | La aplicación es web y adaptable a dispositivos móviles. |
| Eliminación de fotos por parte del invitado | Requerimiento RF-INV-09 retirado. |
