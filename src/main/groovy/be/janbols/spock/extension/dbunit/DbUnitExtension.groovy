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

import groovy.xml.MarkupBuilder
import org.joda.time.LocalDate

/**
  *
  */
class DbUnitExtension extends AbstractAnnotationDrivenExtension<DbUnit>{

    private DbUnitInterceptor interceptor;


    @Override
    void visitFieldAnnotation(DbUnit annotation, FieldInfo field) {
        interceptor = new DbUnitInterceptor(dbUnitAnnotation: annotation, xmlDataFieldInfo: field);
    }


    @Override
    void visitSpec(SpecInfo spec) {
        interceptor.install(spec)
    }





}



@InheritConstructors
class DbUnitInterceptor extends AbstractMethodInterceptor  {

    private DbUnitSetup dbUnitSetup
    FieldInfo xmlDataFieldInfo
    DbUnit dbUnitAnnotation
    DataSource sharedDataSource


    @Override
    void interceptSetupSpecMethod(IMethodInvocation invocation) {
        invocation.proceed()
        sharedDataSource = findDataSource(dbUnitAnnotation, invocation)
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) {
        invocation.proceed()

        //after setup
        def dataSource = sharedDataSource? sharedDataSource: findDataSource(dbUnitAnnotation, invocation)
        if (!dataSource) {
           throw new ExtensionException("Failed to find a javax.sql.DataSource. Specify one as a field or provide one using @DbUnit.datasourceProvider")
        }
        dbUnitSetup = new DbUnitSetup(dataSource)

        String xml = getDataSetXml(xmlDataFieldInfo, invocation.target)
        dbUnitSetup.setup(xml)
    }

    private static String getDataSetXml(FieldInfo xmlDataFieldInfo, Object target) {
        def xmlDataClosure = xmlDataFieldInfo.readValue(target)
        def xmlWriter = new StringWriter()
        def builder = new MarkupBuilder(xmlWriter)
        builder.dataset(xmlDataClosure)
        xmlWriter as String
    }

    private static DataSource findDataSource(DbUnit dbUnitAnnotation, IMethodInvocation invocation) {
        DataSource result

        if (Closure.isAssignableFrom(dbUnitAnnotation.datasourceProvider())){
            result = findDataSourceByProvider(dbUnitAnnotation.datasourceProvider(), invocation.target)
        }

        if (!result) {
            result = findDataSourceByField(invocation)
        }
        return result
    }

    private static DataSource findDataSourceByField(IMethodInvocation iMethodInvocation) {
        def datasourceFieldInfo = iMethodInvocation.spec.allFields.find {
            return DataSource.isAssignableFrom(it.reflection.type)
        }
        return datasourceFieldInfo?.readValue(iMethodInvocation.target)
    }

    private static DataSource findDataSourceByProvider(Class<? extends Closure> dataSourceProviderClass, Object target) {
        try {
            def dataSourceClosure = dataSourceProviderClass.newInstance(target, target)
            return dataSourceClosure();
        } catch (Exception e) {
            throw new ExtensionException("Failed to instantiate datasourceProvider in @DbUnit", e);
        }
    }


    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) {
        dbUnitSetup?.cleanup()
        invocation.proceed()
    }

    @Override
    void install(SpecInfo spec) {
        spec.setupMethod.addInterceptor this
        spec.setupSpecMethod.addInterceptor this
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

