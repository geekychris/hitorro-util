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
package com.hitorro.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.error.ErrorCape;
import com.hitorro.util.core.error.Errors;
import com.hitorro.util.json.JsonInitable;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.Propaccess;

import java.util.function.Predicate;

/**
 * Created by chris on 7/21/17.
 */
public abstract class BaseT implements JsonInitable, ErrorCape {
    public static StringProperty typeKey = new StringProperty("type", "object type", null);
    public static StringProperty nameKey = new StringProperty("name", "", null);
    protected JsonNode node;
    private Errors errors = new Errors();
    private String name;

    @Override
    public boolean init(final JsonNode node) {
        this.name = nameKey.apply(node);
        this.node = node;
        return true;
    }

    public abstract void visit(TypeVisitor visitor, Predicate<BaseT> filter, Propaccess path);

    public JsonNode getMetaNode() {
        return node;
    }

    public String getName() {
        return name;
    }

    @Override
    public Errors getErrors() {
        return errors;
    }
}
