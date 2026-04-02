# HiTorro Type System

The JSON Type System is the core data modeling layer of HiTorro. Every document, field, and processing pipeline is defined through type configurations that describe structure, indexing behavior, dynamic field computation, and internationalization.

## Architecture Overview

```
                          ┌─────────────────┐
                          │  JsonTypeSystem  │  singleton, caches Type instances
                          └────────┬────────┘
                                   │ getType("core_id")
                          ┌────────▼────────┐
                          │    HashCache     │  lazy-loads via mapper
                          │   (typeCache)    │
                          └────────┬────────┘
                                   │
                ┌──────────────────┼──────────────────┐
                │ native (default)                     │ schema (debug flag)
                ▼                                      ▼
       Name2JsonMapper                        Name2SchemaJsonMapper
     config/types/*.json                    config/schemas/*.schema.json
                │                                      │
                │                          JsonSchema2TypeConverter
                │                           (schema → native JSON)
                │                                      │
                └──────────────┬───────────────────────┘
                               │
                               ▼
                      Type.init(JsonNode)
                      Field.init(JsonNode)
                      Group.init(JsonNode)
```

### Key classes

| Class | Package | Role |
|---|---|---|
| `JVS` | `jsontypesystem` | JSON Value System — type-aware wrapper around Jackson JsonNode with path-based access |
| `Type` | `jsontypesystem` | Represents a type definition (fields, super type, primitive type, index seeker) |
| `Field` | `jsontypesystem` | A field within a type (type ref, vector flag, i18n, dynamic mapper, groups) |
| `Group` | `jsontypesystem` | Processing group on a field (index, enrich — with method and tags) |
| `JsonTypeSystem` | `jsontypesystem` | Singleton that manages type loading and caching |
| `PAContextTyped` | `jsontypesystem` | Property access context that uses type metadata for indexed lookups and dynamic fields |
| `JVSVariableResolver` | `jsontypesystem` | Resolves `${variable}` references within JVS documents |
| `JVSMerger` | `jsontypesystem` | Deep-merges two JSON trees (object keys + array positions) |

## Configuration

### Directory layout

```
hitorro-util/
  config/
    types/              ← Source of truth for core type definitions
      core_string.json
      core_id.json
      core_mlselem.json
      ...
    schemas/            ← Source of truth for JSON Schema representations
      core_string.schema.json
      core_id.schema.json
      demo_article.schema.json
      ...
    implementations.json  ← Source of truth for symbolic name → class mappings

config/                 ← Runtime config directory (synced at build time)
  types/                  ← Core types copied from hitorro-util + demo/dm types authored here
  schemas/                ← Copied from hitorro-util at build time
  implementations.json    ← Copied from hitorro-util at build time
  ...
```

### Build-time sync

The `maven-resources-plugin` in `hitorro-util/pom.xml` copies configs to the runtime directory during `process-resources`:

- `hitorro-util/config/schemas/*.schema.json` → `config/schemas/`
- `hitorro-util/config/types/core_*.json` → `config/types/`

The target is controlled by the `hitorro.runtime.config.dir` property:

```xml
<!-- Default: relative to hitorro-util, resolves to the project root config/ -->
<hitorro.runtime.config.dir>${project.basedir}/../config</hitorro.runtime.config.dir>
```

Override for non-standard layouts:

```bash
mvn compile -Dhitorro.runtime.config.dir=/opt/hitorro/config
```

### Build-time sync targets

| Source (hitorro-util) | Target (runtime config) |
|---|---|
| `config/schemas/*.schema.json` | `config/schemas/` |
| `config/types/core_*.json` | `config/types/` |
| `config/implementations.json` | `config/` |

### Runtime resolution

`Env.getBin()` determines the runtime root at startup:

1. System property `-DHT_BIN=/path`
2. Environment variable `HT_BIN`
3. Classpath fallback — finds `config/config.txt`, walks to grandparent

Type configs load from `${HT_BIN}/config/types/`, schemas from `${HT_BIN}/config/schemas/`.

## Native type format

Each type is a JSON file in `config/types/`. Example — `core_id.json`:

