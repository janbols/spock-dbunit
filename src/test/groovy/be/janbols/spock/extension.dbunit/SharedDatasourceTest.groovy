package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Shared
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.TestUtils.createDataSource

/**
  *
  */
class SharedDatasourceTest extends Specification{

    @Shared DataSource dataSource

    @DbUnit def content =  {
        User(id: 1, name: 'janbols')
    }



    def setupSpec(){
        dataSource = createDataSource()
        new Sql(dataSource).execute("CREATE TABLE User(id INT PRIMARY KEY, name VARCHAR(255))")
    }

    def cleanupSpec() {
        new Sql(dataSource).execute("drop table User")
    }

    def "test"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        result.id == 1
    }




}
