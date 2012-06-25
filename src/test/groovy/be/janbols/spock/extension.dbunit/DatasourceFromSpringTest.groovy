package be.janbols.spock.extension.dbunit

import be.janbols.spock.extension.dbunit.DbUnit
import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import spock.lang.Specification
import org.springframework.test.context.ContextConfiguration
import org.springframework.beans.factory.annotation.Autowired

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

    def "test"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        result.id == 1
    }




}
