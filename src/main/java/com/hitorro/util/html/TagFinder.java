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
package com.hitorro.util.html;

import com.hitorro.util.core.string.StringUtil;

import java.text.ParseException;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p/>
 * Simple iterator that scans a buffer to a max character position looking for things that look like:
 * <p/>
 * <html     ...........> For each next, we scan forward looking for the next.  No object instantiation is done in this
 * code.
 * <p/>
 * Currently this code does not take account of escapes....so the tags could be inside text boxes and it wouldnt care.
 */
public class TagFinder {
    private String m_buffer;
    private int m_curr;
    private int m_maxPos;
    private int m_open;
    private int m_close;
    // given <!DOCTYPE ....> the entity name length would be 8 (we scan for the space)
    private int m_entityNameLength;
    private int attrNameStart;
    private int attrNameLength;
    private int attributeValueStart;
    private int attributeValueEnd;
    private int attrStart;

    public void set(String buffer, int maxPos) {
        m_buffer = buffer;
        m_curr = 0;
        // dont want to roll off the end of the world.
        m_maxPos = Math.min(maxPos, buffer.length() - 1);
        m_open = 0;
        m_close = -1;
    }

    /**
     * Advance to the next token. Stops at the max position and stops at the end of the buffer.
     *
     * @return
     */
    public boolean next() {
        m_entityNameLength = 0;
        m_open = m_buffer.indexOf("<", m_close + 1);
        if (m_open == -1 || m_open > m_maxPos) {
            return false;
        }

        for (int i = m_open; i < m_maxPos; i++) {
            char c = m_buffer.charAt(i);
            if (c == '>') {
                m_close = i;
                if (m_entityNameLength == 0) {
                    m_entityNameLength = m_close - m_open - 1;
                }
                return true;
            }
            if (c == ' ' && m_entityNameLength == 0) {
                m_entityNameLength = i - m_open - 1;
            }
        }
        return false;
    }

    public void resetAttributeScan() {
        attrStart = m_open + m_entityNameLength + 1;
    }

