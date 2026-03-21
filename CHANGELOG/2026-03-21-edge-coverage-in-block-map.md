# Edge/Branch Coverage in the Coverage Block Map

**Date:** 2026-03-21

## Summary

Added edge-level coverage information to `BlockDataDTO` so that JDart can determine which specific branches are covered and what type of branching statement each edge represents. The ICFG coverage graph visualization now colors edges based on coverage.

## Changes

### intellij-coverage-model (shared model)

- **New `BranchType` enum** (`blockmap.model.BranchType`): `NORMAL`, `IF_TRUE`, `IF_FALSE`, `SWITCH_CASE`, `SWITCH_DEFAULT`, `GOTO`.
- **New `EdgeCoverageDTO` class** (`blockmap.model.EdgeCoverageDTO`): represents a single outgoing edge with `targetBlockId`, `branchIndex`, `branchType`, `hits`, and `switchCaseKey`.
- **Modified `BlockDataDTO`**: added `edges` field (`List<EdgeCoverageDTO>`). Non-final with inline default `Collections.emptyList()` for backward-compatible Gson deserialization. New constructor overload added; old constructor preserved.

### pathcov

- **`BlockMapBuilder`**: new methods `buildEdgeCoverage`, `determineBranchType`, `determineBranchIndex`, `resolveEdgeHits`, `resolveIfEdgeHits`, `resolveSwitchEdgeHits`, `computeJumpIndexForBlock`, `resolveSwitchCaseKey`. These map IntelliJ jump/switch hit data from `LineDTO` to CFG edges. Multiple `JIfStmt` blocks on the same source line are disambiguated by counting in CFG iteration order.
- **`DotExporter`** (ICFG visualization): edges are now colored based on `EdgeCoverageDTO` data from the block map: `palegreen3` (covered), `lightcoral` (uncovered), `gray` (no data).

## Branch semantics: bytecode vs source-level condition negation

There are three representations of IF branch semantics in this project, and they do NOT all agree on what "true" means:

| System | "true" / index 0 means | "false" / index 1 means |
|---|---|---|
| **Java bytecode** | condition satisfied → jump taken | condition not satisfied → fall through |
| **IntelliJ coverage** (`JumpDTO`) | bytecode jump taken (= source `else`) | bytecode fall through (= source `then`) |
| **SootUp Jimple** (`JIfStmt`) | source condition true (= bytecode fall through) | source condition false (= bytecode jump taken) |

Java compiles `if (cond) { then } else { else }` as `if (!cond) goto else_label; then; goto end; else_label: else`. The bytecode condition is always **negated** relative to source. SootUp re-negates it back to source semantics.

### Consequence for EdgeCoverageDTO

- **`branchType`**: uses **source-level** semantics. `IF_TRUE` = source condition is true. This is unambiguous.
- **`branchIndex`**: uses **source-level** convention (0 = source true, 1 = source false). This does **not** match JDart's bytecode-level convention (0 = bytecode jump taken = source false).
- **`hits`**: correctly mapped. IntelliJ's `trueBranch`/`falseBranch` (bytecode-level) is swapped to align with SootUp's source-level successors.

### How JDart should consume edge coverage

JDart operates at bytecode level, so its `branchIdx` follows bytecode convention. **Do not match by `branchIndex`** — match by `branchType` instead:

- JDart `branchIdx 0` (bytecode jump taken) → corresponds to `IF_FALSE` (source condition false)
- JDart `branchIdx 1` (bytecode fall through) → corresponds to `IF_TRUE` (source condition true)

For SWITCH statements there is no inversion — `branchIndex` and `switchCaseKey` can be used directly.

## Backward Compatibility

- The `edges` field is purely additive. Old JSON without `edges` deserializes to `Collections.emptyList()`.
- `successorBlockIds` is unchanged.
- Block hashing, branch coverage calculation, and block diff are unaffected.
