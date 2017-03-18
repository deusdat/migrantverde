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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.DatabaseEntity;
import com.deusdatsolutions.migrantverde.handlers.MasterHandler;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType;

/**
 * Driver class for the library. Executes all of the migrations.
 * 
 * @author J Patrick Davenport
 *
 */
public class Migrator {
	private static final String MIGRATION_COLLECTION = "MigrantVerde";
	// @formatter:off
	private static final String MIGRATION_APPLIED_QUERY = "FOR m IN " + MIGRATION_COLLECTION
			+ "  FILTER m.action == @action AND m.name == @name " + "RETURN m";
	// @formatter:off
	private final MigrationsFinder finder;
	private final Deserializier deserializier;
	private final MasterHandler handler;
	private final DBContext dbContext;
	private final Action action;
	private boolean fullMigration;
	private final String startingAt;

	public Migrator(	final DBContext dbContext,
						final Action action ) {
		this(dbContext, action, null, new HashMap<String, String>());
	}

	public Migrator(	final DBContext dbContext,
						final Action action,
						final Map<String, String> lookup ) {
		this(dbContext, action, null, lookup);
	}

	public Migrator(	final DBContext dbContext,
						final Action action,
						final String startingAt ) {
		this(dbContext, action, startingAt, new HashMap<String, String>());
	}

	public Migrator(	final DBContext dbContext,
						final Action action,
						final String startingAt,
						Map<String, String> lookup ) {
		super();
		this.action = action;
		this.dbContext = dbContext;
		this.finder = new MigrationsFinder();
		this.deserializier = new Deserializier();
		this.handler = new MasterHandler(action, dbContext, lookup);
		this.startingAt = startingAt;
	}

	public int migrate( final String... migrationRoots ) {
		if ( migrationRoots == null || migrationRoots.length == 0 ) {
			return 0;
		} else {
			int migrations = 0;
			for ( final String migration : migrationRoots ) {
				migrations += migrate(migration);
			}
			return migrations;
		}
	}

	public int migrate( final String migrationRoot ) {
		final SortedSet<Path> migrations = finder.migrations(	migrationRoot,
																this.action);
		fullMigration = isFullMigration(migrations.first());
		int executed = 0;
		for ( final Path p : migrations ) {
			final MigrationType migration = deserializier.get(p);
			final String migrationName = getMigrationName(p);
			if ( startingAt != null && migrationName.compareTo(startingAt) < 0 ) {
				continue;
			}
			if ( notApplied(migrationName) ) {
				final MigrationContext migrationContext = new MigrationContext(migrationName, migration);
				handler.migrate(migrationContext);
				recordMigration(migrationContext);
				executed++;
			}
		}
		return executed;
	}

	private String getMigrationName( final Path p ) {
		final Path fileName = p.getFileName();
		if ( fileName == null ) {
			throw new IllegalArgumentException("Path didn't have a file name " + p);
		}
		return fileName.toString().replace(	".xml",
											"");
	}

	private boolean isFullMigration( final Path first ) {
		final MigrationType migration = deserializier.get(first);
		return action == Action.MIGRATION && migration.getUp().getDatabase() != null
				&& dbDoesntExit(migration.getUp().getDatabase().getName());
	}

	private void initMigrationTracker( final MigrationContext migrationContext ) {
		if ( fullMigration ) {
			final String name = migrationContext.getMigration().getUp().getDatabase().getName();
			dbContext.update(name);
			this.dbContext.db.createCollection(MIGRATION_COLLECTION);
		}

		fullMigration = false;
	}

	private boolean dbDoesntExit( final String name ) {
		ArangoDatabase db = this.dbContext.driver.db(name);
		try {
			db.getInfo();
			return false;
		} catch ( ArangoDBException e ) {
			return true;
		}
	}

	private void recordMigration( final MigrationContext migrationContext ) {
		initMigrationTracker(migrationContext);
		final BaseDocument bd = new BaseDocument();
		bd.addAttribute("name",
						migrationContext.getMigrationVersion());
		bd.addAttribute("action",
						action);
		bd.addAttribute("applied",
						new Date());
		dbContext.db.collection(MIGRATION_COLLECTION).insertDocument(bd);
	}

	private boolean notApplied( final String migrationName ) {
		if ( fullMigration ) {
			return true;
		}
		final Map<String, Object> params = new HashMap<>();
		params.put(	"action",
					action);
		params.put(	"name",
					migrationName);
		int countSize = -1;
		try (ArangoCursor<BaseDocument> cursor = dbContext.db.query(MIGRATION_APPLIED_QUERY,
																	params,
																	null,
																	BaseDocument.class) ) {
			
			
			countSize = cursor.hasNext() ? -1 : 0;
		} catch ( IOException e ) {
			throw new IllegalArgumentException("Couldn't find migration status for " + migrationName, e);
		} catch (NullPointerException npe) {
			System.err.println("Probably ok. Bug in ArangoDB driver for auto closed cursors.");
			npe.printStackTrace();
		}

		return countSize == 0;
	}

}
