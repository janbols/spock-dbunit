package be.janbols.spock.extension.dbunit

import spock.lang.Specification
import org.springframework.beans.factory.annotation.Autowired
import javax.sql.DataSource
import groovy.xml.MarkupBuilder
import org.h2.Driver
import org.apache.tomcat.jdbc.pool.DataSource
import org.springframework.jdbc.core.JdbcTemplate
import groovy.sql.Sql

/**
  *
  */
class DatasourceFromFieldTest extends Specification{

    DataSource dataSource

    @DbUnit def content =  {
        User(id: 1, name: 'janbols')
    }

    def setup(){
        dataSource = new DataSource()
        dataSource.driverClassName = 'org.h2.Driver'
        dataSource.url = 'jdbc:h2:mem:'
        dataSource.username = 'sa'
        dataSource.password= ''
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
