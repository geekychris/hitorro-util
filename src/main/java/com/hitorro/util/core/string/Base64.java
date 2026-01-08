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
package com.hitorro.util.core.string;

/**
 */
/*
    Code originally from WingNut html generation engine
*/

public class Base64 {
    /**
     * decode a Base 64 encoded String. <p><h4>String to byte conversion</h4> This method uses a naive String to byte
     * interpretation, it simply gets each char of the String and calls it a byte.</p> <p>Since we should be dealing
     * with Base64 encoded Strings that is a reasonable assumption.</p> <p><h4>End of data</h4> We don't try to stop the
     * converion when we find the "=" end of data padding char. We simply put zero bytes to the unencode buffer.</p>
     */
    public static final String decode(String encoded) {
        return decode(encoded.getBytes());
    }

    public static final String decode(byte[] encoded) {
        StringBuilder sb = new StringBuilder();
        int maxturns;
        //work out how long to loop for.
        if (encoded.length % 3 == 0) {
            maxturns = encoded.length;
        } else {
            maxturns = encoded.length + (3 - (encoded.length % 3));
        }
        //tells us whether to include the char in the unencode
        boolean skip;
        //the unencode buffer
        byte[] unenc = new byte[4];
        byte b;
        for (int i = 0, j = 0; i < maxturns; i++) {
            skip = false;
            //get the byte to convertToPdf or 0
            if (i < encoded.length) {
                b = encoded[i];
            } else {
                b = 0;
            }
            //test and convertToPdf first capital letters, lowercase, digits then '+' and '/'
            if (b >= 65 && b < 91) {
                unenc[j] = (byte) (b - 65);
            } else if (b >= 97 && b < 123) {
                unenc[j] = (byte) (b - 71);
            } else if (b >= 48 && b < 58) {
                unenc[j] = (byte) (b + 4);
            } else if (b == '+') {
                unenc[j] = 62;
            } else if (b == '/') {
                unenc[j] = 63;
            }
            //if we find "=" then data has finished, we're not really dealing with this now
            else if (b == '=') {
                unenc[j] = 0;
            } else {
                char c = (char) b;
                if (c == '\n' || c == '\r' || c == ' ' || c == '\t') {
                    skip = true;
                }
            }
            //once the array has boiled convertToPdf the bytes back into chars
            if (!skip && ++j == 4) {
                //shift the 6 bit bytes into a single 4 octet word
                int res = (unenc[0] << 18) + (unenc[1] << 12) + (unenc[2] << 6) + unenc[3];
                byte c;
                int k = 16;
                //shift each octet down to read it as char and put to StringBuffer
                while (k >= 0) {
                    c = (byte) (res >> k);
                    if (c > 0) {
                        sb.append((char) c);
                    }
                    k -= 8;
                }
                //reset j and the unencode buffer
                j = 0;
                unenc[0] = 0;
                unenc[1] = 0;
                unenc[2] = 0;
                unenc[3] = 0;
            }
        }
        return sb.toString();
    }

    /**
     * encode plaintext data to a base 64 string
     *
     * @param plain the text to convertToPdf. Ignoring RFC2045 limit of 76 chars.
     * @return the encoded text.
     */

    public static String encode(byte[] plain) {
        StringBuilder sb = new StringBuilder();
        //the encode buffer
        byte[] enc = new byte[3];
        boolean end = false;
        for (int i = 0, j = 0; !end; i++) {
            if (i == plain.length - 1) {
                end = true;
            }
            enc[j++] = plain[i];
            if (j == 3 || end) {
                int res;
                //this is a bit inefficient at the end point
                //worth it for the small decrease in code size?
                res = (enc[0] << 16) + (enc[1] << 8) + enc[2];
                int b;
                int lowestbit = 18 - (j * 6);
                for (int toshift = 18; toshift >= lowestbit; toshift -= 6) {
                    b = res >>> toshift;
                    b &= 63;
                    if (b >= 0 && b < 26) {
                        sb.append((char) (b + 65));
                    }
                    if (b >= 26 && b < 52) {
                        sb.append((char) (b + 71));
                    }
                    if (b >= 52 && b < 62) {
                        sb.append((char) (b - 4));
                    }
                    if (b == 62) {
                        sb.append('+');
                    }
                    if (b == 63) {
                        sb.append('/');
                    }
                }
                //now set the end chars to be pad character if there
                //was less than integral input (ie: less than 24 bits)
                if (end) {
                    if (j == 1) {
                        sb.append("==");
                    }
                    if (j == 2) {
                        sb.append('=');
                    }
                }
                enc[0] = 0;
                enc[1] = 0;
                enc[2] = 0;
                j = 0;
            }
        }
        return sb.toString();

    }

