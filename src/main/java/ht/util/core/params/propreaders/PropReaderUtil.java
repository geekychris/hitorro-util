package ht.util.core.params.propreaders;

import ht.jsontypesystem.JVS;
import ht.jsontypesystem.JVSUtils;
import ht.util.core.Env;
import ht.util.core.params.HTProperties;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.BooleanProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris Utils for loading props from files
 */
public class PropReaderUtil {
    public static final BooleanProperty DebugMode = new BooleanProperty("debug", "If debug mode is enabled loads extra config files", false);

    public static List<File> getProps(final Map<String, String> cmdLineArgs, final HTProperties props, final File directory) {
        JVS cmd = JVSUtils.convertMapToJVS(cmdLineArgs, new JVS());
        List<File> filesConsidered = new ArrayList();
        boolean debugMode = DebugMode.apply(cmd);
        String serverType = Env.ServerType.apply(cmd);
        props.readDirectory(directory, filesConsidered);
        if (!StringUtil.nullOrEmptyString(serverType)) {
            File subDirFile = new File(directory, serverType);
            if (subDirFile.exists()) {
                props.readDirectory(subDirFile, filesConsidered);
            }
        }
        if (debugMode) {
            File debugDir = new File(directory, "debug");
            if (debugDir.exists()) {
                props.readDirectory(debugDir, filesConsidered);
            }
        }
        return filesConsidered;
    }
}
