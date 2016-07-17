package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;

/**
 * A command pattern for reacting to various migration configurations.
 * 
 * @author J Patrick Davenport
 *
 * @param <T>
 */
public interface IMigrationHandler<T> {
	/**
	 * A standard way that the migrations will be applied.
	 * 
	 * @param migration
	 * @param driver
	 * @throws ArangoException
	 *             if Arango doesn't like something.
	 * @throws IllegalArgumentException
	 *             if the code validation doesn't like something from the migration.
	 */
	void migrate(final T migration, ArangoDriver driver) throws ArangoException;
}
