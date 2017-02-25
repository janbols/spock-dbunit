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

import static be.janbols.spock.extension.dbunit.TestUtils.createUserTable
import static be.janbols.spock.extension.dbunit.TestUtils.dropUserTable

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

    def "the row in the User table returns has a created date of today and an expiration date of tomorrow"() {
        when:
        def result = new Sql(dataSource).firstRow("select * from User where name = 'janbols'")
        then:
        new LocalDate(result.created) == LocalDate.now()
        new LocalDate(result.expiration) == LocalDate.now().plusDays(1)
    }




}
