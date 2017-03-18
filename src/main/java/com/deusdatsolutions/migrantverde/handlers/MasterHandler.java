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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.deusdatsolutions.migrantverde.Action;
import com.deusdatsolutions.migrantverde.DBContext;
import com.deusdatsolutions.migrantverde.MigrationContext;
import com.deusdatsolutions.migrantverde.jaxb.ArangoFunctionType;
import com.deusdatsolutions.migrantverde.jaxb.CollectionOperationType;
import com.deusdatsolutions.migrantverde.jaxb.DatabaseOperationType;
import com.deusdatsolutions.migrantverde.jaxb.IndexOperationType;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType.Down;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType.Up;

/**
 * The touch point for all of the specific handlers like CollectionHandler.
 * Essentially a multi-method implementation like one finds in Clojure.
 * 
 * @author J Patrick Davenport
 *
 */
public class MasterHandler {

	private final Map<Class<?>, IMigrationHandler<?>> handlers;
	private final Action action;
	private final DBContext dbContext;

	private static Map<Class<?>, IMigrationHandler<?>> createHandlers( Map<String, String> lookup ) {
		final Map<Class<?>, IMigrationHandler<?>> HANDLERS = new HashMap<>();
		HANDLERS.put(	CollectionOperationType.class,
						new CollectionHandler());
		HANDLERS.put(	DatabaseOperationType.class,
						new DatabaseHandler(lookup));
		HANDLERS.put(	IndexOperationType.class,
						new IndexHandler());
		HANDLERS.put(	ArangoFunctionType.class,
						new FunctionHandler());
		HANDLERS.put(	String.class,
						new AqlHandler(lookup));
		return HANDLERS;
	}

	public MasterHandler(	final Action action,
							final DBContext dbContext ) {
		this(createHandlers(new HashMap<String, String>()), action, dbContext);
	}

	public MasterHandler(	final Action action,
							final DBContext dbContext,
							final Map<String, String> lookup ) {
		this(createHandlers(lookup), action, dbContext);
	}

	/**
	 * Constructor that allows the handlers to be overridden.
	 * 
	 * @param testable you're own migration handlers.
	 * @param action what action you're trying to perform.
	 * @param dbContext the driver and the specific database being migrated.
	 */
	protected MasterHandler(	final Map<Class<?>, IMigrationHandler<?>> testable,
								final Action action,
								final DBContext dbContext ) {
		this.handlers = testable;
		this.action = action;
		this.dbContext = dbContext;
	}

	@SuppressWarnings( "unchecked" )
	public void migrate( final MigrationContext migrationContext ) {
		@SuppressWarnings( "rawtypes" )
		IMigrationHandler handler = null;
		Object migrationConfig;
		final MigrationType migration = migrationContext.getMigration();
		if ( action == Action.MIGRATION ) {
			migrationConfig = input(migration.getUp());
		} else {
			migrationConfig = input(migration.getDown());
		}

		handler = this.handlers.get(migrationConfig.getClass());
		handler.migrate(migrationConfig,
						this.dbContext);
	}

	private Object input( final Down down ) {
		return find(down);
	}

	private Object input( final Up up ) {
		return find(up);
	}

	/**
	 * Finds the first non-null getter value. Since the XSD says you can only
	 * have one such field in the up or down slots, we're protected from having
	 * to chose.
	 * 
	 * @param in
	 * @return the
	 * @throws IllegalArgumentException
	 *             if anything goes bad invoking the getter.
	 */
	private Object find( final Object in ) {
		final Method[] methods = in.getClass().getDeclaredMethods();
		Object r = null;
		for ( final Method m : methods ) {
			if ( m.getName().startsWith("get") && m.getParameterCount() == 0 ) {
				try {
					r = m.invoke(in);
				} catch ( final Exception ex ) {
					throw new IllegalArgumentException("Couldn't process up/down configuration", ex);
				}
				if ( r != null ) {
					break;
				}
			}
		}
		return r;
	}
}
