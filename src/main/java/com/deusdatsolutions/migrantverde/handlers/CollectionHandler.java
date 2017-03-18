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

import com.arangodb.model.CollectionCreateOptions;
import com.deusdatsolutions.migrantverde.DBContext;
import com.deusdatsolutions.migrantverde.jaxb.CollectionOperationType;

/**
 * Applies the logic for operating on collection tags.
 * 
 * @author J Patrick Davenport
 *
 */
public class CollectionHandler implements IMigrationHandler<CollectionOperationType> {

	@Override
	public void migrate( final CollectionOperationType migration, final DBContext driver ) {
		final String name = migration.getName();
		switch ( migration.getAction() ) {
			case CREATE:
				
				final CollectionCreateOptions co = new CollectionCreateOptions();
				
				co.doCompact(migration.isCompactable());
				co.isVolatile(migration.isVolatile());
				if ( migration.getJournalSize() != null ) {
					co.journalSize(migration.getJournalSize());
				}
				if ( migration.getNumberOfShards() != null ) {
					co.numberOfShards(migration.getNumberOfShards());
				}
				co.waitForSync(migration.isWaitForSync());

				if ( !migration.getShardKey().isEmpty() ) {
					co.shardKeys((String[]) migration.getShardKey().toArray());
				}
				driver.db.createCollection(name,
										co);
				break;
			case DROP:
				driver.db.collection(name).drop();;
				break;
			default:
				throw new IllegalArgumentException("Can't hand migration " + migration.getAction());
		}
	}

}
