# Pictures POV App

Guests upload event photos through a QR code without an account; organizers and moderators view, moderate and download them. Requirement IDs refer to `docs/01_functional_requirements.md`.

## Language

### Roles

**User**:
Person with an account and a verified email (RF-AUT-06). An account alone reaches no event (RN-04).

**Administrator**:
User of the platform team with permissions over all events and users.

**Organizer**:
User who holds the accepted OWNER invitation of an event. Each event has one OWNER invitation. `OWNER` names only the invitation role.
_Avoid_: event owner, owner

**Moderator**:
User who holds an accepted MODERATOR invitation for an event, with only the permissions of section 7 of the functional requirements.

**Invitation**:
Grant of a role (OWNER or MODERATOR) on one event to an email. Status *Pending* until accepted (RF-AUT-05), then *Accepted*. One invitation per event and email; emails are stored in lowercase.

**Invitee**:
Person an invitation is addressed to, known only by email until they accept it.

**Guest**:
Attendee of an event. Has no account in the system.

**Device**:
Browser identified by a unique identifier stored in a cookie, issued per event. It is not a verified identity (RN-02).

### Events

**Event**:
Celebration for which photos are collected. It has a public link, a QR code and a lifecycle (RN-06).

**Open**:
Event that accepts guest uploads. Every event starts Open.
_Avoid_: Active, Draft

**Closed**:
Event that does not accept uploads. The organizer can reopen it; organizer and moderators keep access to its photos.

**Archived**:
Event whose photos were deleted at the end of the retention period. Final.

**Upload window**:
Optional date and time interval; uploads open at its start and close at its end.

### Photos

**Gallery**:
Page with an event's visible photos. Only the organizer and moderators can view it.

**Review**:
Page with all of an event's photos, hidden ones included, where the organizer and moderators hide or show them.

**Hidden photo**:
Photo left out of the gallery. It stays in the review and in downloads, ZIP included, and can be shown again.

**Deleted photo**:
Photo permanently removed for users (RN-07).
