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
package com.hitorro.util.urlparser;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.StringUtil;

/**
 * URLCursor, except it keeps track of positions of the url and the first argument.
 */
public class UrlMemoryCursor extends UrlCursor {
    private static final int MaxIndex = 300;
    // positions of the start to a segment of a path within the url or the start of an argument
    private int[] startPositions = new int[MaxIndex];
    private int[] length = new int[MaxIndex];
    private Part[] type = new Part[MaxIndex];
    private long[] hash = new long[MaxIndex];
    private int hostPartIndex;
    private int firstPartIndex;
    private int lastPartIndex;
    private int firstArgIndex;
    private int firstKeyPartLength;

    public boolean nextToken() {
        if (super.nextToken()) {
            if (index >= MaxIndex) {
                Log.util.error("Encountered a Url with more than %s parts: %s", MaxIndex, m_url);
                return false;
            }
            return true;
        }
        return false;
    }

    public String getProtocol() {
        int sp = startPositions[hostPartIndex];
        if (sp < 2) {
            return null;
        }
        int index = m_url.indexOf(':');
        if (index == -1 || index > sp) {
            return null;
        }
        return m_url.substring(0, index);
    }

    public boolean scan(String url) {
        if (!setUrl(url)) {
            return false;
        }
        Part priorPart = Part.Host;
        int ind = 0;
        setTokenParams(ind);

        firstPartIndex = -1;
        lastPartIndex = -1;
        firstArgIndex = -1;
        while (super.nextToken()) {
            // reset hash
            hash[ind] = 0;
            setTokenParams(ind);
            if (m_type == Part.Path) {
                if (priorPart != m_type) {
                    firstPartIndex = ind;
                }
                lastPartIndex = ind;

            } else if (m_type == Part.Argument && priorPart != m_type) {
                // now in args
                firstArgIndex = ind;
                // we can break now since we got the first
                firstKeyPartLength = keyPartLength;
                break;
            } else if (m_type == Part.Host) {
                hostPartIndex = ind;
            }
            ind++;
            if (ind >= MaxIndex) {
                Log.util.error("Encountered a Url with more than %s parts: %s", MaxIndex, m_url);
                return false;
            }
            priorPart = m_type;
        }
        return true;
    }

    public boolean resetToFirstPart() {
        return setTokenParamsToIndex(firstPartIndex);
    }

    public boolean resetToLastPart() {
        return setTokenParamsToIndex(lastPartIndex);
    }

    public boolean resetToLastPart(int minusNPositions) {
        return setTokenParamsToIndex(lastPartIndex - minusNPositions);
    }

    public boolean resetToFirstArg() {
        boolean f = setTokenParamsToIndex(firstArgIndex);
        keyPartLength = firstKeyPartLength;
        return f;
    }

    public boolean resetToHost() {
        return setTokenParamsToIndex(hostPartIndex);
    }

    public boolean previousToken() {
        if (index <= 0) {
            return false;
        }
        return setTokenParamsToIndex(--index);
    }

    /**
     * Get the hash of the part of the string.  If the part is an argument, its ONLY the key part
     *
     * @return
     */
    public long getHash() {
        long h = hash[index];
        if (h == 0) {
            if (m_type == Part.Argument) {
                // never cache the hash of an argument (we are too lazy since we would have to whipe the whole arg listFiles
                // in the scan phase we didnt go through it!
                return StringUtil.hashStringCaseFree(this.m_url, tokenStart, this.keyPartLength);
            } else {
                h = StringUtil.hashStringCaseFree(this.m_url, tokenStart, tokenLength);
                hash[index] = h;
            }
        }
        return h;
    }

    public boolean startsWith(String s) {
        return StringUtil.startsWithIgnoreCase(m_url, tokenStart, s);
    }

    /**
     * get the value of a key value pair hash in case insensative form.  Note that this also works for file extensions
     * when parsing the final part of a uri's path
     *
     * @return
     */
    public long getValueHash() {
        int start = tokenStart + keyPartLength + 1;
        return StringUtil.hashStringCaseFree(this.m_url, start, tokenStart + tokenLength - start);
    }

    public long getKeyHash() {
        return StringUtil.hashStringCaseFree(this.m_url, tokenStart, keyPartLength);
    }


    private boolean setTokenParamsToIndex(int ind) {
        keyPartLength = 0;
        index = ind;
        if (index < 0) {
            return false;
        }
        tokenStart = startPositions[index];
        this.tokenLength = length[index];
        m_type = type[index];
        return true;
    }

    private void setTokenParams(final int index) {
        startPositions[index] = tokenStart;
        length[index] = this.tokenLength;
        type[index] = m_type;
    }
}

