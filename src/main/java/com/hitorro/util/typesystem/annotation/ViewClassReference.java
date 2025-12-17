/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.hitorro.util.typesystem.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
