package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.TestUtils.*

/**
 *
 */
class ContentPerFeatureTest extends Specification {

    DataSource dataSource

    def setup() {
        dataSource = inMemoryDataSource()
        dataSource?.with {createUserTable(it)}
    }

    def cleanup() {
        dataSource?.with {dropUserTable(it)}
    }


    @DbUnit(content = {
        User(id: 1, name: 'janbols')
    })
    def "Specifying the content as a method level annotation inserts the data in the User table"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        result.id == 1
    }

    @DbUnit(content = {
        User(id: 2, name: 'jefkevermeulen')
    })
    def "Specifying other content as a method level annotation inserts the data in the User table"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'jefkevermeulen'")
        then:
        result.id == 2
    }


}
