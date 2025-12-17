package ht.util.core.iterator.mappers;

import ht.util.core.iterator.Mapper;

import java.util.StringTokenizer;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 * <p/>
 * Tokenize a line into individual strings.
 */
public class StringToStringArrayMapper implements Mapper<String, String[]> {
    private String m_tok;

    public StringToStringArrayMapper(String tok) {
        m_tok = tok;
    }

    public String[] apply(String s) {
        StringTokenizer tok = new StringTokenizer(s, m_tok);
        int count = tok.countTokens();
        String row[] = new String[count];
        int i = 0;
        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            row[i++] = t;
        }
        return row;
    }
}
