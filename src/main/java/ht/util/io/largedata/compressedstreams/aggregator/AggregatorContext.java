package ht.util.io.largedata.compressedstreams.aggregator;

import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.ResponseShape;

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
