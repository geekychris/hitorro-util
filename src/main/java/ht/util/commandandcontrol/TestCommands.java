package ht.util.commandandcontrol;

import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.ArrayIterator;
import ht.util.json.keys.IntegerProperty;
import ht.util.json.keys.StringProperty;

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
