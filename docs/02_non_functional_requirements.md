# Non-Functional Requirements Specification

| Field | Value |
|---|---|
| Project | Pictures POV App |
| Document | Non-functional requirements |
| Version | 0.4 |
| Status | Draft |
| Date | 2026-11-17 |
| Owner | Oscar |

## Change history

| Version | Date | Description |
|---|---|---|
| 0.1 | 2026-10-06 | Initial version. |
| 0.2 | 2026-10-06 | ZIP files included in RNF-SEG-05; RNF-REN-06 measures background generation. |
| 0.3 | 2026-10-28 | Guests no longer view the event gallery: RNF-REN-01 measures the organizer's gallery; RNF-REN-05 retired. |
| 0.4 | 2026-11-17 | Translated into English. Identifiers and content are unchanged. |

---

## 1. Conventions

- **Identifier.** `RNF-<CATEGORY>-<NN>`, never reused; removed requirements are marked *Retired*. Categories: `SEG` security, `PRI` privacy, `REN` performance, `CAP` capacity, `DIS` availability, `USA` usability and compatibility, `OPE` operations and maintainability, `COS` cost.
- **Measurement.** Each requirement has a measurable criterion. Percentiles (p95) are computed over a 30-day window in production unless stated otherwise.
- **Priority.** MoSCoW, as in the [functional requirements](01_functional_requirements.md), which also define the RF, RN and P identifiers cited here.

## 2. Security

| ID | Requirement | Priority |
|---|---|---|
| RNF-SEG-01 | All communication between clients and the system must be encrypted with TLS 1.2 or higher. | M |
| RNF-SEG-02 | Authorization of every operation must be validated on the server, regardless of what the interface shows (RN-04, RN-05). | M |
| RNF-SEG-03 | The system must validate the real type of each uploaded file from its content, not its extension or the type declared by the client. Files that fail validation must not be published (RF-SIS-01). | M |
| RNF-SEG-04 | Each event's public identifier must have at least 128 bits of entropy and not be sequential (RF-ORG-02). | M |
| RNF-SEG-05 | Photos and ZIP files must not be accessible through permanent public URLs; access must use signed, temporary links (RN-08, RF-ORG-14). | M |
| RNF-SEG-06 | Endpoints accessible without authentication must rate-limit requests per device and per IP address. | S |
| RNF-SEG-07 | Credentials and secrets must not be stored in the source code or the repository. | M |

## 3. Privacy

| ID | Requirement | Priority |
|---|---|---|
| RNF-PRI-01 | Images shown to guests must not contain location metadata. | S |
| RNF-PRI-02 | The system must store about guests only the device identifier and, if provided, their name. | M |
| RNF-PRI-03 | Photos deleted by retention or permanent deletion must not be recoverable, backups included, once 35 days have passed since deletion. | S |

## 4. Performance

| ID | Requirement | Priority |
|---|---|---|
| RNF-REN-01 | The event gallery, viewed by the organizer or a moderator, must show the first 30 thumbnails in under 3 seconds (p95) on a 4G mobile connection. | M |
| RNF-REN-02 | Each thumbnail must take at most 100 KB. | S |
| RNF-REN-03 | An uploaded photo must be available in the gallery in under 10 seconds (p95) after the upload finishes. | S |
| RNF-REN-04 | API operations, excluding file transfer, must respond in under 500 ms (p95) and under 1.5 s (p99). | S |
| RNF-REN-05 | *Retired.* | — |
| RNF-REN-06 | Background generation of the ZIP file of a 2,000-photo event, from the request until the download link is available on the event page, must complete in under 15 minutes (RF-ORG-14). | S |

## 5. Capacity

| ID | Requirement | Priority |
|---|---|---|
| RNF-CAP-01 | The system must support 300 devices uploading photos simultaneously to one event, with an error rate below 1%. | M |
| RNF-CAP-02 | The system must support 20 events in the Active state simultaneously without degrading the performance targets of section 4. | S |

## 6. Availability and recovery

| ID | Requirement | Priority |
|---|---|---|
| RNF-DIS-01 | Monthly availability of photo upload and the organizer's gallery must be 99.5% or higher. | S |
| RNF-DIS-02 | Retrying an interrupted upload must not consume additional quota or duplicate the photo (RF-INV-05). | M |
| RNF-DIS-03 | If the database is lost, the recovery point (RPO) must not exceed 24 hours and the recovery time (RTO) must not exceed 4 hours. | S |
| RNF-DIS-04 | Photos must be stored in a service with redundancy across at least three availability zones. | M |

## 7. Usability and compatibility

| ID | Requirement | Priority |
|---|---|---|
| RNF-USA-01 | The system must work on the last two major versions of Safari for iOS, Chrome for Android, and desktop Chrome, Safari, Firefox and Edge. | M |
| RNF-USA-02 | The interface must be usable on screens from 360 px wide. | M |
| RNF-USA-03 | A guest must be able to start their first upload within three interactions of opening the event link. | S |
| RNF-USA-04 | The interface must meet WCAG 2.1 level AA. | S |
| RNF-USA-05 | The interface must be available in Spanish and English. | M |

## 8. Operations and maintainability

| ID | Requirement | Priority |
|---|---|---|
| RNF-OPE-01 | All infrastructure must be defined as code and deployed without manual changes. | M |
| RNF-OPE-02 | The system must have two isolated environments, development and production. | M |
| RNF-OPE-03 | Every change must be deployed through an automated process that runs tests before publishing. | M |
| RNF-OPE-04 | The system must emit structured logs with a correlation identifier per request, kept for 30 days. | S |
| RNF-OPE-05 | The system must alert when the API error rate exceeds 5% for 5 consecutive minutes. | S |
| RNF-OPE-06 | The API contract must be documented in OpenAPI format and generated from the code. | S |

## 9. Cost

| ID | Requirement | Priority |
|---|---|---|
| RNF-COS-01 | The infrastructure cost of each environment with no traffic must not exceed 25 USD per month. | S |
| RNF-COS-02 | The infrastructure cost attributable to a 300-photo event, over its whole lifecycle, must not exceed 2 USD. | S |
