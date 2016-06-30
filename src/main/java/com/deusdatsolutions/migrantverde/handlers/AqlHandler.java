package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;

public class AqlHandler implements IMigrationHandler<String> {

	@Override
	public void migrate(final String migration, final ArangoDriver driver) throws ArangoException {
		driver.executeAqlQuery(migration, null, null, null);
	}

}
