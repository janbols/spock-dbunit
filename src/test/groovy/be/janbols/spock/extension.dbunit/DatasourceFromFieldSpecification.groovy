package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Specification

import static SpecUtils.inMemoryDataSource

/**
 * Specification showing that DbUnit uses the datasource that is specified as a field
 */
class DatasourceFromFieldSpecification extends Specification {

    DataSource dataSource

    @DbUnit
    def content = {
        User(id: 1, name: 'janbols')
        User(id: 2, name: 'bluepoet', ip: '127.0.0.1')

        Other_User(id: 1, name: 'bluepoet', ip: '127.0.0.1')
        Other_User(id: 2, name: 'janbols')
        Other_User(id: 3, name: 'tester', ip: '1.2.3.4')
    }

    def setup() {
        dataSource = inMemoryDataSource()
        new Sql(dataSource).execute("CREATE TABLE User(id INT PRIMARY KEY, name VARCHAR(255), ip VARCHAR(50))")
        new Sql(dataSource).execute("CREATE TABLE Other_User(id INT PRIMARY KEY, name VARCHAR(255), ip VARCHAR(50))")
    }

    def cleanup() {
        new Sql(dataSource).execute("drop table User")
        new Sql(dataSource).execute("drop table Other_User")
    }

    def "selecting from the User table returns the user"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        result.id == 1
    }

    def "Check the data without putting the data of the ip field first."() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'bluepoet'")

        then:
        result.id == 2
        result.ip == null
    }

    def "Put the data of the ip field first and check the data."() {
        when:
        def result = new Sql(dataSource).rows("select * from Other_User")

        then:
        result == [
                [ID: 1, NAME: 'bluepoet', IP: '127.0.0.1'],
                [ID: 2, NAME: 'janbols', IP: null],
                [ID: 3, NAME: 'tester', IP: '1.2.3.4']
        ]
    }
}
