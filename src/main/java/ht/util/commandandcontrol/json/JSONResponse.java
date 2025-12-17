package ht.util.commandandcontrol.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import ht.util.commandandcontrol.InfoLevel;
import ht.util.commandandcontrol.MultiRowResponse;
import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.ResponseShape;
import ht.util.core.Log;
import ht.util.core.NumberClassEnum;
import ht.util.core.string.StringUtil;
import ht.util.json.JSONMap;
import ht.util.json.JSONNumber;
import ht.util.json.JSONString;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 */
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