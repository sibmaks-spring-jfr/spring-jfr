package io.github.sibmaks.spring.jfr.core;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JavaFlightRecorderProperty {
    String key();

    String value();

    boolean matchIfMissing() default false;
}
