package com.deusdatsolutions.migrantverde;

import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.CursorResult;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.deusdatsolutions.migrantverde.handlers.MasterHandler;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType;

public class Migrator {
	private static final String MIGRATION_COLLECTION = "MigrantVerde";
	//@formatter:off
	private static final String MIGRATION_APPLIED_QUERY = "FOR m IN " + MIGRATION_COLLECTION
														+ "  FILTER m.action == @action AND m.name == @name " 
														+ "RETURN m";
	//@formatter:off
	private final MigrationsFinder finder;
	private final Deserializier deserializier;
	private final MasterHandler handler;
	private final ArangoDriver driver;
	private final Action action;

	public Migrator(final ArangoDriver driver,
					final Action action) {
		super();
		this.action = action;
		this.driver = driver;
		this.finder = new MigrationsFinder();
		this.deserializier = new Deserializier();
		this.handler = new MasterHandler(action, driver);
	}

	public void migrate(final String migrationRoot) {
		final SortedSet<Path> migrations = finder.migrations(migrationRoot);
		for (final Path p : migrations) {
			final MigrationType migration = deserializier.get(p);
			final String migrationName = p.getFileName().toString().replace(".xml", "");
			try {
				if (notApplied(migrationName)) {
					handler.migrate(new MigrationContext(migrationName, migration));
					recordMigration(migrationName);
				}
			} catch (final ArangoException e) {
				throw new MigrationException("Couldn't perform migration", e);
			}
		}
	}
	
	private void recordMigration(final String migrationName) throws ArangoException {
		final BaseDocument bd = new BaseDocument();
		bd.addAttribute("name", migrationName);
		bd.addAttribute("action", action);
		bd.addAttribute("applied", new Date());
		driver.createDocument(MIGRATION_COLLECTION, bd);
	}
	
	private boolean notApplied(final String migrationName) throws ArangoException {
		getMigrationCollection();
		final Map<String, Object> params = new HashMap<>();
		params.put("action", action);
		params.put("name", migrationName);
		CursorResult<BaseDocument> result = null;
		final int countSize = -1;
		try {
			result = driver.executeAqlQuery(MIGRATION_APPLIED_QUERY, params, null, BaseDocument.class);
			result.getCount();
		} finally {
			if(result != null) {
				result.close();
			}
		}
		
		return countSize == 0;
	}

	private long getMigrationCollection() throws ArangoException {
		CollectionEntity collection = driver.getCollection(MIGRATION_COLLECTION);
		if (collection == null) {
			collection = driver.createCollection(MIGRATION_COLLECTION);
		}
		return collection.getId();
	}
}
