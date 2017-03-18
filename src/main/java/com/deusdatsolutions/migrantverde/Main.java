/*
 * Copyright 2017 DeusDat Solutions Corp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deusdatsolutions.migrantverde;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.arangodb.ArangoDB;

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
	 *            for properties.
	 * @throws IOException if the drive gives out while processing.
	 * @throws FileNotFoundException if the properties file is missing.
	 * @see <a href="https://github.com/arangodb/arangodb-java-driver/blob/master/docs/setup.md">
	 * 			https://github.com/arangodb/arangodb-java-driver/blob/master/docs/setup.md</a>
	 */
	public static void main( final String[] args ) throws FileNotFoundException, IOException {
		final Object[] propsAndFiles = weCool(args);

		final ArangoDB driver = new ArangoDB.Builder()
				.loadProperties((InputStream) propsAndFiles[0])
				.build();
		Properties props = (Properties) propsAndFiles[1];
		final Migrator m = new Migrator(new DBContext(driver, 
		                                              props.getProperty("migrantverde.db")), 
		                                Action.valueOf(props.getProperty("migrantverde.action").toUpperCase()));

		m.migrate(props.getProperty("root"));
	}

	private static Object[] weCool( final String[] args ) throws FileNotFoundException, IOException {
		if ( args.length != 1 ) {
			System.out.println("Incorrect arguments passed:");
			System.exit(1);
		}
		Properties p = new Properties();
		p.load(new FileInputStream(new File(args[0])));
		return new Object[] { new FileInputStream(new File(args[0])), p };
	}

}
