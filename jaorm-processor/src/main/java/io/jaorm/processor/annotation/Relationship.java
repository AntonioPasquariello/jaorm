package io.jaorm.processor.annotation;

import io.jaorm.processor.util.ParameterConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface Relationship {

    RelationshipColumn[] columns();

    @interface RelationshipColumn {

        int NONE = 0;

        String defaultValue() default "";
        ParameterConverter converter() default ParameterConverter.NONE;
        String sourceColumn() default "";
        String targetColumn();
    }
}
