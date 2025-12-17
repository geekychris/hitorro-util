package ht.util.xml;

import org.xml.sax.helpers.AttributesImpl;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class BaseStaxXmlParser {
    protected XMLInputFactory factory;
    protected String elemName;
    private InputStream is;
    private XMLEventReader reader;

    private StringBuilder nameBuilder = new StringBuilder();

    public BaseStaxXmlParser() {
        factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_VALIDATING, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
    }

    public void close() throws Exception {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            throw new IOException(e);
        }
    }

    public void setInput(String text, String elemName) throws XMLStreamException {
        ByteArrayInputStream bios = new ByteArrayInputStream(text.getBytes());
        this.setInputStream(bios, elemName);
    }

    public void setInputStream(InputStream is, String elemName) throws XMLStreamException {
        this.elemName = elemName;
        this.is = is;
        reader = factory.createXMLEventReader(is);
    }

    public XE renderObject() throws XMLStreamException {
        // advance to start element.
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {

                StartElement s = (StartElement) event;
                if (s.getName().getLocalPart().equals(elemName)) {
                    XE root = constructStart(null, s);
                    renderObjectAux(root);
                    return root;
                }
            }
        }
        return null;
    }

    private XE constructStart(final XE parent, final StartElement s) {
        AttributesImpl a = null;
        Iterator iter = s.getAttributes();
        while (iter.hasNext()) {
            //AttributeEventImpl ns = (AttributeEventImpl) iter.next();
            Attribute ns = (Attribute) iter.next();
            if (a == null) {
                a = new AttributesImpl();

            }
            //public void addAttribute (String uri, String localName, String qName,
            //      String type, String value)
            a.addAttribute(ns.getName().getNamespaceURI(), ns.getName().getLocalPart(), ns.getName().getPrefix(), "", ns.getValue());
        }
        String prefix = s.getName().getPrefix();
        String name = s.getName().getLocalPart();
        if (!prefix.equals("")) {
            nameBuilder.setLength(0);
            nameBuilder.append(prefix).append(':').append(name);
            name = nameBuilder.toString();
        }
        XE ret = new XE(parent, name, null, a);
        if (parent != null) {
            parent.addChild(ret);
        }
        return ret;
    }

    private void renderObjectAux(XE root) throws XMLStreamException {
        boolean hitEnd = false;
        while (!hitEnd && reader.hasNext()) {

            XMLEvent event = reader.nextEvent();
            if (event.isCharacters()) {
                Characters c = event.asCharacters();
                if (c.isCData()) {
                    root.setValue(c.getData());
                } else {
                    root.setValue(c.getData());
                }
            } else if (event.isStartElement()) {
                StartElement s = (StartElement) event;

                XE child = constructStart(root, s);
                renderObjectAux(child);

            } else if (event.isEndElement()) {
                hitEnd = true;
            }

        }
        return;
    }
}
