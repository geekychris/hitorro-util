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
package com.hitorro.util.commandandcontrol.responsemappings;

import com.hitorro.util.commandandcontrol.Response;
import com.hitorro.util.commandandcontrol.ResponseMapperInterface;
import com.hitorro.util.commandandcontrol.ResponseShape;
import com.hitorro.util.commandandcontrol.ano.RespColumn;
import com.hitorro.util.commandandcontrol.ano.ResponseDefinition;

import java.util.Map;

/**
 *
 */
public class MapMapping implements ResponseMapperInterface<Map.Entry<Object, Object>> {
    @ResponseDefinition(command = "command",
            rowname = "row",
            columns = {@RespColumn(name = "key", lName = "key"),
                    @RespColumn(name = "value", lName = "value")})
    private ResponseShape keyShape = new ResponseShape();

    public MapMapping() {

    }

    public void setupResponseShape(Response response) {
        response.setResponseShape(getResponseShape());
    }

    @Override
    public void mapResponse(final Response response, final Map.Entry<Object, Object> object) {
        response.addRow(object.getKey(), object.getValue());
    }

    @Override
    public ResponseShape getResponseShape() {
        return keyShape;
    }
}

