package ht.util.statemachine;

import ht.util.core.string.StringUtil;
import ht.util.io.StoreException;
import ht.util.typesystem.HTObjectInputStream;
import ht.util.typesystem.HTObjectOutputStream;
import ht.util.typesystem.HTSerializable;
import ht.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 9, 2007 Time: 8:33:51 AM
 */
@TypeClassMetaInfo(shortTypeName = "Group",
        isView = false,
        isPersisted = false,
        schemaVersion = Group.SerializationVersion)
public class Group implements HTSerializable {
    public static final int SerializationVersion = 1;
    private String name;
    private String parentName;
    private String description;
    private Group parent;

    public Group() {

    }

    public Group(String name, String parent, String description) {
        this.name = name;
        this.description = description;
        parentName = parent;
    }

    public void finalizeInit(MooreStateMachine registry) {
        if (!StringUtil.nullOrEmptyOrBlankString(parentName)) {
            parent = registry.getGroup(parentName);
        }
    }

    public Group getParent() {
        return parent;
    }

    public void setParent(Group group) {
        parent = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(getSerializationVersion());
        os.writeString(name);
        os.writeString(parentName);
        os.writeString(description);
        os.writeVersionedObject(parent);
    }

    public void deserialize(HTObjectInputStream os) throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        switch (version) {
            case 1:
                name = os.readString();
                parentName = os.readString();
                description = os.readString();
                parent = (Group) os.readVersionedObject();
        }

    }

    public int getSerializationVersion() {
        return Group.SerializationVersion;
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

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }
}