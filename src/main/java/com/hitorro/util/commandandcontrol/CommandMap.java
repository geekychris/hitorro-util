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
package com.hitorro.util.commandandcontrol;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.CommandArgs;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.text.ParseException;

/**
 *
 */
public class CommandMap {
    private String m_command;
    private JVS m_map;
    private String m_rawArgs;

	public CommandMap(String row) {
		this(row, true);
	}
    public CommandMap(String row, boolean reportException) {
        int index = row.indexOf(" ");
        m_map = new JVS();
        if (index == -1) {
            m_command = row;
            m_rawArgs = "";
        } else {
            m_command = row.substring(0, index);
            m_rawArgs = row.substring(index + 1);
            try {

				CommandArgs.parseArgs(m_rawArgs, false, true, m_map);
            } catch (ParseException e) {
                if (reportException) {
					Log.commands.error("Unable to parse command %s %e", e, e);
				}
            } catch (PropaccessError e) {
				if (reportException) {
					Log.commands.error("Unable to parse command %s %e", e, e);
				}
            }
        }
    }

    public CommandMap(String cmd, JVS args, String rawArgs) {
        m_command = cmd;
        m_map = args;
        if (StringUtil.nullOrEmptyString(rawArgs)) {
            m_rawArgs = "";
        } else {
            m_rawArgs = rawArgs;
        }
    }

    public String getCommand() {
        return m_command;
    }

    public String getRawArgs() {
        return m_rawArgs;
    }

    public JVS getArgs() {
        return m_map;
    }

    public String toString() {
        return Fmt.S("%s %s", m_command, m_rawArgs);
    }
}
