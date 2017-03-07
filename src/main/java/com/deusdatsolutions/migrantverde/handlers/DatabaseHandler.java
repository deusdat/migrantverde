/*
 * Copyright 2017 DeusDat Solutions Corp
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.deusdatsolutions.migrantverde.handlers;

import java.util.List;
import java.util.Map;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.UserEntity;
import com.deusdatsolutions.migrantverde.jaxb.ActionType;
import com.deusdatsolutions.migrantverde.jaxb.DatabaseOperationType;
import com.deusdatsolutions.migrantverde.jaxb.DatabaseOperationType.User;

import static com.deusdatsolutions.migrantverde.handlers.Replacer.replaceAll;

/**
 * Reactor to the database tag.
 * 
 * @author J Patrick Davenport
 *
 */
public class DatabaseHandler implements IMigrationHandler<DatabaseOperationType> {

	private Map<String, String> lookup;

	public DatabaseHandler( Map<String, String> lookup ) {
		this.lookup = lookup;
	}

	@Override
	public void migrate( final DatabaseOperationType migration, final ArangoDriver driver ) throws ArangoException {
		final ActionType action = migration.getAction();
		final String name = migration.getName();
		switch ( action ) {
			case CREATE:
				final List<User> users = migration.getUser();
				final UserEntity[] withAccess = new UserEntity[users.size()];
				int i = 0;
				for ( final User u : users ) {
					String username = replaceAll(	u.getUsername(),
													this.lookup);
					String password = replaceAll(	u.getPassword(),
													this.lookup);
					final UserEntity ue = new UserEntity(username, password, true, null);
					withAccess[i] = ue;
					i++;
				}
				driver.createDatabase(	name,
										withAccess);
				break;
			case DROP:
				break;
			default:
				throw new IllegalStateException("Can't handle " + action);
		}
	}

}
