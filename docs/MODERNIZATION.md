# HiTorro Modernization & New Features

Summary of improvements to the type system, projection pipeline, and data mapper.

## 1. Type Validation (`JVSValidator`)

Validates a JVS document against a type definition, reporting missing fields, type mismatches, and unexpected fields. Catches data issues before indexing.

### Usage

```java
// With a Type object (requires type system)
List<JVSValidator.Violation> violations = JVSValidator.validate(doc, type);

// With a raw type definition (no type system needed)
JsonNode typeDef = loadJsonFile("config/types/core_sysobject.json");
List<JVSValidator.Violation> violations = JVSValidator.validateAgainstDefinition(doc, typeDef);

// Human-readable report
String report = JVSValidator.reportAgainstDefinition(doc, typeDef);
// "3 violation(s) for type sysobject:
//   [WARNING] count: missing field
//   [ERROR] name: expected string but got NUMBER
//   [INFO] extra: field not defined in type 'sysobject'"
```

### Violation levels

| Level | Meaning | Examples |
|-------|---------|---------|
| `ERROR` | Type mismatch — will cause problems at indexing/query time | String where long expected, scalar where vector expected |
| `WARNING` | Missing field — document is structurally incomplete | Required field absent, null value |
| `INFO` | Extra field — present in document but not in type definition | Fields from a different type version, ad-hoc metadata |

### Design decisions

