package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import org.dbunit.IDatabaseTester
import org.dbunit.dataset.ReplacementDataSet
import org.dbunit.operation.DatabaseOperation
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

/**
  *
  */
@ContextConfiguration(locations='classpath:/spring/context.xml')
class ConfigureTesterTest extends Specification{

    @Autowired DataSource dataSource

    @DbUnit(configure={IDatabaseTester it ->
        it.setUpOperation = DatabaseOperation.CLEAN_INSERT
        it.tearDownOperation = DatabaseOperation.TRUNCATE_TABLE

        (it.dataSet as ReplacementDataSet).addReplacementObject('[TOMORROW]', LocalDateTime.now().plusDays(1).toDate())
    }) def content =  {
        User(id: 1, name: 'janbols', created: '[NOW]', expiration: '[TOMORROW]')
    }


    def setup(){
        new Sql(dataSource).execute("CREATE TABLE User(id INT PRIMARY KEY, name VARCHAR(255), created TIMESTAMP, expiration TIMESTAMP )")
    }

    def cleanup() {
        new Sql(dataSource).execute("drop table User")
    }



    def "test"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        result.id == 1
        new LocalDate(result.created) == LocalDate.now()
        new LocalDate(result.expiration) == LocalDate.now().plusDays(1)
    }




}
