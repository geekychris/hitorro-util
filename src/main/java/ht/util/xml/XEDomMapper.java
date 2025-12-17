package ht.util.xml;

import ht.util.core.iterator.AbstractIterator;
import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.excelaccess.Log;

import javax.xml.stream.XMLStreamException;
import java.io.InputStream;

/**
 *
 */
public class XEDomMapper extends BaseMapper<InputStream, AbstractIterator<XE>> {
    private String path;

    public XEDomMapper(String path) {
        this.path = path;
    }

    public AbstractIterator<XE> apply(InputStream is) {
        try {
            return new StaxXMLBaseChainingIterator(is, path);
        } catch (XMLStreamException e) {
            Log.util.error("%s %e", e, e);
            return null;
        }
    }
}

