package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.TestUtils.*

/**
 *
 */
class StaticContentTest extends Specification {

    DataSource dataSource

    @DbUnit
    def content = DbData.userData

    def setup() {
        dataSource = inMemoryDataSource()
        dataSource?.with {createUserTable(it)}
    }

    def cleanup() {
        dataSource?.with {dropUserTable(it)}
    }

    def "selecting from the User table returns the user"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        result.id == 1
    }


}


class DbData {
    static def userData = { User(id: 1, name: 'janbols') }
}