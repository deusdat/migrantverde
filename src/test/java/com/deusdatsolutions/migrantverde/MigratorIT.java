package com.deusdatsolutions.migrantverde;

import static org.junit.Assert.*;

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

import com.arangodb.ArangoConfigure;
import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.ArangoHost;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.DocumentEntity;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.IndexesEntity;

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
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MigratorIT {

	private static final String TEST_DB = "IntegrationTestDB";

	private static final ArangoDriver DRIVER;
	static {
		ArangoConfigure ac;
		ac = new ArangoConfigure();
		ac.setUser(System.getenv("migrationUser"));
		ac.setPassword(System.getenv("migrationPassword"));
		ac.setArangoHost(new ArangoHost(System.getenv("arangoHost"), 8529));
		ac.init();
		DRIVER = new ArangoDriver(ac);
	}

	/**
	 * This is for future me, or future anyone that runs this test class.
	 * 
	 * This method DROP THE WHOLE DATABASE! Remember that.
	 * 
	 * @throws ArangoException
	 */
	@BeforeClass
	public static void cleanOutOld() throws ArangoException {
		try {
			DRIVER.deleteDatabase(TEST_DB);
		} catch (final ArangoException e) {
			if (e.getCode() != 404) { // 404 is DB not found.
				throw e;
			}
		}
	}

	@Test
	public void a() throws ArangoException {
		@SuppressWarnings("serial")
		final Migrator m = new Migrator(DRIVER, Action.MIGRATION, new HashMap<String, String>() {
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
		final CollectionEntity user = DRIVER.getCollection("Users"); // <-- See
																		// full/2.xml
																		// for
																		// this
																		// setting.
		assertNotNull(user);

		final DocumentEntity<BaseDocument> document = DRIVER.getDocument(	"Users",
																			"dotWarner",
																			BaseDocument.class);
		final String docName = (String) document.getEntity().getAttribute("name");
		assertEquals(	"Should be Dot",
						"Princess Angelina Contessa Louisa Francesca Banana Fanna Bo Besca III",
						docName);

		final IndexesEntity indexes = DRIVER.getIndexes("Users");
		assertEquals(	2,
						indexes.getIndexes().size());

		final IndexEntity indexEntity = indexes.getIndexes().get(1);
		final List<String> expectedFields = new LinkedList<>();
		expectedFields.add("name");
		assertEquals(	"Should find expected field of name",
						expectedFields,
						indexEntity.getFields());
		assertEquals(	"Should have created a hash index",
						indexEntity.getType(),
						IndexType.HASH);
	}

	/**
	 * Re-executes the migration to make sure that nothing actually runs because
	 * all the migration steps were applied.
	 * 
	 * @throws ArangoException
	 */
	@Test
	public void b() throws ArangoException {
		final Migrator m = new Migrator(DRIVER, Action.MIGRATION);
		final int executedMigrations = m.migrate("/full");

		assertEquals(	"Should not have executed a thing",
						0,
						executedMigrations);

		// And now for the actual test!
		final CollectionEntity user = DRIVER.getCollection("Users"); // <-- See
																		// full/2.xml
																		// for
																		// this
																		// setting.
		assertNotNull(user);

		final DocumentEntity<BaseDocument> document = DRIVER.getDocument(	"Users",
																			"dotWarner",
																			BaseDocument.class);
		final String docName = (String) document.getEntity().getAttribute("name");
		assertEquals(	"Should be Dot",
						"Princess Angelina Contessa Louisa Francesca Banana Fanna Bo Besca III",
						docName);
	}

	@Test
	public void c() throws ArangoException, IOException {
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
		DRIVER.setDefaultDatabase("IntegrationTestDB");
		final Migrator m = new Migrator(DRIVER, Action.MIGRATION, "5");
		final String string = tempDir.toString();
		final int migrated = m.migrate("file://" + string);
		assertEquals(	"Should have performed 2 migrations",
						3,
						migrated);
	}

	@Test
	public void d() {
		final Migrator m = new Migrator(DRIVER, Action.MIGRATION);
		final int executedMigrations = m.migrate(	"/migrations/one",
													"/migrations/two");
		assertEquals(	"Should have migrated two files",
						2,
						executedMigrations);
	}
}