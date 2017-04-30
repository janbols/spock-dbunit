package be.janbols.spock.extension.dbunit.support

import be.janbols.spock.extension.dbunit.DbUnit
import org.dbunit.DataSourceDatabaseTester
import org.dbunit.IDatabaseTester
import org.dbunit.database.IDatabaseConnection
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.FieldInfo

/**
 *  Interceptor for setup, feature and cleanup methods for DbUnit
 */
class DbUnitInterceptor extends AbstractMethodInterceptor {

    private IDatabaseTester tester
    private final DbUnit dbUnitAnnotation

    private final DataSourceProvider dataSourceProvider
    private final DataSetProvider dataSetProvider

    DbUnitInterceptor(FieldInfo dataFieldInfo, DbUnit dbUnitAnnotation) {
        assert dataFieldInfo
        assert dbUnitAnnotation
        this.dbUnitAnnotation = dbUnitAnnotation
        this.dataSetProvider = new DataSetProvider(dbUnitAnnotation, dataFieldInfo)
        this.dataSourceProvider = new DataSourceProvider(dbUnitAnnotation)
    }

    DbUnitInterceptor(FeatureInfo featureInfo, DbUnit dbUnitAnnotation) {
        assert featureInfo
        assert dbUnitAnnotation
        this.dbUnitAnnotation = dbUnitAnnotation
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
    }

    volatile IDatabaseConnection currentConnection = null

    @Override
    void interceptFeatureMethod(IMethodInvocation invocation) throws Throwable {
        //after setup to allow datasource setup
        def dataSource = dataSourceProvider.findDataSource()
        if (!dataSource) {
            throw new ExtensionException("Failed to find a javax.sql.DataSource. Specify one as a field or provide one using @DbUnit.datasourceProvider")
        }

        def dataSet = dataSetProvider.findDataSet(invocation.instance)
        if (!dataSet) {
            throw new ExtensionException("Failed to find a the data set. Specify one as a DbUnit-annotated field or provide one using @DbUnit.content")
        }

        //override default behaviour of DataSourceDatabaseTester to always create new connections.
        tester = new DataSourceDatabaseTester(dataSource, dbUnitAnnotation.schema()) {
            @Override
            IDatabaseConnection getConnection() throws Exception {
                if (!currentConnection || currentConnection.connection.isClosed()) {
                    currentConnection = super.connection
                }
                return currentConnection
            }

            @Override
            void closeConnection(IDatabaseConnection connection) throws Exception {
                super.closeConnection(connection)
                currentConnection = null
            }
        }
        tester.dataSet = dataSet
        configureTester(tester, invocation)
        tester.onSetup()

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

}
