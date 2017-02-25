package be.janbols.spock.extension.dbunit

import groovy.sql.Sql
import org.apache.tomcat.jdbc.pool.DataSource

class TestUtils {

    private TestUtils() {}

    static DataSource inMemoryDataSource() {
        return new DataSource().with { dataSource ->
            dataSource.driverClassName = 'org.h2.Driver'
            dataSource.url = 'jdbc:h2:mem:'
            dataSource.username = 'sa'
            dataSource.password = ''
            dataSource
        }
    }

    static void createUserTable(javax.sql.DataSource dataSource){
        assert dataSource
        new Sql(dataSource).execute("CREATE TABLE User(id INT PRIMARY KEY, name VARCHAR(255), created TIMESTAMP, expiration TIMESTAMP )")
    }

    static void dropUserTable(javax.sql.DataSource dataSource){
        assert dataSource
        new Sql(dataSource).execute("drop table User")
    }

}
