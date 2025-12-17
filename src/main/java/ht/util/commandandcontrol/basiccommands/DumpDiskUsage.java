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

