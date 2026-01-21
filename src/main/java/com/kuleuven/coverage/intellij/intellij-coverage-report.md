# IntelliJ Coverage Binary Export – Hierarchical Structure

## 1. Root level

### `ProjectData`

**Role:** Root container of the coverage snapshot

**Contains:**

* `Collection<ClassData>` → *dynamic execution data*
* `Map<String, ClassInstructions>` → *static instruction-count data*

```
ProjectData
├── ClassData*              (dynamic)
└── ClassInstructions*      (static)
```

---

## 2. Class level

### 2.1 `ClassData` (dynamic)

**Represents:** One Java class, line-centric

**Contains:**

* class name
* optional source file name
* `LineData[]` indexed by **line number**

```
ClassData
└── LineData[]   (sparse array, indexed by source line)
```

Methods are **not primary entities**; they are inferred later by grouping lines.

---

### 2.2 `ClassInstructions` (static)

**Represents:** Per-line bytecode instruction-count model for one class

**Contains:**

* `LineInstructions[]` indexed by **line number**

```
ClassInstructions
└── LineInstructions[]   (parallel to LineData[])
```

This array is aligned *by line number* with `LineData`.

---

## 3. Line level

Everything meaningful happens at the **line level**.

## 3.1 `LineData` — dynamic execution facts

**Contains:**

* `hits` → how many times the line executed
* `BranchData` → summary of branch outcomes
* `JumpData[]` → conditional jumps
* `SwitchData[]` → switch statements

```
LineData
├── hits
├── BranchData
├── JumpData[]        (if / && / || / ?:)
└── SwitchData[]      (switch statements)
```

---

### 3.1.1 `BranchData` (dynamic summary)

**Contains:**

* `totalBranches`
* `coveredBranches`

This is **derived from `JumpData` and `SwitchData`**, but stored eagerly for fast aggregation.

---

### 3.1.2 `JumpData` (dynamic)

**Represents:** One conditional jump on the line

**Contains:**

* `trueHits`
* `falseHits`

```
JumpData
├── trueHits
└── falseHits
```

No instruction counts here — **only execution facts**.

---

### 3.1.3 `SwitchData` (dynamic)

**Represents:** One switch statement on the line

**Contains:**

* `defaultHits`
* `keys[]`
* `hits[]` per key

```
SwitchData
├── defaultHits
├── keys[]
└── hits[]
```

Again: **only dynamic execution info**.

---

## 3.2 `LineInstructions` — static bytecode instruction counts

**Answers:**
*“How many JVM bytecode instructions belong to each linear control-flow segment on this source line?”*

**Contains:**

* `myInstructions` → count of straight-line bytecode instructions
* `JumpInstructions[]`
* `SwitchInstructions[]`

```
LineInstructions
├── myInstructions            (straight-line bytecode count)
├── JumpInstructions[]        (per conditional)
└── SwitchInstructions[]      (per switch)
```

This data is computed **at instrumentation time**, by counting actual bytecode instructions between labels and control-flow boundaries.

---

### 3.2.1 `JumpInstructions`

**Represents:** Bytecode instruction counts per jump outcome

**Contains:**

* `getInstructions(true)`
* `getInstructions(false)`

```
JumpInstructions
├── TRUE  → bytecode instruction count
└── FALSE → bytecode instruction count
```

These are **literal JVM instruction counts**, not CFG dominance weights.

---

### 3.2.2 `SwitchInstructions`

**Represents:** Bytecode instruction counts per switch outcome

**Contains:**

* `getInstructions(-1)` → default
* `getInstructions(caseIndex)`

```
SwitchInstructions
├── default → bytecode instruction count
└── case[i] → bytecode instruction count
```

---

## 4. Where static and dynamic data meet

Static and dynamic data are **never merged in the binary format itself**.

They are combined only via:

### `LineInstructions.getInstructionsData(LineData)`

This method:

1. Starts with `myInstructions`
2. Adds bytecode instruction counts for jump outcomes
3. Adds bytecode instruction counts for switch outcomes
4. Uses `JumpData` / `SwitchData` to check which outcomes executed
5. Produces:

```
BranchData(totalInstructions, coveredInstructions)
```

This is what yields numbers like:

```
instr = 6 / 7
```

---

## 5. Full hierarchy overview (compact)

```
ProjectData
├── ClassData (dynamic)
│   └── LineData
│       ├── hits
│       ├── BranchData (dynamic summary)
│       ├── JumpData[]
│       │   └── trueHits / falseHits
│       └── SwitchData[]
│           └── defaultHits / caseHits
│
└── ClassInstructions (static)
    └── LineInstructions
        ├── myInstructions
        ├── JumpInstructions[]
        │   └── TRUE / FALSE instruction counts
        └── SwitchInstructions[]
            └── default / case instruction counts
```

---

## 6. What this model fundamentally is (corrected key insight)

IntelliJ Coverage models **bytecode-level execution structure at source-line granularity**, using:

* **dynamic outcome hits** (what executed)
* **static bytecode instruction counts** (how many instructions lie in each control-flow segment)

> The IntelliJ coverage binary export is a **two-layer model**:
> **dynamic execution facts per line** + **static bytecode instruction counts per line**, merged only at reporting time.
