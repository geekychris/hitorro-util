package ht.util.json.keys;

import ht.util.core.date.DateResolution;
import ht.util.json.keys.propaccess.Propaccess;

import java.util.Date;

public class DateProperty extends BaseMappingProperty<Date> {
    public DateProperty(String path, String description, DateResolution dr, Date defaultValue) {
        super(new Propaccess(path), description, defaultValue, dr.getJsonDateMapper());
    }

    public DateProperty(Propaccess path, String description, DateResolution dr, Date defaultValue) {
        super(path, description, defaultValue, dr.getJsonDateMapper());
    }

    public DateProperty(String path, String description, Date defaultValue) {
        super(new Propaccess(path), description, defaultValue, DateResolution.json.getJsonDateMapper());
    }

    public DateProperty(Propaccess path, String description, Date defaultValue) {
        super(path, description, defaultValue, DateResolution.json.getJsonDateMapper());
    }
}
