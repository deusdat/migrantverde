# Migrant Verde
Migration engine for ArangoDB 2.7+. All of the development was against an instance of 3.0, but should be largely 
backwards compatible. Seen the XSD for details where they're not. You have to provide your own instance of Arango's
driver.

## Running Integration Tests
Most of the tests are honest unit tests. There are few integration tests. These need a running instance of Arango.
To get them to work you need to specify three **environment** variables: arangoHost, migrationPassword, migrationUser.
Since this is a full migration, you need provide a root account. 

## Migration Sets
The core of the system is a directory housing a bunch of XML files (yes, because XML is cool). The
XML files are named in such a way that they naturally sort (like 1.xml, 2.xml, etc). The smallest 
file is the start of the migration set. Each file is applied and tracked in your DB in a collection we add just for 
migration. 

A migration set starts where ever you want. It can be a full migration starting with database/user
creation and ending with index creation. It can be one that assumes the database already exists, with
collections and now you're adding to it. It can ALSO be both. You can start a migration at different
places in the migration list. So for a new developer, the system will migrate from create database -> production mirror.
For production you can start 20 migration steps later and move forward.

## XML Operations.
An XML instance follows the schema found in the project at /migrations.xsd. Presently you can create/delete a 
collection, index and database. You can also run arbitrary non-parameterized AQL and arbitrary Java code against your 
driver. These are defined in the XSD entry **OperationsGroup**. 

Graphs are next on the road map. Presently, DeusDat Solutions doesn't use them, so they're not high on our priority 
list. If the community wants graphs, let us know. We will update if we get 5 "me toos" on the issue tracker.

Each instance of the OperationGroup is within a mandatory <up> within <migration>. Up elements are selected when you 
specify the Action.MIGRATION. <down> elements are applied when you select Action.ROLLBACK. Up's are the presumed, and 
tested options.

Up/Down are carried over from [Waller](https://github.com/deusdat/waller). Our team wonders about the exact practicality
 of <down>'s. 

## From the CLI
While the library makes it easy for Java compatible apps to migrate their DBs, it can be used by the larger community. 
The library is executable. You'll still need a JVM 1.7+ installed.

java -jar migrantverde-0.0.1.jar /path/to/config/file.properties.

The configuration file needs the following key-value pairs.

* host=name/IP
* port=8529
* username=root
* password=somepassword
* db=db_name. If you run a full migration, the system detects the database and sets that as default.
* action=migration|rollback
* root=/path/to/migration/dir

When running a full migration, you need to provide credentials that can create a database. From our use root is a good
option. 