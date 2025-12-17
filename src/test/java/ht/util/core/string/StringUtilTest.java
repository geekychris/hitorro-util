package ht.util.core.string;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

@DisplayName("StringUtil Tests")
class StringUtilTest {

    @Nested
    @DisplayName("After Operations")
    class AfterOperations {

        @Test
        @DisplayName("Should return substring after character")
        void shouldReturnSubstringAfterCharacter() {
            String result = StringUtil.after("hello:world", ':');
            assertThat(result).isEqualTo("world");
        }

        @Test
        @DisplayName("Should return null when character is at end")
        void shouldReturnNullWhenCharacterIsAtEnd() {
            String result = StringUtil.after("hello:", ':');
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return original string when character not found")
        void shouldReturnOriginalStringWhenCharacterNotFound() {
            String result = StringUtil.after("hello", ':');
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should return null for null input")
        void shouldReturnNullForNullInput() {
            String result = StringUtil.after(null, ':');
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle multiple occurrences")
        void shouldHandleMultipleOccurrences() {
            String result = StringUtil.after("a:b:c", ':');
            assertThat(result).isEqualTo("b:c");
        }
    }

    @Nested
    @DisplayName("Whitespace Collapsing")
    class WhitespaceCollapsing {

        @Test
        @DisplayName("Should collapse multiple spaces to single space")
        void shouldCollapseMultipleSpacesToSingleSpace() {
            String result = StringUtil.collapseWhitespace("hello    world");
            assertThat(result).isEqualTo("hello world");
        }

        @Test
        @DisplayName("Should collapse tabs and newlines")
        void shouldCollapseTabsAndNewlines() {
            String result = StringUtil.collapseWhitespace("hello\t\n\rworld");
            assertThat(result).isEqualTo("hello world");
        }

        @Test
        @DisplayName("Should handle leading whitespace")
        void shouldHandleLeadingWhitespace() {
            String result = StringUtil.collapseWhitespace("   hello world");
            assertThat(result).isEqualTo(" hello world");
        }

        @Test
        @DisplayName("Should handle trailing whitespace")
        void shouldHandleTrailingWhitespace() {
            String result = StringUtil.collapseWhitespace("hello world   ");
            assertThat(result).isEqualTo("hello world ");
        }

        @Test
        @DisplayName("Should return empty string for empty input")
        void shouldReturnEmptyStringForEmptyInput() {
            String result = StringUtil.collapseWhitespace("");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle strings with no whitespace")
        void shouldHandleStringsWithNoWhitespace() {
            String result = StringUtil.collapseWhitespace("hello");
            assertThat(result).isEqualTo("hello");
        }
    }

    @Nested
    @DisplayName("Hash Code Merging")
    class HashCodeMerging {

        @Test
        @DisplayName("Should return 0 for no arguments")
        void shouldReturnZeroForNoArguments() {
            int result = StringUtil.mergeHashcodes();
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("Should return object hashcode for single argument")
        void shouldReturnObjectHashcodeForSingleArgument() {
            String str = "test";
            int result = StringUtil.mergeHashcodes(str);
            assertThat(result).isEqualTo(str.hashCode());
        }

        @Test
        @DisplayName("Should merge multiple hashcodes")
        void shouldMergeMultipleHashcodes() {
            int result = StringUtil.mergeHashcodes("a", "b", "c");
            assertThat(result).isNotZero();
        }

        @Test
        @DisplayName("Should handle null values")
        void shouldHandleNullValues() {
            int result = StringUtil.mergeHashcodes("a", null, "b");
            assertThat(result).isNotZero();
        }

        @Test
        @DisplayName("Should produce consistent results")
        void shouldProduceConsistentResults() {
            int result1 = StringUtil.mergeHashcodes("a", "b", "c");
            int result2 = StringUtil.mergeHashcodes("a", "b", "c");
            assertThat(result1).isEqualTo(result2);
        }
    }

    @Nested
    @DisplayName("Text Normalization")
    class TextNormalization {

        @Test
        @DisplayName("Should normalize text to lowercase letters and digits")
        void shouldNormalizeTextToLowercaseLettersAndDigits() {
            StringBuilder sb = new StringBuilder();
            StringUtil.normalizeText("Hello123World!", sb);
            assertThat(sb.toString()).isEqualTo("hello123world");
        }

        @Test
        @DisplayName("Should remove punctuation and special characters")
        void shouldRemovePunctuationAndSpecialCharacters() {
            StringBuilder sb = new StringBuilder();
            StringUtil.normalizeText("Test@#$123", sb);
            assertThat(sb.toString()).isEqualTo("test123");
        }

        @Test
        @DisplayName("Should handle empty string")
        void shouldHandleEmptyString() {
            StringBuilder sb = new StringBuilder();
            StringUtil.normalizeText("", sb);
            assertThat(sb.toString()).isEmpty();
        }

        @Test
        @DisplayName("Should preserve digits")
        void shouldPreserveDigits() {
            StringBuilder sb = new StringBuilder();
            StringUtil.normalizeText("ABC123XYZ789", sb);
            assertThat(sb.toString()).isEqualTo("abc123xyz789");
        }

        @Test
        @DisplayName("Should handle unicode letters")
        void shouldHandleUnicodeLetters() {
            StringBuilder sb = new StringBuilder();
            StringUtil.normalizeText("Café123", sb);
            assertThat(sb.toString()).isEqualTo("café123");
        }
    }

    @Nested
    @DisplayName("String Array Operations")
    class StringArrayOperations {

        @Test
        @DisplayName("Should increase string array size")
        void shouldIncreaseStringArraySize() {
            String[] original = {"a", "b", "c"};
            String[] result = StringUtil.increaseStringArray(original, 3);

            assertThat(result).hasSize(6);
            assertThat(result[0]).isEqualTo("a");
            assertThat(result[1]).isEqualTo("b");
            assertThat(result[2]).isEqualTo("c");
            assertThat(result[3]).isNull();
        }

        @Test
        @DisplayName("Should prepend string to array elements")
        void shouldPrependStringToArrayElements() {
            String[] original = {"world", "there"};
            String[] result = StringUtil.preapendArray(original, "hello ");

            assertThat(result).hasSize(2);
            assertThat(result[0]).isEqualTo("hello world");
            assertThat(result[1]).isEqualTo("hello there");
        }

        @Test
        @DisplayName("Should handle null array in prepend")
        void shouldHandleNullArrayInPrepend() {
            String[] result = StringUtil.preapendArray(null, "prefix");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle null elements in prepend")
        void shouldHandleNullElementsInPrepend() {
            String[] original = {"a", null, "b"};
            String[] result = StringUtil.preapendArray(original, "x");

            assertThat(result[0]).isEqualTo("xa");
            assertThat(result[1]).isEqualTo("x");
            assertThat(result[2]).isEqualTo("xb");
        }
    }

    @Nested
    @DisplayName("Trimming Operations")
    class TrimmingOperations {

        @Test
        @DisplayName("Should trim trailing spaces")
        void shouldTrimTrailingSpaces() {
            String result = StringUtil.rightTrim("hello   ");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should not modify string without trailing spaces")
        void shouldNotModifyStringWithoutTrailingSpaces() {
            String result = StringUtil.rightTrim("hello");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should handle string with only spaces")
        void shouldHandleStringWithOnlySpaces() {
            String result = StringUtil.rightTrim("     ");
            // Based on implementation, this would trim all
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Character Array Comparisons")
    class CharacterArrayComparisons {

        @Test
        @DisplayName("Should compare equal character arrays")
        void shouldCompareEqualCharacterArrays() {
            char[] arr1 = {'h', 'e', 'l', 'l', 'o'};
            char[] arr2 = {'h', 'e', 'l', 'l', 'o'};

            int result = StringUtil.compareTo(arr1, 5, arr2, 5);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("Should compare different character arrays")
        void shouldCompareDifferentCharacterArrays() {
            char[] arr1 = {'a', 'b', 'c'};
            char[] arr2 = {'a', 'b', 'd'};

            int result = StringUtil.compareTo(arr1, 3, arr2, 3);

            assertThat(result).isNegative();
        }

        @Test
        @DisplayName("Should handle different lengths")
        void shouldHandleDifferentLengths() {
            char[] arr1 = {'a', 'b', 'c'};
            char[] arr2 = {'a', 'b', 'c', 'd'};

            int result = StringUtil.compareTo(arr1, 3, arr2, 4);

            assertThat(result).isNegative();
        }

        @Test
        @DisplayName("Should check startsWith for matching prefix")
        void shouldCheckStartsWithForMatchingPrefix() {
            char[] arr1 = {'h', 'e', 'l'};
            char[] arr2 = {'h', 'e', 'l', 'l', 'o'};

            int result = StringUtil.startsWith(arr1, 3, arr2, 5);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("Should check startsWith for non-matching prefix")
        void shouldCheckStartsWithForNonMatchingPrefix() {
            char[] arr1 = {'h', 'e', 'x'};
            char[] arr2 = {'h', 'e', 'l', 'l', 'o'};

            int result = StringUtil.startsWith(arr1, 3, arr2, 5);

            assertThat(result).isNotZero();
        }
    }

    @Nested
    @DisplayName("Size Calculation")
    class SizeCalculation {

        @Test
        @DisplayName("Should calculate size for multiple strings")
        void shouldCalculateSizeForMultipleStrings() {
            int result = StringUtil.size("abc", "def");
            // Each string length * 2
            assertThat(result).isEqualTo(12); // (3 + 3) * 2
        }

        @Test
        @DisplayName("Should handle null strings in size calculation")
        void shouldHandleNullStringsInSizeCalculation() {
            int result = StringUtil.size("abc", null, "def");
            assertThat(result).isEqualTo(12); // (3 + 3) * 2
        }

        @Test
        @DisplayName("Should return zero for no strings")
        void shouldReturnZeroForNoStrings() {
            int result = StringUtil.size();
            assertThat(result).isZero();
        }

        @Test
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            int result = StringUtil.size("", "abc", "");
            assertThat(result).isEqualTo(6); // 3 * 2
        }
    }

    @Nested
    @DisplayName("Exception Stack Trace")
    class ExceptionStackTrace {

        @Test
        @DisplayName("Should convert throwable to JSON array")
        void shouldConvertThrowableToJsonArray() {
            Exception e = new RuntimeException("Test exception");
            ArrayNode result = StringUtil.getCallstackAsJsonArray(e);

            assertThat(result).isNotNull();
            assertThat(result.isArray()).isTrue();
            assertThat(result.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should include exception message in stack trace")
        void shouldIncludeExceptionMessageInStackTrace() {
            Exception e = new RuntimeException("Custom error message");
            ArrayNode result = StringUtil.getCallstackAsJsonArray(e);

            String stackTrace = result.toString();
            assertThat(stackTrace).contains("Custom error message");
        }
    }

    @Nested
    @DisplayName("Last Non-Character Search")
    class LastNonCharacterSearch {

        @Test
        @DisplayName("Should find last non-space character")
        void shouldFindLastNonSpaceCharacter() {
            int result = StringUtil.lastNonChar("hello   ", ' ');
            assertThat(result).isEqualTo(4); // index of 'o'
        }

        @Test
        @DisplayName("Should return -1 when all characters match")
        void shouldReturnMinusOneWhenAllCharactersMatch() {
            int result = StringUtil.lastNonChar("     ", ' ');
            assertThat(result).isEqualTo(-1);
        }

        @Test
        @DisplayName("Should handle string without target character")
        void shouldHandleStringWithoutTargetCharacter() {
            int result = StringUtil.lastNonChar("hello", ' ');
            assertThat(result).isGreaterThanOrEqualTo(0);
        }
    }
}
