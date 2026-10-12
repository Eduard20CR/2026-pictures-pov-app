# Working agreement

| Field | Value |
|---|---|
| Project | Pictures POV App |
| Document | Working agreement (Scrum) |
| Version | 0.1 |
| Status | Ready |
| Date | 2026-10-12 |
| Owner | Oscar |

## Change history

| Version | Date | Description |
|---|---|---|
| 0.1 | 2026-10-12 | Initial version. |

---

## 1. Purpose

This document defines how the project is planned and delivered with a lightweight adaptation of Scrum for a single developer. The branching, commit and pull request rules are defined in the Git workflow guide.

## 2. Roles

| Role | Holder | Responsibility |
|---|---|---|
| Product Owner | Oscar | Owns and orders the Product Backlog; accepts completed items. |
| Developer | Oscar | Plans and delivers the Sprint Backlog. |
| Scrum Master | Oscar, with external mentoring | Protects the process and runs the retrospective. |

## 3. Artifacts

| Artifact | Where it lives |
|---|---|
| Product Backlog | Open issues without a milestone, ordered on the project board. |
| Sprint Backlog | Issues assigned to the current `Sprint N` milestone. |
| Sprint Goal | Description of the sprint milestone. |
| Increment | Everything merged to `main` and deployed to dev that meets the Definition of Done. |

Backlog items are classified with labels:

| Label | Meaning |
|---|---|
| `type: epic` | Large goal that groups stories as sub-issues. Not estimated. |
| `type: story` | User story with value for a guest, organizer or administrator. |
| `type: task` | Technical enabler (infrastructure, CI/CD, ADR) with no direct user value. |
| `type: spike` | Timeboxed research; produces knowledge, not production code. |
| `area: *` | Part of the system affected: `api`, `web`, `infra`, `ci`, `docs`. |
| `priority: *` | MoSCoW priority inherited from the requirements: `must`, `should`, `could`. |
| `points: *` | Relative size in story points (Fibonacci: 1, 2, 3, 5, 8, 13). |

## 4. Events

Sprints last two weeks, from Monday to the Sunday of the following week.

| Event | When | Timebox | Output |
|---|---|---|---|
| Sprint Planning | First day of the sprint | 1 h | Sprint Goal and Sprint Backlog in the milestone. |
| Daily check | Each working day | 5 min | A comment on the issue in progress: done, next, blockers. |
| Backlog Refinement | Mid-sprint | 30 min | Next items meet the Definition of Ready. |
| Sprint Review | Last day of the sprint | 30 min | Demo of the increment in dev; backlog updated. |
| Sprint Retrospective | Last day of the sprint | 30 min | A comment on the closed milestone: what went well, what to improve, one action. |

Items not finished at the end of a sprint return to the Product Backlog and are re-planned; the sprint is not extended.

## 5. Definition of Ready

An item can enter a sprint when:

- It has a clear goal or user story and verifiable acceptance criteria.
- It is estimated at 8 points or fewer; larger items are split.
- Its dependencies are done or planned in the same sprint.
- Any open question has been answered, if necessary through a spike.

## 6. Definition of Done

An item is done when:

- All acceptance criteria are met.
- The code is merged to `main` through a pull request with CI green.
- New behavior is covered by automated tests.
- The change is deployed to dev through the pipeline, with no manual steps.
- Infrastructure changes are in Terraform.
- Affected documentation is updated: ADR if a decision was made, OpenAPI contract if the API changed, and the requirements if they changed.
- No secrets are present in the code or the repository.

## 7. Estimation and velocity

Points measure relative size (effort, complexity and uncertainty), not hours. The reference item is *Get the event link* (1 point). Velocity is the sum of points completed per sprint; from the third sprint, Sprint Planning uses the average of the last three sprints as the capacity guide.
