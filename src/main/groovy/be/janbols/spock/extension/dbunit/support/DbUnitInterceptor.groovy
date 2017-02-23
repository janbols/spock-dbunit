package be.janbols.spock.extension.dbunit.support

import be.janbols.spock.extension.dbunit.DbUnit
import groovy.transform.InheritConstructors
import groovy.xml.MarkupBuilder
import org.dbunit.DataSourceDatabaseTester
import org.dbunit.IDatabaseTester
import org.dbunit.dataset.DataSetException
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import org.spockframework.runtime.model.SpecInfo

/**
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

        String xml = getDataSetXml(xmlDataFieldInfo, invocation.instance)

        tester = new DataSourceDatabaseTester(dataSource, dbUnitAnnotation.schema())
        tester.dataSet = getDataSet(new StringReader(xml))
        configureTester(tester, invocation)
        tester.onSetup()
    }



    private void configureTester(IDatabaseTester tester, IMethodInvocation invocation) {
        def configureClosureClass = dbUnitAnnotation.configure()
        if (configureClosureClass && Closure.isAssignableFrom(configureClosureClass)) {
            try {
                def configureClosure = configureClosureClass.newInstance(invocation.instance, tester)
                configureClosure(tester);
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




    void install(SpecInfo spec) {
        spec.addSetupInterceptor this
        spec.addSetupSpecInterceptor this
        spec.addCleanupInterceptor this
    }



    private ReplacementDataSet getDataSet(def input) throws DataSetException {
        def dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(input))
        dataSet.addReplacementObject("[NULL]", null);
        dataSet.addReplacementObject("[NOW]", new Date());
        return dataSet;
    }


}
