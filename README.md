# Migrant Verde
Migration engine for ArangoDB 2.7+. All of the development was against an instance of 3.0, but should be largely 
backwards compatible. Seen the XSD for details where they're not. You have to provide your own instance of Arango's
driver.

## Running Integration Tests
Most of the tests are honest unit tests. There are few integration tests. These need a running instance of Arango.
To get them to work you need to specify three **environment** variables: arangoHost, migrationPassword, migrationUser.
Since this is a full migration, you need provide a root account. 