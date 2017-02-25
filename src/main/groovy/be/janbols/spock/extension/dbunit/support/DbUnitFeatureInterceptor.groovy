package be.janbols.spock.extension.dbunit.support

import be.janbols.spock.extension.dbunit.DbUnit
import org.dbunit.DataSourceDatabaseTester
import org.dbunit.IDatabaseTester
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo

/**
 *  Interceptor for setup and cleanup methods for DbUnit
 */
class DbUnitFeatureInterceptor extends AbstractMethodInterceptor {

    private DataSourceDatabaseTester tester
    private final FeatureInfo featureInfo
    private final DbUnit dbUnitAnnotation

    private final DataSourceProvider dataSourceProvider
    private final DataSetProvider dataSetProvider


    DbUnitFeatureInterceptor(FeatureInfo featureInfo, DbUnit dbUnitAnnotation) {
        this.dbUnitAnnotation = dbUnitAnnotation
        this.featureInfo = featureInfo
        this.dataSetProvider = new DataSetProvider(dbUnitAnnotation, null)
        this.dataSourceProvider = new DataSourceProvider(dbUnitAnnotation)
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


        if (!featureInfo || featureInfo.featureMethod == invocation.feature.featureMethod) {
            //after setup to allow datasource setup
            def dataSource = dataSourceProvider.findDataSource()
            if (!dataSource) {
                throw new ExtensionException("Failed to find a javax.sql.DataSource. Specify one as a field or provide one using @DbUnit.datasourceProvider")
            }

            def dataSet = dataSetProvider.findDataSet(invocation.instance)
            if (!dataSet) {
                throw new ExtensionException("Failed to find a the data set. Specify one as a DbUnit-annotated field or provide one using @DbUnit.content")
            }

            tester = new DataSourceDatabaseTester(dataSource, dbUnitAnnotation.schema())
            tester.dataSet = dataSet
            configureTester(tester, invocation)
            tester.onSetup()
        }
    }

    @Override
    void interceptFeatureMethod(IMethodInvocation invocation) throws Throwable {
        invocation.proceed()
        
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


}
