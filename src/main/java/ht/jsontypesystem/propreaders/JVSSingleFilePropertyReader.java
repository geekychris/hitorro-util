package ht.jsontypesystem.propreaders;

import ht.jsontypesystem.JVS;
import ht.util.io.FileChangeSet;
import ht.util.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public abstract class JVSSingleFilePropertyReader implements JVSPropertiesReader {
    private boolean participatesInFileDiffCheck;
    private FileChangeSet changeContainer;
    private File file;

    public JVSSingleFilePropertyReader(boolean participatesInFileDiffCheck) {
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

    public void getProperties(JVS props, JVS cmdLineArgs) throws Exception {
        file = this.getFile(props);
        if (participatesInFileDiffCheck) {
            List<File> filesConsidered = new ArrayList();
            filesConsidered.add(file);
            changeContainer = new FileChangeSet(filesConsidered);
        }
        if (FileUtil.nullOrNotExist(file)) {
            return;
        }
        JVS tmp = JVS.read(file);
        props.merge(tmp);
    }

    public abstract File getFile(JVS propsSoFar);
}

