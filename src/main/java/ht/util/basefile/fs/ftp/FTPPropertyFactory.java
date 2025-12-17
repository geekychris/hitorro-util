package ht.util.basefile.fs.ftp;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.configfactories.BaseFilePropertyFactory;
import ht.util.basefile.fs.configfactories.FileSystemConfig;
import ht.util.json.keys.StringProperty;

/**
 *
 */
public class FTPPropertyFactory extends BaseFilePropertyFactory<FTPConfig, FileTranProtFile> {
    public static final String FTP = "ftp";


    public static StringProperty UserNameKey = new StringProperty("username", "Username to login as", null);
    public static StringProperty PasswordKey = new StringProperty("password", "Password to login as", null);
    public static StringProperty HostKey = new StringProperty("host", "Host to login to", null);

    public String[] getNames() {
        return new String[]{"ftpconfig"};
    }

    public FTPConfig getInstance(final JsonNode map, final String type, final String parentPathName) {
        FTPConfig ftp = new FTPConfig();
        ftp.userName = UserNameKey.apply(map);
        ftp.password = PasswordKey.apply(map);
        ftp.host = HostKey.apply(map);
        return ftp;
    }

    public String getProtocol() {
        return FTP;
    }

    /**
     * Assumes format of:
     * <p>
     * ftp://@@host/username/password@@/path
     *
     * @param parts
     * @return
     */

    public FileSystemConfig getConfigFromParts(String parts[]) {
        return new FTPConfig(parts[0], parts[1], parts[2]);
    }
}