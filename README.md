# Migrant Verde
A schema evolution tool for [ArangoDB](https://arangodb.com/). Manage your collections, indices, and data transformations in a centralized way in your source control along with your application.

Migrant Verde automatically updates the databases of your installations when you deploy updates of your application to developer, CI or production systems.

Migrant Verde comes as a light weight library that only depends on [ArangoDB's Java driver](https://www.arangodb.com/arangodb-drivers/) and Java 1.7+. It doesn't bring any other depenencies into your project. If you decide to go with Migrant Verdes CLI as standalone tool, you don't even have to bring it into your project!

The goal behind the project is to apply to ArangoDB years of hard fought lessons (especially those that kicked us in the teeth). We needed a schema version manager that could create a database, add all of the collections, indexes and data population necessary for a developer to create a local VM of ArangoDB that looks like a mini-production. The system should automatically adjust to merges. At the same time, we need a way to downgrade changes when they prove to be troublesome. While providing all of this, it must also support the neat features we all know and chose ArangoDB for: sharding collections on distributed systems. This means that we can't rely on creating the collection automatically if it doesn't exist when inserting a document. Sometimes collections should come preloaded with some documents from start.

Similar processes can be established on other database engines using [Liquibase](https://en.wikipedia.org/wiki/Liquibase)

## Learning by doing (Building a migration set)
### Instantiating a new environment
Step Zero: install a copy of ArangoDB into your favorite VM. While this is not actually required, we're proponents of the [12-factor approach to software development](http://12factor.net/). We agree that developers should be as close to production environments as possible. Hence a VM with the same OS as the production environment will have. This eases the process of contineous delivery, since missing or unavailable dependencies on the system will occur during the development. When you deploy ArangoDB, pay attention to the root password you use for ArangoDB. This is different than root for your VM. 

### Creating a database with access 
The first thing that we'll want to do is **create a database**. As you'll see later, Migrant Verde uses XML as our lingua franca. Let's name this file "**1.xml**".

	<?xml version="1.0" encoding="UTF-8"?>
	<tns:migration xmlns:tns="http://deusdatsolutions.com/projects/migrations/"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://deusdatsolutions.com/projects/migrations/ migrations.xsd ">
		<up>
			<database name="IntegrationTestDB" action="create">
				<user>
					<username>Timmy</username>
					<password>KosmoAndWanda</password>
				</user>
			</database>
		</up>
	</tns:migration>
	
This creates a new database within Arango named IntegrationTestDB. It also creates a new user named *Timmy* with the password *KosmoAndWanda*.

(You might be saying to yourself, "Did that example really have me put a username and password in the repository?" To which we reply, yea. Don't do that. There's an enhancement ticket pending to fix this. Until then, you can skip the user creation at this time and create user later using a different feature supported by Migrant Verde. You could even do it in a file named "1.5.xml")

### Creating Collections
Now that you've got a database, you'll want collections. Databases love collections. Like most apps you want something that holds user information. Let's call that collection "Users". Let's name this file "**2.xml**".

	<?xml version="1.0" encoding="UTF-8"?>
	<tns:migration xmlns:tns="http://deusdatsolutions.com/projects/migrations/"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="http://deusdatsolutions.com/projects/migrations/ migrations.xsd ">
		<up>
			<collection name="Users" action="create"/>
		</up>
	</tns:migration>

While totally ignored in the first step, let's discuss the details of the XML. All migrations are rooted with the tag *migration*. The next tag is "**&lt;up&gt;**". When you invoke the library, you get to pick if your migrating or rolling back a set of files. &lt;up&gt; is used when migrating. &lt;down&gt; is used like up, but when you want to roll back a set of changes. &lt;down&gt; might not even be useful. We're on the fence about that feature, but we're also at 0.0.1 version so ride it we shall.

The collection tag takes two attributes: name and action. The name is the name for the collection. The action can be "create", "drop" or "modify". At present collection doesn't do anything with modify. Create and drop are self-explanatory.

### Executing AQL
With our collection ready, let's add a user. To do this we'll leverage the ability to run arbitrary AQL. Let's name this "**3.xml**".

	<?xml version="1.0" encoding="UTF-8"?>
	<tns:migration 
		xmlns:tns="http://deusdatsolutions.com/projects/migrations/" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		xsi:schemaLocation="http://deusdatsolutions.com/projects/migrations/ migrations.xsd ">
	  <up>
	  	<aql>
	  		INSERT {name : "Princess Angelina Contessa Louisa Francesca Banana Fanna Bo Besca III", age: 14, "_key" : "dotWarner"} IN Users
	  	</aql>
	  </up>
	</tns:migration>
	
You'll need to use the &lt;aql&gt; tag. This tag takes a single AQL statement. While this example is rather simple, you can use it to migrate whole collections at a time. You can add new attributes to documents, split them, etc. The only limit is that the operation has to work within the current database.

### Manipulating Collection Attributes
Finally, let's apply an index to the collection on the attribute name. We'll name this file "**4.xml**".

	<?xml version="1.0" encoding="UTF-8"?>
	<tns:migration 
		xmlns:tns="http://deusdatsolutions.com/projects/migrations/" 
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
		xsi:schemaLocation="http://deusdatsolutions.com/projects/migrations/ migrations.xsd ">
	  <up>
	  	<index name="Users" type="hash" unique="true" action="create">
	  		<field>name</field>
	  	</index>
	  </up>
	</tns:migration>

The name attribute is the collection to which we want to apply the index. The type may be "hash", "capped", "geo", "fulltext" or "skiplist". See [migrations.xsd](src/main/resources/migrations.xsd) for more information.

You probably picked up on the naming scheme. Our migrations got applied in ascending order by name. You don't have to follow that particular pattern. You just have to make sure that the file names order properly. A common alternative is to name them YYYY-MM-DD-Idx-Descriptive-Name.xml. Let the timestamp and possible sub-index (Idx) handle sorting.

## Applying to your code

To see a full example, look at [MigratorIT.java](src/test/java/com/deusdatsolutions/migrantverde/MigratorIT.java). Here's the gist:

First, create a driver like normal.

	ArangoConfigure ac;
	ac = new ArangoConfigure();
	ac.setUser(System.getenv("migrationUser"));
	ac.setPassword(System.getenv("migrationPassword"));
	ac.setArangoHost(new ArangoHost(System.getenv("arangoHost"), 8529));
	ac.init();
	DRIVER = new ArangoDriver(ac);

Second, tell the migration library where your migration set is. If you use absolute paths, the library looks for resources under that folder within the JAR executing the migration. If you want to deploy your migrations seperately from your JAR/WAR, you can specify a physical directory with "file://". 

	final Migrator m = new Migrator(DRIVER, Action.MIGRATION);
	final int executed = m.migrate("/full");
	
The enum Action.MIGRATION tells the system to run a migration. You can also set to ROLLBACK.

Sometimes you might start using Migrate Verde after the you've already gone to production. You want a full migration for new developers, but only want to apply from a certain migration XML forward to production. 

For this example assume a simple naming standard of 1.xml -> 20.xml. We want to start the production system at 5.xml.

	DRIVER.setDefaultDatabase("IntegrationTestDB");
	final Migrator m = new Migrator(DRIVER, Action.MIGRATION, "5");
	
You can take this further using configuration or environment variable for picking the start document name. Replace "5" with `System.getenv("statDoc")`. Developers will configure their runtime with **statDoc=1**.

## XML Operations
First, we've been asked, "Why XML?" Our reason is that XML is natively supported by Java. If we wanted to use YAML or JSON, we'd have to get a parser for that (good design favors using a time tested library rather than rolling our own. Scope management dictates the same). It's also universally understood. While it's not sexy, it makes for easy to read configurations.

An XML instance follows the schema found in the project at /migrations.xsd. Presently you can create/delete a collection, index and database. You can also run arbitrary non-parameterized AQL and arbitrary Java code against your driver. These are defined in the XSD entry **OperationsGroup**. 

## Invoking From the CLI
While the library makes it easy for Java compatible apps to migrate their DBs, it can be used by the larger community. 
You'll still need a JVM 1.7+ installed.

java -cp "/path/to/Arango/Driver.jar" com.deusdatsolutions.migrantverde.Main /path/to/config/file.properties.

The configuration file needs the following key-value pairs.
                                                                    
|	property-key				|	description								|	default value	|
|-------------------------------|-------------------------------------------|-------------------|
|	arangodb.hosts				|	ArangoDB hosts 							|	127.0.0.1:8529	|
|	arangodb.timeout			|	socket connect timeout(millisecond)		|	0				|
|	arangodb.user				|	Basic Authentication User				|					|
|	arangodb.password			|	Basic Authentication Password			|					|
|	arangodb.useSsl				|	use SSL connection						|	false			|
|	arangodb.chunksize			|	VelocyStream Chunk content-size(bytes)	|	30000			|
|	arangodb.connections.max	|	max number of connections				|	1				|
|	migrantverde.db				|	The name of the migrating DB			|					|
|	migrantverde.action			|	Migration or Rollback.					|					|
|	migrantverde.root			|	The path to the migration directory		|					|


When running a full migration, you need to provide credentials that can create a database. From our use root is a good
option. 

The library does not directly download a copy of the ArangoDB Java driver. Please get the latest driver from Arango's site. Include it in the -cp argument as above.

## Using the library
The library is not yet in a central Maven repository. You can download the latest version in the release directory of this repo. You can also build it from scratch using Maven.

## Running Integration Tests
Most of the tests are honest unit tests. There are few integration tests. These need a running instance of Arango.
To get them to work you need to specify three **environment** variables: arangoHost, migrationPassword, migrationUser.
Since this is a full migration, you need provide a root account. 

## Supported ArangoDB Version
Presently supports the new binary protocol. Prior supports v3 via the HTTP API. The prior release, 0.0.1, supports v2. The next version will support the new binary protocol.
