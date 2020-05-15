package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Shared
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.SpecUtils.createUserTable
import static be.janbols.spock.extension.dbunit.SpecUtils.dropUserTable
import static be.janbols.spock.extension.dbunit.SpecUtils.inMemoryDataSource

/**
 * When {@link DbUnit#columnSensing()} is true,
 * extra columns in rows after the first one are not ignored.
 */
class ColumnSensingSpecification extends Specification {
    @Shared
    DataSource dataSource

    @DbUnit(columnSensing = true)
    def content = {
        User(id: 1, /* The entry for name is intentionally missing */)
        User(id: 2, name: 'joe')
    }

    def setupSpec() {
        dataSource = inMemoryDataSource()
        dataSource?.with { createUserTable(it) }
    }

    def cleanupSpec() {
        dataSource?.with { dropUserTable(it) }
    }

    def "value for name column is is inserted"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'joe'")
        then:
        result.id == 2
    }
}
