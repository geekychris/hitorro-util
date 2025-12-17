package ht.util.commandandcontrol;

import ht.util.core.Constants;
import ht.util.core.Env;
import ht.util.core.map.LockBox;
import ht.util.core.map.MapUtil;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import ht.util.io.FileUtil;
import ht.util.io.IOUtil;

import java.io.*;
import java.util.HashSet;
import java.util.List;

/**
 *
 */
public class UserHistoryBuffer {
    private static LockBox lockBox = new LockBox();
    private File directory;
    private String username;
    private File bufferFile;
    private Object lock;
    private HashSet<String> set = MapUtil.makeHashSet(new String[]{"assume", "quit", "history.list"});

    public UserHistoryBuffer(File directory, String username, int limit, CommandSession session) throws FileNotFoundException, UnsupportedEncodingException {
        session.m_commands.clear();
        FileUtil.ensureDirectoryExists(directory);
        bufferFile = new File(directory, Fmt.S("history-%s-%s.txt", Env.getNodeId(), username));
        if (bufferFile.exists() && bufferFile.isFile()) {
            List<String> list = IOUtil.getTailStringListFromFile(limit, FileUtil.getBufferedFileInputStream(bufferFile));

            try {
                for (String row : list) {
                    session.m_commands.add(new CommandMap(row));
                }
            }
            catch (Exception e) {
                System.out.println();
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
            // use a file level VM global lock that means that only one writer can write a buffer of the same name at a time.
            // this does mean that concurrent telnet commands will apply their buffer together.
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


