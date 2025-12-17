package ht.util.xml;

import ht.util.core.iterator.AbstractIterator;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 * Iterator of an XML stream, that based upon some element name will chunk of the xml into domlets that are
 */
public class StaxXMLBaseChainingIterator extends AbstractIterator<XE> {
    private XE curr = null;

    private BaseStaxXmlParser bsParser = new BaseStaxXmlParser();

    public StaxXMLBaseChainingIterator(InputStream is, String elemName) throws XMLStreamException {
        bsParser.setInputStream(is, elemName);
        curr = bsParser.renderObject();
    }

    @Override
    public void close() throws Exception {
        bsParser.close();
    }

    @Override
    public boolean hasNext() {
        return curr != null;
    }

    @Override
    public XE next() {
        XE ret = curr;
        try {
            curr = bsParser.renderObject();
        } catch (XMLStreamException e) {
            return null;
        }
        return ret;
    }

    @Override
    public void remove() {
    }
}
