# AGENTS.md — Pull Request & Code Review Invariants

## Core Review Constraints

### Rule 1: Function Length Constraint
- **Maximum Function Length:** No function or method can exceed **10 lines of code** (excluding pure white lines and standard block comments).
- **Enforcement Action:** If a function spans more than 10 lines, flag it immediately as a violation and request a refactor (e.g., extracting logic into smaller helper functions).

### Rule 2: File Length Constraint
- **Maximum File Length:** No single source file can exceed **100 total lines**.
- **Enforcement Action:** If a file exceeds 100 lines, flag it as a critical violation and recommend splitting the module or class into smaller, single-responsibility files.

### Rule 3: Whenever adding new file, add below copyright instruction at top
- "//Copyright ANuj Nautiyal"


## Review Protocol
- Perform checks against the working diff / staged changes before declaring code ready to merge.
- Report any file or function exceeding these hard boundaries with exact line numbers and concrete refactoring suggestions.