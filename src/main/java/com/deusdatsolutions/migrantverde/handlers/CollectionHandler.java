package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.deusdatsolutions.migrantverde.jaxb.CollectionOperationType;

public class CollectionHandler implements IMigrationHandler<CollectionOperationType> {

	@Override
	public void migrate(final CollectionOperationType migration, final ArangoDriver driver) throws ArangoException {
		switch (migration.getAction()) {
		case CREATE:
			driver.createCollection(migration.getName());
			break;
		case DROP:
			driver.deleteCollection(migration.getName());
			break;
		default:
			throw new IllegalArgumentException("Can't hand migration " + migration.getAction());
		}
	}

}
