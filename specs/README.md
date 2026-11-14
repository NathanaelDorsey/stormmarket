# Specs

All planning documents for this project.

## Directory Structure

```
specs/
├── product/              ← SCOPE, VISION, GLOSSARY
├── release-plan.yaml     ← WSJF epic index and version targeting
├── execution-status.yaml ← flat story/epic status
├── planning-status.yaml  ← discover-phase checklist
├── state.yaml            ← session active_flow, epic, git, handoff
├── tech-architecture/    ← tech-stack, security, test, design, refactor, impact plans
├── verifications/        ← (populated during verify phase)
├── epics/                ← eNN shards (story definitions and tasks)
├── epics/archive/        ← completed or deferred epic shards
├── adr/                  ← Architectural Decision Records
└── bugs/                 ← BUG-*.md + registry.yaml
```

## Conventions

- All YAML files follow bigpowers naming conventions (UPPER_SNAKE_CASE keys)
- Markdown files in `specs/bugs/` reference `registry.yaml` for status tracking
- `specs/state.yaml` `handoff.next_skill` indicates the next planned skill to run
- Epic IDs follow pattern `eNN` (e.g., `e01`, `e02`, `e03`)
- Story files within epics follow pattern `eNNsNN` (e.g., `e01s01`, `e03s01`)