package be.janbols.spock.extension.dbunit

import be.janbols.spock.extension.dbunit.support.DbUnitInterceptor
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

/**
 * Extension for DbUnit annotation.
 */
class DbUnitExtension extends AbstractAnnotationDrivenExtension<DbUnit> {

    private DbUnitInterceptor fieldInterceptor

    @Override
    void visitFieldAnnotation(DbUnit annotation, FieldInfo field) {
        if (fieldInterceptor) throw new ExtensionException("Expected maximum one field annotated with @DbUnit")
        if (annotation.content() != Object) throw new ExtensionException("Specifying the content of the database is only supported for annotations on a feature")
        fieldInterceptor = new DbUnitInterceptor(field, annotation)
    }

    @Override
    void visitFeatureAnnotation(DbUnit annotation, FeatureInfo feature) {
        def interceptor = new DbUnitInterceptor(feature, annotation)
        feature.spec.addSetupSpecInterceptor(interceptor)
        feature.spec.addSetupInterceptor(interceptor)
        feature.featureMethod.addInterceptor(interceptor)
        feature.spec.addCleanupInterceptor(interceptor)

    }

    @Override
    void visitSpec(SpecInfo spec) {
        //Note: spring integration works because the SpringExtension is a global extension and is executed before this one.
        spec.addSetupSpecInterceptor(fieldInterceptor)
        spec.addSetupInterceptor(fieldInterceptor)
        //add field interceptor only to those features that aren't annotated yet by any DbUnit annotation
        spec.features
                .findAll { f -> !f.featureMethod.reflection.annotations*.annotationType().contains(DbUnit) }
                .each { f -> f.featureMethod.addInterceptor(fieldInterceptor)
        }
        spec.addCleanupInterceptor(fieldInterceptor)
    }


}