    public static String encode(String plain) {
        return encode(plain.getBytes());
    }

}

/*
The RC2045 conversion chart,
                 0 A            17 R            34 i            51 z
         1 B            18 S            35 j            52 0
         2 C            19 T            36 k            53 1
         3 D            20 U            37 l            54 2
         4 E            21 V            38 m            55 3
         5 F            22 W            39 n            56 4
         6 G            23 X            40 o            57 5
         7 H            24 Y            41 p            58 6
         8 I            25 Z            42 q            59 7
         9 J            26 a            43 r            60 8
        10 K            27 b            44 s            61 9
        11 L            28 c            45 t            62 +
        12 M            29 d            46 u            63 /
        13 N            30 e            47 v
        14 O            31 f            48 w         (pad) =
        15 P            32 g            49 x
        16 Q            33 h            50 y


ASCII chart
Dec Hex Char   Dec Hex Char  Dec Hex Char  Dec Hex Char
  -----------    ------------  ------------  ------------
   0   0  NUL     32  20        64  40  @     96  60    `
   1   1  SOH     33  21  !     65  41  A     97  61    a
   2   2  STX     34  22  "     66  42  B     98  62    b
   3   3  ETX     35  23  #     67  43  C     99  63    c
   4   4  EOT     36  24  $     68  44  D    100  64    d
   5   5  ENQ     37  25  %     69  45  E    101  65    e
   6   6  ACK     38  26  &     70  46  F    102  66    f
   7   7  BEL     39  27  '     71  47  G    103  67    g
   8   8   BS     40  28  (     72  48  H    104  68    h
   9   9  TAB     41  29  )     73  49  I    105  69    i
  10   A   LF     42  2A  *     74  4A  J    106  6A    j
  11   B   VT     43  2B  +     75  4B  K    107  6B    k
  12   C   FF     44  2C  ,     76  4C  L    108  6C    l
  13   D   CR     45  2D  -     77  4D  M    109  6D    m
  14   E   SO     46  2E  .     78  4E  N    110  6E    n
  15   F   SI     47  2F  /     79  4F  O    111  6F    o
  16  10  DLE     48  30  0     80  50  P    112  70    p
  17  11  DC1     49  31  1     81  51  Q    113  71    q
  18  12  DC2     50  32  2     82  52  R    114  72    r
  19  13  DC3     51  33  3     83  53  S    115  73    s

  20  14  DC4     52  34  4     84  54  T    116  74    t
  21  15  NAK     53  35  5     85  55  U    117  75    u
  22  16  SYN     54  36  6     86  56  V    118  76    v
  23  17  ETB     55  37  7     87  57  W    119  77    w
  24  18  CAN     56  38  8     88  58  X    120  78    x
  25  19   EM     57  39  9     89  59  Y    121  79    y
  26  1A  SUB     58  3A  :     90  5A  Z    122  7A    z
  27  1B  ESC     59  3B  ;     91  5B  [    123  7B    {
  28  1C   FS     60  3C  <     92  5C  \    124  7C    |
  29  1D   GS     61  3D  =     93  5D  ]    125  7D    }
  30  1E   RS     62  3E  >     94  5E  ^    126  7E    ~
  31  1F   US     63  3F  ?     95  5F  _    127  7F  DEL
*/


