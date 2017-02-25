package be.janbols.spock.extension.dbunit

import be.janbols.spock.extension.dbunit.support.DbUnitFeatureInterceptor
import be.janbols.spock.extension.dbunit.support.DbUnitFieldInterceptor
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

/**
 * Extension for DbUnit annotation.
 */
class DbUnitExtension extends AbstractAnnotationDrivenExtension<DbUnit> {

    private DbUnitFieldInterceptor fieldInterceptor;
    private List<DbUnitFeatureInterceptor> featureInterceptors = [];


    @Override
    void visitFieldAnnotation(DbUnit annotation, FieldInfo field) {
        if( fieldInterceptor ) throw new ExtensionException("Expected maximum one field annotated with @DbUnit")
        fieldInterceptor = new DbUnitFieldInterceptor(field, annotation)
    }

    @Override
    void visitFeatureAnnotation(DbUnit annotation, FeatureInfo feature) {
        featureInterceptors << new DbUnitFeatureInterceptor(feature, annotation)
    }

    @Override
    void visitSpec(SpecInfo spec) {
        //Note: spring integration works because the SpringExtension is a global extension and is executed before this one.
        fieldInterceptor?.install(spec)
        featureInterceptors.each {it.install(spec)}
    }


}







