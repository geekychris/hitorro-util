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
package com.hitorro.util.commandandcontrol.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.hitorro.util.commandandcontrol.InfoLevel;
import com.hitorro.util.commandandcontrol.MultiRowResponse;
import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.NumberClassEnum;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.json.JSONMap;
import com.hitorro.util.json.JSONNumber;
import com.hitorro.util.json.JSONString;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;


public class JSONResponse extends Response {
    public static final String BannerKey = "Banner";
    public static final String ErrorKey = "Error";
    public static final String WarningKey = "Warning";
    public static final String InfoKey = "Info";

    protected String headers[];
    protected String transaction;
    protected boolean ended = false;
    JsonGenerator jsonGenerator;
    private JsonFactory jsonFactory;

    public JSONResponse(String trans, OutputStream os) throws IOException {
        jsonFactory = new JsonFactory();
        jsonGenerator = jsonFactory.createGenerator(new PrintWriter(os));
        transaction = trans;
    }

    public static void setMapEntry(final JSONMap map, final Class c, final String name, final Object o) {
        if (o == null) {
            return;
        }
        String value = o.toString();
        NumberClassEnum e = NumberClassEnum.get(c);
        if (e == null) {
            map.put(name, new JSONString(value));
        } else {
            if (!StringUtil.nullOrEmptyString(value)) {
                map.put(name, new JSONNumber(e.parseFromString(value)));
            }
        }
    }

    public void setResponseShape(ResponseShape s) {
        // here we put the header.
        super.setResponseShape(s);
        addHeaderArray(shape.getShortNames());
    }

    public void addBannerRow(String row) {
        try {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(BannerKey, row);
            jsonGenerator.writeEndObject();
        } catch (JsonGenerationException e) {
            Log.util.error("%s %e", e, e);
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        }
    }

    public void addHeaderArray(String columnHeaders[]) {
        headers = columnHeaders;
    }

    public void addRow(Object... elements) {
        addRowArray(elements);
        containers = null;
    }

    public void addRowArray(Object elements[]) {
        write(elements, headers);
    }

    private void write(Object row[], String names[]) {
        try {
            JSONMap map = new JSONMap();
            int size = row.length;
            for (int i = 0; i < size; i++) {
                Object o = row[i];
                if (o != null) {
                    Class c;
                    if (shape.getClasses() != null) {
                        c = shape.getClasses()[i];
                    } else {
                        c = String.class;
                    }
                    String name = names[i];
                    setMapEntry(map, c, name, o);
                }
            }
            map.writeJSONGraph(jsonGenerator);
        } catch (JsonGenerationException e) {
            Log.util.error("%s %e", e, e);
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        }
    }

    public void addMessageAux(String message, String key) {
        try {
            JSONMap map = new JSONMap();
            map.put(key, new JSONString(message));
            map.writeJSONGraph(jsonGenerator);
        } catch (JsonGenerationException e) {
            Log.util.error("%s %e", e, e);
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        }
    }

    public void addInfo(String info) {
        addMessageAux(info, InfoKey);
    }

    public void addInfo(InfoLevel level, String info) {
        switch (level) {
            case Info:
                addMessageAux(info, InfoKey);
                break;
            case Warn:
                addMessageAux(info, WarningKey);
                break;
            case Error:
                addMessageAux(info, ErrorKey);
                break;
        }
    }

    @Override
    public void addStatusUpdateMessage(final String info, final int percentComplete) {
    }

    public void end() {
        if (ended) {
            return;
        }
        try {
            jsonGenerator.close();
        } catch (IOException e) {
            Log.commands.error("%s %e", e, e);
        }
    }

    public MultiRowResponse getMultiRowResponse() {
        if (shape == null) {
            return null;
        }
        return new JSONMultiRowResponse(shape.getHeaderShort().length, this, this.shape);
    }

    public void addMultiRowResponse(MultiRowResponse mrr) {
        mrr.addToResponse(this);
        mrr.clear();
    }
}