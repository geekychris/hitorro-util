/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.typesystem.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 25, 2006 Time: 9:17:28 AM
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewClassReference {
    /**
     * The standard view for displaying the objects in a listFiles.
     */
    String ListView = "listFiles";

    /**
     * The standard view for displaying the objects in a full editor.
     */
    String EditView = "edit";

    /**
     * The view for object inspection.
     */
    String InspectView = "inspect";

    /**
     * A restricted view for editing some of the object by the public.
     */
    String PublicEditView = "publicEdit";

    /**
     * The standard view for displaying fields of the object for search.
     */
    String SearchView = "search";

    /**
     * The name of this view. The name of the view is preferably one of the constants defined in this class.
     *
     * @return the view name
     */
    String name();

    /**
     * The class that holds the view definition. Note that at the present time these view classes will not be
     * instantiated or executed.
     *
     * @return the view class.
     */
    Class viewClass();
}
