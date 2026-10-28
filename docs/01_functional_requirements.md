# Functional Requirements Specification

| Field | Value |
|---|---|
| Project | Pictures POV App |
| Document | Functional requirements |
| Version | 0.5 |
| Status | Ready |
| Date | 2026-10-28 |
| Owner | Oscar |

## Change history

| Version | Date | Description |
|---|---|---|
| 0.1 | 2026-10-06 | Initial version. |
| 0.2 | 2026-10-06 | The ZIP download is generated in the background, stored in Amazon S3 and offered through a temporary link on the event page; the ZIP email notification is removed (RF-ORG-14, RF-SIS-06). |
| 0.3 | 2026-10-08 | Document translated into English. Requirement identifiers are unchanged. |
| 0.4 | 2026-10-27 | New Moderator role and event invitations (OWNER, MODERATOR). Added RF-AUT-05, RF-ORG-22, RF-ORG-23 and RF-MOD-01 to RF-MOD-07. Event assignment uses an OWNER invitation (RF-ADM-02, RF-ADM-03). Updated RF-AUT-01 to RF-AUT-03, RF-ADM-09 to RF-ADM-11, RF-ADM-13, RF-ORG-01, RF-ORG-21, RF-INV-07 and RF-SIS-06. Sections 7 to 9 renumbered to 8 to 10. |
| 0.5 | 2026-10-28 | Guests only upload photos and delete their own; they no longer view the event gallery, and they can do nothing once the event is not Active. Added RF-INV-13. Retired RF-ORG-21, RF-MOD-07, RF-INV-07, RF-INV-10 and RF-SIS-07. Updated the Gallery glossary entry, RF-INV-08, RF-INV-11, AC RF-MOD-06 and section 10. |
---

## 1. Introduction

### 1.1 Purpose

This document defines the functional capabilities of Pictures POV App, a web application that allows the guests of an event to upload photos through a QR code, without creating an account and without seeing other guests' photos, and allows organizers and moderators to view, moderate and download those photos.

### 1.2 Scope

The document covers the functional requirements of the system's four roles (administrator, organizer, moderator and guest) and the system's automatic behavior. Non-functional requirements and business rules are specified in separate documents.

### 1.3 Related documents

| Document | Description |
|---|---|
| [02_requisitos_no_funcionales.md](02_requisitos_no_funcionales.md) | Non-functional requirements (RNF) |

---

## 2. Conventions

