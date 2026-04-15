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
package com.hitorro.util.osprocessexec;

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Platform;
import com.hitorro.util.core.iterator.FilteringIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.MappingIterator;
import com.hitorro.util.core.iterator.mappers.StringToStringArrayMapper;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalAndOperator;
import com.hitorro.util.core.opers.LogicalNotOperator;
import com.hitorro.util.core.opers.StringContainsOperator;

import java.io.IOException;
import java.util.Iterator;


public class ProcessUtil {
    public static Iterator<String[]> find(HTPredicate oper) throws IOException, InterruptedException {
        String[] psCommand = Platform.getPlatform().getPSCommand();
        ExecResultRowMapper mapper = new ExecResultRowMapper(psCommand[0], java.util.Arrays.copyOfRange(psCommand, 1, psCommand.length));
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

    public static final int killProcesses(HTPredicate oper) throws IOException, InterruptedException {
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
                    String[] killCommand = Platform.getPlatform().getKillCommand(Integer.parseInt(pid));
                    ExecResultRowMapper killer = new ExecResultRowMapper(killCommand[0], java.util.Arrays.copyOfRange(killCommand, 1, killCommand.length));
                    killer.map(10);
                    i++;
                }

            }
        }
        return i;
    }

    public static final int killProcesses(String contains, String notValue) throws IOException, InterruptedException {
        HTPredicate c = new StringContainsOperator(contains, true);
        HTPredicate not = new LogicalNotOperator(new StringContainsOperator(notValue, true));

        return killProcesses(new LogicalAndOperator(c, not));
    }

    public static final int killProcesses(String contains) throws IOException, InterruptedException {
        return killProcesses(new StringContainsOperator(contains, true));
    }
}
