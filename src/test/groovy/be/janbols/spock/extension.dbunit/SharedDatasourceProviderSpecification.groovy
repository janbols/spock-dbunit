package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Shared
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.SpecUtils.*

/**
  * Specification showing that DbUnit uses the datasource that is specified in the DbUnit#datasourceProvider closure referencing a shared datasource
  */
class SharedDatasourceProviderSpecification extends Specification{

    @Shared DataSource dataSource

    @DbUnit(datasourceProvider = {
        dataSource
    })
    def content =  {
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
