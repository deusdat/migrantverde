package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;

public interface IMigrationHandler<T> {
	void migrate(final T migration, ArangoDriver driver);
}
