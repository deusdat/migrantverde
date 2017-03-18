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

import static com.deusdatsolutions.migrantverde.handlers.Replacer.replaceAll;

import java.util.List;
import java.util.Map;

import com.arangodb.ArangoDBException;
import com.arangodb.model.UserCreateOptions;
import com.deusdatsolutions.migrantverde.DBContext;
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

	private Map<String, String> lookup;

	public DatabaseHandler( Map<String, String> lookup ) {
		this.lookup = lookup;
	}

	@Override
	public void migrate( final DatabaseOperationType migration, final DBContext ctx ) {
		final ActionType action = migration.getAction();
		final String name = migration.getName();
		switch ( action ) {
			case CREATE:
				final List<User> users = migration.getUser();
				ctx.driver.createDatabase(name);
				ctx.db = ctx.driver.db(name);
				
				for ( final User u : users ) {
					String username = replaceAll(	u.getUsername(),
													this.lookup);
					String password = replaceAll(	u.getPassword(),
													this.lookup);
					UserCreateOptions options = new UserCreateOptions();
					options.active(true);
					
					try {
						ctx.driver.createUser(username, password, options);
					} catch ( ArangoDBException e ) {
						if(e.getResponseCode() == 409) {
							System.err.println("User " + username + " already exists");
						} else {
							e.printStackTrace();
						}
					}

					ctx.db.grantAccess(username);
				}
				break;
			case DROP:
				break;
			default:
				throw new IllegalStateException("Can't handle " + action);
		}
	}

}
