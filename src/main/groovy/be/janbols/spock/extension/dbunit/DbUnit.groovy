package be.janbols.spock.extension.dbunit;

import org.spockframework.runtime.extension.ExtensionAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
          @Target(ElementType.FIELD)
          @ExtensionAnnotation(DbUnitExtension.class)
public @interface DbUnit {
    Class<? extends Closure> value();
}
