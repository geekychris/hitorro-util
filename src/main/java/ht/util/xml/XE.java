package ht.util.xml;

import ht.util.core.string.StringUtil;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class XE {
    private XE parent;
    private String name;
    private String value;
    private Attributes attributes;
    private List<XE> children;

    public XE(XE parent, String name, String value, Attributes attr) {
        this.parent = parent;
        this.name = name;
        this.value = value;
        this.attributes = attr;
    }

    public String toString() {
        return name;
    }

    public XE get(String path) {
        String parts[] = StringUtil.tokenizeFromSingleChar(path, ".");
        return get(parts, 0);
    }

    private XE get(String parts[], int index) {
        if (children == null) {
            return null;
        }
        for (XE child : children) {
            if (parts[index].equals(child.getName())) {
                if (index == parts.length - 1) {
                    return child;
                } else {
                    return child.get(parts, index + 1);
                }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String val) {
        this.value = val;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void addChild(XE child) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(child);
    }

    public List<XE> getChildren() {
        return children;
    }
}