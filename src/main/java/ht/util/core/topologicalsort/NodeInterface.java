package ht.util.core.topologicalsort;

/**
 * Copyright (c) HiTorro 2003-2008, Inc.
 * <p/>
 * User: chris Date: Dec 23, 2008 Time: 1:48:16 PM
 * <p/>
 * Description: Interface that caller must implement for all nodes provided to be sorted.  This interface is used to
 * describe its identity and its dependencies so that the sorter can perform its internal book keeping.
 */
public interface NodeInterface<N extends NodeInterface> {
    /**
     * Some identifying key to represent this node
     *
     * @return
     */
    Object getKey();

    /**
     * array of nodes that this node is dependent on.
     *
     * @return
     */
    N[] getDependentNodes();
}
