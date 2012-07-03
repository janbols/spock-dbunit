package be.janbols.spock.extension.dbunit

import be.janbols.spock.extension.dbunit.support.DbUnitInterceptor
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

/**
 * Extension for DbUnit annotation.
 */
class DbUnitExtension extends AbstractAnnotationDrivenExtension<DbUnit> {

    private DbUnitInterceptor interceptor;


    @Override
    void visitFieldAnnotation(DbUnit annotation, FieldInfo field) {
        interceptor = new DbUnitInterceptor(field, annotation);
    }


    @Override
    void visitSpec(SpecInfo spec) {
        //Note: spring integration works becuase the SpringExtension is a global extension and is executed before this one.
        interceptor.install(spec)
    }


}







