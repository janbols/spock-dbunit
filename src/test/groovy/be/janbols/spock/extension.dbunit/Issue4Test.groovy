package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource
import org.dbunit.IDatabaseTester
import org.dbunit.operation.DatabaseOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Issue
import spock.lang.Specification

/**
  *
  */
@ContextConfiguration(locations='classpath:/spring/context.xml')
class Issue4Test extends Specification{

    @Autowired DataSource dataSource

    @DbUnit(configure = { IDatabaseTester it ->
        it.setUpOperation = DatabaseOperation.CLEAN_INSERT
        it.tearDownOperation = DatabaseOperation.TRUNCATE_TABLE
    })
    def content = {
        User(name: 'test1', ip: '1.2.3.4')
        User(name: 'test2', ip: '127.0.0.1')
    }


    def setup(){
        new Sql(dataSource).execute("CREATE TABLE User(name VARCHAR(255), ip VARCHAR(255))")
    }

    def cleanup() {
        new Sql(dataSource).execute("drop table User")
    }



    @Issue("4")
    def "when selecting all User rows that where inserted, the results are returned"() {
        when:
        def result = new Sql(dataSource).rows("select * from User order by name")
        then:
        result == [
                [NAME: 'test1', IP: '1.2.3.4']
                , [NAME: 'test2', IP: '127.0.0.1']
        ]
    }




}
