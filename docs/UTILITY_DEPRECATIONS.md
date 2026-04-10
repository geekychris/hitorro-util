# Utility Deprecations

Methods in hitorro-util that now have JDK 21 equivalents. Marked `@Deprecated` with `@see`/Javadoc pointing to the replacement. Existing callers continue to work — migrate incrementally when touching those files.

## Deprecated Methods

### StringUtil

| Method | Callers | Replacement |
|--------|---------|-------------|
| `createPadding(char, int)` | 1 | `String.valueOf(c).repeat(n)` |
| `encodeUrl(String)` | 1 | `URLEncoder.encode(s, StandardCharsets.UTF_8)` |
| `decodeUrl(String)` | 1 | `URLDecoder.decode(s, StandardCharsets.UTF_8)` |
| `capitalizeFirstLetter(String)` | 1 | `s.substring(0,1).toUpperCase() + s.substring(1)` |
| `mergeWithJoinToken(List, String)` | 9 | `String.join(token, list)` or `stream().map(Object::toString).collect(joining(token))` |
| `mergeWithJoinToken(Collection, String)` | " | Same as above |
| `mergeWithJoinToken(Object[], String)` | " | Same as above |
| `mergeWithJoinToken(Object[], String, int, int)` | " | Same as above |

### ArrayUtil

| Method | Callers | Replacement |
|--------|---------|-------------|
| `contains(Object[], Object)` | 3 | `Arrays.asList(array).contains(obj)` |
| `toList(Object[])` | 0 | `Arrays.asList(array)` or `List.of(array)` |
| `getStringArrayFromStringList(List)` | low | `list.toArray(String[]::new)` |

### ListUtil

| Method | Callers | Replacement |
|--------|---------|-------------|
| `nullOrEmpty(List)` | 22 | `list == null \|\| list.isEmpty()` |
| `notNullAndContainsRows(List)` | low | `list != null && !list.isEmpty()` |

### MapUtil

| Method | Callers | Replacement |
|--------|---------|-------------|
| `nullOrEmpty(Map)` | 0 | `map == null \|\| map.isEmpty()` |

## NOT Deprecated (semantic mismatch or too embedded)

| Method | Callers | Why kept |
|--------|---------|---------|
| `StringUtil.nullOrEmptyString(String)` | **141** | JDK `isEmpty()` throws NPE on null. Every caller would need a null guard. |
| `StringUtil.nullOrEmptyOrBlankString(String)` | moderate | JDK `isBlank()` treats tabs/newlines as blank; HiTorro only treats spaces. Semantic mismatch. |
| `Fmt.S(pattern, args)` | **278** | Custom `%e` format for exceptions. No JDK equivalent. |
| `ArrayUtil.nullOrEmpty(Object[])` | 34 | Same NPE risk as `nullOrEmptyString`. Inline `arr == null \|\| arr.length == 0` at 34 sites. |
| `IntegerUtil.parseInt(String)` | moderate | Extracts first integer from mixed alphanumeric. No JDK equivalent. |
| `FileUtil.*` | extensive | Complex BaseFile integration. `java.nio.file.Files` defaults to UTF-8 vs platform charset. |

## Migration Strategy

1. **New code**: Use JDK equivalents directly. Don't add new calls to deprecated methods.
2. **Touching a file**: When modifying a file for another reason, replace deprecated calls in that file.
3. **IDE support**: IntelliJ will show strikethrough on deprecated methods and suggest replacements via the `@see` annotations.
4. **No bulk migration**: The 141-caller `nullOrEmptyString` and 278-caller `Fmt.S` stay as-is. Risk outweighs benefit for mass replacement.
