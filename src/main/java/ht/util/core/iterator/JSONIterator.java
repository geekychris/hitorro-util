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
package ht.util.core.iterator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ht.util.integrationevents.Log;
import ht.util.typesystem.TypeIntf;
import ht.util.typesystem.TypeManagerBase;

import java.io.IOException;
import java.io.Reader;

/**
 * JSON Iterator for jackson JsonNode trees
 */
public class JSONIterator extends AbstractIterator<JsonNode> {
    public static final ObjectNode EmptyMap = JsonNodeFactory.instance.objectNode();
    public static final ArrayNode EmptyList = JsonNodeFactory.instance.arrayNode();
    private final static JsonFactory factory = new JsonFactory();
    public static String HTTypeKey = "ht_type";
    private JsonParser parser = null;
    private JsonNode domlet;

    private ObjectNode tmpMap = JsonNodeFactory.instance.objectNode();
    private String typeName;

    public JSONIterator(Reader reader, String typeName) {
        TypeIntf type = null;
        if (TypeManagerBase.get() != null) {
            type = TypeManagerBase.get().getTypeByShortName(typeName);
        }
        if (type != null) {
            this.typeName = typeName;
        }
        init(reader);
    }

    public JSONIterator(Reader reader) {
        init(reader);
    }

    private void init(Reader reader) {
        try {
            parser = factory.createParser(reader);
            // was running out of permspace, dont intern strings.
        } catch (IOException e) {
            Log.unitTime.error("Unable to create parser %s %e", e, e);
        }
        domlet = read();
    }

    @Override
    public void close() throws Exception {
        if (parser != null) {
            parser.close();
        }
    }

    public boolean hasNext() {
        return domlet != null;
    }

    public JsonNode next() {
        JsonNode dom = domlet;
        domlet = read();

        return dom;
    }

    public void remove() {

    }

    JsonNode read() {
        try {
            JsonToken token = parser.nextToken();
            if (token == null || JsonToken.NOT_AVAILABLE == token) {
                return null;
            }
            JsonNode node = readPrivate();

            if (typeName != null && node instanceof ObjectNode) {
                ((ObjectNode) node).put(HTTypeKey, typeName);
            }
            return node;
        } catch (IOException e) {
            Log.util.error("Unable to read %s %e", e, e);
            return null;
        }

    }

    private JsonNode readPrivate() throws IOException {
        JsonToken tok = parser.getCurrentToken();
        switch (tok) {
            case START_OBJECT:
                return readObject();
            case START_ARRAY:
                return readArray();
            case VALUE_STRING:
                return JsonNodeFactory.instance.textNode(parser.getText());
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
                switch (parser.getNumberType()) {
                    case BIG_DECIMAL:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().longValue());
                    case BIG_INTEGER:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().intValue());
                    case DOUBLE:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().doubleValue());
                    case FLOAT:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().floatValue());
                    case INT:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().intValue());
                    case LONG:
                        return JsonNodeFactory.instance.numberNode(parser.getNumberValue().longValue());
                }
            case VALUE_TRUE:
                return JsonNodeFactory.instance.booleanNode(Boolean.TRUE);
            case VALUE_FALSE:
                return JsonNodeFactory.instance.booleanNode(Boolean.FALSE);
            case VALUE_NULL:
                return JsonNodeFactory.instance.nullNode();
            default:


        }
        return null;
    }

    private ObjectNode getObjectNode() {
        if (tmpMap != null) {
            ObjectNode t = tmpMap;
            tmpMap = null;
            return t;
        }
        return JsonNodeFactory.instance.objectNode();
    }

    private JsonNode readObject() throws IOException {
        parser.nextToken();
        ObjectNode map = getObjectNode();
        if (parser.getCurrentToken() != JsonToken.END_OBJECT) {
            // not an empty object
            do {
                String name = parser.getCurrentName();
                parser.nextToken();
                map.put(name, readPrivate());
                parser.nextToken();

            }
            while (parser.getCurrentToken() != JsonToken.END_OBJECT);
        }
        if (map.size() == 0) {
            // we didnt use the apply
            tmpMap = map;
            return EmptyMap;
        }

        return map;
    }

    private ArrayNode getArrayNode() {


        return JsonNodeFactory.instance.arrayNode();
    }

    private JsonNode readArray() throws IOException {
        parser.nextToken();
        ArrayNode list = getArrayNode();
        if (parser.getCurrentToken() != JsonToken.END_ARRAY) {
            // not an empty array
            do {
                list.add(readPrivate());
                parser.nextToken();
            }
            while (parser.getCurrentToken() != JsonToken.END_ARRAY);
        }
        if (list.size() == 0) {
            return EmptyList;
        }
        return list;
    }
}

