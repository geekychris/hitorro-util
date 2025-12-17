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
package com.hitorro.util.commandandcontrol;

import com.hitorro.util.commandandcontrol.ano.CommandDef;
import com.hitorro.util.commandandcontrol.ano.DebugArgAno;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.ArrayIterator;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.StringProperty;

/**
 * Bunch of test commands that should be removed once some real examples exist.  Have a few super simple commands in Env
 * but they currently dont take params or give anything back more complex than single value.
 */
public class TestCommands {

    @CommandDef(command = "test.test2", description = "test2 test description", isInternal = false)
    public static int test2(@DebugArgAno(propType = StringProperty.class, keyName = "a",
            description = "a description", defaultValue = "hello there") String a,
                            @DebugArgAno(propType = IntegerProperty.class, keyName = "b",
                                    description = "b description", defaultValue = "2") int b) {
        return b;
    }

    @CommandDef(command = "test.test1", description = "test1 test description", isInternal = false)
    public int test1(@DebugArgAno(propType = StringProperty.class, keyName = "a",
            description = "a description", defaultValue = "hello") String a,
                     @DebugArgAno(propType = IntegerProperty.class, keyName = "b",
                             description = "b description", defaultValue = "1") int b) {
        return b * 2;
    }

    @CommandDef(command = "test.test3", description = "test2 test description", isInternal = false)
    public AbstractIterator<String> test3(@DebugArgAno(propType = StringProperty.class, keyName = "a",
            description = "a description", defaultValue = "hello there") String a,
                                          @DebugArgAno(propType = IntegerProperty.class, keyName = "b",
                                                  description = "b description", defaultValue = "2") int b) {
        String array[] = {"one", "two", "three", "four", "five"};
        return new ArrayIterator<String>(array);
    }

    @CommandDef(command = "test.test4", description = "test2 test description", isInternal = false)
    public String[] test4(@DebugArgAno(propType = StringProperty.class, keyName = "a",
            description = "a description", defaultValue = "hello there") String a,
                          @DebugArgAno(propType = IntegerProperty.class, keyName = "b",
                                  description = "b description", defaultValue = "2") int b) {
        String array[] = {"one", "two", "three", "four", "five"};
        return array;
    }
}
