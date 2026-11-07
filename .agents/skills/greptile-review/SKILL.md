---
name: greptile-review
description: Run Greptile CLI for pre-push AI branch review. Use when asked for Greptile review, or as part of the default pre-push review set with lint and test. Max 2 runs per change set.
---

# Greptile CLI Review

Run **Greptile** (`greptile`) for automated branch review before push. Pair with **`lint`** and **`test`** skills for the default pre-push review set — the tools catch different issues and mirror CI.

## Prerequisites

```bash
# Install Greptile CLI globally
npm install -g greptile@latest

# Login with your Greptile API key (get from https://app.greptile.com)
greptile login
```

## Commands

| Command | Purpose |
|---------|---------|
| `greptile review -b main --agent` | Review current branch vs main (default for agents) |
| `greptile review --diff` | Findings inline with relevant code |
| `greptile review --json` | Machine-readable output |
| `greptile review --resume` | Continue the latest unfinished review |
| `greptile review show [ID]` | Reopen a past review |

## Rules

- **Max 2** Greptile runs per change set (workflow discipline)
- **Commit before running** — Greptile reviews branch commits vs base; uncommitted changes are ignored
- **Default to `-b main`** unless the repo uses a different default branch
- Run from the repository root on the feature branch
- Run **after** `lint` and `test` skills pass

## Usage

When asked to run Greptile or as part of pre-push review:

1. Ensure changes are committed and lint/tests pass (`./mvnw verify`)
2. Run `greptile review -b main --agent` (can run parallel with other review tools)
3. Analyze findings; address critical/major issues
4. After fixes to Greptile-sensitive areas, optional verification pass (2nd run max)
5. Track run count (max 2 per change set)

## Local Setup

```bash
# 1. Install CLI
npm install -g greptile@latest

# 2. Login (requires account at https://app.greptile.com)
greptile login

# 3. Verify
greptile whoami

# 4. Run locally on feature branch
git checkout feature/my-branch
git commit -am "my changes"
greptile review -b main --agent
```

## References

- [Greptile CLI](https://www.greptile.com/cli)
- [CLI Reference](https://www.greptile.com/docs/code-review/greptile-cli.md)
- [Quickstart](https://www.greptile.com/docs/quickstart.md)