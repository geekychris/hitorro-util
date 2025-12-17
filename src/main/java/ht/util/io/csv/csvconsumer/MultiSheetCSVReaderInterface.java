package ht.util.io.csv.csvconsumer;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2005 Time: 10:21:19 AM
 */
public interface MultiSheetCSVReaderInterface {
    void readLines(CSVConsumer consumer, String sheetName)
            throws IOException;
}
