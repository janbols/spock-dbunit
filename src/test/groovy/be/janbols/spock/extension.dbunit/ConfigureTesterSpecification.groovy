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

import static SpecUtils.createUserTable
import static SpecUtils.dropUserTable

/**
  * Specification showing how to configure the database by accessing the IDatabaseTester in the DbUnit#configure closure
  */
@ContextConfiguration(locations='classpath:/spring/context.xml')
class ConfigureTesterSpecification extends Specification{

    @Autowired DataSource dataSource

    @DbUnit(configure={IDatabaseTester it ->
        //tell db unit how to setup and teardown the database
        it.setUpOperation = DatabaseOperation.CLEAN_INSERT
        it.tearDownOperation = DatabaseOperation.TRUNCATE_TABLE

        //tell db unit to replace all occurrences of [TOMORROW] with the real value
        (it.dataSet as ReplacementDataSet).addReplacementObject('[TOMORROW]', LocalDateTime.now().plusDays(1).toDate())
    })
    def content =  {
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
