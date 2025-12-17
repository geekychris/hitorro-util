package ht.util.core.iterator.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.AbstractIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;


/**
 *
 */
public class BaseJsonIterator extends AbstractIterator<JsonNode> {
    private final static JsonFactory factory = new JsonFactory();

    protected HTJSONParser jnparser;
    protected JsonParser parser = null;
    protected JsonNode domlet;

    protected boolean closed = false;

    public BaseJsonIterator(JsonParser parser, boolean switchOnToken) {
        init(parser);
    }

    public BaseJsonIterator(BaseFile baseFile) throws IOException {
        init(factory.createParser(baseFile.getInputStreamRaw()));
    }

    public BaseJsonIterator(InputStream is) throws IOException {
        init(factory.createParser(is));
    }

    public BaseJsonIterator(Reader reader) throws IOException {
        init(factory.createParser(reader));
    }

    private void init(final JsonParser parser) {
        this.parser = parser;
        jnparser = new HTJSONParser(parser);
    }

    @Override
    public void close() throws Exception {
        if (parser != null) {
            parser.close();
        }
        super.close();
    }

    public boolean hasNext() {
        if (closed) {
            return false;
        }
        if (domlet == null) {
            domlet = jnparser.read();
            if (domlet == null) {
                closed = true;
                return false;
            }
        }
        return true;
    }

    public JsonNode next() {
        JsonNode dom = domlet;
        domlet = null;
        return dom;
    }

    public void remove() {

    }


}
