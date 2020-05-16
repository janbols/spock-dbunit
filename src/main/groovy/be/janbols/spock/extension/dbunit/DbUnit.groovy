package be.janbols.spock.extension.dbunit

import org.spockframework.runtime.extension.ExtensionAnnotation

import javax.sql.DataSource
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Allows specifying the xml data as a closure field. This avoids the need to link to a separate xml file containing the xml data.
 * For this DbUnit needs to get hold of a  {@link javax.sql.DataSource} either by returning it in the {@link DbUnit#datasourceProvider} method or by adding it as a field in the specification.
 * f.e.
 * <pre>
 class MyDbUnitTest extends Specification{

    DataSource dataSource

    {@literal @}DbUnit
    def content =  {
        User(id: 1, name: 'jackdaniels', createdOn: '[NOW]')
    }
    ...
 }
  </pre>
 * The values for the collumns in the sql data are replaced using the {@link org.dbunit.dataset.ReplacementDataSet}. By default this is done for the keyword [NULL] and [NOW]
 */
@Retention(RetentionPolicy.RUNTIME)
          @Target([ElementType.FIELD, ElementType.METHOD])
          @ExtensionAnnotation(DbUnitExtension.class)
public @interface DbUnit {
    /**
     * Allows a {@link javax.sql.DataSource} to be provided inside the given closure. A dataSource can also be provided as a field of the test class. In that case you don't need to specify one in here.
     * @return A closure returning a {@link javax.sql.DataSource}
     */
    Class<? extends Closure<DataSource>> datasourceProvider() default Object.class;

    /**
     * Allows you to configure the {@link org.dbunit.IDatabaseTester} passed as the closure's argument.
     * This can be used f.e. to specify other {@link org.dbunit.operation.DatabaseOperation} for setup and teardown than the defaults.
     * This also allows you to specify the schema used or add a an operation listener.
     * Finally it allows you to specify replacements for the sql values you specified as the database tester is initially set up with a {@link org.dbunit.dataset.ReplacementDataSet}.
     * @return A closure with a IDatabaseTester as input argument configured with the data specified on the accompanying field.
     * @see org.dbunit.IDatabaseTester
     * @see org.dbunit.operation.DatabaseOperation
     * @see org.dbunit.AbstractDatabaseTester#setUpOperation
     * @see org.dbunit.AbstractDatabaseTester#tearDownOperation
     * @see org.dbunit.dataset.ReplacementDataSet
     */
    Class<? extends Closure> configure() default Object.class;

    /**
     * Name of the schema to use.
     * @return
     */
    String schema() default "";

    /**
     * Optional Closure containing the content of the database. This can only be used in a DbUnit annotation on a feature.
     * For DbUnit annotation on a field, the field itself is expected to contain the content.
     * f.e.
     * <pre>
     class MyDbUnitTest extends Specification{

         {@literal @}DbUnit(content = {
            User(id: 1, name: 'janbols')
         })
         def "feature with specific databasse content"() {
            ...
         }

     }
     </pre>
     * @return
     */
    Class<? extends Closure> content() default Object.class;

    /**
     * Enables "column sensing" feature of DBUnit,
     * where the list of columns for a table can vary - it is no longer deducted from the first row.
     */
    boolean columnSensing() default false;
}