- **Identifier.** Each requirement has a stable identifier in the format `RF-<AREA>-<NN>`. An identifier is never reused; removed requirements are marked as *Retired*.
- **Areas.** `AUT` authentication, `ADM` administrator, `ORG` organizer, `MOD` moderator, `INV` guest, `SIS` system.
- **Wording.** Each requirement describes a single verifiable capability, in the form "The system must allow the [role] to …" or "The system must …".
- **Priority (MoSCoW).** **M** (Must): mandatory for the first version. **S** (Should): important, not blocking. **C** (Could): desirable. **W** (Won't): out of scope for this version.
- **Acceptance criteria (AC).** Included for requirements that need additional precision to be verifiable.
- **References.** `RN-NN` refers to a business rule and `P-NN` to a configurable parameter, both defined in [reglas-de-negocio.md](reglas-de-negocio.md).

## 3. Glossary

| Term | Definition |
|---|---|
| Administrator | Member of the platform team with permissions over all events and users. |
| Organizer | Customer who holds an accepted OWNER invitation for an event. Also called the event owner. Each event has exactly one owner. |
| Moderator | User who holds an accepted MODERATOR invitation for an event, with the limited permissions of section 7. |
| Invitation | Grant of a role (OWNER or MODERATOR) on one event to an email. Status *Pending* until accepted (RF-AUT-05), then *Accepted*. One invitation per event and email; emails are stored in lowercase. |
| Guest | Attendee of an event. Has no account in the system. |
| Device | Browser identified by a unique identifier stored in a cookie, issued per event. It is not a verified identity (RN-02). |
| Event | Celebration for which photos are collected. It has a public link, a QR code and a lifecycle (RN-06). |
| Gallery | Set of visible photos of an event. Only the organizer and moderators can view it. |
| Hidden photo | Photo that is not shown in the gallery but is kept and can be shown again. |
| Deleted photo | Photo permanently removed for users (RN-07). |
| Upload window | Date and time interval during which guests can upload photos to an event. |

---

## 4. Authentication

Applies to administrators, organizers and moderators. Guests do not authenticate.

| ID | Requirement | Priority |
|---|---|---|
| RF-AUT-01 | The system must allow administrators, organizers and moderators to **sign in** with a Google account or with email and password. | M |
| RF-AUT-02 | The system must allow administrators, organizers and moderators to **sign out**. | M |
| RF-AUT-03 | The system must allow a user with email and password to **recover their password**. | M |
| RF-AUT-04 | The system must require **multi-factor authentication** for administrators. | S |
| RF-AUT-05 | The system must allow an invitee to **accept an invitation** to an event after creating an account and verifying their email. | M |

**AC RF-AUT-05**
1. The invitee can create the account after the invitation was sent.
2. The system accepts the invitation only if the token has `email_verified = true` and its email matches the invitation email; otherwise it responds 403 and the invitation remains *Pending*.
3. Emails are compared in lowercase.
4. On acceptance, the invitation becomes *Accepted*, is linked to the user, and the event appears in the user's list of events with the invitation's role.
5. A removed invitation cannot be accepted.

---

## 5. Administrator

### 5.1 Event management

| ID | Requirement | Priority |
|---|---|---|
| RF-ADM-01 | The system must allow the administrator to **create an event** by providing a name, the event date and the organizer's email. | M |
| RF-ADM-02 | The system must allow the administrator to **assign an event to an organizer** by creating an OWNER invitation for their email, even if the organizer does not yet have an account. | M |
| RF-ADM-03 | The system must allow the administrator to **edit** an event's data and **reassign it** to another email by replacing its OWNER invitation. | S |
| RF-ADM-04 | The system must allow the administrator to **delete an event**. | M |
| RF-ADM-05 | The system must allow the administrator to **view all events**, paginated and sorted by date. | M |
| RF-ADM-06 | The system must allow the administrator to **search events by organizer email**. | M |
| RF-ADM-07 | The system must allow the administrator to **configure, per event,** the per-device photo limit and the upload window. | S |
| RF-ADM-08 | The system must show the administrator, per event, the **number of photos and the storage used**. | S |

**AC RF-ADM-02**
1. Each event has exactly one OWNER invitation; the event has no other owner reference.
2. One invitation per event and email; the email is stored in lowercase.
3. The organizer gets access only after accepting the invitation (RF-AUT-05); a user whose email is not verified does not see the event (RN-04).
4. When the invitation is created, the system notifies the organizer by email (RF-SIS-06).

**AC RF-ADM-03**
1. On reassignment, the previous OWNER invitation is removed and the previous owner loses access to the event.
2. The new OWNER invitation is *Pending* until accepted (RF-AUT-05).

**AC RF-ADM-04**
1. The system asks for confirmation by having the user type the event name.
2. The event becomes inaccessible immediately and its photos are deleted in accordance with RN-07.

**AC RF-ADM-07**
1. If no limit is configured, the default value P-01 applies.
2. If no upload window is configured, uploads remain open while the event is in the Active state (RN-06).

### 5.2 User management

| ID | Requirement | Priority |
|---|---|---|
| RF-ADM-09 | The system must allow the administrator to **search organizers and moderators** by email. | M |
| RF-ADM-10 | The system must allow the administrator to **block and unblock** an organizer or moderator. | M |
| RF-ADM-11 | The system must allow the administrator to **delete** an organizer or moderator. | S |
| RF-ADM-12 | The system must allow the administrator to **grant and revoke the administrator role** for another user. | S |

**AC RF-ADM-10**
1. A blocked user cannot sign in.
2. The active sessions of a blocked user are invalidated within a period not exceeding P-09.
3. The events of a blocked organizer remain accessible to guests (RN-09).

**AC RF-ADM-11**
1. Before deleting, the system shows the events where the user is owner or moderator.
2. The system does not allow deleting a user who owns events until those events are reassigned or deleted.
3. Deleting a user removes their MODERATOR invitations.

**AC RF-ADM-12**
1. The system does not allow revoking the role from the last administrator.
2. The first administrator is provisioned outside the application, as part of the infrastructure.

### 5.3 Audit

| ID | Requirement | Priority |
|---|---|---|
| RF-ADM-13 | The system must **record** the author and date of the creation, deletion and reassignment of events, the blocking and deletion of users, role changes and the creation, acceptance and removal of invitations, and allow the administrator to view that record. | C |

---

## 6. Organizer

### 6.1 Managing their events

| ID | Requirement | Priority |
|---|---|---|
| RF-ORG-01 | The system must allow the organizer to **view the list of their events**. | M |
| RF-ORG-02 | The system must allow the organizer to **get the event link** to share it with guests. | M |
| RF-ORG-03 | The system must allow the organizer to **download the event's QR code** in a printable format (PNG and PDF). | M |
| RF-ORG-04 | The system must allow the organizer to **open or close uploads** manually, regardless of the upload window. | S |
| RF-ORG-05 | The system must show the organizer **event statistics**: uploaded photos, participating devices and hidden photos. | C |

**AC RF-ORG-01:** the list contains only the events for which the organizer holds an accepted OWNER invitation (RF-AUT-05, RN-04).

### 6.2 Customization

| ID | Requirement | Priority |
|---|---|---|
| RF-ORG-06 | The system must allow the organizer to **change the event name**. | M |
| RF-ORG-07 | The system must allow the organizer to **upload or change the event's cover photo**. | S |
| RF-ORG-08 | The system must allow the organizer to **choose the event's main color**. | S |
| RF-ORG-09 | The system must allow the organizer to **set a welcome message** for guests. | C |
| RF-ORG-21 | *Retired.* | — |

**AC RF-ORG-08:** the color is selected from a predefined palette whose colors meet a minimum contrast of 4.5:1 with the text (WCAG 2.1 level AA).

### 6.3 Viewing and downloading photos

| ID | Requirement | Priority |
|---|---|---|
| RF-ORG-10 | The system must allow the organizer to **view all photos** of their event, including hidden ones. | M |
| RF-ORG-11 | The system must allow the organizer to **view each photo at full size** and navigate between them. | M |
| RF-ORG-12 | The system must allow the organizer to **filter and group photos by uploader**. | S |
| RF-ORG-13 | The system must allow the organizer to **download an individual photo** at its original resolution. | M |
| RF-ORG-14 | The system must allow the organizer to **download all of the event's photos** in a ZIP file. | M |
| RF-ORG-15 | The system must allow the organizer to **select several photos** and download them in a ZIP file. | C |

**AC RF-ORG-10:** hidden photos are visually distinguished from visible ones.

**AC RF-ORG-11:** the grid view shows thumbnails; the original image is requested only when a photo is opened.

**AC RF-ORG-12**
1. Photos are grouped by device.
2. Each group shows the name provided by the guest; if none was provided, a generic label is shown.
3. Two devices that provide the same name are presented as separate groups.

**AC RF-ORG-14**
1. When the download is requested, the system generates the file in a background process; the organizer does not need to keep the page open while it is generated.
2. The generated file is stored in Amazon S3.
3. The event page shows the generation status (in progress, available or failed) and, when the file is available, a link to download it.
4. The download link is a signed, temporary URL that expires in accordance with P-07 (RNF-SEG-05).
5. The system does not send the file or the link by email.
6. If the event's photos have not changed since the last generation, the system reuses the existing file.
7. The number of generations per event is limited by P-08 (RN-08).

### 6.4 Moderation

| ID | Requirement | Priority |
|---|---|---|
| RF-ORG-16 | The system must allow the organizer to **hide** a photo from the gallery. | M |
| RF-ORG-17 | The system must allow the organizer to **show again** a hidden photo. | M |
| RF-ORG-18 | The system must allow the organizer to **delete** a photo. | M |
| RF-ORG-19 | The system must allow the organizer to **hide or delete all photos from a device** in a single action. | S |
| RF-ORG-20 | The system must allow the organizer to choose between **post-moderation**, in which photos are published when uploaded, and **pre-moderation**, in which they are published after approval. | C |

**AC RF-ORG-18**
1. The system asks for confirmation before deleting.
2. The photo stops being visible immediately and is deleted in accordance with RN-07.
3. Deletion does not restore the guest's quota (RN-03).

**AC RF-ORG-20:** the default mode is post-moderation.

### 6.5 Moderators

| ID | Requirement | Priority |
|---|---|---|
| RF-ORG-22 | The system must allow the organizer to **invite a moderator** by email, even if the invitee does not yet have an account. | M |
| RF-ORG-23 | The system must allow the organizer to **remove a moderator** or a pending moderator invitation. | M |

**AC RF-ORG-22**
1. The system creates a *Pending* MODERATOR invitation; the email is stored in lowercase.
2. The system rejects the invitation if the email already has one for the event.
3. The system notifies the invitee by email (RF-SIS-06).
4. The invitee gets access only after accepting the invitation (RF-AUT-05).
5. Only the organizer can invite moderators.

**AC RF-ORG-23**
1. The organizer sees the event's moderators and pending invitations with their status.
2. Removing deletes the invitation; the moderator loses access to the event immediately.
3. The removed email can be invited again.
4. The OWNER invitation cannot be removed with this action (RF-ADM-03).

---

## 7. Moderator

A moderator acts only on the events for which they hold an accepted MODERATOR invitation.

| ID | Requirement | Priority |
|---|---|---|
| RF-MOD-01 | The system must allow the moderator to **view the list of the events they moderate**. | M |
| RF-MOD-02 | The system must allow the moderator to **view all photos** of the event, including hidden ones, at full size (as in RF-ORG-10 and RF-ORG-11). | M |
| RF-MOD-03 | The system must allow the moderator to **filter and group photos by uploader** (as in RF-ORG-12). | S |
| RF-MOD-04 | The system must show the moderator **event statistics** (as in RF-ORG-05). | C |
| RF-MOD-05 | The system must allow the moderator to **get the event link and download its QR code** (as in RF-ORG-02 and RF-ORG-03). | M |
| RF-MOD-06 | The system must allow the moderator to **hide** a photo and **show again** a hidden photo (as in RF-ORG-16 and RF-ORG-17). | M |
| RF-MOD-07 | *Retired.* | — |

**AC RF-MOD-01:** each event in the list shows that the user's role is moderator.

**AC RF-MOD-06**
1. The moderator cannot delete photos, download photos or ZIP files, open or close uploads, edit the event's customization, change the moderation mode, or invite or remove moderators.
2. The server enforces these restrictions and responds 403.
3. System notifications (RF-SIS-06) go only to the organizer.

---

## 8. Guest

A guest can only upload photos and delete the photos they uploaded, and only while the event is in the Active state (RN-06). A guest never sees photos uploaded by other devices.

| ID | Requirement | Priority |
|---|---|---|
| RF-INV-01 | The system must allow the guest to **access the event page** from the QR code or the link **without signing in**. | M |
| RF-INV-02 | The system must allow the guest to **upload photos from their device's gallery or by taking them with the camera**. | M |
| RF-INV-03 | The system must **limit the number of photos** a device can upload to an event (RN-01). | M |
| RF-INV-04 | The system must show the guest **the number of photos they have left** to upload. | M |
| RF-INV-05 | The system must allow the guest to **select several photos at once**, must show the **progress** of each upload and must allow **retrying** a failed upload. | M |
| RF-INV-06 | The system must allow the guest to **provide their name** to identify their photos. | S |
| RF-INV-07 | *Retired.* | — |
| RF-INV-08 | The system must allow the guest to **view the photos they uploaded** from their device. | M |
| RF-INV-09 | *Retired.* | — |
| RF-INV-10 | *Retired.* | — |
| RF-INV-11 | The system must inform the guest when the event **is not available**. | M |
| RF-INV-12 | The system must ask the guest to **accept the terms of use** before their first upload. | S |
| RF-INV-13 | The system must allow the guest to **delete a photo they uploaded**. | M |

**AC RF-INV-03**
1. The limit is validated on the server; the system does not authorize new uploads for a device that has reached the limit.
2. If the guest selects more photos than they have left, the system informs them before starting the upload.
3. Quota is reserved when each upload is authorized. If the upload is not completed within period P-10, the reserved quota is released.

**AC RF-INV-04:** the number is shown before photos are selected and is updated after each completed upload.

**AC RF-INV-06**
1. The name is optional and accepts up to 40 characters.
2. The name is remembered on the device for the same event and can be changed.

**AC RF-INV-08**
1. The guest sees only the photos uploaded from their device for that event, as thumbnails.
2. Photos uploaded by other devices are never shown to the guest.

**AC RF-INV-11**
1. An event is available to guests only in the Active state (RN-06).
2. A code that does not exist and an event in any other state produce the same 'event not found' message, so the system does not reveal whether a code exists.
3. When the event is not available, the guest cannot view, upload or delete photos.

**AC RF-INV-13**
1. The guest can delete only photos uploaded from their device, and only while the event is Active.
2. The server validates the device identifier on every deletion.
3. The system asks for confirmation before deleting.
4. The photo stops being visible immediately and is deleted in accordance with RN-07.
5. Deletion does not restore the guest's quota (RN-03).

---

## 9. System

Behavior the system performs without direct user intervention.

| ID | Requirement | Priority |
|---|---|---|
| RF-SIS-01 | The system must **accept only images** in JPEG, PNG, WebP or HEIC format, with a size not exceeding P-02. | M |
| RF-SIS-02 | The system must **generate a thumbnail** in WebP format for each uploaded photo. | M |
| RF-SIS-03 | The system must **correct the orientation** of images according to their EXIF metadata and **remove location metadata** from images shown to third parties. | S |
| RF-SIS-04 | The system must generate each event's link with a **non-sequential, unpredictable identifier**. | M |
| RF-SIS-05 | The system must **automatically delete the photos** of an event once the retention period P-05 has elapsed since the event date. | S |
| RF-SIS-06 | The system must **notify by email** the invitee when an invitation is created for them, and the organizer before the automatic deletion of their event's photos. | S |
| RF-SIS-07 | *Retired.* | — |

**AC RF-SIS-01**
1. The system validates the actual content type, not just the extension or the type declared by the client.
2. Files that fail validation are discarded and not shown in the gallery.

**AC RF-SIS-05:** the advance notice to the organizer is sent with the lead time P-06.

---

## 10. Out of scope

The following capabilities are not part of this version (priority W):

| Capability | Note |
|---|---|
| Video uploads | — |
| Payments and subscription plans | — |
| Self-service organizer registration | Events are assigned only by an administrator, through an OWNER invitation. |
| Comments and reactions on photos | — |
| Photo editing | Includes filters and cropping. |
| Facial recognition and automatic grouping by person | — |
| Native mobile app | The application is web-based and responsive on mobile devices. |
| Event gallery for guests | Guests do not view other guests' photos. Requirements RF-INV-07, RF-INV-10, RF-ORG-21, RF-MOD-07 and RF-SIS-07 retired. |