```json
{
  "name": "id",
  "fields": [
    {"name": "domain", "type": "core_string"},
    {"name": "did", "type": "core_string"},
    {
      "name": "id",
      "type": "core_string",
      "dynamic": {
        "class": "com.hitorro.jsontypesystem.dynamic.MultiValueMergerDM",
        "fields": [".domain", ".did"]
      },
      "groups": [
        {"name": "index", "method": "identifier", "tags": ["basic"]}
      ]
    },
    {
      "name": "id_hash",
      "type": "core_long",
      "vector": true,
      "dynamic": {
        "class": "com.hitorro.jsontypesystem.dynamic.DynamicMapper",
        "mapper": {"class": "com.hitorro.jsontypesystem.dynamic.mappers.FPHashMapper"},
        "fields": [".id"]
      }
    }
  ]
}
```

### Field properties

| Property | Type | Description |
|---|---|---|
| `name` | string | Field name |
| `type` | string | Type reference (e.g. `core_string`, `mlselem`) |
| `vector` | boolean | If true, field is an array of the referenced type |
| `i18n` | boolean | Field is language-dependent |
| `dynamic` | object | Dynamic field mapper config — class, mapper, input fields |
| `groups` | array | Processing groups (index, enrich) with method and tags |

### Type-level properties

| Property | Type | Description |
|---|---|---|
| `name` | string | Type name |
| `super` | string | Parent type (inheritance via field merging) |
| `primitivetype` | string | For leaf types: `string`, `long`, `boolean`, `date` |
| `indexseeker` | object | Index seeker class for array element lookup (e.g. language-keyed MLS) |
| `fetchlang` | boolean | Whether to fetch language metadata |

## JSON Schema format

Each type has a corresponding `.schema.json` file using standard JSON Schema (draft 2020-12) plus HiTorro extension properties (`x-hitorro-*`).

### Extension properties

| Extension | Applies to | Maps to native |
|---|---|---|
| `x-hitorro-name` | schema root | `name` |
| `x-hitorro-super` | schema root | `super` |
| `x-hitorro-primitivetype` | schema root | `primitivetype` |
| `x-hitorro-indexseeker` | schema root | `indexseeker` |
| `x-hitorro-fetchlang` | schema root | `fetchlang` |
| `x-hitorro-dynamic` | property | `dynamic` |
| `x-hitorro-groups` | property | `groups` |
| `x-hitorro-i18n` | property | `i18n` |

### Example — `demo_article.schema.json`

Extends `core_sysobject` (inherits id, times, title, body, description):

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "$id": "demo_article.schema.json",
  "title": "article",
  "x-hitorro-name": "article",
  "x-hitorro-super": "sysobject",
  "allOf": [
    {"$ref": "core_sysobject.schema.json"},
    {
      "type": "object",
      "properties": {
        "author": {"$ref": "core_string.schema.json"},
        "category": {
          "type": "array",
          "items": {"$ref": "core_string.schema.json"}
        },
        "content": {"$ref": "core_mls.schema.json"},
        "source_url": {"$ref": "core_url.schema.json"}
      }
    }
  ]
}
```

### Ref styles

| Style | Format | Used in |
|---|---|---|
| File-based | `"$ref": "core_string.schema.json"` | Split schema files |
| Bundled | `"$ref": "#/$defs/core_string"` | Single-file bundles |

Both are supported by `JsonSchema2TypeConverter`.

## Schema-based type loading

Enable with the system property:

```bash
java -Dhitorro.typesystem.useJsonSchema=true -jar hitorro-app.jar
```

Or programmatically:

```java
JsonTypeSystem.setUseJsonSchema(true);
```

When enabled, types load from `.schema.json` files first. If a schema file doesn't exist for a type, it **falls back to native config** automatically. Log output shows which path each type took:

```
Loaded type from schema: core_string (/.../config/schemas/core_string.schema.json)
Type 'core_query' not found in schemas, fell back to native config
```

## Integrity checking

`TypeConfigIntegrityChecker` compares a native type config against a schema-converted one, field by field:

```java
TypeConfigIntegrityChecker checker = new TypeConfigIntegrityChecker();
List<String> diffs = checker.compare(nativeJson, schemaJson);
// [] = structurally equivalent
// ["fields.x.type: string != long", ...] = differences found

