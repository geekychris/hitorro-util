package ht.util.propertykeys;

import ht.util.commandandcontrol.ano.DebugArgAno;
import ht.util.core.date.DateResolution;
import ht.util.core.string.StringUtil;
import ht.util.json.keys.PropertyKeyValidationException;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * Date is represented as a UTC long value rather than readable englishified text User: chris
 */
public class UTCLongDateProperty extends PropertyKey<Date> {
    private Date m_defaultVal;
    private boolean m_mustExist;

    public UTCLongDateProperty(DebugArgAno ano) throws ParseException {
        this(ano.keyName(), ano.description(), ano.mustExist(), DateResolution.Day.parse(ano.defaultValue()));
    }

    public UTCLongDateProperty(String key, String description, boolean mustExist, Date defaultVal) {
        super(key, description);
        m_defaultVal = defaultVal;
        m_mustExist = mustExist;
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

        long l = Long.parseLong(sValue);
        Date bval = new Date(l);

        return bval;
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
