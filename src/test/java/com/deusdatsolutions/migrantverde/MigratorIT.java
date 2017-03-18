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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;

/**
 * This is an integration test to make sure that the system can run a full
 * migration set. You must specify the following environment parameters:
 * migrationUser, migrationPassword, arangoHost.
 * 
 * This is a ground up set of tests. It purposely jacks with the database to
 * make sure it's working from a clean slate.
 * 
 * Pay close attention to the annotation. All instance methods are ran in
 * alphabetical order. This is to do things like create the DB from scratch and
 * then try to re-run the same migration over it.
 * 
 * @author J Patrick Davenport
 *
 */
@FixMethodOrder( MethodSorters.NAME_ASCENDING )
public class MigratorIT {
	private static final String TEST_DB = "IntegrationTestDB";
	private static final DBContext CTX;

	static {
		ArangoDB driver = new ArangoDB.Builder()
				.loadProperties(MigratorIT.class.getResourceAsStream("/migrantverde.properties")).build();
		CTX = new DBContext(driver, TEST_DB);
	}

	/**
	 * This is for future me, or future anyone that runs this test class.
	 * 
	 * This method DROP THE WHOLE DATABASE! Remember that.
	 * 
	 * @throws ArangoException
	 */
	@BeforeClass
	public static void cleanOutOld() {
		try {
			CTX.db.drop();
		} catch ( ArangoDBException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void a() {
		@SuppressWarnings( "serial" )
		final Migrator m = new Migrator(CTX, Action.MIGRATION, new HashMap<String, String>() {
			{
				put("username",
					"Timmy");
				put("password",
					"CosmoAndWanda");
				put("petsname",
					"Unknown");
			}
		});
		final int executed = m.migrate("/full");

		assertEquals(	"Should have executed migrations",
						4,
						executed);

		// And now for the actual test!
		final ArangoCollection user = CTX.db.collection("Users"); // <-- See
																	// full/2.xml
																	// for
																	// this
																	// setting.
		assertNotNull(user);

		BaseDocument document = user.getDocument(	"dotWarner",
													BaseDocument.class);
		final String docName = (String) document.getAttribute("name");
		assertEquals(	"Should be Dot",
						"Princess Angelina Contessa Louisa Francesca Banana Fanna Bo Besca III",
						docName);

		final List<IndexEntity> indexes = new LinkedList<>(CTX.db.collection("Users").getIndexes());
		assertEquals(	2,
						indexes.size());

		final IndexEntity indexEntity = indexes.get(1);
		final List<String> expectedFields = new LinkedList<>();
		expectedFields.add("name");
		assertEquals(	"Should find expected field of name",
						expectedFields,
						indexEntity.getFields());
		assertEquals(	"Should have created a hash index",
						indexEntity.getType(),
						IndexType.hash);
	}

	/**
	 * Re-executes the migration to make sure that nothing actually runs because
	 * all the migration steps were applied.
	 * 
	 * @throws ArangoException
	 */
	@Test
	public void b() {
		final Migrator m = new Migrator(CTX, Action.MIGRATION);
		final int executedMigrations = m.migrate("/full");

		assertEquals(	"Should not have executed a thing",
						0,
						executedMigrations);

		// And now for the actual test!
		final ArangoCollection user = CTX.db.collection("Users"); // <-- See
																	// full/2.xml
																	// for
																	// this
																	// setting.
		assertNotNull(user);

		final BaseDocument document = CTX.db.collection("Users").getDocument(	"dotWarner",
																				BaseDocument.class);
		final String docName = (String) document.getAttribute("name");
		assertEquals(	"Should be Dot",
						"Princess Angelina Contessa Louisa Francesca Banana Fanna Bo Besca III",
						docName);
	}

	@Test
	public void c() throws IOException {
		// Copy the migrations from the full and after, to make sure that we
		// only migrate 2.
		final File tmpFile = File.createTempFile(	"arangod",
													"db");
		final File tempDir = tmpFile.getParentFile();

		{
			final URI tempUri = new File(tempDir, "1.xml").toURI();
			Files.copy(	getClass().getResourceAsStream("/full/1.xml"),
						Paths.get(tempUri),
						StandardCopyOption.REPLACE_EXISTING);
		}

		{
			final URI tempUri = new File(tempDir, "2.xml").toURI();
			Files.copy(	getClass().getResourceAsStream("/full/2.xml"),
						Paths.get(tempUri),
						StandardCopyOption.REPLACE_EXISTING);
		}

		{
			final URI tempUri = new File(tempDir, "3.xml").toURI();
			Files.copy(	getClass().getResourceAsStream("/full/3.xml"),
						Paths.get(tempUri),
						StandardCopyOption.REPLACE_EXISTING);
		}

		{
			final URI tempUri = new File(tempDir, "4.xml").toURI();
			Files.copy(	getClass().getResourceAsStream("/full/4.xml"),
						Paths.get(tempUri),
						StandardCopyOption.REPLACE_EXISTING);
		}

		{
			final URI tempUri = new File(tempDir, "5.xml").toURI();
			Files.copy(	getClass().getResourceAsStream("/after/5.xml"),
						Paths.get(tempUri),
						StandardCopyOption.REPLACE_EXISTING);
		}

		{
			final URI tempUri = new File(tempDir, "6.xml").toURI();
			Files.copy(	getClass().getResourceAsStream("/after/6.xml"),
						Paths.get(tempUri),
						StandardCopyOption.REPLACE_EXISTING);
		}
		{
			final URI tempUri = new File(tempDir, "7.xml").toURI();
			Files.copy(	getClass().getResourceAsStream("/after/7.xml"),
						Paths.get(tempUri),
						StandardCopyOption.REPLACE_EXISTING);
		}
		final Migrator m = new Migrator(CTX, Action.MIGRATION, "5");
		final String string = tempDir.toString();
		final int migrated = m.migrate("file://" + string);
		assertEquals(	"Should have performed 2 migrations",
						3,
						migrated);
	}

	@Test
	public void d() {
		final Migrator m = new Migrator(CTX, Action.MIGRATION);
		final int executedMigrations = m.migrate(	"/migrations/one",
													"/migrations/two");
		assertEquals(	"Should have migrated two files",
						2,
						executedMigrations);
	}
}