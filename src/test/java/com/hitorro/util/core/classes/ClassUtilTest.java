/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.classes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hitorro.util.json.JSONUtil;
import com.hitorro.util.json.keys.PropertyKeyValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Characterization tests for ClassUtil, JsonNodeClassMapper, and JSONUtil.ensureJsonNode.
 *
 * These tests capture the CURRENT behavior before refactoring, so that the refactoring
 * can be verified to be behavior-preserving.
 */
@DisplayName("ClassUtil / JsonNodeClassMapper / ensureJsonNode Characterization Tests")
class ClassUtilTest {

    // ---------------------------------------------------------------
    // Helper classes used in the tests (no dependency on type system)
    // ---------------------------------------------------------------
    public static class ConcreteClass {}

    public static abstract class AbstractClass {}

    public static class ChildClass extends ConcreteClass {}

    public interface TestInterface {}

    public static class InterfaceImpl implements TestInterface {}

    // ---------------------------------------------------------------
    // 1. ClassUtil.getInstanceSwallowError(Class, Class)
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("ClassUtil.getInstanceSwallowError(Class, Class)")
    class GetInstanceSwallowErrorTests {

        @Test
        @DisplayName("returns instance for valid concrete class with null requiredSuper")
        void validClass_nullSuper() {
            Object result = ClassUtil.getInstanceSwallowError(ConcreteClass.class, null);
            assertThat(result).isNotNull().isInstanceOf(ConcreteClass.class);
        }

        @Test
        @DisplayName("returns instance when requiredSuper is satisfied")
        void validClass_correctSuper() {
            Object result = ClassUtil.getInstanceSwallowError(ChildClass.class, ConcreteClass.class);
            assertThat(result).isNotNull().isInstanceOf(ChildClass.class);
        }

        @Test
        @DisplayName("returns instance when requiredSuper is Object.class")
        void validClass_objectSuper() {
            Object result = ClassUtil.getInstanceSwallowError(ConcreteClass.class, Object.class);
            assertThat(result).isNotNull().isInstanceOf(ConcreteClass.class);
        }

