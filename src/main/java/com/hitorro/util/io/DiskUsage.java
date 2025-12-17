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
package com.hitorro.util.io;

import com.hitorro.util.core.Constants;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.MappingIterator;
import com.hitorro.util.core.iterator.mappers.StringToStringArrayMapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.osprocessexec.ExecResultRowMapper;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Aug 15, 2005 Time: 8:50:28 AM
 */
public class DiskUsage {
    private static long lastRefresh = -1;
    private static List<DFRow> rows = null;

    public static final List<DFRow> getDFCached(long maxAge) {
        boolean refetch = false;
        long time = System.currentTimeMillis();
        if (rows == null) {
            refetch = true;
            lastRefresh = time;
        } else {

            if (lastRefresh + maxAge < time) {
                lastRefresh = time;
                refetch = true;
            }
        }
        if (refetch) {
            rows = getDF();
        }
        return rows;
    }


    public static final List<DFRow> getDF() {
        ExecResultRowMapper mapper = new ExecResultRowMapper("/bin/df", new String[]{});

        Iterator<String> iter = null;
        List<DFRow> rows = new ArrayList<DFRow>();
        try {
            iter = mapper.map(10000);
            int i = 0;
            Mapper<String, String[]> stringMapper = new StringToStringArrayMapper("   ");
            MappingIterator<String, String[]> mapIter = new MappingIterator<String, String[]>(iter, stringMapper);
            // Filesystem              512-blocks      Used    Avail Capacity  Mounted on
            int count = 0;
            String blocks;
            long blockSize = 512;

            while (mapIter.hasNext()) {
                String row[] = mapIter.next();
                if (count++ == 0) {
                    blocks = row[1];
                    String parts[] = StringUtil.tokenizeFromSingleChar(blocks, "-");
                    if (parts.length == 2) {
                        blocks = parts[0];
                    }
                    try {
                        blockSize = Constants.getBytesFromString(blocks);
                    } catch (ParseException e) {
                        blockSize = 512;
                    }
                    continue;
                }

                DFRow r = new DFRow();
                if (r.set(row, blockSize)) {
                    rows.add(r);
                }
            }
        } catch (IOException e) {
            Log.util.error("Exception %s %e", e, e);
        } catch (InterruptedException e) {
            Log.util.error("Exception %s %e", e, e);
        }

        return rows;
    }

    public static class DFRow {
        private String fileSystem;
        private long blocks;
        private long usedBlocks;
        private long availBlocks;
        private int percentCapacity;
        private String mountedOn;
        private long blockSize;

        public String toString() {
            return Fmt.S("%s on %s %s%% full, free %s", fileSystem, mountedOn, percentCapacity, StringUtil.getBytesNeatForm(blockSize * availBlocks));
        }

        /**
         * We really want 6 rows, the first few could be munged up so we will compress down those till we have 6.
         *
         * @param row
         */
        public boolean set(String row[], long blockSize) {
            setBlockSize(blockSize);
            if (row.length < 6) {
                return false;
            }
            int fieldZeroSize = row.length - 5;
            int indexOfPercent = findIndexPercent(row);
            if (indexOfPercent == -1) {
                // couldnt find a percent, giving up.
                return false;
            }
            if (fieldZeroSize == 1) {
                fileSystem = row[0];
            } else {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < (indexOfPercent - 3); i++) {
                    sb.append(row[i]);
                }
                fileSystem = sb.toString();
            }
            blocks = Long.parseLong(row[indexOfPercent - 3]);
            usedBlocks = Long.parseLong(row[indexOfPercent - 2]);
            availBlocks = Long.parseLong(row[indexOfPercent - 1]);

            String perc = row[indexOfPercent].substring(0, row[indexOfPercent].length() - 1);
            percentCapacity = Integer.parseInt(perc);

            StringBuilder sb = new StringBuilder();
            for (int i = indexOfPercent + 1; i < row.length; i++) {
                sb.append(row[i]);
                if (sb.length() > 0) {
                    sb.append(" ");
                }
            }
            mountedOn = sb.toString();
            return true;
        }

        private int findIndexPercent(String row[]) {
            for (int i = 0; i < row.length; i++) {
                if (row[i].endsWith("%")) {
                    return i;
                }
            }
            return -1;
        }

        public String getFileSystem() {
            return fileSystem;
        }

        public void setFileSystem(String fileSystem) {
            this.fileSystem = fileSystem;
        }

        public long getBlocks() {
            return blocks;
        }

        public void setBlocks(long blocks) {
            this.blocks = blocks;
        }

        public long getUsedBlocks() {
            return usedBlocks;
        }

        public void setUsedBlocks(long usedBlocks) {
            this.usedBlocks = usedBlocks;
        }

        public long getAvailBlocks() {
            return availBlocks;
        }

        public void setAvailBlocks(long availBlocks) {
            this.availBlocks = availBlocks;
        }

        public int getPercentCapacity() {
            return percentCapacity;
        }

        public void setPercentCapacity(int percentCapacity) {
            this.percentCapacity = percentCapacity;
        }

        public String getMountedOn() {
            return mountedOn;
        }

        public void setMountedOn(String mountedOn) {
            this.mountedOn = mountedOn;
        }

        public long getBlockSize() {
            return blockSize;
        }

        public void setBlockSize(int blockSize) {
            this.blockSize = blockSize;
        }

        public void setBlockSize(long blockSize) {
            this.blockSize = blockSize;
        }
    }
}


