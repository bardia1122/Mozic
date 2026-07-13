# Project governance (F1 — GitHub setup)

The F1 scaffold (multi-module Gradle build, convention plugins, version catalog,
Hilt, CI) lives in the repository. Two F1 items are configured in the GitHub UI
(or via `gh`/API) rather than in code and must be done once by a repo admin.

## 1. Branch protection on `main`

Settings → Branches → Add rule for `main`:

- Require a pull request before merging — **1 approval**.
- Require status checks to pass before merging — select the **CI / Build &
  checks** job from `.github/workflows/ci.yml`.
- Require branches to be up to date before merging.
- Do not allow bypassing the above (applies to admins too).
- Require linear history (we squash-merge; see CONTRIBUTING).

Via CLI:

```bash
gh api -X PUT repos/:owner/:repo/branches/main/protection \
  -H "Accept: application/vnd.github+json" \
  -f 'required_status_checks[strict]=true' \
  -f 'required_status_checks[contexts][]=Build & checks' \
  -f 'enforce_admins=true' \
  -F 'required_pull_request_reviews[required_approving_review_count]=1' \
  -f 'restrictions=' 
```

## 2. Project board

Create a GitHub Project (Projects → New project → Board) with columns
**To Do → In Progress → Done**, per CONTRIBUTING §10. Every numbered step
(F*, A1–A6, B1–B6, C1–C6) is an issue with an owner and an area label; the PR
closes it via `Closes #NN`.

## Reviewer routing (from the plans)

- All `:core:*` PRs → **architecture owner** (Person B).
- `:core:network` (C2) → **Person B** (its main consumer).
- `DownloadState` seam (A4) → **Person B**.
- `:app` `B6` downloads/smart-play seam → **Person A**.
- Song-share card (I1) → **Person A**.
