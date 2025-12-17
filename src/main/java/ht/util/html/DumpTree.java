package ht.util.html;

import ht.util.core.Console;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.net.URL;

/**
 *
 */
public class DumpTree extends HTMLTreeWalker<DumpTreeCollector> {
    @Override
    public void processNode(final String name, final Node child, final Document doc, final URL sourceUrl, final DumpTreeCollector c) {

        NamedNodeMap map = child.getAttributes();

        Console.bprintln(c.sb, "%s", name);
        if (map != null) {
            int count = map.getLength();

            for (int i = 0; i < count; i++) {
                Node n = map.item(i);
                Console.bprint(c.sb, "%s, ", n.toString());

            }
            Console.bprintln(c.sb);
        }

    }

    public DumpTreeCollector getContainer() {
        return new DumpTreeCollector();
    }
}


