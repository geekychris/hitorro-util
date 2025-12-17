/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.basefile.tools.dirreplicator;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.opers.HTPredicate;

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
