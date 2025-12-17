package ht.util.commandandcontrol;

import org.xml.sax.helpers.AttributesImpl;

/**
 * <p/>
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Aug 2, 2005 Time: 10:15:59 PM Defines the
 * offset within the table to apply fields (in an multi row response)
 */
public class GroupTuple {
    String setName;
    String name;
    String shortName;
    Class type;
    int size;
    AttributesImpl attribute;

    public GroupTuple(String setName, String nameIn, String shortNameIn, Class typeIn, int sizeIn) {
        this.setName = setName;
        name = nameIn;
        shortName = shortNameIn;
        type = typeIn;
        size = sizeIn;
        attribute = new AttributesImpl();
        attribute.addAttribute("", "name", "", "", shortName);
    }

    public int getSize() {
        return size;
    }

    public String getSetName() {
        return setName;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }

    public Class getTupleType() {
        return type;
    }
}