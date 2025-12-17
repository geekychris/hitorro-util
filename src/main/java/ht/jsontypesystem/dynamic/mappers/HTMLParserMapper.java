package ht.jsontypesystem.dynamic.mappers;

import ht.util.core.iterator.mappers.BaseMapper;
import ht.util.html.HTMLParser;

import java.io.IOException;

public class HTMLParserMapper extends BaseMapper<String, String> {
    private HTMLParser hp;

    public HTMLParserMapper() {
        hp = new HTMLParser();
    }

    public HTMLParserMapper(char seperator) {
        hp = new HTMLParser(seperator);
    }

    public String apply(String s) {
        try {
            hp.setSourceFromPlainText(s);
        } catch (IOException e) {
            // swallow
        }

        return hp.getHtmlText();
    }
}
