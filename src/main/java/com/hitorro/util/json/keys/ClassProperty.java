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
package com.hitorro.util.json.keys;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.json.keys.propaccess.Propaccess;

public class ClassProperty extends BaseMappingProperty<Class> {
    public ClassProperty(String path, String description, Class defaultValue) {
        super(new Propaccess(path), description, defaultValue, ClassPropertyMapper.me);
    }

    public ClassProperty(Propaccess path, String description, Class defaultValue) {
        super(path, description, defaultValue, ClassPropertyMapper.me);
    }
}

class ClassPropertyMapper implements Mapper<JsonNode, Class> {
    public static ClassPropertyMapper me = new ClassPropertyMapper();

    ClassPropertyMapper() {

    }

    public Class apply(JsonNode jsonNodes) {
        String sValue = jsonNodes.textValue();

        return ClassUtil.getClassForName(sValue, null);
    }

}