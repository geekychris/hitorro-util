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
package com.hitorro.util.io.largedata.compressedstreams.aggregator;

import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Jan 16, 2008 Time: 8:44:36 AM Keeps track of
 * a set of aggregators
 */
public class AggregatorContext {
    private Map<String, Aggregator> map = new HashMap<String, Aggregator>();
    private List<Aggregator> list = new ArrayList<Aggregator>();


    public void add(Aggregator aggr) {
        map.put(aggr.getFileExtension(), aggr);
        list.add(aggr);
    }

    public Aggregator getAggregator(String name) {
        return map.get(name);
    }

    public List<Aggregator> getAggregatorList() {
        return list;
    }

    public void list(Response response) {
        ResponseShape shape = new ResponseShape("AggregateList", "Aggregate");
        shape.addHeader("Name", "Description");
        response.setResponseShape(shape);
        for (Aggregator agr : list) {
            response.addRow(agr.getFileExtension(), agr.getDescription());
        }
    }
}
