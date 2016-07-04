package com.deusdatsolutions.migrantverde;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoHost;

/**
 * Access point to run the migration from the CLI.
 * 
 * @author J Patrick Davenport
 *
 */
public class Main {
	private Main() {
	}

	/**
	 * Allows a user to run a migration from the CLI.
	 * 
	 * @param args
	 *            requires one entry, the path to the property file.
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(final String[] args) throws FileNotFoundException, IOException {
		final Properties props = weCool(args);

		final ArangoConfigure ctx = new ArangoConfigure();
		ctx.setArangoHost(new ArangoHost(props.getProperty("host"), Integer.parseInt(props.getProperty("port"))));
		ctx.setUser(props.getProperty("user"));
		ctx.setPassword(props.getProperty("password"));
		ctx.setDefaultDatabase(props.getProperty("db"));
		ctx.init();

		final ArangoDriver driver = new ArangoDriver(ctx);
		final Migrator m = new Migrator(driver, Action.valueOf(props.getProperty("action").toUpperCase()));

		m.migrate(props.getProperty("root"));
	}

	private static Properties weCool(final String[] args) throws FileNotFoundException, IOException {
		if (args.length != 1) {
			System.out.println("Incorrect arguments passed:");
			System.out.println("The only argument is a file with the following properties");
			System.out.println("host=192.168.56.128");
			System.out.println("port=8529");
			System.out.println("username=root");
			System.out.println("password=somepassword");
			System.out.println("db=db_name_to_migrate");
			System.out.println("action=migration|rollback");
			System.out.println("root=/path/to/migration");
			System.exit(1);
		}

		final Properties props = new Properties();
		props.load(new FileInputStream(new File(args[0])));
		return props;
	}

}
