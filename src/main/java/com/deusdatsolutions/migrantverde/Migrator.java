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
import com.arangodb.entity.StringsResultEntity;
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
	private boolean fullMigration;

	public Migrator(final ArangoDriver driver,
					final Action action) {
		super();
		this.action = action;
		this.driver = driver;
		this.finder = new MigrationsFinder();
		this.deserializier = new Deserializier();
		this.handler = new MasterHandler(action, driver);
	}

	public int migrate(final String migrationRoot) {
		final SortedSet<Path> migrations = finder.migrations(migrationRoot, this.action);
		fullMigration = isFullMigration(migrations.first());
		int executed = 0;
		for (final Path p : migrations) {
			final MigrationType migration = deserializier.get(p);
			final String migrationName = p.getFileName().toString().replace(".xml", "");
			try {
				if (notApplied(migrationName)) {
					final MigrationContext migrationContext = new MigrationContext(migrationName, migration);
					handler.migrate(migrationContext);
					recordMigration(migrationContext);
					executed++;
				}
			} catch (final ArangoException e) {
				throw new MigrationException("Couldn't perform migration", e);
			}
		}
		return executed;
	}
	
	private boolean isFullMigration(final Path first) {
		final MigrationType migration = deserializier.get(first);
		return action == Action.MIGRATION && migration.getUp().getDatabase() != null && dbDoesntExit(migration.getUp().getDatabase().getName());
	}

	private void initMigrationTracker(final MigrationContext migrationContext) throws ArangoException {
		if(fullMigration) {
			final String name = migrationContext.getMigration().getUp().getDatabase().getName();
			driver.setDefaultDatabase(name);
		}
		try {
			driver.getCollection(MIGRATION_COLLECTION);
		} catch (final ArangoException e) {
			if(e.getCode() == 404) {
				driver.createCollection(MIGRATION_COLLECTION);
			}
		}

		fullMigration = false;
	}
	
	private boolean dbDoesntExit(final String name) {
		boolean result = false;
		try {
			final StringsResultEntity databases = driver.getDatabases();
			result = !databases.getResult().contains(name);
		} catch (final ArangoException e) {
			
		}
		return result;
	}

	private void recordMigration(final MigrationContext migrationContext) throws ArangoException {
		initMigrationTracker(migrationContext);
		final BaseDocument bd = new BaseDocument();
		bd.addAttribute("name", migrationContext.getMigrationVersion());
		bd.addAttribute("action", action);
		bd.addAttribute("applied", new Date());
		driver.createDocument(MIGRATION_COLLECTION, bd);
	}
	
	private boolean notApplied(final String migrationName) throws ArangoException {
		if(fullMigration) {
			return true;
		}
		final Map<String, Object> params = new HashMap<>();
		params.put("action", action);
		params.put("name", migrationName);
		CursorResult<BaseDocument> result = null;
		int countSize = -1;
		try {
			result = driver.executeAqlQuery(MIGRATION_APPLIED_QUERY, params, null, BaseDocument.class);
			final BaseDocument uniqueResult = result.getUniqueResult();
			countSize = uniqueResult == null ? 0 : -1;
		} finally {
			if(result != null) {
				result.close();
			}
		}
		
		return countSize == 0;
	}

}
