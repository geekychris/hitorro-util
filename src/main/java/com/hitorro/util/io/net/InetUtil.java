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
package com.hitorro.util.io.net;

import com.hitorro.util.core.IntegerUtil;
import com.hitorro.util.core.string.StringUtil;

public class InetUtil {
    /**
     * Convert an IP4 IP Address (ie 66.10.27.100) to a unique integer
     *
     * @param address - String IP address: ie "66.10.27.100"
     * @return int
     */
    public static int inetAddressToInt(String address) {
        int byteAccum = 0;
        int intAccum = 0;
        boolean nextByte = true;

        for (int i = 0; i < address.length(); i++) {
            char c = address.charAt(i);

            if (IntegerUtil.isNumber(c)) {
                if (nextByte) {
                    nextByte = false;
                    byteAccum = 0;
                }

                byteAccum = byteAccum * 10;
                byteAccum = byteAccum + IntegerUtil.charNumbertoInt(c);
            }

            if (c == '.' || i == address.length() - 1 || !IntegerUtil.isNumber(c)) {
                intAccum = intAccum << 8;
                intAccum = intAccum | 0;
                intAccum = intAccum | byteAccum;
                nextByte = true;
            }
        }

        return intAccum;
    }


    /**
     * Convert unique integer representation of an IP address to array representing addresses 4 bytes of data.
     *
     * @param address - unique integer representation of IP address
     * @return array of int represent the 4 bytes in an IP address
     */
    public static int[] intToInetAddress(int address) {
        int[] inetBytes = {0, 0, 0, 0};

        for (int i = 24, j = 0; i >= 0; i = i - 8, j++) {
            inetBytes[j] = (address >> i) & 0x000000FF;
        }

        return inetBytes;
    }


    /**
     * Convert unique integer representation of an IP address to String form, ie "66.10.27.100"
     *
     * @param address - unique integer representation of IP address
     * @return String representation of IP address
     */
    public static String intToInetAddressString(int address) {
        int[] inetBytes = intToInetAddress(address);
        String[] inetBytesString = {"0", "0", "0", "0"};


        for (int i = 0; i <= 3; i++) {
            if (inetBytes[0] != 0) {
                inetBytesString[i] = String.valueOf(inetBytes[i]);
            }
        }

        return StringUtil.mergeWithJoinToken(inetBytesString, ".");
    }
}
