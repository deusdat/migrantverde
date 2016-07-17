package com.deusdatsolutions.migrantverde.handlers;

import java.util.List;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.UserEntity;
import com.deusdatsolutions.migrantverde.jaxb.ActionType;
import com.deusdatsolutions.migrantverde.jaxb.DatabaseOperationType;
import com.deusdatsolutions.migrantverde.jaxb.DatabaseOperationType.User;

/**
 * Reactor to the database tag.
 * 
 * @author J Patrick Davenport
 *
 */
public class DatabaseHandler implements IMigrationHandler<DatabaseOperationType> {

	@Override
	public void migrate(final DatabaseOperationType migration, final ArangoDriver driver) throws ArangoException {
		final ActionType action = migration.getAction();
		final String name = migration.getName();
		switch (action) {
		case CREATE:
			final List<User> users = migration.getUser();
			final UserEntity[] withAccess = new UserEntity[users.size()];
			int i = 0;
			for (final User u : users) {
				final UserEntity ue = new UserEntity(u.getUsername(), u.getPassword(), true, null);
				withAccess[i] = ue;
				i++;
			}
			driver.createDatabase(name, withAccess);
			break;
		case DROP:
			break;
		default:
			throw new IllegalStateException("Can't handle " + action);
		}
	}

}
