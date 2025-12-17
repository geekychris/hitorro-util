package ht.util.core.iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Open up a stax iterator and decode some kind of object from it.
 */
public abstract class BaseStaxIterator<E> extends AbstractIterator<E> {
    protected static XMLInputFactory iFactory = javax.xml.stream.XMLInputFactory.newInstance();
    protected XMLStreamReader reader;
    protected E e = null;
    protected InputStream is;

    public BaseStaxIterator(InputStream is, String encoding) throws XMLStreamException, UnsupportedEncodingException {
        this.is = is;

        reader = iFactory.createXMLStreamReader(new InputStreamReader(is, encoding));
        init();
    }

    public abstract E readNext();

    private void init() throws XMLStreamException {
        if (reader.hasNext()) {
            reader.next();
        }
        e = readNext();
    }

    @Override
    public void close() throws Exception {
        if (is != null) {
            is.close();
            is = null;
        }
    }

    @Override
    public boolean hasNext() {
        return e != null;
    }

    @Override
    public E next() {
        E curr = e;
        e = readNext();
        return curr;
    }

    @Override
    public void remove() {
    }
}
