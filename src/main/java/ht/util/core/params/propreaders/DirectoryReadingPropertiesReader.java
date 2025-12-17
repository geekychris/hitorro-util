package ht.util.core.params.propreaders;

import ht.util.core.Env;
import ht.util.core.params.HTProperties;
import ht.util.io.FileChangeSet;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Load properties from the provided directory (if it exists). If it exists we also will look to see if there is a sub
 * directory for the server type and load everything in there too. If we are in debug mode a final "debug" directory
 * will be looked for, so in most part we have:
 * <p/>
 * directory/*.properties /<servertype>/*.properties /debug/*.properties
 */
public class DirectoryReadingPropertiesReader implements PropertiesReader {
    protected File otherDir = null;
    private DirectoryType type;
    private boolean participatesInFileDiffCheck;
    private FileChangeSet changeContainer;

    public DirectoryReadingPropertiesReader(DirectoryType type, boolean participatesInFileDiffCheck) {
        this.type = type;
        this.participatesInFileDiffCheck = participatesInFileDiffCheck;

    }

    public DirectoryReadingPropertiesReader(File other, boolean participatesInFileDiffCheck) {
        this.type = DirectoryType.Other;
        otherDir = other;
        this.participatesInFileDiffCheck = participatesInFileDiffCheck;

    }

    /**
     * Detect if files have changed
     *
     * @return
     */
    public boolean havePropertiesChanged() {
        if (!participatesInFileDiffCheck) {
            // never considers any changes
            return false;
        }
        if (changeContainer == null) {
            return false;
        }
        return changeContainer.hasAnythingChangedResetting();
    }

    public File getDirectory() {

        switch (type) {
            case Home:
                return new File(Env.getHome(), "config");
            case Bin:
                return new File(Env.getBin(), "config");
            case Other:
                return otherDir;
        }
        return null;
    }

    public void getProperties(HTProperties props, Map<String, String> cmdLineArgs) {
        File directory = getDirectory();

        List<File> filesConsidered = PropReaderUtil.getProps(cmdLineArgs, props, directory);
        if (participatesInFileDiffCheck) {
            changeContainer = new FileChangeSet(filesConsidered);
        }
    }


}
