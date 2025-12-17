package ht.util.commandandcontrol.responsemappings;

import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.ResponseMapperInterface;
import ht.util.commandandcontrol.ResponseShape;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;
import ht.util.core.GenericKeyValue;

/**
 *
 */
public class KeyValuePairMapping implements ResponseMapperInterface<GenericKeyValue<Object, Object>> {
    @ResponseDefinition(command = "command",
            rowname = "row",
            columns = {@RespColumn(name = "key", lName = "key"),
                    @RespColumn(name = "value", lName = "value")})
    private ResponseShape keyShape = new ResponseShape();

    public KeyValuePairMapping() {

    }

    public void setupResponseShape(Response response) {
        response.setResponseShape(getResponseShape());
    }

    @Override
    public void mapResponse(final Response response, final GenericKeyValue<Object, Object> object) {
        response.addRow(object.getKey(), object.getValue());
    }

    @Override
    public ResponseShape getResponseShape() {
        return keyShape;
    }
}

