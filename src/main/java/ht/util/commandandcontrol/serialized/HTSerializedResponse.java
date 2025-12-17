package ht.util.commandandcontrol.serialized;


import ht.util.commandandcontrol.*;

import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */

public class HTSerializedResponse extends Response {
    private List<InfoRow> list;

    //
    public HTSerializedResponse(List<InfoRow> list) {
        this.list = list;
    }

    public void setResponseShape(ResponseShape s) {
        // here we put the header.
        super.setResponseShape(s);
        addHeaderArray(shape.getShortNames());
    }

    public void addBannerRow(String row) {
    }

    public void addHeaderArray(String columnHeaders[]) {
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
        // do nothing
    }


    public void addRowArray(Object elements[]) {
        Row rr = new Row();
        rr.setNames(this.shape.getShortNames());
        rr.setRow(elements);
        addToResponse(rr);
    }


    public void addInfo(InfoLevel level, String info) {
        addToResponse(new InfoRow(level.name(), info));
    }

    public void end() {
    }

    public MultiRowResponse getMultiRowResponse() {
        if (shape == null) {
            return null;
        }
        return new HTSerializedMultiRowResponse(shape.getShortNames().length, this, this.shape);
    }

    public void addMultiRowResponse(MultiRowResponse mrr) {
        mrr.addToResponse(this);
        mrr.clear();
    }

    void addToResponse(InfoRow r) {
        list.add(r);
    }
}

