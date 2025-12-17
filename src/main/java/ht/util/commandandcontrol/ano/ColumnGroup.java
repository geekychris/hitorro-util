package ht.util.commandandcontrol.ano;

import ht.util.core.KeyValue;

/**
 * Defines withing a ResponseDefinition which columns participate in a group.  A group defines a subset of columns that
 * act as a tuple. when rendered to xml this means that they get grouped together in a sub element and can support
 * repeated values as a tuple set. This is a cheap way to implement more complex shapes than a simple column row model
 * of output.
 */
public @interface ColumnGroup {
    /**
     * Column index (starting at 0) that indicates the starting column
     *
     * @return
     */
    int start();

    /**
     * Number of columns participating in this group.
     *
     * @return
     */
    int size();

    /**
     * Name of the group that is used in rendering to xml
     *
     * @return
     */
    String name();

    /**
     * Class that is used for rendering the group.
     *
     * @return
     */
    Class groupType() default KeyValue.class;
}
