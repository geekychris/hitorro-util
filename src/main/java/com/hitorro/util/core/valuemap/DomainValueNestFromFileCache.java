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
package com.hitorro.util.core.valuemap;

import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.csv.CSVReader;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


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
