package ht.util.commandandcontrol.responsemappings;

import ht.util.commandandcontrol.Response;
import ht.util.commandandcontrol.ResponseMapperInterface;
import ht.util.commandandcontrol.ResponseShape;
import ht.util.commandandcontrol.ano.RespColumn;
import ht.util.commandandcontrol.ano.ResponseDefinition;

/**
 *
 */
public class SingleObjectToStringMapping implements ResponseMapperInterface<Object> {
    @ResponseDefinition(command = "command",
            rowname = "row",
            columns = {@RespColumn(name = "value", lName = "value")})
    private ResponseShape keyShape = new ResponseShape();

    public SingleObjectToStringMapping() {

    }

    public void setupResponseShape(Response response) {
        response.setResponseShape(getResponseShape());
    }

    @Override
    public void mapResponse(final Response response, final Object object) {
        response.addRow(object);
    }

    @Override
    public ResponseShape getResponseShape() {
        return keyShape;
    }
}
