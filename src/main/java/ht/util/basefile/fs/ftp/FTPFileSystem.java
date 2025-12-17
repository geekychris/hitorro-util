package ht.util.basefile.fs.ftp;

import ht.util.basefile.fs.BaseFile;
import ht.util.basefile.fs.BaseFileSystem;
import ht.util.core.Console;
import ht.util.core.Log;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

/**
 *
 */
public class FTPFileSystem extends BaseFileSystem<FileTranProtFile, FTPFileSystem> {
    private FTPClient client = new FTPClient();
    private FTPConfig config;

    public FTPFileSystem(FTPConfig ftpConfig) throws IOException {
        this.config = ftpConfig;
        connect(config.getHost(), config.getUserName(), config.getPassword());
    }

    public static void testBasic() throws IOException {
        FTPConfig config = new FTPConfig("ftp.riken.jp", "anonymous", "chris@hitorro.com");
        FTPFileSystem sys = new FTPFileSystem(config);
        BaseFile bf = sys.getFile("/Linux/centos/6.0/isos/x86_64");
        BaseFile files[] = bf.listFiles();
        for (BaseFile f : files) {
            Console.println("%s, %s, age: %s modified: %s, size:%s", f.getName(), f.getAbsolutePath(), f.ageOfFile() / 1000, f.getModifiedTime(), f.length());
        }
        Console.println("done");
    }

    public boolean connect(String host, String userName, String password) throws IOException {
        client.connect(host);
        int code = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(code)) {
            Log.ftpfs.error("Unable to connect, error code: %s, %s", code, client.getReplyString());
            return false;
        }
        client.login(userName, password);
        client.setFileType(FTP.BINARY_FILE_TYPE);

        return true;
    }

    @Override
    public boolean deleteFileSystem() {
        return false;
    }

    @Override
    public FileTranProtFile getFile(final BaseFile af) {
        return new FileTranProtFile(this, null, af.getRelativePath());
    }

    @Override
    public FileTranProtFile getFile(final String path) {
        return new FileTranProtFile(this, null, path);
    }

    @Override
    public FileTranProtFile getFileEnsuringDir(final String path) {
        return getFile(path);
    }

    FTPClient client() {
        return client;
    }
}
