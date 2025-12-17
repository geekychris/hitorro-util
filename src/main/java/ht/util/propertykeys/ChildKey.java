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
package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.core.params.HTProperties;
import ht.util.json.keys.PropertyKeyValidationException;

import java.util.List;
import java.util.Map;

/**
 * Get the child keys as a listFiles of strings
 *
 * @author chris
 */
public class ChildKey extends PropertyKey<List<String>> {
    public ChildKey(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
    }

    public ChildKey(String key, String description) {
        super(key, description);
    }

    @Override
    public String getPropertyType() {
        return "List";
    }

    public List<String> getList(Map<String, String> map) {
        return HTProperties.getProperties().getChildKeys(this.m_key);
    }

    @Override
    public List<String> apply(Map<String, String> map) {
        return getList(map);
    }

    @Override
    protected void validate(String sVal) throws PropertyKeyValidationException {
        //not used.
    }

    public void validate(Map<String, String> map)
            throws PropertyKeyValidationException {
        if (getList(map) == null) {
            throw new PropertyKeyValidationException("List not available ", this.m_key, "<<null>>");
        }
    }

}
