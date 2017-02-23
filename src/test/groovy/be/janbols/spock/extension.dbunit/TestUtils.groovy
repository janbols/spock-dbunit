package be.janbols.spock.extension.dbunit

import org.apache.tomcat.jdbc.pool.DataSource


class TestUtils {

    static DataSource createDataSource(){
        def dataSource = new DataSource()
        dataSource.driverClassName = 'org.h2.Driver'
        dataSource.url = 'jdbc:h2:mem:'
        dataSource.username = 'sa'
        dataSource.password = ''
        return dataSource
    }
}
