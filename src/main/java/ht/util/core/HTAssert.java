package ht.util.core;

import ht.util.log.Logger;


public class HTAssert {
    public static final void assertThat(boolean flag, String msg) {
        if (flag == false) {
            Throwable t = new Throwable();
            Log.util.error(Logger.generateMessageWithCallstack(msg, t));
        }
    }
}
