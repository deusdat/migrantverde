package com.deusdatsolutions.migrantverde.handlers;

import java.util.Map;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;

import static com.deusdatsolutions.migrantverde.handlers.Replacer.*;
/**
 * Supports converting an AQL configuration into a realized executiong.
 * 
 * @author J Patrick Davenport
 *
 */
public class AqlHandler implements IMigrationHandler<String> {
	private final Map<String, String> lookup;
	public AqlHandler(Map<String, String> lookup) {
		this.lookup = lookup;
	}

	@Override
	public void migrate(final String migration, final ArangoDriver driver) throws ArangoException {
		driver.executeAqlQuery(replaceAll(migration, lookup), null, null, null);
	}

}
