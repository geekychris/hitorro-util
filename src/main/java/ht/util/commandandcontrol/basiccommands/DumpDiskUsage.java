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
package ht.util.commandandcontrol.basiccommands;

import ht.jsontypesystem.JVS;
import ht.util.commandandcontrol.*;
import ht.util.commandandcontrol.ano.CommandDef;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.core.Constants;
import ht.util.core.string.StringUtil;
import ht.util.io.DiskUsage;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@CommandDef(command = "env.df", description = "Disk usage")
public class DumpDiskUsage extends Command {
    @ResponseDefinition(command = "df",
            rowname = "device",
            columns = {@RespColumn(name = "FileSystem", lName = "fs"),
                    @RespColumn(name = "Mount Point", lName = "mountpoint"),
                    @RespColumn(name = "Used", lName = "used"),
                    @RespColumn(name = "Free", lName = "free"),
                    @RespColumn(name = "Percent Free", lName = "percfree")})
    private ResponseShape header = new ResponseShape();


    public boolean execute(String rawValue, JVS args, Response response, CommandSession session, RestOperations operation) throws Exception {
        response.setResponseShape(header);

        List<DiskUsage.DFRow> rows = DiskUsage.getDFCached(Constants.MillisInSecond * 5);
        for (DiskUsage.DFRow row : rows) {
            long blockSize = row.getBlockSize();
            response.addRow(row.getFileSystem(),
                    row.getMountedOn(),
                    StringUtil.getBytesNeatForm(row.getUsedBlocks() * blockSize),
                    StringUtil.getBytesNeatForm(row.getAvailBlocks() * blockSize),
                    row.getPercentCapacity());
        }
        response.end();
        return true;
    }
}

