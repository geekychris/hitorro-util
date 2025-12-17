package ht.util.html;

import ht.util.core.Log;
import org.cyberneko.html.parsers.DOMParser;
import org.w3c.dom.*;

import java.net.URL;

/**
 *
 */
public abstract class HTMLTreeWalker<C> {
    private DOMParser m_parser; // object that creates DOM from m_htmlPage

    public void setDOMParser(DOMParser parser) {
        m_parser = parser;
    }

    public boolean visit(String tagName,
                         URL sourceUrl,
                         C container) {
        try {
            String upperCaseTagName = tagName.toUpperCase();
            Document doc = null;
            if (this.m_parser != null) {
                doc = this.m_parser.getDocument();
            }
            if (doc != null) {

                Element htmlElement = doc.getDocumentElement();
                if (upperCaseTagName.equals("HTML")) {
                    // Recursively accumulate all text from the root
                    visitChild(doc, htmlElement, container, sourceUrl, 0);
                } else {
                    // Grab non-tag text from all copies of this tag
                    NodeList nodeList = htmlElement.getElementsByTagName(upperCaseTagName);
                    int len = nodeList.getLength();
                    for (int i = 0; i < len; i++) {
                        Element element = (Element) nodeList.item(i);
                        visitChild(doc, element, container, sourceUrl, 0);
                    }
                }
            }
        } catch (DOMException dome) {
            Log.httpfetcher.error("Unable to parse page with error %s", dome.getLocalizedMessage());
            return false;
        }
        return true;
    }


    private void visitChild(Document doc, Element element, C container, URL sourceUrl, int depth) {
        String tagName = element.getTagName();
        Node child = element.getFirstChild();

        while (null != child) {
            String name = child.getNodeName();

            processNode(name, child, doc, sourceUrl, container);

            int nodeType = child.getNodeType();
            if (Node.ELEMENT_NODE == nodeType) {
                if (element == child || depth > 100) {
                    //XXX TODO Anti short circuit mechanism...cant see much having a depth of 100 or more!!!
                    return;
                }
                visitChild(doc, (Element) child, container, sourceUrl, depth++);
            }

            child = child.getNextSibling();
        }

    }


    public abstract void processNode(String name, Node child, Document doc, URL sourceUrl, C container);

}
