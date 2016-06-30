package com.deusdatsolutions.migrantverde;

import static org.junit.Assert.*;

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

/**
 * This is an integration test to make sure that the system can run a full migration set. You must specify the following
 * environment parameters: migrationUser, migrationPassword, arangoHost.
 * 
 * This is a ground up set of tests. It purposely jacks with the database to make sure it's working from a clean slate.
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
	public void createSimpleDatabase() throws ArangoException {
		final Migrator m = new Migrator(DRIVER, Action.MIGRATION);
		final int executed = m.migrate("/full");

		assertEquals("Should have executed migrations", 3, executed);

		// And now for the actual test!
		final CollectionEntity user = DRIVER.getCollection("Users"); // <-- See full/2.xml for this setting.
		assertNotNull(user);

		final DocumentEntity<BaseDocument> document = DRIVER.getDocument("Users", "dotWarner", BaseDocument.class);
		final String docName = (String) document.getEntity().getAttribute("name");
		assertEquals("Should be Dot", "Princess Angelina Contessa Louisa Francesca Banana Fanna Bo Besca III", docName);
	}

	@Test
	public void runDup() throws ArangoException {
		final Migrator m = new Migrator(DRIVER, Action.MIGRATION);
		final int executedMigrations = m.migrate("/full");

		assertEquals("Should not have executed a thing", 0, executedMigrations);

		// And now for the actual test!
		final CollectionEntity user = DRIVER.getCollection("Users"); // <-- See full/2.xml for this setting.
		assertNotNull(user);

		final DocumentEntity<BaseDocument> document = DRIVER.getDocument("Users", "dotWarner", BaseDocument.class);
		final String docName = (String) document.getEntity().getAttribute("name");
		assertEquals("Should be Dot", "Princess Angelina Contessa Louisa Francesca Banana Fanna Bo Besca III", docName);
	}

}
