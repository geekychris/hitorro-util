package ht.util.basefile.tools.queue.reader.serializer;

import java.util.HashMap;
import java.util.Map;


public class WalkerDeserializerFactory {
    private static WalkerDeserializerFactory me = new WalkerDeserializerFactory();
    private Map<String, WalkerDeserializer> map = new HashMap();

    private WalkerDeserializer defaultSerializer;

    public WalkerDeserializerFactory() {
        defaultSerializer = null;
    }

    public static WalkerDeserializerFactory getInstance() {
        return me;
    }

    public void add(WalkerDeserializer deserializer, String extension, boolean defaultVal) {
        map.put(extension, deserializer);
        if (defaultVal == true) {
            defaultSerializer = deserializer;
        }
    }

    public WalkerDeserializer getDeserializer(String extension) {
        WalkerDeserializer ser = map.get(extension);
        if (ser != null) {
            return ser;
        }
        return defaultSerializer;
    }
}
