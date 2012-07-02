spock-dbunit
============

Dbunit extension for spock avoiding a separate xml file when doing db related tests.
Normally when using dbUnit, you specify your sql data in a separate xml file. This can become cumbersome so you might want to avoid this using this spock extension.
Using groovy's MarkupBuilder you just specify your sql as a field like the example below:

     class MyDbUnitTest extends Specification{

        DataSource dataSource

        @DbUnit
        def content =  {
            User(id: 1, name: 'jackdaniels', createdOn: '[NOW]')
        }

        ...

