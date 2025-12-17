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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p>
 * User: chris
 * Date: Jul 12, 2004
 * Time: 2:03:58 PM
 * <p>
 * Description:
 * <p>
 * SHIT IMPLEMENTATION of bytes to input stream
 */

/**
 * Provides a CInputStream from a given String. Internally converts the String to a byte array taking the lower 8 bits of
 * each char and ignoring the upper 8 bits.
 */

public class StringInputStream extends InputStream
        implements Serializable {

    /**
     * The String that represents the internal byte array as a String.
     */
    protected String Text = null;
    /**
     * The internal byte array.
     */
    protected byte[] bytes = null;
    /**
     * Not used in this StringOutputStream but readlimit is still set in the mark method.
     */
    protected int readlimit = 0;
    /**
     * Originally set to zero. Set to position in the mark method.
     */
    protected long resetPosition = 0;
    /**
     * The position in the internal byte array.
     */
    protected long position = 0;

    /**
     * Creates a StringInputStream from a String.
     *
     * @param s String to convertToPdf to the internal byte array.
     */
    public StringInputStream(String S) {
        super();
        Text = S;
        bytes = convertStringToBytes(S);
        readlimit = bytes.length;
    }

    /**
     * Creates a StringInputStream from a byte array.
     *
     * @param b byte array to set at the internal byte array.
     */
    public StringInputStream(byte[] b) {
        super();
        Text = new String(b);
        bytes = b;
        readlimit = bytes.length;
    }

    /**
     * Converts a String to a byte array, taking the eight lower bits of each char as the eight bits of the bytes for
     * the byte array.
     *
     * @param Str the String to convertToPdf to byte array.
     *
     * @return the new byte array converted from a String.
     */
    public static byte[] convertStringToBytes(String Str) {
        char[] NewChr = Str.toCharArray();
        byte[] NewByt = new byte[NewChr.length];
        for (int i = 0; i < NewByt.length; i++) {
            int Ci = NewChr[i] & 255;
            NewByt[i] = (byte) Ci;
        }
        return NewByt;
    }

    /**
     * Converts a byte array to a String, taking the eight bits of each byte as the lower eight bits of the chars in the
     * String.
     *
     * @param bytes the byte array to convertToPdf to char array.
     *
     * @return the new String converted from a byte array.
     */
    public static String convertBytesToString(byte[] bytes) {
        return new String(convertBytesToChars(bytes));
    }

    /**
     * Converts a byte array to a char array, taking the eight bits of each byte as the lower eight bits of the char.
     *
     * @param bytes the byte array to convertToPdf to char array.
     *
     * @return the new char array converted from a byte array.
     */
    public static char[] convertBytesToChars(byte[] bytes) {
        char[] NewChr = new char[bytes.length];
        for (int i = 0; i < NewChr.length; i++) {
            int Ci = bytes[i] & 255;
            NewChr[i] = (char) Ci;
        }
        return NewChr;
    }

    /**
     * Replaces all occurences of char of one type with another char in a given byte array and returns it. Change is
     * made to the byte array and it is also returned.
     *
     * @param Bytes the byte array to change bytes in.
     * @param Old   the char to find, converted to a byte by the lower eight bits, ignoring the higher eight bits.
     * @param New   the char to replace all occurences of Old char converted to a byte by the lower eight bits, ignoring
     *              the higher eight bits.
     *
     * @return the changed byte array.
     */
    public static byte[] replaceBytes(byte[] Bytes, char Old, char New) {
        for (int i = 0; i < Bytes.length; i++) {
            int bint = Bytes[i] & 255; //full byte is byte & 255 - converts to int
            if (bint == (Old & 127)) {
                Bytes[i] = (byte) (New & 127); //ASCII is char & 127
            }
        }
        return Bytes;
    }

    /**
     * Replaces one String with another where it occurs of a byte array making a new array due to the possibility of
     * different size. Goes through the array just once, so any new occurances of Old String that appear due to the New
     * String replacement are not replaced. Does no change to the byte array parameter Bytes.
     *
     * @param Bytes the byte array copy and search through but does no change to this parameter, returning the resulting
     *              byte array.
     * @param Old   the old String to replace.
     * @param New   the new String to replace Old String with.
     *
     * @return the new byte array with replacements done.
     */
    public static byte[] replaceBytes(byte[] Bytes, String Old, String New) {
        String NewStr = replace(new String(Bytes), Old, New);
        char[] NewChr = NewStr.toCharArray();
        byte[] NewByt = new byte[NewChr.length];
        for (int i = 0; i < NewByt.length; i++) {
            int Ci = NewChr[i] & 255;
            NewByt[i] = (byte) Ci;
        }
        return NewByt;
    }

    /**
     * <P>Convenience method for writing bytes to an COutputStream. Closes resources within a try finally block.
     *
     * @param OPut COutputStream to write to.
     * @param bbuf The contents to write to the COutputStream, OPut.
     *
     * @throws Exception Probably an IO Exception if any.
     */

    public static void writeBytes(FileOutputStream OPut, byte[] bbuf) throws Exception {
        try {
            OPut.write(bbuf, 0, bbuf.length);
            OPut.flush();
        } catch (Exception ex) {
            throw ex;
        } finally {
            OPut.close();
        }
    }

    /**
     * <P>Convenience method for reading bytes from an CInputStream. Closes resources within a try finally block.
     *
     * @param IPut CInputStream to read from.
     *
     * @return The contents read from the CInputStream, IPut.
     * @throws Exception Probably an IO Exception if any.
     */

    public static byte[] readBytes(InputStream IPut) throws Exception {
        Vector BytArrsV = new Vector();
        int size = 0;
        byte[] FinalVal = new byte[0];
        int read = 0;
        try {
            int i = 0;
            byte[] bbuf = new byte[1024];
            while ((read = IPut.read(bbuf, 0, bbuf.length)) > -1) {
                byte[] bbuf2 = new byte[read];
                for (i = 0; i < bbuf2.length; i++) {
                    bbuf2[i] = bbuf[i];
                }
                BytArrsV.addElement(bbuf2);
                size += read;
            }
            FinalVal = new byte[size];
            int j = 0;
            for (i = 0; i < BytArrsV.size(); i++) {
                byte[] byarr = (byte[]) BytArrsV.elementAt(i);
                for (int k = 0; k < byarr.length; k++) {
                    FinalVal[j++] = byarr[k];
                }
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            IPut.close();
        }
        return FinalVal;
    }

    /**
     * <P>Used to replace one String segment with another String segment inside a String. Similar to the replace method
     * in String but instead of using char it uses String for replacing old with new.
     *
     * @param Text The String from which is produced the new String with which replacement has occurred.
     * @param Old  The old String that is replaced by the new one in The Text String.
     * @param New  The new String to replace the old String in the Text String.
     *
     * @return The new String with replacement having occurred.
     */

    public static String replace(String Text, String Old, String New) {
        if (Old.length() == 0) {
            return Text;
        }
        StringBuffer buf = new StringBuffer();
        int i = 0, j = 0;
        while ((i = Text.indexOf(Old, j)) > -1) {
            buf.append(Text.substring(j, i) + New);
            j = i + Old.length();
        }
        if (j < Text.length()) {
            buf.append(Text.substring(j));
        }
        return buf.toString();
    }

    /**
     * Gets the String that is of the internal byte array.
     *
     * @return the String representing the internal byte array.
     */
    public String toString() {
        return Text;
    }

    /**
     * Gets the String that is of the internal byte array.
     *
     * @return the String representing the internal byte array.
     */
    public String getString() {
        return Text;
    }

    /**
     * Gets the internal byte array.
     *
     * @return the internal byte array.
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * The whole internal byte array is available.
     *
     * @return the length of the internal byte array.
     */
    public int available() {
        return bytes.length;
    }

    /**
     * Makes Text and bytes (the internal byte array) fields set to null.
     */
    public void close() {
        Text = null;
        bytes = null;
        readlimit = 0;
        resetPosition = 0;
        position = 0;
    }

    /**
     * sets resetPosition to current position in StringInputStream. if reset is called position shall be set to
     * resetPosition.
     *
     * @param readlimit is not used in this CInputStream, StringInputStream but its value is still set as the value of
     *                  the global readlimit int.
     */
    public void mark(int readlimit) {
        this.readlimit = readlimit; //not used for reset.
        resetPosition = position;
    }

    /**
     * Methods mark and reset are supported in this CInputStream, StringInputStream.
     *
     * @return true.
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * reads a single byte from the internal byte array and returns it as an int, which is made by bitwise Anding the
     * eight bits of the byte by 255.
     *
     * @return the number of bytes read or -1 if the end of the byte array is reached and there are no more bytes to
     *         read.
     */
    public int read() {
        if (position >= bytes.length) {
            return -1;
        }
        return bytes[(int) position++] & 255; //converts byte to int.
    }

    /**
     * reads through internal byte array by the length of byte array b and places the read bytes into byte array b. Will
     * not read beyond length of internal byte array.
     *
     * @param b the byte array to contain the read bytes.
     *
     * @return the number of bytes read or -1 if the end of the byte array is reached and there are no more bytes to
     *         read.
     */
    public int read(byte[] b) {
        if (position >= bytes.length) {
            return -1;
        }
        int i = 0;
        FOR:
        for (i = 0; i < b.length; i++) {
            if (position >= bytes.length) {
                break FOR;
            }
            b[i] = bytes[(int) position++];
        }
        return i;
    }

    /**
     * reads through internal byte array by len bytes and places them in byte array b starting at index off in byte
     * array b. Will not read beyond length of internal byte array.
     *
     * @param b   the byte array to contain the read bytes.
     * @param off the starting index in byte array b to start reading the bytes into.
     * @param len the number of bytes to read from the internal byte array into byte array b.
     *
     * @return the number of bytes read or -1 if the end of the byte array is reached and there are no more bytes to
     *         read.
     */
    public int read(byte[] b, int off, int len) {
        if (position >= bytes.length) {
            return -1;
        }
        int i = 0;
        int pos = off;
        FOR:
        for (i = 0; i < len && pos < b.length; i++) {
            if (position >= bytes.length) {
                break FOR;
            }
            b[pos++] = bytes[(int) position++];
        }
        return i;
    }

    /**
     * resets position to resetPosition, which starts out as 0 and becomes set to position whenever mark method is
     * called.
     */
    public void reset() {
        position = resetPosition;
    }

    /**
     * skips ahead in the internal byte array by n bytes.
     *
     * @param n - the number of bytes to be skipped.
     *
     * @return the actual number of bytes skipped.
     */
    public long skip(long n) {
        if (n <= 0) {
            return (long) 0;
        }
        long prevpos = position;
        long newpos = position + n;
        position = newpos > bytes.length ? bytes.length : newpos;
        return position - prevpos;
    }
}

/*Java and all Java-based marks are trademarks or registered trademarks
 *of Sun Microsystems, inc. in the U.S. and other countries.
 */