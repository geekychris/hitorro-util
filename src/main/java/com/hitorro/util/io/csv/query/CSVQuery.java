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
package com.hitorro.util.io.csv.query;

import gnu.trove.map.hash.TObjectIntHashMap;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.iterator.CloseableIterator;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.core.iterator.mappers.BaseMapper;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.csv.ArrayArrayMappingIterator;
import com.hitorro.util.io.csv.CSVIteratorImpl;
import com.hitorro.util.io.csv.ColumnTableMeta;
import com.hitorro.util.json.keys.propaccess.PropaccessError;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class CSVQuery {
    private String cols[];
    private BaseFile sourceFile;
    private CSVTableMeta meta;
    private ColumnTableMeta columnTableMeta;
    private Map<String, BaseMapper> mapAlternatives = new HashMap();
    private HTPredicate<String[]> where = null;
    private TObjectIntHashMap<String> fieldIntMap = new TObjectIntHashMap<String>();

    public static CSVQuery selectFromMeta(BaseFile sourceFile, CSVTableMeta meta, String... cols) {
        CSVQuery q = new CSVQuery();
        q.cols = cols;
        q.sourceFile = sourceFile;
        q.meta = meta;
        for (int i = 0; i < cols.length; i++) {
            q.fieldIntMap.put(cols[i].toLowerCase(), i);
        }
        return q;
    }

    public int getColumnCount() {
        return fieldIntMap.size();
    }

    public ColumnTableMeta getColumnTableMeta() {
        return columnTableMeta;
    }

    public CSVTableMeta getCSVTableMeta() {
        return this.meta;
    }

    public int getIndexOf(String field) {
        return fieldIntMap.get(field.toLowerCase());
    }

    /**
     * Use an alternative me for a field if you dont like the primitive one or the one defined by the meta layer.
     *
     * @param field
     * @param mapper
     */
    public CSVQuery setFieldMapping(String field, BaseMapper mapper) {
        mapAlternatives.put(field.toLowerCase(), mapper);
        return this;
    }

    public CSVQuery where(HTPredicate<String[]> where) {
        this.where = where;
        return this;
    }

    public CloseableIterator<Object[]> execute() throws IOException {
        CSVIteratorImpl impl = new CSVIteratorImpl(sourceFile, "UTF-8");
        JVS jvs = new JVS();
        int indices[] = new int[cols.length];
        Mapper<String, Object> mappers[] = new Mapper[cols.length];
        columnTableMeta = impl.getMeta();
        for (int i = 0; i < cols.length; i++) {
            indices[i] = columnTableMeta.getColumnInt(cols[i]);
            BaseMapper bm = mapAlternatives.get(cols[i].toLowerCase());
            if (bm != null) {
                mappers[i] = bm;
            } else {
                mappers[i] = meta.get(indices[i]).getMapper();
            }
        }
        if (where != null) {

            try {
                jvs.set("cols", StringUtil.mergeWithJoinToken(impl.getColumnNames(), ","));
            } catch (PropaccessError propaccessError) {
                return null;
            }
            where.initFromMap(jvs.getJsonNode());
        }

        ArrayArrayMappingIterator iter = new ArrayArrayMappingIterator(impl, mappers, indices);
        return iter;
    }
}
