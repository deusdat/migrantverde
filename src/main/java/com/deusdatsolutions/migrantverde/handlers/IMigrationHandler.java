package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;

public interface IMigrationHandler<T> {
	void migrate(final T migration, ArangoDriver driver) throws ArangoException;
}
