package ht.util.core.http;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.tools.BaseFileUtil;
import ht.util.io.FileUtil;

/**
 *
 */
public class JSONHTTPIterableClient extends HTTPIterableClient<JsonNode, JsonNode> {
    public JSONHTTPIterableClient(String name, String password) {
        super(name, password, FileUtil.inputstream2JacksonjsonReader, BaseFileUtil.os2JJsonSink);
    }

    public JSONHTTPIterableClient() {
        super(null, null, FileUtil.inputstream2JacksonjsonReader, BaseFileUtil.os2JJsonSink);
    }
}
