[![Build Status](https://travis-ci.org/janbols/spock-dbunit.svg?branch=master)](https://travis-ci.org/janbols/spock-dbunit)
[![Download](https://api.bintray.com/packages/janbols/maven/spock-dbunit/images/download.svg) ](https://bintray.com/janbols/maven/spock-dbunit/_latestVersion)

spock-dbunit
============

[Dbunit](http://dbunit.sourceforge.net/) extension for [spock](http://spockframework.org/) avoiding writing separate xml files
when doing db related tests.

Normally when using dbUnit, you specify your sql data in a separate xml file. This can become cumbersome 
and makes it more difficult to understand how your test is set up. 
You can avoid this using this spock extension.

Using groovy's [MarkupBuilder](http://groovy-lang.org/processing-xml.html#_markupbuilder) you just specify your sql as a field 
like in the example below:

```groovy
     class MyDbUnitTest extends Specification{

        DataSource dataSource

        @DbUnit
        def content =  {
            User(id: 1, name: 'jackdaniels', createdOn: '[NOW]')
        }

        ...
```

The above code will setup dbUnit to insert the specified row in the User table.
It will take the data source specified in the datasource field to get the connection to the database.

Configuration
-------------
#### Finding the DataSource
dbUnit needs a [DataSource](https://docs.oracle.com/javase/7/docs/api/javax/sql/DataSource.html) to connect to the database. There are several ways to do this:
* by specifying a datasourceProvider as an extra closure parameter in the `@DbUnit` annotation. The closure returns a configured DataSource
   ```groovy
    class MyDbUnitTest extends Specification{
    
        @DbUnit(datasourceProvider = {
            inMemoryDataSource()
        })
        def content =  {
            User(id: 1, name: 'janbols')
        }
    
        ...
   ```
* by specifying a DataSource field in your specification
   ```groovy
    class MyDbUnitTest extends Specification{
    
        DataSource dataSource
    
        @DbUnit
        def content = {
            User(id: 1, name: 'janbols')
        }
    
        def setup() {
            dataSource = inMemoryDataSource()
        }    
        ...
   ```
* by specifying a `@Shared` DataSource field in your specification
   ```groovy
    class MyDbUnitTest extends Specification{

        @Shared DataSource dataSource
    
        @DbUnit def content =  {
            User(id: 1, name: 'janbols')
        }
    
        def setupSpec(){
            dataSource = inMemoryDataSource()
        }
        ...
   ```

#### Configuring the DatabaseTester
The dbUnit [DatabaseTester](http://dbunit.sourceforge.net/apidocs/org/dbunit/IDatabaseTester.html)
can also be configured as an extra closure in the `@DbUnit` annotation. An example can be seen below:

```groovy
     class MyDbUnitTest extends Specification{

         @DbUnit(configure={IDatabaseTester it ->
            it.setUpOperation = DatabaseOperation.CLEAN_INSERT
            it.tearDownOperation = DatabaseOperation.TRUNCATE_TABLE
    
            (it.dataSet as ReplacementDataSet)
                .addReplacementObject('[TOMORROW]', LocalDateTime.now().plusDays(1).toDate())
         }) 
         def content =  {
            User(id: 1, name: 'jackdaniels', created: '[NOW]', expiration: '[TOMORROW]')
        }
        
        ...
```

In the example above, the DatabaseTester is being configured to do a clean insert during setup and a table truncate during cleanup.
In addition all `[TOMORROW]` fields are being replaced with the date of tomorrow.

#### Specifying the schema
You can specify the default schema using the `schema` field in the `DbUnit` annotation. 
The example below shows an example:

```groovy
     class MyDbUnitTest extends Specification{

        DataSource dataSource
    
        @DbUnit(schema = "otherSchema")
        def content = {
            User(id: 1, name: 'janbols')
        }
        
        ...
```

#### Use different content per feature
You can specify different database content per feature. This can be done by adding a `DbUnit` annotation 
on the feature method. 
The content can be specified in the `content` field of the `DbUnit` annotation. 
It accepts a closure that specifies the database content. 
When there's also a `DbUnit` annotation on a field containing database content, 
the one on the feature takes precedence and the one on the field is ignored. 
An example is shown below:
```groovy
     class MyDbUnitTest extends Specification{
            
            //default db content for all features that don't override this one
            @DbUnit
            def content =  {
                User(id: 3, name: 'dinatersago')
            }
        
            ...
        
            //DbUnit on a feature overrides the one in the content field
            @DbUnit(content = {
                User(id: 1, name: 'janbols')
            })
            def "feature with own database content"() {
                ...
            }
        
            def "feature without own database content taking the content of the field"() {
                ...
            }
            
            ...

```

Getting started
---
To enable this Spock extension, you need to add a dependency to this project and a dependency to dbUnit

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
        <version>0.3</version>
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
        testCompile( 'be.janbols:spock-dbunit:0.3' )
        testCompile( 'org.dbunit:dbunit:2.5.1' )
    }

If you prefer, you can just download the jar directly 
from [JCenter](http://jcenter.bintray.com/be/janbols/spock-dbunit/0.3/spock-dbunit-0.3.jar).

Changes
---
#### Version 0.3
* Be able to override the `DbUnit` content per feature (https://github.com/janbols/spock-dbunit/issues/7)
* Be able to specify the schema (https://github.com/janbols/spock-dbunit/issues/6)

#### Version 0.2
* Easier way to see if spring-jdbc is on the classpath
* Publish in bintray

#### Version 0.1
* Initial version

