/*
    Copyright (c) 2007 HiTorro


    User: chris
*/

package ht.util.io.csv;

import ht.util.basefile.fs.BaseFile;
import ht.util.io.csv.csvconsumer.CSVConsumer;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Read a CSV file, Excel-style. This code will read an excel-style csv file (double-quotes to escape a quote).
 */
public class CSVReader extends CSVReaderBase {
    public CSVReader(String filepath) throws FileNotFoundException {
        super(filepath);
    }

    public CSVReader(File inFile) throws FileNotFoundException {
        super(inFile);
    }

    public CSVReader(BaseFile filepath, String encoding) throws IOException {
        super(filepath, encoding);
    }

    public CSVReader(BaseFile filepath, String encoding, char seperator) throws IOException {
        super(filepath, encoding, seperator);
    }

    public CSVReader(File inFile, char seperator) throws FileNotFoundException {
        super(inFile, seperator);
    }

    public CSVReader(InputStream inStream) {
        super(inStream);
    }

    public CSVReader(InputStream inStream, Charset cset) {
        super(inStream, cset);
    }

    public CSVReader(Reader rdr, char seperator) {
        super(rdr, seperator);
    }

    public CSVReader(Reader rdr) {
        super(rdr);
    }


    /**
     * Send all the data from the csv file to a CSV consumer. The CSVConsumer will first be handed a line containing the
     * column names and then will be handed all the remaning lines of the file.  Conventionally this method is called
     * immediately after constructing the CSVReader, in which case all the lines are sent to the consumer.  The
     * CSVReader should still be closed when the routine finishes
     *
     * @param consumer the consumer which will be given the file's data
     */
    public void readLines(CSVConsumer consumer)
            throws IOException {
        int row = 0;
        if (m_colNames != null) {
            consumer.line(row++, m_colNames);
        }
        // read the rest of the file, parsing the lines, and
        // handing the data to the consumer
        String[] line = getNextRow();
        while (line != null) {
            consumer.line(row++, line);
            line = getNextRow();
        }
    }
}