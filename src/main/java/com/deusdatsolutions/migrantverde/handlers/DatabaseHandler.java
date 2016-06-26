package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;
import com.deusdatsolutions.migrantverde.jaxb.NamedOperationType;

public class DatabaseHandler implements IMigrationHandler<NamedOperationType> {

	@Override
	public void migrate(final NamedOperationType migration, final ArangoDriver driver) {
		// TODO Auto-generated method stub

	}

}
