package ht.util.core.iterator.sinks;


import ht.util.integrationevents.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 */
public class JSONSinkAsArray extends JsonSink {
    public JSONSinkAsArray(OutputStream os) {
        super(os);
    }

    public boolean start() {
        try {
            ow.write('[');
        } catch (IOException e) {
            Log.util.error("Unable to write out start block %s", e);
        }
        return true;
    }

    public void writeOutSeperator() throws IOException {
        ow.write(',');
    }

    public boolean stop() {
        try {
            ow.write(']');
            ow.flush();
            ow.close();
        } catch (IOException e) {
            Log.util.error("Unable to write out end block %s", e);
        }
        return true;
    }
}
