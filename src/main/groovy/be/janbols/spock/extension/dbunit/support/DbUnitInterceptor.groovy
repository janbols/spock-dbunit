package be.janbols.spock.extension.dbunit.support

import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.dbunit.DataSourceDatabaseTester
import org.spockframework.runtime.model.FieldInfo
import be.janbols.spock.extension.dbunit.DbUnit
import javax.sql.DataSource

import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.extension.ExtensionException
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import groovy.xml.MarkupBuilder
import org.spockframework.runtime.model.SpecInfo
import org.dbunit.dataset.IDataSet
import org.dbunit.dataset.DataSetException
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.dbunit.IDatabaseTester

/**
 *  Interceptor for setup and cleanup methods for DbUnit
 *  Interceptor for setup and cleanup methods for DbUnit
 */
@InheritConstructors
class DbUnitInterceptor extends AbstractMethodInterceptor {

    private DataSourceDatabaseTester tester
    private final FieldInfo xmlDataFieldInfo
    private final DbUnit dbUnitAnnotation
    private final DataSourceProvider dataSourceProvider

    DbUnitInterceptor(FieldInfo xmlDataFieldInfo, DbUnit dbUnitAnnotation) {
        this.xmlDataFieldInfo = xmlDataFieldInfo
        this.dbUnitAnnotation = dbUnitAnnotation
        dataSourceProvider = new DataSourceProvider(dbUnitAnnotation)
    }

    @Override
    void interceptSetupSpecMethod(IMethodInvocation invocation) {
        invocation.proceed()
        dataSourceProvider.withSetupSpecInvocation(invocation)
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) {
        invocation.proceed()
        dataSourceProvider.withSetupInvocation(invocation)

        //after setup to allow datasource setup
        def dataSource = dataSourceProvider.findDataSource()
        if (!dataSource) {
            throw new ExtensionException("Failed to find a javax.sql.DataSource. Specify one as a field or provide one using @DbUnit.datasourceProvider")
        }

        String xml = getDataSetXml(xmlDataFieldInfo, invocation.target)

        tester = new DataSourceDatabaseTester(dataSource)
        tester.dataSet = getDataSet(new StringReader(xml))
        Closure testerConfigurer = getTesterConfigurer(tester, invocation)
        if (testerConfigurer) {
            testerConfigurer.call(tester)
        }

        tester.onSetup()
    }



    private Closure getTesterConfigurer(IDatabaseTester tester, IMethodInvocation invocation) {
        def configureClosureClass = dbUnitAnnotation.configure()
        if (configureClosureClass && Closure.isAssignableFrom(configureClosureClass)) {
            try {
                def dataSourceClosure = configureClosureClass.newInstance(invocation.target, tester)
                return dataSourceClosure(tester);
            } catch (Exception e) {
                throw new ExtensionException("Failed to instantiate tester configurer in @DbUnit", e);
            }
        }
    }




    private static String getDataSetXml(FieldInfo xmlDataFieldInfo, Object target) {
        def xmlDataClosure = xmlDataFieldInfo.readValue(target)
        def xmlWriter = new StringWriter()
        def builder = new MarkupBuilder(xmlWriter)
        builder.dataset(xmlDataClosure)
        xmlWriter as String
    }









    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) {
        tester?.onTearDown()
        invocation.proceed()
    }




    @Override
    void install(SpecInfo spec) {
        spec.setupMethod.addInterceptor this
        spec.setupSpecMethod.addInterceptor this
        spec.cleanupMethod.addInterceptor this

    }



    private ReplacementDataSet getDataSet(def input) throws DataSetException {
        def dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(input))
        dataSet.addReplacementObject("[NULL]", null);
        dataSet.addReplacementObject("[NOW]", LocalDateTime.now().toDate());
        return dataSet;
    }


}
