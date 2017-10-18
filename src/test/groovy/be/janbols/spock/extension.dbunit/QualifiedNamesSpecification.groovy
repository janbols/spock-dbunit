package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import org.dbunit.IDatabaseTester
import org.dbunit.database.DatabaseConfig
import spock.lang.Issue
import spock.lang.Specification

import static SpecUtils.inMemoryDataSource

/**
 * Specification showing how to use qualified table names
 */
@Issue("12")
class QualifiedNamesSpecification extends Specification {

    DataSource dataSource

    @DbUnit( configure = { IDatabaseTester it ->
        it.connection.config.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true)
    })

    def content = {
        'Foo.Bar'(id: 42)
    }

    def setup() {
        dataSource = inMemoryDataSource()
        def sql = new Sql(dataSource)
        sql.execute("CREATE SCHEMA if not exists Foo")
        sql.execute("CREATE TABLE Foo.Bar(id INT PRIMARY KEY)")
    }

    def cleanup() {
        new Sql(dataSource).execute("drop table Foo.Bar")
        new Sql(dataSource).execute("drop schema Foo")
    }

    def "dbUnit fills the table in the correct schema when using qualified names"() {
        when:
        def foobar = new Sql(dataSource).firstRow("select * from Foo.Bar")

        then:
        foobar.id == 42
    }
}