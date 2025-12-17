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
package com.hitorro.util.io;

import com.hitorro.util.core.Log;

import java.io.*;


/**
 * Class <code>TextReader</code> provides methods for reading character type data an input source, either the keyboard
 * or a text file (characters such as digits and letters only). <code>TextReader</code> objects can be constructed
 * either from an <code>CInputStream</code> such as <code>System.in</code> (the keyboard) and in fact is done
 * automatically with this instantiation: <br> <br> <code> &nbsp;&nbsp;TextReader keyboard = new TextReader( );<br>
 * </code> <br> or by specifying the name of a file as a <code>String</code> as in <br> <br> <code>
 * &nbsp;&nbsp;TextReader inputFile = new TextReader( "input.data" ); <br> </code> <br> Written by Stuart Reges at the
 * Univerity of Arizona 6/11/98 with modifications by Rick Mercer August 1998 through January 2002.
 */

public class TextReader {
    /**
     * Reads one string (terminated by end-of-file or whitespace). This method will skip any leading whitespace.
     * Precondition stream contains at least one nonwhitespace character
     *
     * @return The next string in the input disk file or that is typed at the keyboard.
     */
    StringBuffer wordBuf = new StringBuffer();
    //--instance variables
    // PushbackReader used here to avoid bugs in the 1.1 BufferedReader class
    // It also allows for a "peek" method.
    private PushbackReader in;   // the input stream
    // true If from keyboard or false when input is from a file
    private boolean rePrompting; // users should be prompted, but not files


    /**
     * Construct an object to obtain input from the keyboard (no arguments)
     */
    public TextReader() { // pre : input stream is open for reading
        // post: constructs a TextReader object associated with the keyboard
        in = new PushbackReader(new InputStreamReader(System.in));
        rePrompting = true;
    }

    public TextReader(InputStreamReader isr) {
        try {
            in = new PushbackReader(isr);
        } catch (Exception e) {
            Log.util.warn("Can't open input stream");
        }
    }

    /**
     * Construct an object used to obtain input from the disk file with only text data such as letters, digits and other
     * characters such as * % ).
     *
     * @param fileName A String that repesents a disk on file such as "/usr/me/number.dat" or "C:\\cs101\\number.dat".
     */
    public TextReader(File fileName) { // pre : fileName is the name of a file that can be opened for reading
        // post: constructs a TextReader tied to the given file
        try {
            in = new PushbackReader(new FileReader(fileName));
        } catch (Exception e) {
            Log.util.warn("Can't open input file '%s'", fileName);
        }
    }

    public TextReader(String buffer) {
        try {
            in = new PushbackReader(new StringReader(buffer));
        } catch (Exception e) {
            Log.util.warn("Unable to create reader from buffer");
        }

    }

    private void error(String where) { // Have a standard way of displaying error messages
        Log.util.warn("\n***Failure in %s message.", where);
        if (!rePrompting) { // When reading from an input file, some IDEs will close output
            // window before the above message can be read by the tester.
            // So cause a pause
            Log.util.warn("Press enter to terminate program . . . ");
            char ch = read();
        }
        Log.util.warn("Program terminating . . . ");
        System.exit(1);
    }

    /**
     * Use this method to read in entire lines of data such as a persons full name or an address.  You can also use it
     * to enter a string that maay or may not have blanks spaces. Precondition: The input is not at end-of-file of the
     * input stream.
     *
     * @return All characters (including blank spaces) up until the enter key is entered.
     */
    public String readLine() {
        String result = "";

        try {
            do {
                int next = in.read();
                if (next == '\r') // skip carriage-return on Windows systems
                {
                    continue;
                }
                if (next == -1 || next == '\n') {
                    break;
                }
                result += (char) next;
            }
            while (true);
        } catch (Exception e) {
            error("readLine");
        }

        return result;
    }

    /**
     * Returns the next character on the input stream. Same as read. Precondition: The input is not at end-of-file of
     * the input stream.
     *
     * @return The next character in the input stream.
     */
    public char readChar() {
        return read();
    }

