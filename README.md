[![Build Status](https://travis-ci.org/janbols/spock-dbunit.svg?branch=master)](https://travis-ci.org/janbols/spock-dbunit)

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

Getting started
---
To enable this Spock extension, you need to add a dependency to this and a dependency to dbUnit

using Maven:

Enable the JCenter repository:

    <repository>
      <id>jcenter</id>
      <name>JCenter Repo</name>
      <url>http://jcenter.bintray.com</url>
    </repository>
Add spock-reports to your <dependencies>:

    <dependency>
        <groupId>be.janbols</groupId>
        <artifactId>spock-dbunit</artifactId>
        <version>0.2</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.dbunit</groupId>
        <artifactId>dbunit</artifactId>
        <version>2.5.1</version>
        <scope>test</scope>
    </dependency>


using Gradle:

    repositories {
      jcenter()
    }

    dependencies {
        testCompile( 'be.janbols:spock-dbunit:0.2' )
        testCompile( 'org.dbunit:dbunit:2.5.1' )
    }

If you prefer, you can just download the jar directly from [JCenter](http://jcenter.bintray.com/be/janbols/spock-dbunit/0.2/spock-dbunit-0.2.jar).

