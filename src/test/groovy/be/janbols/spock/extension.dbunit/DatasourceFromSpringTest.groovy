package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

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
        new Sql(dataSource).execute("CREATE TABLE User(id INT PRIMARY KEY, name VARCHAR(255))")
    }

    def cleanup() {
        new Sql(dataSource).execute("drop table User")
    }

    def "test"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        result.id == 1
    }







}