    /**
     * Returns the next character on the input stream. Same as readChar. Precondition: The input is not at end-of-file
     * of the input stream.
     *
     * @return The next character in the input stream.
     */
    public char read() { // pre : not at end-of-file of the input stream
        // post: reads the next character of input and returns it

        char result = ' ';

        try {
            result = (char) in.read();
            if (result == '\r') // skip carriage-return on Windows systems
            {
                result = (char) in.read();
            }
        } catch (Exception e) {
            Log.util.warn("Failure in call on read method, program terminated.");
            System.exit(1);
        }

        return result;
    }

    /**
     * Puts the given character back into the input stream to be read again.
     */
    public void unread(char ch) {
        try {
            in.unread((byte) ch);
        } catch (Exception e) {
            error("unread");
        }
    }

    /**
     * Use this to find out what the next character is when what you do depends on it. Especially useful when processing
     * complex file input with unknown amounts of input data. The peek method allows for multiple sentinels in the input
     * file.
     *
     * @return the next character in the input stream without actually reading it.
     */
    public char peek() {
        int next = 0;

        try {
            next = in.read();
        } catch (Exception e) {
            error("peek");
        }
        if (next != -1) {
            unread((char) next);
        }

        return (char) next;
    }

    public String readWord() {
        wordBuf.setLength(0);
        int next = 0;
        try {

            do {
                next = in.read();
            }
            while (next != -1 && Character.isWhitespace((char) next));

            while (next != -1 && !Character.isWhitespace((char) next)) {
                wordBuf.append((char) next);
                next = in.read();
            }

            while (next != -1 && next != '\n' && Character.isWhitespace((char) next)) {
                next = in.read();
            }

            if (next != -1 && next != '\n') {
                unread((char) next);
            }
        } // end try
        catch (Exception e) {
            error("readWord");
        } // end catch

        if (next == -1) {
            // check to see if the word contained anything...if not we got to the end of file
            if (wordBuf.length() == 0) {
                return null;
            }
        }
        return wordBuf.toString();
    }

    /**
     * Reads an int and skips any trailing whitespace on current line. Keeps trying if floating point number is invalid.
     * Precondition: next token in input stream is an int.
     *
     * @return The next integer in the input disk file or that is typed at the keyboard.
     */
    public int readInt() {
        int result = 0;
        do // keep on trying until a valid double is entered
        {
            try {
                String intAsString = readWord();
                if (intAsString.equalsIgnoreCase("quit")) {
                    System.exit(1);
                }
                result = Integer.parseInt(intAsString);
                break;  // result is good, jump out of loop in order to return result;
            } catch (Exception e) {
                if (rePrompting) {
                    Log.util.warn("Invalid integer. Try again or enter QUIT to terminate program");
                } else {
                    error("readInt");
                    break;
                }
            }
        }
        while (true);

        return result;
    }

    /**
     * Reads a floating-point number and skips any trailing whitespace on the current line. Keeps trying if floating
     * point number is invalid. Precondition: next token in input stream is an valid number (int or double).
     *
     * @return The next floating point number in the input disk file or that is typed at the keyboard.
     */
    public double readDouble() { // pre : next token in input stream is a double
        // post: reads double and skips any trailing whitespace on current line
        //       Keeps trying if floating point number is invalid
        double result = 0.0;

        do  // keep on trying until a valid double is entered
        {
            try {
                String doubleAsString = readWord();
                if (doubleAsString.equalsIgnoreCase("quit")) {
                    System.exit(1);
                }
                result = Double.parseDouble(doubleAsString);
                break;  // result is good, jump out of loop down to return result;
            } catch (Exception e) {
                if (rePrompting) {
                    Log.util.warn("Invalid floating-point number. Try again or enter QUIT to terminate program");
                } else {
                    error("readDouble");
                    break;
                }
            }
        }
        while (true);
        return result;
    }

    /**
     * Find out if the input stream has more data that can be read. This is especially useful when processing file data
     * where the size of the file is not determined (or changes a lot).
     *
     * @return true if input stream is ready for reading otherwise returns false.
     */
    public boolean ready() {
        boolean result = false;

        try {
            result = in.ready();
        } catch (IOException e) {
            error("ready");
        }
        return result;
    }
}

