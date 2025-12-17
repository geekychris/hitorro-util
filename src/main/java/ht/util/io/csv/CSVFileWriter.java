/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

    
    User: chris
*/

package ht.util.io.csv;

import ht.util.core.Log;
import ht.util.io.FileUtil;

import java.io.*;
import java.util.List;

/**
 * Write a CSV file.
 */
public class CSVFileWriter implements CSVWriter {
    private PrintStream _printer = null;
    private int _nColumns;
    private char seperator = ',';

    public CSVFileWriter(String filepath, List<String> colNames) {
        this(new File(filepath), colNames);
    }

    public CSVFileWriter(File outfile, List<String> colNames) {
        try {
            _printer = new PrintStream(FileUtil.getBufferedFileOutputStream(outfile));
        } catch (IOException ioe) {
            Log.util.error(ioe, "Error creating output for CSVFileWriter");
            _printer = null;
        }
        _nColumns = colNames.size();

        // write out the header
        writeRow(colNames);
    }

    public CSVFileWriter(File outfile, String colNames[]) {
        this(outfile, null, colNames);
    }

    public CSVFileWriter(File outfile, String encoding, String colNames[]) {
        try {
            FileOutputStream fout = new FileOutputStream(outfile);
            if (encoding == null) {
                // use default encoding
                _printer = new PrintStream(fout);
            } else {
                _printer = new PrintStream(fout, true, encoding);
            }
        } catch (IOException ioe) {
            Log.util.error(ioe, "Error creating output for CSVFileWriter");
            _printer = null;
        }
        _nColumns = colNames.length;

        // write out the header
        writeRow(colNames);
    }

    public CSVFileWriter(OutputStream outStream, List<String> colNames) {
        this(outStream, colNames, ',');
    }

    public CSVFileWriter(OutputStream outStream, List<String> colNames, char seperator) {
        _printer = new PrintStream(outStream);
        _nColumns = colNames.size();

        // write out the header
        writeRow(colNames);
    }


    public CSVFileWriter(PrintStream out, String colNames[]) {
        _printer = out;
        _nColumns = colNames.length;

        // write out the header
        writeRow(colNames);
    }

    public static void writeValue(PrintStream printer, String val) {
        if (val == null) {
            return;
        }

        // check for commas and quotes
        int qi = val.indexOf('"');
        int ci = val.indexOf(',');

        // if we have neither, just print the value
        if (qi < 0 && ci < 0) {
            printer.print(val);
            return;
        }

        // if we have just commas, just slap on some quotes
        if (qi < 0) {
            printer.print('"');
            printer.print(val);
            printer.print('"');
            return;
        }

        // with embedded quotes, we'll have to put quotes and double any
        // contained quotes
        printer.print('"');
        int len = val.length();
        for (int ii = 0; ii < len; ii++) {
            char cc = val.charAt(ii);
            if (cc == '"') {
                printer.print("\"\"");
            } else {
                printer.print(cc);
            }
        }
        printer.print('"');
    }

    /**
     * Get the printer being used to write to the output. This is useful if you want to do specialized formatting.
     *
     * @return the printer, will be null after the writer is closed
     */
    public PrintStream getPrinter() {
        return _printer;
    }

    public void close() {
        if (_printer != null) {
            _printer.close();
            _printer = null;
        }
    }

    public void writeRow(List<String> values) {
        if (_printer == null) {
            return;
        }

        for (int ii = 0; ii < _nColumns; ii++) {
            if (ii > 0) {
                _printer.print(seperator);
            }
            if (values.get(ii) != null) {
                String sval = values.get(ii);
                writeValue(sval); // take care of quotes and commas
            }
        }
        _printer.println();
    }

    public void writeRow(Object values[]) {
        if (_printer == null) {
            return;
        }

        for (int ii = 0; ii < _nColumns; ii++) {
            if (ii > 0) {
                _printer.print(seperator);
            }
            if (values[ii] != null) {
                String sval = values[ii].toString();
                writeValue(sval); // take care of quotes and commas
            }
        }
        _printer.println();
    }

    protected void finalize()
            throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Write a value, taking care of commas and quotes.
     *
     * @param val The value to flushToDisk
     */
    private void writeValue(String val) {
        writeValue(_printer, val);
    }
}
