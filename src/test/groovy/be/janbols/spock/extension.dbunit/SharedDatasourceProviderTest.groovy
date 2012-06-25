package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Shared
import spock.lang.Specification

/**
  *
  */
class SharedDatasourceProviderTest extends Specification{

    @Shared DataSource dataSource

    @DbUnit(datasourceProvider = {
        dataSource
    }) def content =  {
        User(id: 1, name: 'janbols')
    }



    def setupSpec(){
        dataSource = new DataSource()
        dataSource.driverClassName = 'org.h2.Driver'
        dataSource.url = 'jdbc:h2:mem:'
        dataSource.username = 'sa'
        dataSource.password= ''
        new Sql(dataSource).execute("CREATE TABLE User(id INT PRIMARY KEY, name VARCHAR(255))")
    }


    def "test"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        result.id == 1
    }




}
