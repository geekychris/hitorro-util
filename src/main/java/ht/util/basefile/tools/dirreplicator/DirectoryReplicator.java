package ht.util.basefile.tools.dirreplicator;

import ht.util.basefile.fs.BaseFile;
import ht.util.core.Env;
import ht.util.core.Log;
import ht.util.core.opers.HTPredicate;

import java.io.IOException;

/**
 *
 */
public class DirectoryReplicator {
    public static String CopyInfoFile = "copyinfofile.ser";
    public static String MachineInfo = "machine.txt";
    private BaseFile source;
    private BaseFile target;
    private HTPredicate<BaseFile> oper;
    private int retryLimit;

    public DirectoryReplicator(BaseFile source, BaseFile target, HTPredicate<BaseFile> oper, int retryLimit) {
        this.source = source;
        this.target = target;
        this.oper = oper;
        this.retryLimit = retryLimit;
    }

    /**
     * @return
     */
    public CopyStatus copy() throws IOException {
        long currTime = System.currentTimeMillis();
        String machine = Env.getMachineKey();
        BaseFile srcInfo = source.getChild(CopyInfoFile);
        BaseFile targetInfo = target.getChild(CopyInfoFile);
        BaseFile targetMachine = target.getChild(MachineInfo);
        long srcVer;
        long targetVer;
        if (srcInfo.exists()) {
            srcVer = srcInfo.readLong();
            // src exists, does target?
            if (targetInfo.exists()) {
                targetVer = targetInfo.readLong();
                if (srcVer == targetVer) {
                    // nothing todo
                    return CopyStatus.AlreadyCopied;
                }
                // target is different
                if (targetVer > srcVer) {
                    // this is a soft error state, seems that either the clock is wrong or some other process copied to this location.
                    Log.filesystem.info("directory copy from %s to %s failed because target has newer time stamp from copied from %s machine",
                            srcInfo, targetInfo, machine);
                    return CopyStatus.TargetNewerThanSource;
                }
            }
            int tries = 0;
            while (tries < retryLimit) {
                tries++;
                if (copyAux()) {
                    srcInfo.delete();
                    srcInfo.writeLong(currTime);
                    targetInfo.delete();
                    targetInfo.writeLong(currTime);
                    targetMachine.writeString(machine);
                    return CopyStatus.Copied;
                }
            }
            return CopyStatus.MaxRetriesReached;

        }
        return CopyStatus.SourceMissing;
    }

    private boolean copyAux() throws IOException {
        BaseFile targTmp = this.target.getPeerExtension("tmp");
        if (this.source.copyDirectory(this.target, oper)) {
            target.deleteContentOfDir(true);
            targTmp.renameTo(target);
            return true;
        }
        return false;
    }

}
