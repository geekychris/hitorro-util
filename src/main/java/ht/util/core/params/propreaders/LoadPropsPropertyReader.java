package ht.util.core.params.propreaders;

import ht.util.core.Console;
import ht.util.core.params.HTProperties;
import ht.util.core.string.StringUtil;
import ht.util.io.FileChangeSet;
import ht.util.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Load property files defined on the commandline.  A file on the command line could be a single file or a directory
 * containing further property files.  In this case the same scanning model applies as @link
 * JVSDirectoryReadingPropertiesReader
 */
public class LoadPropsPropertyReader implements PropertiesReader {
    public static final String LoadProps = "loadprops";
    private FileChangeSet changeContainer;

    private boolean participatesInFileDiffCheck;

    public LoadPropsPropertyReader(boolean participatesInFileDiffCheck) {
        this.participatesInFileDiffCheck = participatesInFileDiffCheck;
    }

    public void getProperties(HTProperties props, Map<String, String> cmdLineArgs) {
        String loadProp = cmdLineArgs.get(LoadProps);
        String loadProps[] = StringUtil.tokenizeFromSingleChar(loadProp, ",");
        List<File> files = new ArrayList();
        for (String prop : loadProps) {
            if (!StringUtil.nullOrEmptyOrBlankString(prop)) {
                File loadPropsFile = new File(prop);
                if (FileUtil.notNullAndExists(loadPropsFile)) {
                    if (loadPropsFile.isDirectory()) {
                        files.addAll(PropReaderUtil.getProps(cmdLineArgs, props, loadPropsFile));
                    } else {
                        files.add(loadPropsFile);
                        props.readFile(loadPropsFile, true);
                    }
                } else {
                    Console.eprintln("Property file %s does not exist, cannot initialize", prop);
                    System.exit(-1);
                }
            }
        }
        changeContainer = new FileChangeSet(files);

    }

    public boolean havePropertiesChanged() {
        if (!participatesInFileDiffCheck) {
            return false;
        }
        if (changeContainer == null) {
            return false;
        }
        return changeContainer.hasAnythingChangedResetting();
    }
}
