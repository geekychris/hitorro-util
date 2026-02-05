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
package com.hitorro.util.io.csv;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Log;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 *
 */
public class CSVReaderBase {
    protected BufferedReader m_reader = null;
    protected String[] m_colNames = null;
    protected char seperatorToken = ',';
    protected boolean adjustColumns = true;

    //-------------------------------------------------------------------------

    /**
     * Construct a CSVReader on a file The file is given by a filepath, and is assummed to exist.  The default character
     * set is assumed for reading.
     *
     * @param filepath The file to read
     */
    public CSVReaderBase(String filepath)
            throws FileNotFoundException {
        this(new FileReader(filepath), ',');
    }

    /**
     * Construct a CSVReader on a file The file is given by a File, and is assummed to exist.  The default character set
     * is assumed for reading.
     *
     * @param inFile The file to read
     */
    public CSVReaderBase(File inFile)
            throws FileNotFoundException {
        this(new FileReader(inFile), ',');
    }

    public CSVReaderBase(BaseFile filepath, String encoding)
            throws IOException {
        this(filepath.getReader(encoding), ',');
    }

    public CSVReaderBase(BaseFile filepath, String encoding, char seperator)
            throws IOException {
        this(filepath.getReader(encoding), seperator);
    }

    public CSVReaderBase(File inFile, char seperator)
            throws FileNotFoundException {
        this(new FileReader(inFile), seperator);
    }

    /**
     * Construct a CSVReader on an input stream, using default character set.
     *
     * @param inStream The stream to read
     */
    public CSVReaderBase(InputStream inStream) {
        this(new InputStreamReader(inStream), ',');
    }

    /**
     * Construct a CSVReader on an input stream.
     *
     * @param inStream The stream to read
     * @param cset     The character set to use
     */
    public CSVReaderBase(InputStream inStream, Charset cset) {
        this(new InputStreamReader(inStream, cset), ',');
    }

    /**
     * Construct a CSVReader on a reader.
     *
     * @param rdr The Reader to consume
     */
    public CSVReaderBase(Reader rdr, char seperator) {
        init(seperator, rdr);
    }

    /**
     * Construct a CSVReader on a reader.
     *
     * @param rdr The Reader to consume
     */
    public CSVReaderBase(Reader rdr) {
        init(',', rdr);
    }

    /**
     * Break up a string into tokens in Excel CSV file style This method will take a source string and break it into
     * substrings.  The tokens are separated by a separator character.  A token may begin with a quote character, in
     * which case it doesn't end until the next quote character is encountered.  Note that a doubled quote character
     * doesn't count as an end quote in this case, it is simply a quote character. <br> The only way for the separator
     * character to be in a token is for the token to be quoted. <br> Leading and trailing white space around a token is
     * considered to be part of the token <br>
     *
     * @param source    The string we will break into tokens
     * @param separator The separator character
     * @param quote     The quote character
     * @return an array of tokens, or null if source is null
     */
    static String[] csvTokenize(String source, char separator, char quote) {
        if (source == null) {
            return null;
        }

        ArrayList<String> scratch = new ArrayList();
        int slen = source.length();

        char[] buffer = new char[slen];
        int bufferind = 0;
        char addchar = '?';

        boolean inbare = false; // in unquoted token
        boolean inquote = false; // in quoted token
        boolean doaddchar = false;
        boolean doendtoken = false;
        for (int ii = 0; ii < slen; ii++) {
            char cc = source.charAt(ii);
            if (bufferind == 0 && cc == ' ') {
                // skip over spaces.
                continue;
            }
            if (inquote) {
                if (cc == quote) {
                    if (ii < slen - 1 &&
                            source.charAt(ii + 1) == quote) {
                        // doubled quote, not end of token
                        addchar = quote;
                        ii++; // to account for extra advancement
                        doaddchar = true;
                    } else {
                        inquote = false;
                        inbare = true; // to handle the rest of the token (presumably nothing)
                    }
                } else {
                    addchar = cc;
                    doaddchar = true;
                }
            } else if (inbare) {
                if (cc == separator) {
                    doendtoken = true;

                } else {
                    addchar = cc;
                    doaddchar = true;
                }
            } else {
                // starting a token - with quote or not?
                if (cc == separator) {
                    doendtoken = true; // empty token, which is legal
                } else if (cc == quote) {
                    inquote = true;
                } else {
                    inbare = true;
                    addchar = cc;
                    doaddchar = true;
                }
            }

            if (doaddchar) {
                if (bufferind < buffer.length) {
                    buffer[bufferind++] = addchar;
                }
                doaddchar = false; // reset flag
            }
            if (doendtoken) {
                if (bufferind > 0) {
                    while (buffer[bufferind - 1] == ' ') {
                        // remove trailing spaces
                        bufferind--;
                    }
                }
                String tk = new String(buffer, 0, bufferind);
                //tk = removePaddedQuotes(tk);
                bufferind = 0;
                scratch.add(tk);
                inquote = false;
                inbare = false;
                doendtoken = false; // reset flag

            }
        }

        // handle the last token
        if (bufferind > 0) {
            while (buffer[bufferind - 1] == ' ') {
                // remove trailing spaces
                bufferind--;
            }
        }
        String tk = new String(buffer, 0, bufferind);
        scratch.add(tk);

        Object[] sr = scratch.toArray();
        String[] retval = new String[sr.length];
        System.arraycopy(sr, 0, retval, 0, sr.length);

        return retval;
    }

