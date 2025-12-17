package ht.util.basefile.fs.ftp;

import ht.util.basefile.fs.configfactories.FileSystemConfig;

import java.io.IOException;

/**
 *
 */
public class FTPConfig extends FileSystemConfig<FTPFileSystem> {
    String host;
    String userName;
    String password;

    public FTPConfig() {
    }

    public FTPConfig(String host, String username, String password) {
        this.host = host;
        this.userName = username;
        this.password = password;
    }

    public FTPFileSystem getFileSystem() throws IOException {
        return new FTPFileSystem(this);
    }

    public String getHost() {
        return host;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}