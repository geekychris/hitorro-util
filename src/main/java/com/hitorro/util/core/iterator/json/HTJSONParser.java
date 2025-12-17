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
package com.hitorro.util.core.iterator.json;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * Created by chris on 5/16/16.
 */
public class HTJSONParser {
    protected JsonParser parser = null;

    public HTJSONParser(JsonParser parser) {
        this.parser = parser;
    }

    public JsonNode read() {
        try {
            JsonToken token = parser.nextToken();
            if (token == null || JsonToken.NOT_AVAILABLE == token) {
                return null;
            }
            return readPrivate();
        } catch (IOException e) {
            throw new JSONParseException(e);
        }

    }

    protected JsonNode readPrivate() throws IOException {
        JsonToken tok = parser.getCurrentToken();

        return switchOnToken(tok);
    }

    protected final JsonNode switchOnToken(final JsonToken tok) throws IOException {
        switch (tok) {
            case VALUE_STRING:
                return JsonNodeFactory.instance.textNode(parser.getText());
            case START_OBJECT:
                return readObject();
            case START_ARRAY:
                return readArray();
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

    private final JsonNode readObject() throws IOException {
        try {
            parser.nextToken();
            ObjectNode map = JsonNodeFactory.instance.objectNode();
            if (parser.getCurrentToken() != JsonToken.END_OBJECT) {
                // not an empty object
                do {
                    String name = parser.getCurrentName();
                    parser.nextToken();
                    map.put(name, readPrivate());
                    parser.nextToken();

                } while (parser.getCurrentToken() != JsonToken.END_OBJECT);
            }

            return map;
        } catch (JsonParseException jpe) {
            JsonLocation jl = jpe.getLocation();
            // XXX Can we put some better logging exception description here?
            throw jpe;
        }
    }

    private final ArrayNode getArrayNode() {
        return JsonNodeFactory.instance.arrayNode();
    }

    private final JsonNode readArray() throws IOException {
        parser.nextToken();
        ArrayNode list = getArrayNode();
        if (parser.getCurrentToken() != JsonToken.END_ARRAY) {
            // not an empty array
            do {
                list.add(readPrivate());
                parser.nextToken();
            } while (parser.getCurrentToken() != JsonToken.END_ARRAY);
        }
        return list;
    }

}
