package ht.util.html;

import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.HTSerializable;
import ht.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 1, 2005 Time: 6:56:34 PM
 */

@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.LinkSet,
        isView = false,
        isPersisted = false,
        schemaVersion = LinkSet.SerialVersion)
public class LinkSet implements HTSerializable {
    public static final int SerialVersion = 1;
    private List<Link> m_links = new ArrayList<Link>();

    public LinkSet() {

    }

    public void add(Link link) {
        m_links.add(link);
    }

    public List<Link> getLinks() {
        return m_links;
    }

    public void setLinks(List<Link> links) {
        m_links = links;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(getSerializationVersion());
        os.writeListOfHTSerializable(m_links);
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        os.readListOfHTSerializable(m_links);

    }

    public int getSerializationVersion() {
        return SerialVersion;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean hasGuid() {
        return false;
    }

    public boolean hasSoftGuid() {
        return false;
    }
}
