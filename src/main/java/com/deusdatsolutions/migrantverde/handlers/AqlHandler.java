package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;

/**
 * Supports converting an AQL configuration into a realized executiong.
 * 
 * @author J Patrick Davenport
 *
 */
public class AqlHandler implements IMigrationHandler<String> {

	@Override
	public void migrate(final String migration, final ArangoDriver driver) throws ArangoException {
		driver.executeAqlQuery(migration, null, null, null);
	}

}
