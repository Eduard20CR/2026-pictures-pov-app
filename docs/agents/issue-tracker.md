# Issue tracker: GitHub

Issues live in GitHub Issues on `Eduard20CR/2026-pictures-pov-app`. Use `gh` (2.94+).

| Operation | Command |
|---|---|
| Create | `gh issue create --title "..." --body-file - --label "..."`; add `--parent <epic>` and `--blocked-by <n>,<n>` |
| Read | `gh issue view <n> --json number,title,body,labels,comments` |
| List | `gh issue list --state open --json number,title,labels` with `--label` filters |
| Sub-issue | `gh issue edit <parent> --add-sub-issue <child>` |
| Block | `gh issue edit <child> --add-blocked-by <blocker>`; startable when every blocker is closed |
| Open blockers | `gh api repos/{owner}/{repo}/issues/<n> --jq .issue_dependencies_summary.blocked_by` (0 = startable) |
| Comment / label | `gh issue comment <n> --body "..."` / `gh issue edit <n> --add-label "..."` |
| Close | comment the reason, then `gh issue close <n>` |

## Conventions

- Labels: `type:` epic (groups issues as sub-issues, not estimated) / story (value a user sees: "As a guest…") / technical (a system need users never see: "As the system, I need…") / bug (a defect: what happens versus what should happen, with steps to reproduce). Issues state the need, never the implementation; that is decided when the issue is refined. No tasks or spikes; `area:` api, web, infra, ci, docs; `priority:` must, should, could (MoSCoW); `points:` 1, 2, 3, 5, 8, 13. Items over 8 points are split. Stories use *Given / when / then* criteria and end with a `## References` line citing `RF-`, `RN-`, `P-` or `RNF-` IDs.
- Acceptance criteria live only in issues; the requirements documents hold one line per requirement.
- "Publish to the issue tracker" means create a GitHub issue. "Fetch the ticket" means **Read** above.
- **PRs as a request surface: no.**

## Leftovers

A **leftover** is anything found but not resolved in the current work: a review finding, a follow-up, a risk, a stale doc, a manual step still owed. Every leftover gets a record on the tracker before the work is reported done:

| Case | Record |
|---|---|
| An open issue or epic already covers it | Comment there, naming the finding and the PR or commit that surfaced it |
| It came from a story's work | New issue as a sub-issue of that story, so the story stays open until it is resolved |
| Otherwise | New issue (story, technical or bug) under the closest epic |

New leftover issues take every label above plus `ready-for-agent`. The final report lists each leftover with its issue or comment link; a leftover without a link is still open work.

## Wayfinding (`/wayfinder`)

- **Map:** one issue labelled `wayfinder:map`; tickets are its sub-issues labelled `wayfinder:<research|prototype|grilling|task>`.
- **Frontier:** open sub-issues with no open blocker and no assignee; first in map order wins.
- **Claim:** `gh issue edit <n> --add-assignee @me`. **Resolve:** comment the answer, close, add a gist + link to the map's Decisions so far.
