package com.deusdatsolutions.migrantverde.handlers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.deusdatsolutions.migrantverde.Action;
import com.deusdatsolutions.migrantverde.MigrationContext;
import com.deusdatsolutions.migrantverde.MigrationException;
import com.deusdatsolutions.migrantverde.jaxb.ArangoFunctionType;
import com.deusdatsolutions.migrantverde.jaxb.CollectionOperationType;
import com.deusdatsolutions.migrantverde.jaxb.DatabaseOperationType;
import com.deusdatsolutions.migrantverde.jaxb.IndexOperationType;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType.Down;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType.Up;

/**
 * The touch point for all of the specific handlers like CollectionHandler. Essentially a multi-method implementation
 * like one finds in Clojure.
 * 
 * @author J Patrick Davenport
 *
 */
public class MasterHandler {
	private static final Map<Class<?>, IMigrationHandler<?>> HANDLERS = new HashMap<>();
	static {
		HANDLERS.put(CollectionOperationType.class, new CollectionHandler());
		HANDLERS.put(DatabaseOperationType.class, new DatabaseHandler());
		HANDLERS.put(IndexOperationType.class, new IndexHandler());
		HANDLERS.put(ArangoFunctionType.class, new FunctionHandler());
		HANDLERS.put(String.class, new AqlHandler());
	}

	private final Map<Class<?>, IMigrationHandler<?>> handlers;
	private final Action action;
	private final ArangoDriver driver;

	public MasterHandler(	final Action action,
							final ArangoDriver driver) {
		this(HANDLERS, action, driver);
	}

	public MasterHandler(	final Map<Class<?>, IMigrationHandler<?>> testable,
							final Action action,
							final ArangoDriver driver) {
		this.handlers = testable;
		this.action = action;
		this.driver = driver;
	}

	@SuppressWarnings("unchecked")
	public void migrate(final MigrationContext migrationContext) {
		@SuppressWarnings("rawtypes")
		IMigrationHandler handler = null;
		Object migrationConfig;
		final MigrationType migration = migrationContext.getMigration();
		if (action == Action.MIGRATION) {
			migrationConfig = input(migration.getUp());
		} else {
			migrationConfig = input(migration.getDown());
		}

		handler = this.handlers.get(migrationConfig.getClass());
		try {
			handler.migrate(migrationConfig, driver);
		} catch (final ArangoException e) {
			throw new MigrationException("Could migrate: " + migrationConfig, e);
		}
	}

	private Object input(final Down down) {
		return find(down);
	}

	private Object input(final Up up) {
		return find(up);
	}

	/**
	 * Finds the first non-null getter value. Since the XSD says you can only have one such field in the up or down
	 * slots, we're protected from having to chose.
	 * 
	 * @param in
	 * @return the
	 * @throws IllegalArgumentException
	 *             if anything goes bad invoking the getter.
	 */
	private Object find(final Object in) {
		final Method[] methods = in.getClass().getDeclaredMethods();
		Object r = null;
		for (final Method m : methods) {
			if (m.getName().startsWith("get") && m.getParameterCount() == 0) {
				try {
					r = m.invoke(in);
				} catch (final Exception ex) {
					throw new IllegalArgumentException("Couldn't process up/down configuration", ex);
				}
				if (r != null) {
					break;
				}
			}
		}
		return r;
	}
}
