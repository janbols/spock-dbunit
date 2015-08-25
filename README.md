spock-dbunit
============

[Dbunit](http://dbunit.sourceforge.net/) extension for [spock](https://github.com/spockframework/spock) avoiding a separate xml file
when doing db related tests.

Normally when using dbUnit, you specify your sql data in a separate xml file. This can become cumbersome so you might want to
avoid this using this spock extension.

Using groovy's MarkupBuilder you just specify your sql as a field like the example below:

     class MyDbUnitTest extends Specification{

        DataSource dataSource

        @DbUnit
        def content =  {
            User(id: 1, name: 'jackdaniels', createdOn: '[NOW]')
        }

        ...

The above code will setup dbUnit to insert the specified row in the User table.
It will take the data source specified in the datasource field to get the connection to the database.

Configuration
-------------
dbUnit needs a data source to connect to the database. This is done by specifying a datasourceProvider as an extra closure
parameter in the @DbUnit annotation.
Alternatively, it will look for a DataSource field in your specification and use that one.

The dbUnit [DatabaseTester](see http://dbunit.sourceforge.net/apidocs/org/dbunit/IDatabaseTester.html)
can also be configured as an extra closure in the @DbUnit annotation. An example can be seen below:

     @DbUnit(configure={IDatabaseTester it ->
        it.setUpOperation = DatabaseOperation.CLEAN_INSERT
        it.tearDownOperation = DatabaseOperation.TRUNCATE_TABLE

        (it.dataSet as ReplacementDataSet).addReplacementObject('[TOMORROW]', LocalDateTime.now().plusDays(1).toDate())
     }) 
     def content =  {
        User(id: 1, name: 'jackdaniels', created: '[NOW]', expiration: '[TOMORROW]')
    }

In the example above, the DatabaseTester is being configured to do a clean insert during setup and a table truncate during cleanup.
In addition all '[TOMORROW]' fields are being replaced with LocalDateTime.now().plusDays(1).toDate().