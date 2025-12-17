package ht.util.basefile;

import com.fasterxml.jackson.databind.JsonNode;
import ht.util.basefile.fs.BaseFile;
import ht.util.core.iterator.Mapper;
import ht.util.core.string.Fmt;
import ht.util.core.string.StringUtil;

public class Name2JsonMapper implements Mapper<String, JsonNode> {
    private BaseFile directory;
    private String defaultDomain;

    public Name2JsonMapper(BaseFile directory, String defaultDomain) {
        this.directory = directory;
        this.defaultDomain = defaultDomain;
    }

    @Override
    public JsonNode apply(String s) {
        if (StringUtil.nullOrEmptyString(s)) {
            return null;
        }
        int index = s.indexOf("_");
        if (index == -1 && !StringUtil.nullOrEmptyString(defaultDomain)) {
            s = Fmt.S("%s_%s", defaultDomain, s);
        }
        s = s.toLowerCase();
        BaseFile file = directory.getChild("%s.json", s);
        if (file.exists()) {
            try {
                return file.getJsonNode();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}