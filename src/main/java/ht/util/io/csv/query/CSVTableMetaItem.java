package ht.util.io.csv.query;

import ht.util.core.UtilDataType;
import ht.util.core.iterator.mappers.BaseMapper;

/**
 *
 */
public class CSVTableMetaItem {
    private String field;
    private UtilDataType dt;
    private BaseMapper altMapper;

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public BaseMapper getAlternativeMapper() {
        return this.altMapper;
    }

    public void setAlternativeMapper(BaseMapper mapper) {
        this.altMapper = mapper;
    }

    public UtilDataType getDt() {
        return dt;
    }

    public void setDt(final UtilDataType dt) {
        this.dt = dt;
    }

    public BaseMapper getMapper() {
        if (this.altMapper != null) {
            return this.altMapper;
        }
        return getDt().getFromStringMapper();
    }
}