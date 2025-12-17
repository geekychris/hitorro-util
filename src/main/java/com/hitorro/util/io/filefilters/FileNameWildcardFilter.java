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
package com.hitorro.util.io.filefilters;

import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.StringUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

/**
 * HTPredicate that does general wildcard matching.  Can search for *oobar.txt  foo*bar.txt foobar.txt*  foo*b*ar.txt etc.
 *
 * @author chris
 */
public class FileNameWildcardFilter implements FilenameFilter {
    private String _match;
    private boolean _ignoreCase;
    private String[] _tokens;
    private String[] _wildcardMap = {"*", "\\*",
            "%", "\\%",
            "\\*", "\\*",
            "\\%", "\\%"};


    /**
     * @param match      string to test
     * @param wildcard   wildcard character embedded in string to test
     * @param ignoreCase true if we wish to ignore case
     */
    public FileNameWildcardFilter(String match, String wildcard, boolean ignoreCase) {

        if (ignoreCase) {
            _match = match.toLowerCase();
            wildcard = wildcard.toLowerCase();
        } else {
            _match = match;
        }
        _ignoreCase = ignoreCase;

        wildcard = getWildcard(wildcard);

        _tokens = _match.split(wildcard);

    }


    public boolean accept(File dir, String name) {
        boolean match = true;
        int start = 0;
        int current;

        if (_ignoreCase) {
            name = name.toLowerCase();
        }

        /**
         *  iterate through the tokens.
         *  start   = index of the end of previous token.
         *  current = index of the beginning of the current token.
         */
        for (int i = 0; i < _tokens.length; i++) {
            if (!StringUtil.nullOrEmptyString(_tokens[i])) {
                if (i < _tokens.length - 1)                //  first token & middle tokens
                {
                    current = name.indexOf(_tokens[i], start);
                    current = name.indexOf(_tokens[i], start);

                    if (i == 0 && current != 0)          //   first token
                    {
                        match = false;
                    } else if (current < start)           //   middle tokens
                    {
                        match = false;
                    } else {
                        start = current + _tokens[i].length();
                    }
                } else if (i == _tokens.length - 1)        //  last token
                {
                    current = name.lastIndexOf(_tokens[i]);

                    if (current < start) {
                        match = false;
                    }
                }

                if (!match) {
                    break;
                }
            }
        }
        return match;
    }


    private String getWildcard(String wildcard) {
        Map wildcardMap = MapUtil.createMapFromArray(_wildcardMap, 2, 0, 1);
        String wildcardValue = (String) wildcardMap.get(wildcard);

        if (StringUtil.nullOrEmptyString(wildcardValue)) {
            wildcardValue = wildcard;
        }

        if (StringUtil.nullOrEmptyString(wildcardValue)) {
            wildcardValue = "\\*";
        }

        return wildcardValue;
    }
}
