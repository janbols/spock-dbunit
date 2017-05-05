package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import org.dbunit.IDatabaseTester
import org.dbunit.database.DatabaseConfig
import org.dbunit.dataset.Column
import org.dbunit.dataset.filter.IColumnFilter
import org.dbunit.operation.DatabaseOperation
import spock.lang.Issue
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.SpecUtils.inMemoryDataSource


@Issue("10")
class Issue10Specification extends Specification {

    DataSource dataSource

    def setup() {
        dataSource = inMemoryDataSource()
        new Sql(dataSource).execute("CREATE TABLE tableWithoutPK(no_key1 VARCHAR(255), no_key2 VARCHAR(255))")
    }

    def cleanup() {
        new Sql(dataSource).execute("drop table tableWithoutPK")
    }


    @DbUnit(configure = { IDatabaseTester it ->
        it.connection.config.setProperty(DatabaseConfig.PROPERTY_PRIMARY_KEY_FILTER, new MyPrimaryKeyFilter())
        it.setUpOperation = DatabaseOperation.REFRESH
        it.tearDownOperation = DatabaseOperation.NONE
    }, content = {
        tableWithoutPK(no_key1: 'value1', no_key2: 'value2')
    })
    def "add value to table without pk"() {
        when:
        def result = new Sql(dataSource).rows("select * from tableWithoutPK")
        then:
        result[0].no_key1 == 'value1'
        result[0].no_key2 == 'value2'
    }

    static class MyPrimaryKeyFilter implements IColumnFilter {
        @Override
        boolean accept(String tableName, Column column) {
            return true
        }
    }

}
