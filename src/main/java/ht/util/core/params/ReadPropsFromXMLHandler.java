package ht.util.core.params;

import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 *
 */
public class ReadPropsFromXMLHandler extends DefaultHandler {
    public static final String ConfigRoot = "configroot";
    public Map<String, String> map = new TreeMap();
    private StringBuilder builder = new StringBuilder();
    private Stack<String> path = new Stack();

    /**
     * When you see a start tag, print it out and then increase indentation by two spaces. If the element has
     * attributes, place them in parens after the element name.
     */
    public void startElement(String namespaceUri,
                             String localName,
                             String qualifiedName,
                             Attributes attributes) {
        qualifiedName = qualifiedName.toLowerCase();
        if (qualifiedName.equals(ConfigRoot)) {
            // we ignore the top level
            return;
        }
        builder.setLength(0);
        String child = getChildName(qualifiedName);
        for (int i = 0; i < attributes.getLength(); i++) {
            String n = attributes.getQName(i);
            String v = attributes.getValue(i);
            String fullName = Fmt.S("%s.%s", child, n);
            map.put(fullName, v);
        }

    }

    /**
     * When you see the end tag, print it out and decrease indentation level by 2.
     */

    public void endElement(String namespaceUri,
                           String localName,
                           String qualifiedName) {

        qualifiedName = qualifiedName.toLowerCase();
        if (qualifiedName.equals(ConfigRoot)) {
            // we ignore the top level
            return;
        }

        if (builder.length() != 0) {
            String s = builder.toString().trim();

            if (!StringUtil.nullOrEmptyString(s)) {
                map.put(path.peek(), s);

            }
            builder.setLength(0);
        }
        path.pop();
    }

    private String getChildName(String name) {
        if (path.size() == 0) {
            path.push(name);
            return name;
        }
        String ret = Fmt.S("%s.%s", path.peek(), name);
        path.push(ret);
        return ret;
    }

    public void characters(char[] chars,
                           int startIndex,
                           int endIndex) {
        builder.append(chars, startIndex, endIndex);
    }
}

