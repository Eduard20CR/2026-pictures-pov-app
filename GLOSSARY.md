# Pictures POV App

Guests upload event photos through a QR code without an account; organizers and moderators view, moderate and download them. Requirement IDs refer to `docs/01_functional_requirements.md`.

## Language

### Roles

**Administrator**:
Member of the platform team with permissions over all events and users.

**Organizer**:
Customer who holds an accepted OWNER invitation for an event. Each event has exactly one organizer. `OWNER` names only the invitation role.
_Avoid_: event owner, owner

**Moderator**:
User who holds an accepted MODERATOR invitation for an event, with the limited permissions of section 7 of the functional requirements.

**Invitation**:
Grant of a role (OWNER or MODERATOR) on one event to an email. Status *Pending* until accepted (RF-AUT-05), then *Accepted*. One invitation per event and email; emails are stored in lowercase.

**Guest**:
Attendee of an event. Has no account in the system.

**Device**:
Browser identified by a unique identifier stored in a cookie, issued per event. It is not a verified identity (RN-02).

### Events and photos

**Event**:
Celebration for which photos are collected. It has a public link, a QR code and a lifecycle (RN-06).

**Draft**:
Event created but not yet open to guests.

**Active**:
Event open to guests for uploads.

**Closed**:
Event whose uploads have ended; organizer and moderators keep access to its photos.

**Archived**:
Event whose photos were deleted at the end of the retention period. Final.

**Deleted event**:
Event removed by an administrator. Final.

**Gallery**:
Set of visible photos of an event. Only the organizer and moderators can view it.

**Hidden photo**:
Photo that is not shown in the gallery but is kept and can be shown again.

**Deleted photo**:
Photo permanently removed for users (RN-07).

**Upload window**:
Date and time interval during which guests can upload photos to an event.
