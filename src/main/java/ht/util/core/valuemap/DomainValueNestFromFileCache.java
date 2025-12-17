package ht.util.core.valuemap;

import ht.util.core.Log;
import ht.util.core.iterator.Mapper;
import ht.util.core.string.Fmt;
import ht.util.io.FileUtil;
import ht.util.io.csv.CSVReader;
import ht.util.io.csv.csvconsumer.CSVConsumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
public class DomainValueNestFromFileCache implements Mapper<String, DomainValueMap> {
    private File m_directory;
    private String m_extension;
    private String m_eventName;

    /**
     * Constructor
     */
    public DomainValueNestFromFileCache(File directory, String extension, String eventName) {
        m_eventName = eventName;
        m_extension = extension;
        m_directory = directory;
    }

    public DomainValueMap apply(String key) {
        File f = new File(m_directory, Fmt.S("%s.%s", key, m_extension));
        if (FileUtil.notNullAndExists(f)) {
            DomainValueMap<String> dvm = new DomainValueMap<String>();
            try {
                CSVReader reader = new CSVReader(f);
                DVMReader dvmConsumer = new DVMReader(dvm);
                reader.readLines(dvmConsumer);
                return dvm;
            } catch (FileNotFoundException e) {
                Log.util.error("%s %e", e, e);
            } catch (IOException e) {
                Log.util.error("%s %e", e, e);
            }
        }
        return null;
    }


    public String eventName() {
        return m_eventName;
    }
}

class DVMReader implements CSVConsumer {
    private DomainValueMap<String> dvm;

    public DVMReader(DomainValueMap<String> dvm) {
        this.dvm = dvm;
    }

    /**
     * Expect columns: domain, valuekey, value
     */
    public void line(int rowCount, String[] line) {
        if (rowCount == 0) {
            // dont want to process zeroth row.
            return;
        }
        if (line == null || line.length != 3) {
            Log.util.error("Line does not have 3 parts");
        }

        String domain = line[0].toLowerCase();
        String valueKey = line[1].toLowerCase();
        String value = line[2];
        ValueMap<String> vm = dvm.getValueMap(domain);
        if (vm == null) {
            vm = new FlatValueMap<String>();
            vm.setDomain(domain);
            dvm.addValueMap(vm, domain);
        }
        vm.setValue(value, valueKey);
    }
}
