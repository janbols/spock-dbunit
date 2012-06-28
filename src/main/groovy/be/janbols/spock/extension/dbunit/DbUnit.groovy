package be.janbols.spock.extension.dbunit;

import org.spockframework.runtime.extension.ExtensionAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target
import javax.sql.DataSource;

/**
 * Allows specifying the xml data as a closure field. This avoids the need to link to a separate xml file containing the xml data.
 * For this DbUnit needs to get hold of a  {@link javax.sql.DataSource} either by returning it in the {@link DbUnit#datasourceProvider} method or by adding it as a field in the test class.
 * f.e.
 * <pre>
 class MyDbUnitTest extends Specification{

    DataSource dataSource

    {@literal @}DbUnit
    def content =  {
        User(id: 1, name: 'janbols')
    }
    ...
 }
  </pre>
 * The field containing the sql data can be replaced using the {@link org.dbunit.dataset.ReplacementDataSet}. By default this is done for the keyword [NULL] and [NOW]
 */
@Retention(RetentionPolicy.RUNTIME)
          @Target(ElementType.FIELD)
          @ExtensionAnnotation(DbUnitExtension.class)
public @interface DbUnit {
    /**
     * Allows a {@link javax.sql.DataSource} to be provided inside the given closure. A dataSource can also be provided as a field of the test class. In that case you don't need to specify one in here.
     * @return A closure returning a {@link javax.sql.DataSource}
     */
    Class<? extends Closure<DataSource>> datasourceProvider() default Object;

    /**
     * Allows you to configure the {@link org.dbunit.IDatabaseTester}.
     * This can be used f.e. to specify other {@link org.dbunit.operation.DatabaseOperation} for setup and teardown than the defaults.
     * This also allows you to specify the schema used or add a an operation listener.
     * Finally it also allows you to specify replacements as the database tester is initially setup with a {@link org.dbunit.dataset.ReplacementDataSet}.
     * @return A closure with a IDatabaseTester as input argument configured with the data specified on the accompanying field.
     * @see org.dbunit.IDatabaseTester
     * @see org.dbunit.operation.DatabaseOperation
     * @see org.dbunit.AbstractDatabaseTester#setUpOperation
     * @see org.dbunit.AbstractDatabaseTester#tearDownOperation
     * @see org.dbunit.dataset.ReplacementDataSet
     */
    Class<? extends Closure> configure() default Object;

}
