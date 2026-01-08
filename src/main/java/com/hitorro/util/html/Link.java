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
package com.hitorro.util.html;

import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 */
@TypeClassMetaInfo(shortTypeName = TypeClassMetaInfo.Link,
        isView = false,
        isPersisted = false,
        schemaVersion = Link.SerialVersion)
public class Link implements HTSerializable, Comparable<Link> {
    public static final int SerialVersion = 2;
    public static final UrlOnlyLinkComparitor URLOnlyComparitor = new UrlOnlyLinkComparitor();
    public static final UrlAndTypeLinkComparitor UrlAndTypeComparitor = new UrlAndTypeLinkComparitor();
    private static final String[] BlockList = new String[]{"javascript", "skype", "callto", "webcal", "feed", "aim", "mms", "pcast", "news", "secondlife", "mailto", "irc"};
    private LinkType type;
    private String url;
    private String title;
    private String typeString;
    private boolean relativeUrl = true;
    private String originalUrl;
    private long urlHash;
    private long publishDate;

    private String sourceUrl;
    private long sourceHash;
    private List<GenericKeyValue<String, String>> attr;

    public Link() {

    }

    public Link(String url, LinkType type, String title, String typeString, URL root) {
        this(url, type, title, typeString, root, "");
    }


    public Link(String url, LinkType type, String title, String typeString, URL root, String source) {
        this(url, type, title, typeString, root, new ArrayList());
        this.setSource(source);
    }

    public Link(String url, LinkType type, String title, String typeString, URL root, List<GenericKeyValue<String, String>> attr) {
        this.attr = attr;
        this.url = url;
        originalUrl = url;
        this.url = expandUrl(this, url, root);
        urlHash = FPHash64.getFP(this.url);
        this.type = type;
        this.typeString = typeString;
        this.title = title;
    }

    public static final String expandUrl(Link l, String url, URL source) {
        if (source == null) {
            return url;
        }
        try {
            url = url.trim();

            if (StringUtil.startsWithIndexIgnoreCase(url, BlockList) != -1) {
                return url;
            }
            if (StringUtil.startsWithIgnoreCase(url, "http")) {
                if (l != null) {
                    l.relativeUrl = false;
                }
            }
            return new URL(source, url).toString();

        } catch (MalformedURLException e) {

        }
        return url;
    }

    public String toString() {
        return url;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(getSerializationVersion());
        os.writeString(type.getName());
        os.writeString(url);
        os.writeString(title);
        os.writeString(typeString);
        os.writeBoolean(relativeUrl);
        os.writeString(originalUrl);
        os.writeLong(urlHash);

        // as of version 2
        os.writeString(sourceUrl);
        os.writeLong(sourceHash);
        os.writeLong(publishDate);
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();

        type = LinkType.getTypeName(os.readString());
        url = os.readString();
        title = os.readString();
        typeString = os.readString();
        relativeUrl = os.readBoolean();
        originalUrl = os.readString();
        urlHash = os.readLong();
        if (version > 1) {
            sourceUrl = os.readString();
            sourceHash = os.readLong();
            publishDate = os.readLong();
        }

    }

    public int getSerializationVersion() {
        return SerialVersion;
    }

    public long getPublishedDate() {
        return publishDate;
    }

    public void setPublishedDate(long date) {
        publishDate = date;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public long getSourceHash() {
        return sourceHash;
    }

    public void setSource(String url) {
        sourceUrl = url;
        sourceHash = FPHash64.getFP(url);
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

    public List<GenericKeyValue<String, String>> getAttributes() {
        return attr;
    }

    public long getUrlHash() {
        return urlHash;
    }

    public int compareTo(Link o) {
        return url.compareTo(o.getUrl());
    }

    public LinkType getLinkType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public boolean isRelativeUrl() {
        return relativeUrl;
    }

    /**
     * Url not made into an absolute value
     *
     * @return
     */
    public String getOriginalUrl() {
        return url;
    }

    public String getTypeString() {
        return typeString;
    }

    public String getTitle() {
        return title;
    }

    public void setLink(String url) {
        this.url = url;
    }


    public enum LinkType {
        PingBack("pb"),
        Alternate("a"),
        StyleSheet("ss"),
        ShortCutIcon("sc"),
        Anchor("a"),
        EditURI("eu"),
        Image("im"),
        Unknown("u");

        private static HashMap<String, LinkType> s_byShortName;
        private String name;

        LinkType(String name) {
            name = name.toLowerCase();
            setMapEntry(this);
        }

        public static LinkType getTypeName(String name) {
            return s_byShortName.get(name.toLowerCase());
        }

        public static int size() {
            return s_byShortName.size();
        }

        private static void setMapEntry(LinkType filter) {
            if (s_byShortName == null) {
                s_byShortName = new HashMap<String, LinkType>();
            }
            s_byShortName.put(filter.getName(), filter);
        }

        public String getName() {
            return name;
        }
    }
}

class UrlOnlyLinkComparitor implements Comparator<Link> {

    public int compare(Link link, Link link1) {
        long l = (link.getUrlHash() - link1.getUrlHash());
        if (l > 0) {
            return 1;
        }
        if (l < 0) {
            return -1;
        }
        return 0;
    }
}

class UrlAndTypeLinkComparitor implements Comparator<Link> {

    public int compare(Link link, Link link1) {
        int i = link.getUrl().compareTo(link1.getUrl());
        if (i == 0) {
            return 0;
        }
        return link.getLinkType().ordinal() - link1.getLinkType().ordinal();
    }
}
