package be.janbols.spock.extension.dbunit

import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import javax.sql.DataSource
import org.dbunit.DataSourceDatabaseTester
import org.dbunit.operation.DatabaseOperation
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.DataSetException
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import org.spockframework.runtime.model.SpecInfo

import org.spockframework.runtime.model.FieldInfo
import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.IMethodInvocation
import java.lang.reflect.Field
import groovy.xml.MarkupBuilderHelper
import groovy.xml.MarkupBuilder
import org.joda.time.LocalDate

/**
  *
  */
class DbUnitExtension extends AbstractAnnotationDrivenExtension<DbUnit>{

    private MyInterceptor interceptor;


    @Override
    void visitFieldAnnotation(DbUnit annotation, FieldInfo field) {
        interceptor = new MyInterceptor(dataSourceClosureClass: annotation.value(), xmlDataFieldInfo: field);
    }


    @Override
    void visitSpec(SpecInfo spec) {
        interceptor.install(spec)
    }





}



@InheritConstructors
class MyInterceptor extends AbstractMethodInterceptor  {



    private DbUnitSetup dbUnitSetup
    FieldInfo xmlDataFieldInfo
    Class<? extends Closure> dataSourceClosureClass


    private Closure createCondition(Class<? extends Closure> clazz, Object target) {
        try {
            def closure = clazz.newInstance(target, target)
            return closure;
        } catch (Exception e) {
            throw new ExtensionException("Failed to instantiate @DbUnit", e);
        }
    }




    @Override
    void interceptSetupMethod(IMethodInvocation invocation) {

        invocation.proceed()
        def dataSourceClosure = createCondition(dataSourceClosureClass, invocation.target)
        def dataSource = dataSourceClosure()
        def xmlDataClosure = xmlDataFieldInfo.readValue(invocation.target)
        def xmlWriter = new StringWriter()
        def builder = new MarkupBuilder(xmlWriter)
        builder.dataset(xmlDataClosure)
        dbUnitSetup = new DbUnitSetup(dataSource)
        dbUnitSetup.setup(xmlWriter as String)
    }

    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) {
        dbUnitSetup.cleanup()
        invocation.proceed()
    }

    @Override
    void install(SpecInfo spec) {
        spec.setupMethod.addInterceptor this
        spec.cleanupMethod.addInterceptor this

    }





}


class DbUnitSetup {
    private final DataSource dataSource
    DataSourceDatabaseTester tester

    DbUnitSetup(DataSource dataSource) {
        this.dataSource = new TransactionAwareDataSourceProxy(dataSource)
    }

    void setup(String xmlData, String schema = null) {
        tester = new DataSourceDatabaseTester(dataSource, schema)
        tester.setUpOperation = DatabaseOperation.CLEAN_INSERT
        tester.tearDownOperation = DatabaseOperation.DELETE_ALL
        tester.dataSet = getDataSet(new StringReader(xmlData))
        tester.onSetup()
    }

    void cleanup() {
        if (tester) {
            tester.onTearDown()
        }
    }



    private IDataSet getDataSet(def input) throws DataSetException {
        def dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(input))
        dataSet.addReplacementObject("[NULL]", null);
        dataSet.addReplacementObject("[NOW]", LocalDate.now().toDate());
        dataSet.addReplacementObject("[TOMORROW]", LocalDate.now().plusDays(1).toDate());
        return dataSet;
    }

}

