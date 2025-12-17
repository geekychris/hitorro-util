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
package ht.util.osprocessexec;

import ht.util.core.Constants;
import ht.util.core.Platform;
import ht.util.core.iterator.FilteringIterator;
import ht.util.core.iterator.Mapper;
import ht.util.core.iterator.MappingIterator;
import ht.util.core.iterator.mappers.StringToStringArrayMapper;
import ht.util.core.opers.HTPredicate;
import ht.util.core.opers.LogicalAndOperator;
import ht.util.core.opers.LogicalNotOperator;
import ht.util.core.opers.StringContainsOperator;

import java.io.IOException;
import java.util.Iterator;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class ProcessUtil {
    public static Iterator<String[]> find(HTPredicate oper) throws IOException, InterruptedException {
        ExecResultRowMapper mapper = Platform.getPlatform().getPSExec();
        if (mapper == null) {
            return null;
        }
        Iterator<String> iter = mapper.map(10);
        FilteringIterator<String> filt = new FilteringIterator<String>(iter, oper);
        Mapper<String, String[]> stringMapper = new StringToStringArrayMapper(" ");
        return new MappingIterator<String, String[]>(filt, stringMapper);
    }

    public static Iterator<String[]> find(String contains) throws IOException, InterruptedException {
        return find(new StringContainsOperator(contains, true));
    }

    public static int killProcesses(HTPredicate oper) throws IOException, InterruptedException {
        Iterator<String[]> mapIter = find(oper);
        if (mapIter == null) {
            return -1;
        }

        int i = 0;
        while (mapIter.hasNext()) {
            String sRow[] = mapIter.next();
            if (sRow != null && sRow.length >= 2) {
                String pid = sRow[1];
                if (Constants.isNumber(pid)) {
                    ExecResultRowMapper killer = Platform.getPlatform().getKill(Integer.parseInt(pid));
                    killer.map(10);
                    i++;
                }

            }
        }
        return i;
    }

    public static int killProcesses(String contains, String notValue) throws IOException, InterruptedException {
        HTPredicate c = new StringContainsOperator(contains, true);
        HTPredicate not = new LogicalNotOperator(new StringContainsOperator(notValue, true));

        return killProcesses(new LogicalAndOperator(c, not));
    }

    public static int killProcesses(String contains) throws IOException, InterruptedException {
        return killProcesses(new StringContainsOperator(contains, true));
    }
}
