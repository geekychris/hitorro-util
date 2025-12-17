package ht.util.commandandcontrol;

/**
 * wrapper that allows one to construct a row that contains repeating fields (jagged data).
 *
 * @author chris
 */
public abstract class MultiRowResponse {
    protected ResponseShape shape;
    protected RenderingContainer containers[];

    @SuppressWarnings("unchecked")
    public MultiRowResponse(int columns, ResponseShape r) {
        shape = r;
    }

    public void addTuple(int offset, Object... elems) {
        addTupleArray(offset, elems);
    }

    public void setRenderingRow(RenderingContainer... containers) {
        this.containers = containers;
    }

    public abstract void addTupleArray(int offset, Object elems[]);

    public abstract void addThrowable(int column, Throwable t, int stackDepth, int startFrom);

    public abstract void clear();

    public abstract boolean add(int index, Object addMe);

    /**
     * Process the contents of this multi row response into a normal response object.
     *
     * @param response
     */
    public abstract void addToResponse(Response response);
}
