package ht.util.commandandcontrol;

import ht.util.io.csv.CSVFormattedWriter;
import ht.util.io.csv.CSVWriter;

import java.util.List;

/*
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 *
 * User: chris
 */
public class CSVWriterResponse implements CSVWriter {
    private CSVFormattedWriter writer;
    private Response response;

    public CSVWriterResponse(Response response, CSVFormattedWriter writer, String transaction, String rowName) {
        this.writer = writer;
        this.response = response;
        ResponseShape shape = new ResponseShape(transaction, rowName);
        shape.addHeader(writer.getColumnNames());
        response.setResponseShape(shape);
    }

    public void writeRow(Object values[]) {
        response.addRowArray(values);
    }

    public void writeRow(List<String> values) {
        response.addRowArray(values.toArray());
    }

    public void close() {
        response.end();
    }
}
