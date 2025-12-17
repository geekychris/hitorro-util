package ht.util.basefile.tools;

import ht.util.basefile.filters.FileEndsWith;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.Log;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.excelaccess.POICSVIterator;
import ht.util.io.csv.CSVIterator;
import ht.util.io.csv.CSVIteratorImpl;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Naive mapping from basefile to a csviterator.  You get some control if you think its a multi sheet xls file and you
 * want to control which sheet to read.
 */
public class BaseFileCSV2CSVIteratorMapper extends BaseMapper<BaseFile, CSVIterator> {
    private static final FileEndsWith XLSExt = new FileEndsWith("xls", true, true);

    private String sheet;
    private String encoding;
    private char seperator;

    public BaseFileCSV2CSVIteratorMapper(String sheet, String encoding) {
        this(sheet, encoding, ',');
    }

    public BaseFileCSV2CSVIteratorMapper(String sheet, String encoding, char seperator) {
        this.sheet = sheet;
        this.encoding = encoding;
        this.seperator = seperator;
    }

    @Override
    public CSVIterator apply(final BaseFile e) {
        if (XLSExt.test(e)) {
            try {
                return new POICSVIterator(BaseFileUtil.bf2inputstream.apply(e), sheet, true);
            } catch (IOException e1) {
                Log.io.error("Unable to construct POICSVIterator due to %s %e", e, e);
                return null;
            }
        } else {
            try {
                return new CSVIteratorImpl(new InputStreamReader(BaseFileUtil.bf2inputstream.apply(e), encoding), seperator);
            } catch (IOException e1) {
                Log.io.error("Unable to construct csviteratorimpl due to %s %e", e, e);
                return null;
            }
        }
    }
}
