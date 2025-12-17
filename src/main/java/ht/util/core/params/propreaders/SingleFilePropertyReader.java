package ht.util.core.params.propreaders;

import ht.util.core.params.HTProperties;
import ht.util.io.FileChangeSet;
import ht.util.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public abstract class SingleFilePropertyReader implements PropertiesReader {
    private boolean participatesInFileDiffCheck;
    private FileChangeSet changeContainer;
    private File file;

    public SingleFilePropertyReader(boolean participatesInFileDiffCheck) {
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

    public void getProperties(HTProperties props, Map<String, String> cmdLineArgs) {
        file = this.getFile(props);
        if (participatesInFileDiffCheck) {
            List<File> filesConsidered = new ArrayList();
            filesConsidered.add(file);
            changeContainer = new FileChangeSet(filesConsidered);
        }
        if (FileUtil.nullOrNotExist(file)) {
            return;
        }
        props.readFile(file, true);
    }

    public abstract File getFile(HTProperties propsSoFar);
}

