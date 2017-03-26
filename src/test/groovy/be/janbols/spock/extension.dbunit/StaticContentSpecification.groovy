package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.SpecUtils.*

/**
 * Specification showing how you can leverage closures specified as static content for easier reuse
 */
class StaticContentSpecification extends Specification {

    DataSource dataSource

    //content field references a static closure specified in another class
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

/**
 * Class containing reusable closures of db content
 */
class DbData {
    static def userData = { User(id: 1, name: 'janbols') }
}