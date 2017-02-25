package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.TestUtils.inMemoryDataSource

/**
 *
 */
class SchemaTest extends Specification {

    DataSource dataSource

    @DbUnit(schema = "otherSchema")
    def content = {
        User(id: 1, name: 'janbols')
    }

    def setup() {
        dataSource = inMemoryDataSource()
        def sql = new Sql(dataSource)
        sql.execute("CREATE SCHEMA if not exists otherSchema")
        sql.execute("CREATE TABLE otherSchema.User(id INT PRIMARY KEY, name VARCHAR(255))")
    }

    def cleanup() {
        new Sql(dataSource).execute("drop table otherSchema.User")
        new Sql(dataSource).execute("drop schema otherSchema")
    }

    def "dbUnit fills the table in the correct schema when specifying it in the annotation"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from otherSchema.User where name = 'janbols'")

        then:
        result.id == 1
    }
}
