package ht.util.core.iterator.sinks;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.Constants;
import ht.util.core.HTRuntimeException;
import ht.util.core.Log;
import ht.util.core.string.StringUtil;

import java.io.*;

/**
 *
 */
public class JsonSink extends BaseSink<JsonNode> {
    protected ObjectMapper mapper = new ObjectMapper();
    protected OutputStream os;
    protected int count = 0;
    protected Writer ow;
    protected boolean flushOnAdd = false;
    protected String preRamble = null;
    protected String postRamble = null;
    protected boolean closed = false;
    protected boolean started = false;
    protected int logEveryN = 0;

    public JsonSink(OutputStream os, boolean setIndent, boolean flushOnAdd) {
        this(os, setIndent);
        this.flushOnAdd = flushOnAdd;
    }

    public JsonSink(BaseFile bf) throws IOException {
        this(bf.getDataOutputStream());
    }

    public JsonSink(OutputStream os) {
        this.os = os;
        if (os == null) {
            Log.util.error("Output Stream is null");
            return;
        }
        try {
            ow = new OutputStreamWriter(os, Constants.UTF8);
        } catch (UnsupportedEncodingException e) {
            Log.util.error("Unable to open outputStream");
            throw new HTRuntimeException("Unable to open output writer %s %e", e, e);
        }

        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        // stop flushing after every write over the wire!
        mapper.configure(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM, false);
    }

    /**
     * If we want to output in smile format (binary) do so with this option.
     *
     * @param os
     * @param indent
     * @param smileFormat
     */
    public JsonSink(OutputStream os, boolean indent, boolean smileFormat, boolean flushOnAdd) {
        this(os, indent);
        this.flushOnAdd = flushOnAdd;
        if (smileFormat) {
            mapper = new ObjectMapper(new SmileFactory());
        }
    }

    public JsonSink(OutputStream os, boolean indent) {
        this(os);

        setIndent(indent);
    }

    public void setPrePostRamble(String pre, String post) {
        this.preRamble = pre;
        this.postRamble = post;
    }

    /**
     * Output a count of how many elements processed at every n elements
     *
     * @param n
     */
    public void logEveryNElements(int n) {
        logEveryN = n;
    }

    public void setIndent(boolean indent) {
        if (indent) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        } else {
            mapper.disable(SerializationFeature.INDENT_OUTPUT);
        }
    }

    @Override
    public boolean init(JsonNode node) {
        return true;
    }

    public boolean start() {
        if (!started) {
            if (!StringUtil.nullOrEmptyString(preRamble)) {
                try {
                    ow.write(preRamble);
                    ow.flush();
                } catch (IOException e) {
                    throw new HTRuntimeException("Unable to write to output writer %s %e", e, e);
                }
            }
            started = true;
        }
        return started;
    }

    public void writeOutSeperator() throws IOException {
        ow.write('\n');
    }

    public boolean add(JsonNode o) {
        if (o == null) {
            return false;
        }
        try {
            if (count > 0) {
                writeOutSeperator();
            }
            mapper.writeValue(ow, o);
            count++;
            if (logEveryN > 0 && count % logEveryN == 0) {
                Log.util.info("Another %s docs %s", logEveryN, count);
            }

            if (flushOnAdd) {
                ow.flush();
            }
            return true;
        } catch (IOException e) {
            Log.util.error("unable to put in sink, %s %e", e, e);
            return false;
        }
    }

    public boolean stop() {
        if (started) {
            if (closed) {
                Log.util.error("Trying to end something that is closed %e", new Throwable());
            } else {
                try {
                    if (!StringUtil.nullOrEmptyString(postRamble)) {
                        ow.write(postRamble);
                    }

                    if (count > 0) {
                        ow.flush();
                    }
                } catch (IOException e) {
                    Log.util.error("unable to close stream in JsonSink, %s %e", e, e);
                }
            }
            started = false;
        }
        return !started;
    }


    public void close() {
        if (closed == false) {
            try {
                ow.close();
            } catch (IOException e) {
                Log.util.error("unable to close stream in JsonSink, %s", e);
            }
            closed = true;
        }
    }
}

