/*
    Copyright (c) 2003 - present HiTorro All rights reserved.

*/

package ht.util.typesystem.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 23, 2006 Time: 6:07:53 PM
 */
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
