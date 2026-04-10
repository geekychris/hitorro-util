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

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.map.LockBox;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.IOUtil;

import java.io.*;
import java.util.HashSet;
import java.util.List;


public class UserHistoryBuffer {
    private static LockBox lockBox = new LockBox();
    private File directory;
    private String username;
    private File bufferFile;
    private Object lock;
    private HashSet<String> set = MapUtil.makeHashSet(new String[] { "assume", "quit", "history.list" });

    public UserHistoryBuffer(File directory, String username, int limit, CommandSession session)
            throws FileNotFoundException, UnsupportedEncodingException {
        session.m_commands.clear();
        FileUtil.ensureDirectoryExists(directory);
        bufferFile = new File(directory, Fmt.S("history-%s-%s.txt", Env.getNodeId(), username));
        if (bufferFile.exists() && bufferFile.isFile()) {
            List<String> list = IOUtil.getTailStringListFromFile(limit,
                    FileUtil.getBufferedFileInputStream(bufferFile));

            for (String row : list) {
                try {
                    session.m_commands.add(new CommandMap(row, false));
                } catch (Exception e) {
                    // Skip malformed history entries
                    com.hitorro.util.core.Log.commands.debug("Skipping malformed history entry: %s", row);
                }
            }
        }
        lock = lockBox.getLock(bufferFile);
    }

    public void removeFile() {
        bufferFile.delete();
    }

    public String getUsername() {
        return username;
    }

    public boolean addRow(String command, String rawArgs) throws IOException {
        if (set.contains(command.toLowerCase())) {
            return false;
        }
        synchronized (lock) {
            // use a file level VM global lock that means that only one writer can write a
            // buffer of the same name at a time.
            // this does mean that concurrent telnet commands will apply their buffer
            // together.
            OutputStream os = FileUtil.getBufferedFileOutputStream(bufferFile, true);
            if (StringUtil.nullOrEmptyString(rawArgs)) {
                os.write(command.getBytes());
            } else {
                os.write(StringUtil.strcat(command, " ", rawArgs).getBytes());
            }

            os.write(Constants.NewLineChar);
            os.flush();
            os.close();
        }
        return true;
    }
}