    /**
     * Scan the current tag for an attribute by the given name, if so we prepare the markers for the boundaries of its
     * value.
     *
     * @return true if the attribute is found.  At that point, you can pull out the attribute value using
     * getAttributeValue
     */
    public boolean findAttribute(String attr) {
        int start = m_open + m_entityNameLength + 1;
        resetAttributeScan();

        while (true) {
            if (getNextAttribute()) {
                if (StringUtil.subStringEqualsIgnoreCase(m_buffer, attrNameStart, attrNameLength, attr)) {
                    return true;
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Get the attribute name, typically not called for performance reasons since it has to create a String object.
     * Typically use case assumes you know what attribute your looking for, unless your just enumerating all the
     * attributes of the entity.
     *
     * @return
     */
    public String getAttributeName() {
        return m_buffer.substring(attrNameStart, attrNameStart + attrNameLength);
    }

    /**
     * Gets the attributes value or empty string if the attribute has no value
     *
     * @return
     */
    public String getAttributeValue() {
        if (attributeValueStart == -1) {
            return "";
        }
        return m_buffer.substring(attributeValueStart, attributeValueEnd);
    }

    /**
     * Stolen from the KeyValueUtil
     *
     * @return
     * @throws ParseException
     */
    public boolean getNextAttribute() {
        this.attributeValueStart = 0;
        this.attributeValueEnd = 0;
        this.attrNameStart = 0;
        attrNameLength = 0;
        boolean inQuotes = false;
        int state = 0;
        // 0=whitespace
        // 1=key
        // 2= whitespacebefore equals
        // 3=whitspacefollowing equals
        // 4 = value
        char c = ' ';
        String key = null;
        for (int i = attrStart + 1; i < m_close; i++) {
            c = m_buffer.charAt(i);
            switch (state) {
                case 0:
                    // processing white space potentially
                    if (c != ' ') {
                        // tis not white space enter next state
                        state = 1;
                        // just put this char as its part of the key
                        if (attrNameStart == 0) {
                            attrNameStart = i;
                        }

                    }
                    break;
                case 1:
                    // in key
                    if (c == ' ') {
                        // have hit white space end of key
                        // now look for equals
                        state = 2;
                        this.attrNameLength = i - attrNameStart;
                    } else if (c == '=') {
                        // now look for white space following key
                        this.attrNameLength = i - attrNameStart;
                        state = 3;
                    } else {

                    }
                    break;
                case 2:
                    // in
                    if (c == ' ') {
                        // do nothing
                    } else if (c == '=') {
                        // have hit equals following key
                        this.attrNameLength = i - attrNameStart;
                        // now look for white space following key
                        state = 3;
                    } else {
                        // case where there is no value
                        state = 1;
                        this.attributeValueStart = -1;
                        this.attributeValueEnd = -1;
                        if (attrNameLength == 0) {
                            this.attrNameLength = i - attrNameStart;
                        }
                        attrStart = i - 1;
                        return true;
                    }

                    break;
                case 3:
                    // start of value
                    if (c == ' ') {
                        // do nothing
                    } else {
                        // found char
                        state = 4;
                        if (c == '\'' || c == '\"') {
                            inQuotes = true;
                        } else {
                            this.attributeValueStart = i;
                        }

                    }
                    break;
                case 4:
                    // processing value
                    if (c == '\'' || c == '\"') {
                        inQuotes = !inQuotes;
                    } else {
                        if (!inQuotes) {
                            if (c == ' ') {
                                this.attributeValueEnd = i - 1;
                                attrStart = i;
                                return true;
                            } else {
                                if (attributeValueStart == 0) {
                                    this.attributeValueStart = i;
                                }
                            }

                        } else {
                            if (attributeValueStart == 0) {
                                this.attributeValueStart = i;
                            }
                        }
                    }
            }
        }
        attrStart = m_buffer.length();
        // deal with end of array case
        if (state == 1) {

            state = 2;
            this.attributeValueStart = -1;
            this.attributeValueEnd = -1;

            return true;
        }
        if (state == 4) {
            if (inQuotes == false) {
                attributeValueEnd = m_close - 1;
                return true;
            } else {
                // XXX ERROR?
                return false
                        ;
            }
        }

        return false;
    }

    /**
     * Stolen from the KeyValueUtil
     *
     * @return
     * @throws ParseException
     */
    public Map<String, String> getAttributes() {

        TreeMap<String, String> props = new TreeMap<String, String>();
        StringBuilder buff = new StringBuilder();
        boolean inQuotes = false;
        int state = 0;
        // 0=whitespace
        // 1=key
        // 2= whitespacebefore equals
        // 3=whitspacefollowing equals
        // 4 = value
        char c = ' ';
        String key = null;
        for (int i = m_open + this.m_entityNameLength + 1; i < m_close; i++) {
            c = m_buffer.charAt(i);
            switch (state) {
                case 0:
                    // processing white space potentially
                    if (c != ' ') {
                        // tis not white space enter next state
                        state = 1;
                        // for now I dont care about special chars.
                        buff.append(c);
                    }
                    break;
                case 1:
                    // in key
                    if (c == ' ') {
                        // have hit white space end of key
                        // now look for equals
                        state = 2;
                    } else if (c == '=') {
                        // have hit equals followig key
                        key = buff.toString();
                        buff.setLength(0);
                        // now look for white space following key
                        state = 3;
                    } else {
                        // just put this char as its part of the key
                        buff.append(c);
                    }
                    break;
                case 2:
                    // in
                    if (c == ' ') {
                        // do nothing
                    } else if (c == '=') {
                        // have hit equals following key
                        key = buff.toString();
                        buff.setLength(0);
                        // now look for white space following key
                        state = 3;
                    } else {
                        // something else, its malformed
                        if (true) {
                            state = 1;
                            props.put(buff.toString(), "true");
                            buff.setLength(0);
                            buff.append(c);
                        } else {
                            return null;
                        }
                    }
                    break;
                case 3:
                    if (c == ' ') {
                        // do nothing
                    } else {
                        // found char
                        state = 4;
                        if (c == '\'' || c == '\"') {
                            inQuotes = true;
                        } else {
                            buff.append(c);
                        }
                    }
                    break;
                case 4:
                    // processing value
                    if (c == '\'' || c == '\"') {
                        inQuotes = !inQuotes;
                    } else {
                        if (inQuotes) {
                            buff.append(c);
                        } else {
                            if (c == ' ') {
                                // end, hit space not in quotes
                                state = 0;
                                props.put(key, buff.toString());
                                buff.setLength(0);
                            } else {
                                buff.append(c);
                            }
                        }
                    }
            }

        }
        // deal with end of array case
        if (state == 1) {
            // could be we have a flag at the end of the line
            if (true) {
                state = 2;
                props.put(buff.toString(), "true");
                buff.setLength(0);
                buff.append(c);
            } else {
                return null;
            }
        }
        if (state == 4) {
            if (inQuotes == false) {
                props.put(key, buff.toString());
                buff.setLength(0);
            } else {
                return null;
            }
        }

        return props;
    }

    public int getTagStart() {
        return m_open;
    }

    public int getTagStop() {
        return m_close;
    }

    public boolean isTagEqualIgnoreCase(String tag) {
        int start = m_open + 1;
        return StringUtil.subStringEqualsIgnoreCase(m_buffer, start, m_entityNameLength, tag);
    }

    /**
     * Get the elements tag name, not really expected to be called in bulk as it creates string objects.
     *
     * @return
     */
    public String getTag() {
        int start = m_open + 1;
        return m_buffer.substring(start, start + m_entityNameLength);
    }
}
