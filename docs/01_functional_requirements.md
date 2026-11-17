# Functional Requirements Specification

| Field | Value |
|---|---|
| Project | Pictures POV App |
| Document | Functional requirements |
| Version | 1.2 |
| Status | Ready |
| Date | 2026-11-17 |
| Owner | Oscar |

## Change history

| Version | Date | Description |
|---|---|---|
| 0.1 | 2026-10-06 | Initial version. |
| 0.2 | 2026-10-06 | The ZIP download is generated in the background, stored in Amazon S3 and offered through a temporary link on the event page; the ZIP email notification is removed (RF-ORG-14, RF-SIS-06). |
| 0.3 | 2026-10-08 | Document translated into English. Requirement identifiers are unchanged. |
| 0.4 | 2026-10-27 | New Moderator role and event invitations (OWNER, MODERATOR). Added RF-AUT-05, RF-ORG-22, RF-ORG-23 and RF-MOD-01 to RF-MOD-07. Event assignment uses an OWNER invitation (RF-ADM-02, RF-ADM-03). Updated RF-AUT-01 to RF-AUT-03, RF-ADM-09 to RF-ADM-11, RF-ADM-13, RF-ORG-01, RF-ORG-21, RF-INV-07 and RF-SIS-06. Sections 7 to 9 renumbered to 8 to 10. |
| 0.5 | 2026-10-28 | Guests only upload photos and delete their own; they no longer view the event gallery, and they can do nothing once the event is not Active. Added RF-INV-13. Retired RF-ORG-21, RF-MOD-07, RF-INV-07, RF-INV-10 and RF-SIS-07. Updated the Gallery glossary entry, RF-INV-08, RF-INV-11, AC RF-MOD-06 and section 10. |
| 0.6 | 2026-11-03 | RF-INV-05 shows the status of each upload instead of its progress; added AC RF-INV-05. A percentage progress bar is out of scope (section 10). |
| 0.7 | 2026-11-17 | Glossary moved to the root `GLOSSARY.md`; section 3 now links to it. Definitions are unchanged. |
| 0.8 | 2026-11-17 | Added section 3 with business rules RN-01 to RN-09 and parameters P-01 to P-12, cited but never defined before; P-11 retired with the protected gallery. Glossary link moved to section 1.3; fixed the link to the non-functional requirements. |
| 0.9 | 2026-11-17 | Organizers and moderators share one list of events, labelled by role (AC RF-ORG-01). Invitations are accepted from that list (AC RF-AUT-05.6). Reassigning to a current moderator is rejected (AC RF-ADM-03.3). Declining an invitation is out of scope. |
| 1.0 | 2026-11-17 | Acceptance criteria moved to the GitHub issues, so each criterion has one home; this document keeps requirements, business rules, parameters and scope. |
| 1.1 | 2026-11-17 | RN-06 defines the event lifecycle transitions. |
| 1.2 | 2026-11-17 | Full review. Added RF-AUT-06 (create an account), RF-ORG-24 (gallery) and RN-10 (deleting a user). Sign-in with Google out of scope (RF-AUT-01, RN-04). RN-06: states Open, Closed and Archived; events start Open; deletion is not a state. Hiding affects only the gallery (RF-ORG-14, RF-ORG-16, RF-ORG-20). Location removed from thumbnails only (RF-SIS-03). Moderator permissions as an allow-list (section 7). Administrator manages users (RF-ADM-09 to RF-ADM-11). Out of scope: "self-service event creation". Fixed the link to section 3. |

---

## 1. Introduction

### 1.1 Purpose

This document defines the functional capabilities of Pictures POV App, a web application that allows the guests of an event to upload photos through a QR code, without creating an account and without seeing other guests' photos, and allows organizers and moderators to view, moderate and download those photos.

### 1.2 Scope

The document covers the functional requirements of the system's four roles (administrator, organizer, moderator and guest) and the system's automatic behavior, plus the business rules and parameters they share (section 3). Non-functional requirements are specified in a separate document.

