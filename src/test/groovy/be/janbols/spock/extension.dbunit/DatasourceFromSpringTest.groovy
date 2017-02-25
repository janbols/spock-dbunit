package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static be.janbols.spock.extension.dbunit.TestUtils.createUserTable
import static be.janbols.spock.extension.dbunit.TestUtils.dropUserTable

/**
  *
  */
@ContextConfiguration(locations='classpath:/spring/context.xml')
class DatasourceFromSpringTest extends Specification{

    @Autowired DataSource dataSource

    @DbUnit def content =  {
        User(id: 1, name: 'janbols')
    }


    def setup(){
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
