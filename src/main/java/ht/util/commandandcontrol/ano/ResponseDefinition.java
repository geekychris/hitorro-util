package ht.util.commandandcontrol.ano;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseDefinition {
    String command();

    String rowname();

    RespColumn[] columns();

    ColumnGroup[] groups() default {};
}