        @Test
        @DisplayName("returns null when requiredSuper is not satisfied")
        void wrongSuperclass_returnsNull() {
            // ConcreteClass is NOT a subclass of List
            Object result = ClassUtil.getInstanceSwallowError(ConcreteClass.class, List.class);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns instance when class implements required interface")
        void classImplementsInterface() {
            Object result = ClassUtil.getInstanceSwallowError(InterfaceImpl.class, TestInterface.class);
            assertThat(result).isNotNull().isInstanceOf(InterfaceImpl.class);
        }

        @Test
        @DisplayName("returns null for abstract class (InstantiationException swallowed)")
        void abstractClass_returnsNull() {
            Object result = ClassUtil.getInstanceSwallowError(AbstractClass.class, null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns null for interface (InstantiationException swallowed)")
        void interfaceClass_returnsNull() {
            Object result = ClassUtil.getInstanceSwallowError(Runnable.class, null);
            assertThat(result).isNull();
        }
    }

    // ---------------------------------------------------------------
    // 2. ClassUtil.getClassForName
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("ClassUtil.getClassForName")
    class GetClassForNameTests {

        @Test
        @DisplayName("returns Class for a valid fully-qualified class name")
        void validClassName() {
            Class<?> result = ClassUtil.getClassForName("java.util.ArrayList", null);
            assertThat(result).isEqualTo(ArrayList.class);
        }

        @Test
        @DisplayName("returns Class when requiredSuper is satisfied")
        void validClassName_correctSuper() {
            Class<?> result = ClassUtil.getClassForName("java.util.ArrayList", List.class);
            assertThat(result).isEqualTo(ArrayList.class);
        }

        @Test
        @DisplayName("returns null when requiredSuper is not satisfied")
        void validClassName_wrongSuper() {
            // ArrayList is not a Runnable
            Class<?> result = ClassUtil.getClassForName("java.util.ArrayList", Runnable.class);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns null for an invalid class name (ClassNotFoundException swallowed)")
        void invalidClassName_returnsNull() {
            Class<?> result = ClassUtil.getClassForName("com.does.not.Exist", null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns null when classname is null")
        void nullClassName_returnsNull() {
            Class<?> result = ClassUtil.getClassForName(null, null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("returns Class with Object.class as requiredSuper (always passes)")
        void objectSuperAlwaysPasses() {
            Class<?> result = ClassUtil.getClassForName("java.lang.String", Object.class);
            assertThat(result).isEqualTo(String.class);
        }
    }

    // ---------------------------------------------------------------
    // 3. ClassUtil.isSubClass
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("ClassUtil.isSubClass")
    class IsSubClassTests {

        @Test
        @DisplayName("returns true when cTest is a direct subclass of cSuper")
        void directSubclass() {
            assertThat(ClassUtil.isSubClass(ChildClass.class, ConcreteClass.class)).isTrue();
        }

        @Test
        @DisplayName("returns true when cTest is the same class as cSuper")
        void sameClass() {
            assertThat(ClassUtil.isSubClass(ConcreteClass.class, ConcreteClass.class)).isTrue();
        }

        @Test
        @DisplayName("returns true when cTest implements cSuper interface")
        void implementsInterface() {
            assertThat(ClassUtil.isSubClass(InterfaceImpl.class, TestInterface.class)).isTrue();
        }

        @Test
        @DisplayName("returns true for any class with Object.class as super")
        void objectIsSuperOfAll() {
            assertThat(ClassUtil.isSubClass(String.class, Object.class)).isTrue();
        }

        @Test
        @DisplayName("returns false when cTest is NOT a subclass of cSuper")
        void notSubclass() {
            assertThat(ClassUtil.isSubClass(ConcreteClass.class, List.class)).isFalse();
        }

        @Test
        @DisplayName("returns false when cTest is the parent (not subclass in the other direction)")
        void parentIsNotSubclass() {
            assertThat(ClassUtil.isSubClass(ConcreteClass.class, ChildClass.class)).isFalse();
        }

        @Test
        @DisplayName("returns false when cTest is null")
        void nullTest() {
            assertThat(ClassUtil.isSubClass(null, Object.class)).isFalse();
        }

        @Test
        @DisplayName("returns false when cSuper is null")
        void nullSuper() {
            assertThat(ClassUtil.isSubClass(String.class, null)).isFalse();
        }

        @Test
        @DisplayName("returns false when both are null")
        void bothNull() {
            assertThat(ClassUtil.isSubClass(null, null)).isFalse();
        }
    }

    // ---------------------------------------------------------------
    // 4. ClassUtil helper methods
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("ClassUtil.getBareName")
    class GetBareNameTests {

        @Test
        @DisplayName("returns simple name for a class with a package")
        void classWithPackage() {
            assertThat(ClassUtil.getBareName(String.class)).isEqualTo("String");
        }

        @Test
        @DisplayName("returns empty string for null class")
        void nullClass() {
            assertThat(ClassUtil.getBareName(null)).isEqualTo("");
        }

        @Test
        @DisplayName("handles inner class name (uses $ separator from getName)")
        void innerClass() {
            String name = ClassUtil.getBareName(ConcreteClass.class);
            // getName() returns ...ClassUtilTest$ConcreteClass, so bare name is after last dot
            assertThat(name).contains("ConcreteClass");
        }
    }

    @Nested
    @DisplayName("ClassUtil.isAbstract")
    class IsAbstractTests {

        @Test
        @DisplayName("returns true for abstract class")
        void abstractClass() {
            assertThat(ClassUtil.isAbstract(AbstractClass.class)).isTrue();
        }

        @Test
        @DisplayName("returns false for concrete class")
        void concreteClass() {
            assertThat(ClassUtil.isAbstract(ConcreteClass.class)).isFalse();
        }

        @Test
        @DisplayName("returns true for interface (interfaces are abstract)")
        void interfaceIsAbstract() {
            assertThat(ClassUtil.isAbstract(TestInterface.class)).isTrue();
        }
    }

    @Nested
    @DisplayName("ClassUtil.translateClassFilenameToCanonical")
    class TranslateClassFilenameTests {

        @Test
        @DisplayName("converts path/to/Foo.class to path.to.Foo")
        void standardConversion() {
            String result = ClassUtil.translateClassFilenameToCanonical("com/hitorro/Foo.class");
            // File.separator on macOS is /, so com/hitorro/Foo -> com.hitorro.Foo
            assertThat(result).isEqualTo("com.hitorro.Foo");
        }

        @Test
        @DisplayName("returns null for non-.class file")
        void nonClassFile() {
            String result = ClassUtil.translateClassFilenameToCanonical("com/hitorro/Foo.java");
            assertThat(result).isNull();
        }
    }

    // ---------------------------------------------------------------
    // 5. JsonNodeClassMapper
    //
    // Note: JsonNodeClassMapper.apply(JsonNode) reads the "class" field from the
    // JsonNode via classKey (a StringProperty), then delegates to getValidated().
    // We test getValidated() directly to avoid triggering ImplementationRegistry's
    // filesystem-dependent ensureLoaded() via Env.getBinConfigBaseFile().
    // The apply() -> classKey.apply() -> getValidated() delegation is a thin wrapper.
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("JsonNodeClassMapper.getValidated")
    class JsonNodeClassMapperTests {

        @BeforeAll
        static void warmUpImplementationRegistry() {
            // ImplementationRegistry.ensureLoaded() triggers Env.getBinBaseFileSystem()
            // which may throw ExceptionInInitializerError if BaseFileSystem static init
            // fails (e.g. when HT_BIN is not set). The error is absorbed here so that
            // ensureLoaded() marks itself as loaded and subsequent resolve() calls work.
            try {
                ImplementationRegistry.getMe().resolve("warmup");
            } catch (Throwable ignored) {
                // ExceptionInInitializerError or NoClassDefFoundError — absorbed intentionally.
                // After this, ImplementationRegistry.loaded == true, so resolve() won't retry.
            }
        }

        @Test
        @DisplayName("valid FQN class name instantiates the class")
        void validClassName_instantiates() {
            JsonNodeClassMapper<Object> mapper = new JsonNodeClassMapper<>(Object.class, "testkey", null);
            Object result = mapper.getValidated("java.util.ArrayList", Object.class, "testkey");
            assertThat(result).isNotNull().isInstanceOf(ArrayList.class);
        }

        @Test
        @DisplayName("null class name and no defaultClass returns null")
        void nullClassName_noDefault_returnsNull() {
            JsonNodeClassMapper<Object> mapper = new JsonNodeClassMapper<>(Object.class, "testkey", null);
            Object result = mapper.getValidated(null, Object.class, "testkey");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null class name with a defaultClass uses default")
        void nullClassName_withDefault_usesDefault() {
            JsonNodeClassMapper<Object> mapper = new JsonNodeClassMapper<>(Object.class, "testkey", ArrayList.class);
            Object result = mapper.getValidated(null, Object.class, "testkey");
            assertThat(result).isNotNull().isInstanceOf(ArrayList.class);
        }

        @Test
        @DisplayName("empty string with a defaultClass uses default")
        void emptyString_withDefault_usesDefault() {
            JsonNodeClassMapper<Object> mapper = new JsonNodeClassMapper<>(Object.class, "testkey", ArrayList.class);
            Object result = mapper.getValidated("", Object.class, "testkey");
            assertThat(result).isNotNull().isInstanceOf(ArrayList.class);
        }

        @Test
        @DisplayName("empty string with no default returns null")
        void emptyString_noDefault_returnsNull() {
            JsonNodeClassMapper<Object> mapper = new JsonNodeClassMapper<>(Object.class, "testkey", null);
            Object result = mapper.getValidated("", Object.class, "testkey");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("invalid single-part class name throws PropertyKeyValidationException")
        void invalidClassName_singlePart_throwsException() {
            JsonNodeClassMapper<Object> mapper = new JsonNodeClassMapper<>(Object.class, "testkey", null);
            // When class is not found, the code falls through the parts.length==1 branch
            // to the general branch, which throws PropertyKeyValidationException
            assertThatThrownBy(() ->
                    mapper.getValidated("com.does.not.Exist", Object.class, "testkey")
            ).isInstanceOf(PropertyKeyValidationException.class);
        }

        @Test
        @DisplayName("wrong requiredSuper returns null (class found but doesn't match)")
        void wrongRequiredSuper_returnsNull() {
            JsonNodeClassMapper<Serializable> mapper = new JsonNodeClassMapper<>(Serializable.class, "testkey", null);
            // ConcreteClass does not implement Serializable
            String className = ConcreteClass.class.getName();
            Object result = mapper.getValidated(className, Serializable.class, "testkey");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("class#field syntax reads a public static field value")
        void hashSyntax_validStaticField() {
            JsonNodeClassMapper<Object> mapper = new JsonNodeClassMapper<>(Object.class, "testkey", null);
            // Boolean.TRUE is a public static field on java.lang.Boolean
            Object result = mapper.getValidated("java.lang.Boolean#TRUE", Object.class, "testkey");
            assertThat(result).isEqualTo(Boolean.TRUE);
        }

        @Test
        @DisplayName("class#field syntax with invalid field throws PropertyKeyValidationException")
        void hashSyntax_invalidField_throwsException() {
            JsonNodeClassMapper<Object> mapper = new JsonNodeClassMapper<>(Object.class, "testkey", null);
            // java.lang.String exists but has no public field "nonexistentField"
            assertThatThrownBy(() ->
                    mapper.getValidated("java.lang.String#nonexistentField", Object.class, "testkey")
            ).isInstanceOf(PropertyKeyValidationException.class);
        }
    }

    // ---------------------------------------------------------------
    // 6. JSONUtil.ensureJsonNode
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("JSONUtil.ensureJsonNode")
    class EnsureJsonNodeTests {

        @Test
        @DisplayName("String value returns TextNode")
        void stringValue() {
            JsonNode result = JSONUtil.ensureJsonNode("hello");
            assertThat(result).isNotNull();
            assertThat(result.isTextual()).isTrue();
            assertThat(result.textValue()).isEqualTo("hello");
        }

        @Test
        @DisplayName("empty String returns TextNode with empty string")
        void emptyString() {
            JsonNode result = JSONUtil.ensureJsonNode("");
            assertThat(result).isNotNull();
            assertThat(result.isTextual()).isTrue();
            assertThat(result.textValue()).isEqualTo("");
        }

        @Test
        @DisplayName("Integer value returns IntNode")
        void integerValue() {
            JsonNode result = JSONUtil.ensureJsonNode(42);
            assertThat(result).isNotNull();
            assertThat(result.isInt()).isTrue();
            assertThat(result.intValue()).isEqualTo(42);
        }

        @Test
        @DisplayName("negative Integer value")
        void negativeInteger() {
            JsonNode result = JSONUtil.ensureJsonNode(-7);
            assertThat(result).isNotNull();
            assertThat(result.intValue()).isEqualTo(-7);
        }

        @Test
        @DisplayName("Long value returns LongNode")
        void longValue() {
            JsonNode result = JSONUtil.ensureJsonNode(123456789L);
            assertThat(result).isNotNull();
            assertThat(result.isLong()).isTrue();
            assertThat(result.longValue()).isEqualTo(123456789L);
        }

        @Test
        @DisplayName("Double value returns DoubleNode")
        void doubleValue() {
            JsonNode result = JSONUtil.ensureJsonNode(3.14);
            assertThat(result).isNotNull();
            assertThat(result.isDouble()).isTrue();
            assertThat(result.doubleValue()).isCloseTo(3.14, within(0.0001));
        }

        @Test
        @DisplayName("Float value returns FloatNode")
        void floatValue() {
            JsonNode result = JSONUtil.ensureJsonNode(2.5f);
            assertThat(result).isNotNull();
            assertThat(result.isFloat()).isTrue();
            assertThat(result.floatValue()).isCloseTo(2.5f, within(0.0001f));
        }

        @Test
        @DisplayName("Float is checked BEFORE Double (order matters for instanceof)")
        void floatBeforeDouble() {
            // A Float is NOT a Double, so the Float branch should handle it
            Float f = 1.0f;
            JsonNode result = JSONUtil.ensureJsonNode(f);
            assertThat(result).isNotNull();
            assertThat(result.isFloat()).isTrue();
        }

        @Test
        @DisplayName("Boolean true returns BooleanNode true")
        void booleanTrue() {
            JsonNode result = JSONUtil.ensureJsonNode(true);
            assertThat(result).isNotNull();
            assertThat(result.isBoolean()).isTrue();
            assertThat(result.booleanValue()).isTrue();
        }

        @Test
        @DisplayName("Boolean false returns BooleanNode false")
        void booleanFalse() {
            JsonNode result = JSONUtil.ensureJsonNode(false);
            assertThat(result).isNotNull();
            assertThat(result.isBoolean()).isTrue();
            assertThat(result.booleanValue()).isFalse();
        }

        @Test
        @DisplayName("JsonNode passthrough returns same instance")
        void jsonNodePassthrough() {
            ObjectNode original = JsonNodeFactory.instance.objectNode();
            original.put("key", "value");
            JsonNode result = JSONUtil.ensureJsonNode(original);
            assertThat(result).isSameAs(original);
        }

        @Test
        @DisplayName("TextNode passthrough returns same instance")
        void textNodePassthrough() {
            JsonNode original = JsonNodeFactory.instance.textNode("test");
            JsonNode result = JSONUtil.ensureJsonNode(original);
            assertThat(result).isSameAs(original);
        }

        @Test
        @DisplayName("null returns null")
        void nullValue() {
            // null is not instanceof anything, so all checks fail and method returns null
            JsonNode result = JSONUtil.ensureJsonNode(null);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("unknown type returns null (e.g. a List)")
        void unknownType_returnsNull() {
            JsonNode result = JSONUtil.ensureJsonNode(new ArrayList<>());
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("unknown type returns null (e.g. a custom object)")
        void customObject_returnsNull() {
            JsonNode result = JSONUtil.ensureJsonNode(new ConcreteClass());
            assertThat(result).isNull();
        }
    }
}
