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
import ht.util.core.Log;
import ht.util.core.date.DateResolution;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.PropertyKeyValidationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
public class DateProperty extends PropertyKey<Date> {
    public static final SimpleDateFormat YYMMDD = new SimpleDateFormat("yyMMdd");
    private Date m_defaultVal;
    private boolean m_mustExist;
    private SimpleDateFormat formatter = YYMMDD;

    public DateProperty(DebugArgAno ano) {
        super(ano.keyName(), ano.description());
        if (ano.defaultValue() != null) {
            try {
                m_defaultVal = DateResolution.Day.parse(ano.defaultValue());
            } catch (ParseException e) {
                Log.util.error("Unable to parse date %s %e %e", ano.defaultValue(), e, e);
            }
            m_mustExist = ano.mustExist();
        }
    }

    public DateProperty(String key, String description, boolean mustExist, Date defaultVal) {
        super(key, description);
        m_defaultVal = defaultVal;
        m_mustExist = mustExist;
    }

    public DateProperty(String key, String description, boolean mustExist, Date defaultVal, SimpleDateFormat formatterIn) {
        super(key, description);
        m_defaultVal = defaultVal;
        m_mustExist = mustExist;
        formatter = formatterIn;
    }

    public String getPropertyType() {
        return "Date";
    }

    public Date apply(Map<String, String> map) {
        String sValue = getValueFromConfig(map);
        if (sValue == null) {
            if (m_mustExist) {
                throw new PropertyKeyValidationException("Property missing", this.m_key, "<<null>>");
            }
            return m_defaultVal;
        }
        validate(sValue);
        Date bval = null;
        try {
            // Formatter is not thread safe!
            synchronized (formatter) {
                bval = formatter.parse(sValue);
            }
        } catch (ParseException e) {
            Log.util.error("Exception %s %e", e, e);
        }
        return bval;
    }

    public long getTimeMillis() {
        return getTimeMillis(null);
    }

    public long getTimeMillis(Map<String, String> map) {
        Date bval = apply(map);
        return new Long(bval.getTime());
    }

    public void validate(Map<String, String> map)
            throws PropertyKeyValidationException {
        if (m_mustExist || !StringUtil.nullOrEmptyOrBlankString(getValueFromConfig(map))) {
            super.validate(map);
        }
    }

    protected void validate(String sValue)
            throws PropertyKeyValidationException {
        return;
    }
}