String report = checker.report(nativeJson, schemaJson);
// "OK — configs are structurally equivalent"
// "2 difference(s):\n  - fields.x.type: ..."
```

The checker normalizes type name prefixes (`core_string` == `string`) and treats empty groups arrays as equivalent to missing.

## Programmatic schema conversion

### Type → JSON Schema

```java
Type2JsonSchemaConverter converter = new Type2JsonSchemaConverter(RefStyle.FILE);
ObjectNode schema = converter.convert(type);
```

### JSON Schema → Native type JSON

```java
SchemaFileLoader loader = new SchemaFileLoader();

// From file
ObjectNode typeJson = loader.loadOne(new File("config/schemas/demo_article.schema.json"));

// From string
ObjectNode typeJson = loader.loadFromString(schemaJsonString);

// Load entire directory
Map<String, ObjectNode> allTypes = loader.loadDirectory(new File("config/schemas"));
```

## Implementation Registry

Type configs reference Java classes by name for dynamic field mappers, index seekers, and
mapper implementations. The `ImplementationRegistry` allows these to be **symbolic names**
instead of fully qualified class names, decoupling configs from Java packages.

### Config file

`config/implementations.json` (source of truth: `hitorro-util/config/implementations.json`):

```json
{
  "html-scrubber": "com.hitorro.jsontypesystem.dynamic.mappers.Json2HTMLScrubbedJson",
  "pos-tokenizer": "com.hitorro.jsontypesystem.dynamic.POSTokenizer",
  "sentence-segmenter": "com.hitorro.jsontypesystem.dynamic.SentenceSegmenter",
  "ner-markup": "com.hitorro.jsontypesystem.dynamic.NERMarkupMapper",
  "fp-hash": "com.hitorro.jsontypesystem.dynamic.mappers.FPHashMapper",
  "iso-language-seeker": "com.hitorro.jsontypesystem.IsoLanguageSeeker",
  ...
}
```

### Usage in type configs

Before (FQN class name):
```json
{"class": "com.hitorro.jsontypesystem.dynamic.mappers.Json2HTMLScrubbedJson"}
```

After (symbolic name):
```json
{"class": "html-scrubber"}
```

Both forms work — the registry resolves symbolic names to class names, and FQN class names
pass through unchanged. This means existing configs continue to work without modification.

### Resolution order

1. **Programmatic overrides** — registered via `ImplementationRegistry.getMe().register()`
2. **Config file entries** — loaded from `implementations.json` on first access
3. **Passthrough** — if no match, the value is assumed to be a FQN class name

### Programmatic override

```java
// Override an implementation (e.g. for testing or custom deployments)
ImplementationRegistry.getMe().register("html-scrubber", "com.example.MyCustomScrubber");
```

Programmatic registrations take priority over config file entries, so they serve as
the override mechanism without modifying any config files.

### Integration point

The registry is called from `JsonNodeClassMapper.getValidated()` — the single method
through which all `"class"` fields in JSON configs are resolved to Java instances.
No other code changes are needed to support symbolic names.

## Tests

Run all type system tests:

```bash
cd hitorro-util && mvn test -Dtest="com.hitorro.jsontypesystem.**"
```

| Test class | Tests | What it covers |
|---|---|---|
| `JVSTest` | 30 | get/set, getBoolean, isEmpty, remove, clone, exists, merge, variables |
| `JVSVariableResolverTest` | 10 | Variable resolution (single, multiple, missing, override) |
| `JVSMergerTest` | 7 | Object/array merge, off-by-one fixes |
| `JsonSchema2TypeConverterTest` | 9 | Schema → native JSON for all property types |
| `SchemaFileLoaderTest` | 9 | File loading, string loading, directory loading, article comparison |
| `TypeConfigIntegrityTest` | 17 | Native vs schema comparison for all core types + demo_article |
| `ImplementationRegistryTest` | 12 | Registration, JSON loading, override precedence, passthrough |
| `TypeConfigIntegrityTest` | 17 | Native vs schema comparison for all core types + demo_article |
| `Type2JsonSchemaConverterTest` | 8 | Native → schema (requires type system init) |
| `RoundTripTest` | 1 | Full round-trip (requires type system init) |
