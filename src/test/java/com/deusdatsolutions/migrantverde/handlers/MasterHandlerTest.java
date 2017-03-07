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
package com.deusdatsolutions.migrantverde.handlers;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.deusdatsolutions.migrantverde.Action;
import com.deusdatsolutions.migrantverde.MigrationContext;
import com.deusdatsolutions.migrantverde.jaxb.CollectionOperationType;
import com.deusdatsolutions.migrantverde.jaxb.DatabaseOperationType;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType.Down;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType.Up;

public class MasterHandlerTest {
	private final CollectionHandler colHandler = Mockito.mock(CollectionHandler.class);
	private final DatabaseHandler databaseHandler = Mockito.mock(DatabaseHandler.class);
	private final AqlHandler aqlHandler = Mockito.mock(AqlHandler.class);
	private final ArangoDriver mockDriver = Mockito.mock(ArangoDriver.class);
	private final MasterHandler forMigration;
	private final MasterHandler forRollback;

	final Map<Class<?>, IMigrationHandler<?>> handlers = new HashMap<>();

	{
		handlers.put(	CollectionOperationType.class,
						colHandler);
		handlers.put(	DatabaseOperationType.class,
						databaseHandler);
		handlers.put(	String.class,
						aqlHandler);

		forMigration = new MasterHandler(handlers, Action.MIGRATION, mockDriver);
		forRollback = new MasterHandler(handlers, Action.ROLLBACK, mockDriver);
	}

	@Test
	public void shouldCallMigrateFakeCollection() throws ArangoException {
		final MigrationType mt = new MigrationType();
		final Up up = new Up();
		up.setCollection(new CollectionOperationType());
		mt.setUp(up);

		forMigration.migrate(new MigrationContext("", mt));

		Mockito.verify(	colHandler,
						Mockito.times(1))
				.migrate(	mt.getUp().getCollection(),
							mockDriver);
	}

	@Test
	public void shouldCallMigrateFakeDatabase() throws ArangoException {
		final MigrationType mt = new MigrationType();
		final Up up = new Up();
		up.setDatabase(new DatabaseOperationType());
		mt.setUp(up);

		forMigration.migrate(new MigrationContext("", mt));

		Mockito.verify(	databaseHandler,
						Mockito.times(1))
				.migrate(	mt.getUp().getDatabase(),
							mockDriver);
	}

	@Test
	public void shouldCallMigrateFakeAQL() throws ArangoException {
		final MigrationType mt = new MigrationType();
		final Up up = new Up();
		up.setAql("FOR u IN users RETURN u");
		mt.setUp(up);

		forMigration.migrate(new MigrationContext("", mt));

		Mockito.verify(	aqlHandler,
						Mockito.times(1))
				.migrate(	mt.getUp().getAql(),
							mockDriver);
	}

	@Test
	public void shouldCallRollbackFakeCollection() throws ArangoException {
		final MigrationType mt = new MigrationType();
		final Down down = new Down();
		down.setCollection(new CollectionOperationType());
		mt.setDown(down);

		forRollback.migrate(new MigrationContext("", mt));

		Mockito.verify(	colHandler,
						Mockito.times(1))
				.migrate(	mt.getDown().getCollection(),
							mockDriver);
	}

	@Test
	public void shouldCallRollbackFakeDatabase() throws ArangoException {
		final MigrationType mt = new MigrationType();
		final Down down = new Down();
		down.setDatabase(new DatabaseOperationType());
		mt.setDown(down);

		forRollback.migrate(new MigrationContext("", mt));

		Mockito.verify(	databaseHandler,
						Mockito.times(1))
				.migrate(	mt.getDown().getDatabase(),
							mockDriver);
	}

	@Test
	public void shouldCallRollbackFakeAQL() throws ArangoException {
		final MigrationType mt = new MigrationType();
		final Down down = new Down();
		down.setAql("FOR u IN users RETURN u");
		mt.setDown(down);

		forRollback.migrate(new MigrationContext("", mt));

		Mockito.verify(	aqlHandler,
						Mockito.times(1))
				.migrate(	mt.getDown().getAql(),
							mockDriver);
	}
}
