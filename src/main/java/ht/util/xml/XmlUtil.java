package ht.util.xml;


public class XmlUtil {

    /*

     */
    public static final String escapeXML(String toEncode) {
        StringBuilder buff = new StringBuilder();

        return escapeXML(toEncode, buff).toString();
    }

    /**
     * Possibly not the most optimial routine but looks for existence of reserved xml characters and escapes them using
     * the predefined entity references.
     *
     * @param toEncode
     * @param buffer
     * @return buffer with escaped characters in.
     */


    public static final StringBuilder escapeXML(String toEncode, StringBuilder buffer) {
        int size = toEncode.length();
        for (int i = 0; i < size; i++) {
            char c = toEncode.charAt(i);
            switch (c) {
                case '&':
                    buffer.append("&amp;");
                    break;
                case '<':
                    buffer.append("&lt;");
                    break;
                case '>':
                    buffer.append("&gt;");
                    break;
                case '\'':
                    buffer.append("&apos;");
                    break;
                case '"':
                    buffer.append("&quote;");
                    break;
                default:
                    buffer.append(c);
            }
        }
        return buffer;
    }
}
