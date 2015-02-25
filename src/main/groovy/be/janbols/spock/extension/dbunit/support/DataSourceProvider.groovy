package be.janbols.spock.extension.dbunit.support

import be.janbols.spock.extension.dbunit.DbUnit
import org.spockframework.runtime.extension.ExtensionException
import org.spockframework.runtime.extension.IMethodInvocation
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy

import javax.sql.DataSource

/**
 *  Provides the datasource by calling the DbUnit.datasourceProvider closure or by looking for a shared or unshared DataSource field
 */
class DataSourceProvider {

    private final DbUnit dbUnitAnnotation
    private IMethodInvocation setupSpecInvocation
    private IMethodInvocation setupInvocation

    DataSourceProvider(DbUnit dbUnitAnnotation) {
        this.dbUnitAnnotation = dbUnitAnnotation
    }

    def withSetupSpecInvocation(IMethodInvocation invocation) {
        this.setupSpecInvocation = invocation
    }

    def withSetupInvocation(IMethodInvocation invocation) {
        this.setupInvocation = invocation
    }

    DataSource findDataSource() {
        DataSource result = doFindDataSource(dbUnitAnnotation.datasourceProvider(), setupSpecInvocation)
        if (!result) {
            result = doFindDataSource(dbUnitAnnotation.datasourceProvider(), setupInvocation)
        }

        if (result) {
            result = makeTransactionalAware(result)
        }

        return result
    }



    private static DataSource doFindDataSource(Class<? extends Closure> dataSourceProviderClosureClass, IMethodInvocation invocation) {
        DataSource result


        if (dataSourceProviderClosureClass && Closure.isAssignableFrom(dataSourceProviderClosureClass)) {
            result = findDataSourceByProvider(dataSourceProviderClosureClass, invocation.target.currentInstance)
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

        def currentValue = datasourceFieldInfo?.readValue(iMethodInvocation.target.currentInstance)
        return !currentValue ? datasourceFieldInfo?.readValue(iMethodInvocation.target.sharedInstance) : currentValue
    }

    private static DataSource findDataSourceByProvider(Class<? extends Closure> dataSourceProviderClass, Object target) {
        try {
            def dataSourceClosure = dataSourceProviderClass.newInstance(target, target)
            return dataSourceClosure();
        } catch (Exception e) {
            throw new ExtensionException("Failed to instantiate datasourceProvider in @DbUnit", e);
        }
    }

    /**
     *  use transaction aware data source so changes are visible during the same @Transactional annotated feature
     */
    private static DataSource makeTransactionalAware(DataSource dataSource) {
        if (dataSource instanceof TransactionAwareDataSourceProxy) {
            return dataSource
        } else {
            new TransactionAwareDataSourceProxy(dataSource)
        }
    }


}