    static final String removePaddedQuotes(String p) {
        int length = p.length();
        int start = 0;
        int end = length - 1;
        char c = p.charAt(start);
        while (start < length && c == ' ') {
            start++;
            c = p.charAt(start);
        }
        if (c != '"') {
            // dont care, its not a quote.
            return p;
        }
        c = p.charAt(end);
        while (end >= 0 && c == ' ') {
            start++;
            c = p.charAt(start);
        }
        if (c != '"') {
            // dont care, its not a quote.
            return p;
        }
        return null;
    }

    public String[] getHeader() {
        return m_colNames;
    }

    protected void init(final char seperator, final Reader rdr) {
        seperatorToken = seperator;
        try {
            m_reader = new BufferedReader(rdr);

            readColumnHeaders();
        } catch (IOException ioe) {
            Log.util.warn("Error opening csv file: %s", ioe);
            m_reader = null;
        }
    }

    /**
     * Read the first line, to get the column headers
     */
    private void readColumnHeaders()
            throws IOException {
        if (m_reader == null) {
            return;
        }

        String inline = m_reader.readLine();
        m_colNames = csvTokenize(inline, seperatorToken, '"');
    }

    /**
     * Fix a row so that it has the right number of columns
     */
    protected String[] adjustValueLength(String[] vals0) {
        String[] vals = new String[m_colNames.length];

        // copy up to correct length
        int nCopy = (vals0.length < vals.length) ? vals0.length : vals.length;
        System.arraycopy(vals0, 0, vals, 0, nCopy);

        // fill in anything missing with empty strings
        for (int ii = vals0.length; ii < vals.length; ii++) {
            vals[ii] = "";
        }

        return vals;
    }

    /**
     * Dispose of resources used by the reader
     */
    public boolean close() {
        if (m_reader == null) {
            return true;
        }

        try {
            m_reader.close();
        } catch (IOException ioe) {
        }
        m_reader = null;
        return true;
    }

    /**
     * Get the names of the file's columns If the file couldn't be found or read, will return null
     *
     * @return the column names (in the order they will appear) or null if the file could not be read
     */
    public String[] getColumnNames() {
        return m_colNames;
    }

    /**
     * Get the next row of data The columns will be in the same order as the column names.  We guarantee that the same
     * number of columns are returned as there are column names.
     *
     * @return The column values, or null if there is no more data
     */
    public String[] readCSVLine()
            throws IOException {
        return getNextRow();
    }


    /**
     * Get the next row of data The columns will be in the same order as the column names.  We guarantee that the same
     * number of columns are returned as there are column names.
     *
     * @return The column values, or null if there is no more data
     */
    public String[] getNextRow()
            throws IOException {
        if (m_reader == null) {
            return null;
        }
        String inline = m_reader.readLine();
        if (inline != null) {
            String[] vals = csvTokenize(inline, seperatorToken, '"');
            if (adjustColumns && vals.length != m_colNames.length) {
                vals = adjustValueLength(vals);
            }
            return vals;
        }

        // if we are here, we're done with the file - close it
        try {
            m_reader.close();
            m_reader = null;
        } catch (IOException ioe) {
            // ignore failures while closing
        }

        return null;
    }


    /**
     * Make sure we've discarded our resources
     */
    @SuppressWarnings("removal") // finalize() is deprecated for removal; consider using try-with-resources
    @Override
    protected void finalize() {
        close();
    }
}
