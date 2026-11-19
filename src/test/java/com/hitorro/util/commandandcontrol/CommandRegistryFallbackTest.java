package com.hitorro.util.commandandcontrol;

import com.hitorro.util.commandandcontrol.ano.ArgType;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.json.keys.BaseMappingProperty;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.LongProperty;
import com.hitorro.util.json.keys.StringProperty;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A debug command's argument whose property class has no DebugArgAno
 * constructor is built from the annotation's values. IntegerProperty and
 * LongProperty used to fail here, which logged FATAL and shut every service
 * down at startup (hitorro-example's DMS then never set object GUIDs).
 */
class CommandRegistryFallbackTest {

    private static DebugArgAno arg(Class<?> type, String def) {
        return new DebugArgAno() {
            public Class propType() { return type; }
            public String keyName() { return "count"; }
            public String description() { return "how many"; }
            public String defaultValue() { return def; }
            public boolean mustExist() { return false; }
            public ArgType argType() { return ArgType.Regular; }
            public Class<? extends Annotation> annotationType() { return DebugArgAno.class; }
        };
    }

    @Test
    void buildsEveryCorePropertyType() throws Exception {
        assertInstanceOf(StringProperty.class, CommandRegistry.fallbackProperty(StringProperty.class, arg(StringProperty.class, "x")));
        assertInstanceOf(BooleanProperty.class, CommandRegistry.fallbackProperty(BooleanProperty.class, arg(BooleanProperty.class, "true")));
        BaseMappingProperty i = CommandRegistry.fallbackProperty(IntegerProperty.class, arg(IntegerProperty.class, "10"));
        assertInstanceOf(IntegerProperty.class, i);
        assertInstanceOf(IntegerProperty.class, CommandRegistry.fallbackProperty(IntegerProperty.class, arg(IntegerProperty.class, "")));
        assertInstanceOf(LongProperty.class, CommandRegistry.fallbackProperty(LongProperty.class, arg(LongProperty.class, "5")));
    }

    @Test
    void unknownShapeStillFails() {
        assertThrows(NoSuchMethodException.class, () -> CommandRegistry.fallbackProperty(Object.class, arg(Object.class, "")));
    }
}
