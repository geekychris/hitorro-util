package ht.util.io.csv;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;
import ht.util.core.iterator.sinks.BaseSink;
import ht.util.core.string.StringUtil;
import ht.util.io.StoreException;
import ht.util.json.keys.BasefileProperty;
import ht.util.json.keys.StringListFromDelimitedKey;
import ht.util.json.keys.StringProperty;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class CSVSink extends BaseSink<String[]> {
    public static final BasefileProperty OutputFileKey = new BasefileProperty("file", "outputfile using basefile syntax");
    public static final StringListFromDelimitedKey ColumnsKey = new StringListFromDelimitedKey("columns", "", ",", null);
    public static final StringProperty SepKey = new StringProperty("seperator", "", ",");
    private CSVWriter writer;

    public CSVSink() {
        // for init config driven instantiation only.
    }

    public CSVSink(String columns[], OutputStream os, char seperator) throws UnsupportedEncodingException {
        init(columns, os, "UTF-8", seperator);
    }

    public CSVSink(String columns[], OutputStream os, String encoding, char seperator) throws UnsupportedEncodingException {
        init(columns, os, encoding, seperator);
    }

    private void init(final String[] columns, final OutputStream os, final String encoding, char seperator) throws UnsupportedEncodingException {
        List<String> cols = new ArrayList();
        for (String s : columns) {
            cols.add(s);
        }
        PrintStream pw = new PrintStream(os, true, encoding);
        writer = new CSVFileWriter(pw, cols, seperator);
    }

    @Override
    public boolean init(JsonNode node) {
        BaseFile bf = OutputFileKey.apply(node);
        List<String> cols = ColumnsKey.apply(node);
        String sep = SepKey.apply(node);
        char seperator = ',';
        if (!StringUtil.nullOrEmptyString(sep)) {
            seperator = sep.charAt(0);
        }
        try {
            init(cols.toArray(new String[cols.size()]), bf.getDataOutputStream(), "UTF-8", seperator);
        } catch (IOException e) {
            Log.util.error("Unable to initialize CSVSink", e, e);
            return false;
        }
        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean add(final String[] o) throws IOException, StoreException {
        writer.writeRow(o);
        return true;
    }

    @Override
    public boolean stop() throws IOException {
        writer.close();
        return true;
    }
}