### 1.3 Related documents

| Document | Description |
|---|---|
| [02_non_functional_requirements.md](02_non_functional_requirements.md) | Non-functional requirements (RNF) |
| [GLOSSARY.md](../GLOSSARY.md) | Domain terms |

---

## 2. Conventions

- **Identifier.** Each requirement has a stable identifier in the format `RF-<AREA>-<NN>`. An identifier is never reused; removed requirements are marked as *Retired*.
- **Areas.** `AUT` authentication, `ADM` administrator, `ORG` organizer, `MOD` moderator, `INV` guest, `SIS` system.
- **Wording.** Each requirement describes a single verifiable capability, in the form "The system must allow the [role] to …" or "The system must …".
- **Priority (MoSCoW).** **M** (Must): mandatory for the first version. **S** (Should): important, not blocking. **C** (Could): desirable. **W** (Won't): out of scope for this version.
- **Acceptance criteria.** Live only in the GitHub issue that implements each requirement; the issue cites the requirement ID.
- **References.** `RN-NN` refers to a business rule and `P-NN` to a configurable parameter, both defined in [section 3](#3-business-rules-and-parameters).

## 3. Business rules and parameters

### 3.1 Business rules

Identifier format `RN-<NN>`, never reused.

| ID | Rule |
|---|---|
| RN-01 | A device can upload at most the event's per-device photo limit (P-01 by default), and an event at most P-03 photos in total. |
| RN-02 | A device is identified only by a cookie; it is not a verified identity. |
| RN-03 | Hiding or deleting a photo does not restore quota. |
| RN-04 | A user accesses an event only through an *Accepted* invitation whose email matches their verified email. Each email maps to one account. |
| RN-05 | The permissions of each role are fixed (administrator, organizer, moderator; section 7 for the moderator) and are checked on the server for every request. |
| RN-06 | An event is Open, Closed or Archived, and is created Open; only Open accepts guests. The organizer closes and reopens uploads; the upload window, if set, opens them at its start and closes them at its end, and a manual change in between stands. When P-05 elapses after the event date, the event becomes Archived and its photos are deleted; Archived is final. An administrator can delete an event in any state; it then disappears for everyone (RN-07). |
| RN-07 | Deletion takes effect for users immediately; the file is physically removed after P-04. |
| RN-08 | Original photos and ZIP files are accessible only through signed, temporary links (P-07, P-12). ZIP generation per event is limited by P-08. |
| RN-09 | Blocking an organizer does not affect guests: their events remain accessible. |
| RN-10 | A user who is the organizer of an event that is not Archived cannot be deleted until it is reassigned or deleted. Deleting a user removes their MODERATOR invitations. |

### 3.2 Parameters

Identifier format `P-<NN>`, never reused.

| ID | Parameter | Value |
|---|---|---|
| P-01 | Default per-device photo limit | 10 photos |
| P-02 | Maximum file size | 20 MB |
| P-03 | Maximum photos per event | 2,000 photos |
| P-04 | Grace period before a deleted file is physically removed | 7 days |
| P-05 | Photo retention after the event date | 90 days |
| P-06 | Lead time of the notice before automatic deletion | 7 days |
| P-07 | Validity of a ZIP download link | 24 hours |
| P-08 | ZIP generations per event per day | 3 |
| P-09 | Maximum time to invalidate a blocked user's sessions | 1 hour |
| P-10 | Time allowed to complete an authorized upload | 15 minutes |
| P-11 | *Retired.* | — |
| P-12 | Validity of a single-photo download link | 15 minutes |

---

## 4. Authentication

Applies to users: anyone with an account. Guests do not authenticate.

| ID | Requirement | Priority |
|---|---|---|
| RF-AUT-01 | The system must allow users to **sign in** with email and password. | M |
| RF-AUT-02 | The system must allow users to **sign out**. | M |
| RF-AUT-03 | The system must allow users to **recover their password** by email. | M |
| RF-AUT-04 | The system must require **multi-factor authentication** for administrators. | S |
| RF-AUT-05 | The system must allow a user to **accept an invitation** addressed to their verified email (RN-04). | M |
| RF-AUT-06 | The system must allow anyone to **create an account** with email and password, confirmed by a code sent to that email. | M |

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

### 5.2 User management

| ID | Requirement | Priority |
|---|---|---|
| RF-ADM-09 | The system must allow the administrator to **search users** by email. | M |
| RF-ADM-10 | The system must allow the administrator to **block and unblock** a user (P-09, RN-09). | M |
| RF-ADM-11 | The system must allow the administrator to **delete** a user (RN-10). | S |
| RF-ADM-12 | The system must allow the administrator to **grant and revoke the administrator role** for another user. | S |

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
| RF-ORG-04 | The system must allow the organizer to **close and reopen uploads** manually, regardless of the upload window (RN-06). | S |
| RF-ORG-05 | The system must show the organizer **event statistics**: uploaded photos, participating devices and hidden photos. | C |

### 6.2 Customization

| ID | Requirement | Priority |
|---|---|---|
| RF-ORG-06 | The system must allow the organizer to **change the event name**. | M |
| RF-ORG-07 | The system must allow the organizer to **upload or change the event's cover photo**. | S |
| RF-ORG-08 | The system must allow the organizer to **choose the event's main color**. | S |
| RF-ORG-09 | The system must allow the organizer to **set a welcome message** for guests. | C |
| RF-ORG-21 | *Retired.* | — |

### 6.3 Viewing and downloading photos

The **gallery** shows an event's visible photos; the **review** shows all of them, hidden ones included, and is where photos are hidden or shown.

| ID | Requirement | Priority |
|---|---|---|
| RF-ORG-24 | The system must allow the organizer to **view the event's gallery**. | M |
| RF-ORG-10 | The system must allow the organizer to **review all photos** of their event, including hidden ones. | M |
| RF-ORG-11 | The system must allow the organizer to **view each photo at full size** and navigate between them. | M |
| RF-ORG-12 | The system must allow the organizer to **filter and group photos by uploader**. | S |
| RF-ORG-13 | The system must allow the organizer to **download an individual photo** at its original resolution. | M |
| RF-ORG-14 | The system must allow the organizer to **download all of the event's photos**, hidden ones included, in a ZIP file (RN-08). | M |
| RF-ORG-15 | The system must allow the organizer to **select several photos** and download them in a ZIP file. | C |

### 6.4 Moderation

| ID | Requirement | Priority |
|---|---|---|
| RF-ORG-16 | The system must allow the organizer to **hide** a photo from the gallery. | M |
| RF-ORG-17 | The system must allow the organizer to **show again** a hidden photo. | M |
| RF-ORG-18 | The system must allow the organizer to **delete** a photo. | M |
| RF-ORG-19 | The system must allow the organizer to **hide or delete all photos from a device** in a single action. | S |
| RF-ORG-20 | The system must allow the organizer to choose between **post-moderation**, in which photos enter the gallery when uploaded, and **pre-moderation**, in which they start hidden until shown. | C |

### 6.5 Moderators

| ID | Requirement | Priority |
|---|---|---|
| RF-ORG-22 | The system must allow the organizer to **invite a moderator** by email, even if the invitee does not yet have an account. | M |
| RF-ORG-23 | The system must allow the organizer to **remove a moderator** or a pending moderator invitation. | M |

---

## 7. Moderator

A moderator can do only what this section lists, and only on events where they hold an accepted MODERATOR invitation; anything else is refused with 403 (RN-05).

| ID | Requirement | Priority |
|---|---|---|
| RF-MOD-01 | The system must allow the moderator to **view the list of the events they moderate**. | M |
| RF-MOD-02 | The system must allow the moderator to **view the gallery and review all photos** at full size (as in RF-ORG-24, RF-ORG-10 and RF-ORG-11). | M |
| RF-MOD-03 | The system must allow the moderator to **filter and group photos by uploader** (as in RF-ORG-12). | S |
| RF-MOD-04 | The system must show the moderator **event statistics** (as in RF-ORG-05). | C |
| RF-MOD-05 | The system must allow the moderator to **get the event link and download its QR code** (as in RF-ORG-02 and RF-ORG-03). | M |
| RF-MOD-06 | The system must allow the moderator to **hide** a photo and **show again** a hidden photo (as in RF-ORG-16 and RF-ORG-17). | M |
| RF-MOD-07 | *Retired.* | — |

---

## 8. Guest

A guest can only upload photos and delete the photos they uploaded, and only while the event is Open (RN-06). A guest never sees photos uploaded by other devices.

| ID | Requirement | Priority |
|---|---|---|
| RF-INV-01 | The system must allow the guest to **access the event page** from the QR code or the link **without signing in**. | M |
| RF-INV-02 | The system must allow the guest to **upload photos from their device's gallery or by taking them with the camera**. | M |
| RF-INV-03 | The system must **limit the number of photos** a device can upload to an event (RN-01). | M |
| RF-INV-04 | The system must show the guest **the number of photos they have left** to upload. | M |
| RF-INV-05 | The system must allow the guest to **select several photos at once**, must show the **status** of each upload and must allow **retrying** a failed upload. | M |
| RF-INV-06 | The system must allow the guest to **provide their name** to identify their photos. | S |
| RF-INV-07 | *Retired.* | — |
| RF-INV-08 | The system must allow the guest to **view the photos they uploaded** from their device. | M |
| RF-INV-09 | *Retired.* | — |
| RF-INV-10 | *Retired.* | — |
| RF-INV-11 | The system must inform the guest when the event **is not available**. | M |
| RF-INV-12 | The system must ask the guest to **accept the terms of use** before their first upload. | S |
| RF-INV-13 | The system must allow the guest to **delete a photo they uploaded**. | M |

---

## 9. System

Behavior the system performs without direct user intervention.

| ID | Requirement | Priority |
|---|---|---|
| RF-SIS-01 | The system must **accept only images** in JPEG, PNG, WebP or HEIC format, with a size not exceeding P-02. | M |
| RF-SIS-02 | The system must **generate a thumbnail** in WebP format for each uploaded photo. | M |
| RF-SIS-03 | The system must **correct the orientation** of thumbnails according to their EXIF metadata and keep **no location metadata** in them; originals are kept unchanged. | S |
| RF-SIS-04 | The system must generate each event's link with a **non-sequential, unpredictable identifier**. | M |
| RF-SIS-05 | The system must **automatically delete the photos** of an event once the retention period P-05 has elapsed since the event date. | S |
| RF-SIS-06 | The system must **notify by email** the invitee when an invitation is created for them, and the organizer before the automatic deletion of their event's photos. | S |
| RF-SIS-07 | *Retired.* | — |

---

## 10. Out of scope

The following capabilities are not part of this version (priority W):

| Capability | Note |
|---|---|
| Video uploads | — |
| Sign-in with Google | Email and password only ([ADR 0018](adr/0018-self-sign-up-email-only.md)). |
| Payments and subscription plans | — |
| Self-service event creation | Anyone can create an account (RF-AUT-05), but events are assigned only by an administrator, through an OWNER invitation (RN-04). |
| Declining an invitation | A *Pending* invitation stays pending until accepted or removed (RF-ORG-23, RF-ADM-03). |
| Comments and reactions on photos | — |
| Photo editing | Includes filters and cropping. |
| Facial recognition and automatic grouping by person | — |
| Native mobile app | The application is web-based and responsive on mobile devices. |
| Percentage progress bar per upload | Photos upload in a few seconds; the status of each upload is shown instead (RF-INV-05). |
| Event gallery for guests | Guests do not view other guests' photos. Requirements RF-INV-07, RF-INV-10, RF-ORG-21, RF-MOD-07 and RF-SIS-07 retired. |
