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
public @interface UiProperties {
    // constants defining allowed display types for fields
    // the values of these must test the jwcid constants in TypedField.html
    String BooleanDisplay = "booleanField";
    String CalendarDisplay = "calendarField";
    String ContentsDisplay = "contentsField";
    String DateDisplay = "dateField";
    String DoubleFieldDisplay = "textFieldDouble";
    String DetailListDisplay = "detailList";
    String FileUploadDisplay = "fileUpload";
    String IntFieldDisplay = "textFieldInt";
    String ReadOnlyDisplay = "readOnly";
    String SelectListDisplay = "selectList";
    String VersionableObjectDisplay = "systemObject";
    String TextAreaDisplay = "textArea";
    String TextFieldDisplay = "textField";

    /**
     * User-friendly name of the field, for labels, etc.
     *
     * @return the user-friendly name of the field
     */
    String displayName();

    /**
     * The display type of the field. Will be one of the *Display constants of this class.
     *
     * @return the display type
     */
    String displayType() default TextFieldDisplay;

    /**
     * The display order of the field. In an editor the fields will be in ascending order.
     *
     * @return the ordering
     */
    int order() default 9999;

    /**
     * The width of the text field. This is in units of "columns".
     *
     * @return the field width in columns
     */
    int width() default 80;

    /**
     * The display height of the field. Used by text areas.
     *
     * @return the field height in "rows".
     */
    int height() default 15;

    /**
     * The format for the value. Used for text displays.
     *
     * @return the format
     */
    String format() default "";
}
