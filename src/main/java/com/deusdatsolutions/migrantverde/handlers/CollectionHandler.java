package com.deusdatsolutions.migrantverde.handlers;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.CollectionOptions;
import com.deusdatsolutions.migrantverde.jaxb.CollectionOperationType;

/**
 * Applies the logic for operating on collection tags.
 * 
 * @author J Patrick Davenport
 *
 */
public class CollectionHandler implements IMigrationHandler<CollectionOperationType> {

	@Override
	public void migrate( final CollectionOperationType migration, final ArangoDriver driver ) throws ArangoException {
		final String name = migration.getName();
		switch ( migration.getAction() ) {
			case CREATE:
				final CollectionOptions co = new CollectionOptions();
				co.setDoCompact(migration.isCompactable());
				co.setIsVolatile(migration.isVolatile());
				if ( migration.getJournalSize() != null ) {
					co.setJournalSize(migration.getJournalSize());
				}
				if ( migration.getNumberOfShards() != null ) {
					co.setNumberOfShards(migration.getNumberOfShards());
				}
				co.setWaitForSync(migration.isWaitForSync());

				if ( !migration.getShardKey().isEmpty() ) {
					co.setShardKeys(migration.getShardKey());
				}
				driver.createCollection(name,
										co);
				break;
			case DROP:
				driver.deleteCollection(name);
				break;
			default:
				throw new IllegalArgumentException("Can't hand migration " + migration.getAction());
		}
	}

}
