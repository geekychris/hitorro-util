package ht.util.io.csv.csvconsumer;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2005 Time: 9:45:15 AM
 */
public interface CSVConsumer {
    void line(int rowCount, String[] line);
}
