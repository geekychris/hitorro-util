package ht.util.io;

import java.io.File;
import java.util.List;

/**
 * Keeps track of a set of files
 */
public class FileChangeSet {
    private List<FileChangeContainer> cont;

    public FileChangeSet(List<File> files) {
        cont = FileChangeContainer.getChangesFromFileList(files);
    }

    public boolean hasAnythingChanged() {
        boolean changed = false;
        for (FileChangeContainer fcc : cont) {
            changed = fcc.hasChanged();

        }
        return changed;
    }

    public boolean hasAnythingChangedResetting() {
        boolean changed = false;
        for (FileChangeContainer fcc : cont) {
            changed = fcc.hasChangedReset();

        }
        return changed;
    }
}
