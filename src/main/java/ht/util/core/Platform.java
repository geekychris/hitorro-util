package ht.util.core;

import ht.util.core.classes.ClassUtil;
import ht.util.core.string.Fmt;
import ht.util.osprocessexec.ExecResultRowMapper;
import ht.util.osprocessexec.SimpleExec;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;

/**
 * User: chris
 */

public enum Platform {
    MacOSX("jnilib", "/usr/bin/", "", "mac", true) {
        public File getJavaRoot() {
            return super.getJavaRoot().getParentFile();
        }

        public ExecResultRowMapper getPSExec() {
            return new ExecResultRowMapper("/bin/ps", new String[]{"-ajxwww"});
        }

        public void getJavaClassPath(StringBuilder b, File jreRoot, FilenameFilter filter) throws IOException {
            ClassUtil.getExpandedClassPath(b, new File(jreRoot, "Home/lib"), filter);
            b.append(Env.getPathSeperator());
            ClassUtil.getExpandedClassPath(b, new File(jreRoot, "Classes"), filter);
        }

        public File getJava(File jreRoot) {
            return new File(jreRoot, "Home/bin/java");
        }

    },
    Windows("dll", "", "exe", "windows", false) {
        public ExecResultRowMapper getKill(String pid) {
            return null;
        }

        public ExecResultRowMapper getPSExec() {
            return null;
        }

        private String lnCommand;

        protected String getSoftLinkCommand(File file, File linkFile) {
            if (lnCommand == null) {
                lnCommand = new File(Env.getPlatformScriptDirectory(), "ln.exe").getAbsolutePath();
            }
            return Fmt.S("fsutil hardlink create %s %s", linkFile.getAbsolutePath(), file.getAbsolutePath());
        }

        public boolean softLink(File file, File linkFile) throws IOException {
            StringBuilder buff = new StringBuilder();
            String command = null;

            //TODO to be implemented
            if (lnCommand == null) {
                lnCommand = new File(Env.getPlatformScriptDirectory(), "ln.exe").getAbsolutePath();
            }
            command = Fmt.S("fsutil hardlink create %s %s", linkFile.getAbsolutePath(), file.getAbsolutePath());

            try {
                SimpleExec.exec(command, buff, true);
            } catch (InterruptedException e) {
                throw new IOException(e.getMessage());
            }
            return true;
        }

        protected String getChmodCommand(String perms, String pattern) {
            // to be implemented.
            return null;
        }
    },
    Linux("so", "/usr/bin/", "", "linux", true) {

    },
    Solaris("so", "/usr/bin/", "", "solaris", true),
    Other("so", "", "", "", false);

    public static Platform s_platform;
    private String soExtension;
    private String binPath;
    private String extension;
    private String platDir;
    private String shortName;
    private boolean isUnixVariant;

    Platform(String soExtension, String binPath, String ext, String shortName, boolean isUnixVar) {
        this.soExtension = soExtension;
        this.binPath = binPath;
        this.extension = ext;
        this.platDir = Fmt.S("${HT_BIN}/scripts/%s/", shortName);
        this.shortName = shortName;
        this.isUnixVariant = isUnixVar;
    }

    public static final Platform getPlatform() {
        if (s_platform == null) {
            String plat = Env.getOsName().toLowerCase();
            if (plat.contains("mac")) {
                s_platform = Platform.MacOSX;
            } else if (plat.contains("linux")) {
                s_platform = Platform.Linux;
            } else if (plat.contains("win")) {
                s_platform = Platform.Windows;
            } else if (plat.contains("sun")) {
                s_platform = Platform.Solaris;
            } else {
                s_platform = Platform.Other;
            }
        }
        return s_platform;
    }

    public ExecResultRowMapper getPSExec() {
        return new ExecResultRowMapper("/bin/ps", new String[]{"-efww"});
    }

    public ExecResultRowMapper getKill(int pid) {
        return new ExecResultRowMapper("/bin/kill", new String[]{"-9", Integer.toString(pid)});
    }

    public void getJavaClassPath(StringBuilder b, File jreRoot, FilenameFilter filter) throws IOException {
        ClassUtil.getExpandedClassPath(b, new File(jreRoot, "lib"), filter);
    }

    public File getJava(File jreRoot) throws IOException {
        return new File(jreRoot, "bin/java");
    }

    protected String getSoftLinkCommand(File file, File linkFile) {
        return Fmt.S("/bin/ln -s %s %s", file.getAbsolutePath(), linkFile.getAbsolutePath());
    }

    protected String getChmodCommand(String perms, String pattern) {
        return Fmt.S("/bin/chmod %s %s", perms, pattern);
    }

    public boolean softLink(File file, File linkFile) throws IOException {
        StringBuilder buff = new StringBuilder();
        String command = null;

        command = getSoftLinkCommand(file, linkFile);

        try {
            SimpleExec.exec(command, buff, true);
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }

        return true;
    }

    public boolean chmod(String perms, String pattern) throws IOException {
        StringBuilder buff = new StringBuilder();
        String command = getChmodCommand(perms, pattern);

        try {
            SimpleExec.exec(command, buff, true, perms, pattern);
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }


        return true;
    }

    public boolean isUnixVariant() {
        return isUnixVariant;
    }

    public String getPlatformScriptPath() {
        return platDir;
    }

    public String getExeFileExtension() {
        return extension;
    }

    public String getUserBinDir() {
        return binPath;
    }

    public String getShortName() {
        return shortName;
    }

    public void addParamsToEnvironment(Map<String, String> map) {
        map.put(Env.OS_BIN_DIR, binPath);
        map.put(Env.EXEC_EXTENSION, extension);
        map.put(Env.SCRIPT_PLAT_DIR, platDir);
    }

    public String getSharedObjectExtension() {
        return soExtension;
    }

    public File getJavaRoot() {
        String home = System.getProperty("java.home");
        return new File(home);
    }
}
