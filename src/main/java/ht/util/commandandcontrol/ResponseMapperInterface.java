package ht.util.commandandcontrol;

/**
 *
 */
public interface ResponseMapperInterface<T> {
    /**
     * Start of response by setting up the response shape.
     *
     * @param response
     */
    void setupResponseShape(Response response);

    void mapResponse(Response response, T object);

    ResponseShape getResponseShape();
}
