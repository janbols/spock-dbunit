package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Shared
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.SpecUtils.*

/**
  * Specification showing that DbUnit uses the datasource in a shared datasource field
  */
class SharedDatasourceSpecification extends Specification{

    @Shared DataSource dataSource

    @DbUnit def content =  {
        User(id: 1, name: 'janbols')
    }



    def setupSpec(){
        dataSource = inMemoryDataSource()
        dataSource?.with {createUserTable(it)}
    }

    def cleanupSpec() {
        dataSource?.with {dropUserTable(it)}
    }

    def "selecting from the User table returns the user"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        result.id == 1
    }




}