- Dynamic fields (those with a `"dynamic"` config) are **skipped** — they're computed at enrichment time, not stored
- The `"type"` field is always allowed (it's metadata, not a typed field)
- Composite type fields (mls, id, dates) with object values pass the type check — only primitive type mismatches are flagged

### Tests: 12 tests

`JVSValidatorTest` — structural validation (valid, missing, extra, empty), type mismatches (string/number/vector/array), null handling, dynamic field skipping, violation model, report format.

---

## 2. Enrichment Observability (`ExecutionTrace`)

Records runtime execution details for the projection pipeline. Attach to a `ProjectionContext` to capture which fields were projected, timing, skips, and errors.

### Usage

```java
ProjectionContext pc = new ProjectionContext();
pc.trace = new ExecutionTrace();  // opt-in: only traces when set
pc.source = inputJvs;
pc.target = new JVS();

executionNode.project(pc);

// Summary
System.out.println(pc.trace.summary());
// "5 action(s) in 342µs, 2 skip(s), 0 error(s)"

// Detailed trace
System.out.println(pc.trace.detail());
// "  [IndexerAction] title.text_en_s — 45µs"
// "  [IndexerAction] body.text_en_m — 120µs"
// "  [SKIP] optional_field — value was null"
```

### Record types

```java
ExecutionTrace.ActionEntry(String path, String actionType, long elapsedMicros)
ExecutionTrace.SkipEntry(String path, String reason)
ExecutionTrace.ErrorEntry(String path, String message)
```

### Design decisions

- Opt-in via `pc.trace = new ExecutionTrace()` — no overhead when not tracing
- Uses Java records for trace entries — immutable, compact
- Micros-level timing for action-level profiling
- The trace object is reusable (clear between documents) or single-use

### Tests: 7 tests

`ExecutionTraceTest` — record actions, total timing, skips, errors, empty trace, summary format, attachment to ProjectionContext.

---

## 3. Schema-Aware Document Generation (`TypeAwareGenerator`)

Generates JVS documents from type definitions using the `DataGenerators` framework. Reads the type's field definitions and produces appropriate generated values for each field based on its primitive type.

### Usage

```java
// From a raw type definition (no type system needed)
JsonNode typeDef = loadJsonFile("config/types/core_sysobject.json");
JVS doc = TypeAwareGenerator.generateFromDefinition(typeDef, generators);

// From a Type object
JVS doc = TypeAwareGenerator.generate(type, generators);
```

### Field type mapping

| Type | Generated value |
|------|----------------|
| `core_string` | Random first + last name |
| `core_long` | Random long 1-100000 |
| `core_boolean` | Random boolean |
| `core_date` | Random ISO date |
| `core_mls` | `{"mls": [{"text": "lorem...", "lang": "en"}]}` |
| `core_dates` | `{"created": "...", "modified": "..."}` |
| `core_id` | `{"domain": "company_name", "did": "uuid"}` |
| vector fields | Array of 1-4 generated values |
| dynamic fields | Skipped (computed at enrichment time) |

### Design decisions

- Dynamic fields are skipped — they'd be computed by the enrichment pipeline after generation
- The `type` field is set on the generated document for type system compatibility
- Works with raw `JsonNode` type definitions, not just `Type` objects — avoids `JsonTypeSystem` dependency in tests

### Tests: 9 tests

`TypeAwareGeneratorTest` — primitive fields (string, long, boolean, date), vector fields, all-fields composite, dynamic skip, type name setting, uniqueness across calls.

---

## 4. Pattern Matching in `ensureJsonNode` (Java 21)

Replaced the chain of `instanceof` checks in `JSONUtil.ensureJsonNode()` with a Java 21 pattern-matching switch expression.

### Before

```java
public static JsonNode ensureJsonNode(Object value) {
    if (value instanceof JsonNode) return (JsonNode) value;
    if (value instanceof String) return textNode(value.toString());
    if (value instanceof Integer) return numberNode(((Integer) value).intValue());
    if (value instanceof Float) return numberNode(((Float) value).floatValue());
    if (value instanceof Double) return numberNode(((Double) value).doubleValue());
    if (value instanceof Long) return numberNode(((Long) value).longValue());
    if (value instanceof Boolean) return booleanNode((Boolean) value);
    return null;
}
```

### After

```java
public static JsonNode ensureJsonNode(Object value) {
    return switch (value) {
        case null -> null;
        case JsonNode jn -> jn;
        case String s -> textNode(s);
        case Integer i -> numberNode(i);
        case Long l -> numberNode(l);
        case Float f -> numberNode(f);
        case Double d -> numberNode(d);
        case Boolean b -> booleanNode(b);
        case Short s -> numberNode(s.intValue());
        case Byte b -> numberNode(b.intValue());
        default -> null;
    };
}
```

### What changed

- Cleaner, more readable
- Added `null` case (was implicit)
- Added `Short` and `Byte` handling (previously returned null)
- Pattern bindings eliminate explicit casts
- Single expression instead of 7 if-return blocks

---

## 5. `Class.newInstance()` Replacement (Java 9+ compliance)

Replaced deprecated `Class.newInstance()` with `Class.getDeclaredConstructor().newInstance()` in `ClassUtil` and `ClassFactory`.

### Why

`Class.newInstance()` was deprecated in Java 9 because it:
- Propagates checked exceptions from constructors without declaring them
- Bypasses compile-time checked exception handling
- Can't handle constructors that throw checked exceptions properly

### Files changed

| File | Change |
|------|--------|
| `ClassUtil.getInstanceSwallowError()` | `theClass.newInstance()` → `theClass.getDeclaredConstructor().newInstance()` |
| `ClassFactory.getInstance()` | Same replacement |
| `ClassFactory.getInstanceSwallowException()` | Updated exception handling to catch `Exception` |

### Error handling improvement

The new code also catches `NoSuchMethodException` (no default constructor) and `InvocationTargetException` (constructor threw), providing better error messages than the old blanket `InstantiationException` / `IllegalAccessException`.

---

## 6. Enrichment Pipeline (earlier improvements)

Documented here for completeness — these were implemented in the projection cleanup phase:

### `ExecutionNode.dump()`

Static visualization of the execution plan tree:

```java
String plan = executionBuilder.getExecutor().dump();
// [root]
//   id (id)
//     domain (string) → 1 action(s) [IndexerAction]
//     id (string) → 1 action(s) [IndexerAction]
//   title (mls)
//     mls[] (mlselem)
//       clean (string) → 1 action(s) [IndexerAction]
```

### IndexerAction type preservation fix

First value of multi-valued fields was being converted to text via `val.textValue()`. Now uses `on.set(field, val)` which preserves the original JsonNode type (long, boolean, etc.).

### Cache key collision fix

`BaseProjectionMapper.setCache()` was using the same cache key `"e"` for all tag combinations. Now uses class name + tags: `"JVS2SolrMapper:basic"`.

---

## Test Summary

| Feature | Test class | Count |
|---------|-----------|-------|
| Type Validation | `JVSValidatorTest` | 12 |
| Execution Trace | `ExecutionTraceTest` | 7 |
| Type-Aware Generation | `TypeAwareGeneratorTest` | 9 |
| ClassUtil characterization | `ClassUtilTest` | 54 |
| Pattern matching (ensureJsonNode) | covered by existing tests | — |
| newInstance fix | covered by existing tests | — |
| Field Dependency Graph | `FieldDependencyGraphTest` | 11 |
| Type Diff / Migration | `TypeDiffTest` | 8 |
| Type Versioning | `TypeVersionTest` | 5 |
| Conditional Generators | `ConditionalGeneratorTest` | 5 |
| Parallel Transform (virtual threads) | `ParallelTransformMapperTest` | 5 |
| Raw generics → diamond operator | covered by existing tests | — |
| Text blocks / var | covered by existing tests | — |

**Total project test count: 460 (up from 344 baseline), 0 failures.**

---

## 7. Field Dependency Graph (`FieldDependencyGraph`)

Extracts and analyzes the DAG of dynamic field dependencies within a type. The dependency info comes from the `"fields"` array in dynamic field configs (e.g., `[".text", ".lang"]`).

### Usage

```java
JsonNode typeDef = loadJsonFile("config/types/core_mlselem.json");
FieldDependencyGraph graph = FieldDependencyGraph.fromDefinition(typeDef);

// Direct dependencies
graph.getDependencies("clean");       // → {"text"}
graph.getDependencies("segmented");   // → {"segmented_span", "clean"}

// Topological sort (correct enrichment order)
graph.topologicalSort();
// → [lang, text, clean, pos, segmented_span, segmented, segmented_ner, ...]

// Impact analysis: what breaks if "text" changes?
graph.getImpacted("text");
// → {clean, pos, segmented_span, segmented, segmented_ner, ...}

// Cycle detection
graph.hasCycle();  // → false

// Visualization
System.out.println(graph.dump());
// "text (source)"
// "text → clean"
// "lang, clean → pos"
// "segmented_span, clean → segmented"

System.out.println(graph.toMermaid());
// "graph TD"
// "    text --> clean"
// "    clean --> segmented"
```

### Tests: 11 tests

Linear chain, multiple inputs, empty graph, topological sort, impact analysis (downstream + leaf), cycle detection, text dump, Mermaid diagram, full `core_mlselem` pipeline.

---

## 8. Type Diff and Migration (`TypeDiff`)

Compares two type definitions and reports added, removed, and modified fields. Can generate a Groovy migration script for the Data Mapper DSL.

### Usage

```java
JsonNode oldDef = loadJsonFile("config/types/v1/core_article.json");
JsonNode newDef = loadJsonFile("config/types/v2/core_article.json");

// Diff
List<TypeDiff.Change> changes = TypeDiff.diff(oldDef, newDef);
// → [ADDED] excerpt — type core_mls
// → [REMOVED] summary — type was core_string
// → [MODIFIED] tags — vector: false → true

// Report
String report = TypeDiff.report(oldDef, newDef);

// Generate migration script
String script = TypeDiff.generateMigrationScript(oldDef, newDef);
// copyAll()
// delete "target.summary"
// set "target.excerpt", ""  // type: core_mls
```

### Change kinds

| Kind | Meaning |
|------|---------|
| `ADDED` | Field exists in new type but not old |
| `REMOVED` | Field exists in old type but not new |
| `MODIFIED` | Field exists in both but type, vector, i18n, or dynamic changed |

### Migration script generation

The generated Groovy script uses the Data Mapper DSL:
- `copyAll()` preserves all existing fields
- `delete` for removed fields
- `set` with sensible defaults for added fields (empty string, 0, false, `gen.date()`)
- Comments for dynamic fields (they're computed) and modified fields (need manual review)

### Tests: 8 tests

Identical types, added/removed/modified fields, vector changes, readable report, no-changes report, migration script generation.

---

## 9. Script Library / Imports

Transform scripts can now load shared functions from `config/transforms/lib/`:

```groovy
def lib = load("common")

set "target.slug", lib.slugify(gen.fullName())
set "target.excerpt", lib.excerpt(gen.lorem(), 120)
set "target.phone", lib.formatPhone(gen.phone())
set "target.price_display", lib.formatPrice(99.95, "USD")
```

### Bundled library: `lib/common.groovy`

| Function | Description |
|----------|-------------|
| `slugify(text)` | URL-safe slug: "Hello World" → "hello-world" |
| `titleCase(text)` | "hello world" → "Hello World" |
| `excerpt(text, maxLen)` | Truncate at word boundary with "..." |
| `formatPhone(raw)` | Normalize to (XXX) XXX-XXXX |
| `formatPrice(amount, currency)` | "$99.95", "€42.00" |

### Adding your own libraries

Create a `.groovy` file in `config/transforms/lib/` that returns a Map:

```groovy
// lib/my_helpers.groovy
def myFunc = { String input -> input.reverse() }
[myFunc: myFunc]
```

---

## 10. Raw Generics → Diamond Operator

Replaced raw generic collections (`new HashMap()`, `new ArrayList()`) with diamond operator (`new HashMap<>()`, `new ArrayList<>()`) in the type system and executor packages. Eliminates compiler warnings without changing behavior.

---

## 11. Type Versioning (`TypeVersion`)

Semantic versioning for type definitions. Types can include a `"version"` field:

```json
{"name": "mlselem", "version": "2.1.0", "fields": [...]}
```

### Usage

```java
TypeVersion v = TypeVersion.fromDefinition(typeDef);
v.version();    // "2.1.0"
v.major();      // 2
v.bumpMinor();  // TypeVersion("mlselem", "2.2.0")

// Suggest bump level from a diff
String bump = TypeVersion.suggestedBump(oldDef, newDef);
// "major" (field removed/type changed), "minor" (field added), "none"
```

Java record — immutable, implements `Comparable<TypeVersion>`.

---

## 12. Conditional & Chained Generators

New `Generators` factory methods for data-dependent generation:

```java
// Conditional: salary depends on department
Generator salary = Generators.conditional(
    () -> dept.equals("Engineering"),
    Generators.randomInt(100000, 200000),
    Generators.randomInt(50000, 80000));

// Transform output
Generator upper = Generators.transform(nameGen, s -> s.toString().toUpperCase());

// Format string from multiple generators
Generator fullName = Generators.format("%s %s", firstNameGen, lastNameGen);

// Weighted random pick
Generator tier = Generators.weightedPick(
    new String[]{"free", "pro", "enterprise"},
    new double[]{70, 25, 5});
```

---

## 13. Parallel Transform with Virtual Threads (`ParallelTransformMapper`)

Uses Java 21 virtual threads for batch document transformation. Each document gets its own virtual thread with a fresh Groovy script instance (thread-safe).

### Usage

```java
List<JVS> inputs = ... ; // 1000 documents
String script = Files.readString(Path.of("config/transforms/enrich_person.groovy"));

// Parallel batch — one virtual thread per document
List<JVS> results = ParallelTransformMapper.transformBatch(inputs, script, generators);

// With AI operations
List<JVS> results = ParallelTransformMapper.transformBatch(
    inputs, script, generators, aiOps, enrichOps);

// From iterator with batching (batch size = 50)
List<JVS> results = ParallelTransformMapper.transformIterator(
    inputIterator, 50, script, generators);
```

Order is preserved, failed documents produce `null` entries. Ideal for I/O-bound transforms (AI translate/summarize).

---

## 14. Text Blocks & `var` (Java 15/10)

- Test files: JSON string literals converted to text blocks (`"""..."""`)
- New code: `var` used for local variables where type is obvious from RHS
- Existing code: not changed (risk/reward too low for mechanical bulk changes)
