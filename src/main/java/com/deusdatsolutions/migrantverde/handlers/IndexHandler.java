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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.model.FulltextIndexOptions;
import com.arangodb.model.GeoIndexOptions;
import com.arangodb.model.HashIndexOptions;
import com.arangodb.model.PersistentIndexOptions;
import com.arangodb.model.SkiplistIndexOptions;
import com.deusdatsolutions.migrantverde.DBContext;
import com.deusdatsolutions.migrantverde.jaxb.ActionType;
import com.deusdatsolutions.migrantverde.jaxb.IndexKindType;
import com.deusdatsolutions.migrantverde.jaxb.IndexOperationType;

/**
 * Reactor for the index configuration.
 * 
 * @author J Patrick Davenport
 *
 */
public class IndexHandler implements IMigrationHandler<IndexOperationType> {

	@Override
	public void migrate( final IndexOperationType migration, final DBContext driver ) {
		final ActionType action = migration.getAction();
		switch ( action ) {
			case CREATE:
				create(	migration,
						driver);
				break;
			case DROP:
				drop(	migration,
						driver);
				break;
			default:
				throw new IllegalArgumentException("Unable to apply " + action + " to an index");
		}
	}

	private void drop( final IndexOperationType migration, final DBContext ctx ) {
		final String collName = migration.getName();
		Collection<IndexEntity> indexes = ctx.db.collection(collName).getIndexes();
		for ( final IndexEntity ie : indexes ) {
			final IndexEntityWrapper wrapper = new IndexEntityWrapper(ie);
			if ( migration.equals(wrapper) ) {
				ctx.db.deleteIndex(wrapper.getName());
				break;
			}
		}
	}

	private void create( final IndexOperationType migration, final DBContext ctx ) {
		final IndexKindType type = migration.getType();

		final String[] fields = migration.getField().toArray(new String[0]);
		final String collection = migration.getName();

		switch ( type ) {
			case FULLTEXT:
				FulltextIndexOptions options = new FulltextIndexOptions();
				options.minLength(migration.getMinLength().intValue());
				ctx.db.collection(collection).createFulltextIndex(	Arrays.asList(fields),
																	options);
				break;
			case PERSISTENT:
				ctx.db.collection(collection).createPersistentIndex(Arrays.asList(fields),
																	new PersistentIndexOptions()
																			.sparse(migration.isSparse())
																			.unique(migration.isUnique()));
				break;
			case GEO:
				ctx.db.collection(collection).createGeoIndex(	Arrays.asList(fields),
																new GeoIndexOptions()
																		.geoJson(migration.isGeoJson()));
				break;
			case HASH:
				ctx.db.collection(collection).createHashIndex(	Arrays.asList(fields),
																new HashIndexOptions()
																		.sparse(migration.isSparse())
																		.unique(migration.isUnique()));
				break;
			case SKIPLIST:
				ctx.db.collection(collection).createSkiplistIndex(	Arrays.asList(fields),
																	new SkiplistIndexOptions()
																			.sparse(migration.isSparse())
																			.unique(migration.isUnique()));
				break;
			default:
				break;

		}
	}

	private static final class IndexEntityWrapper extends IndexOperationType {
		private final IndexEntity ie;

		public IndexEntityWrapper( final IndexEntity ie ) {
			super();
			if ( ie == null ) {
				throw new NullPointerException("IndexEntity can't be null");
			}
			this.ie = ie;
		}

		@Override
		public List<String> getField() {
			return ie.getFields() == null ? new LinkedList<String>() : new LinkedList<>(ie.getFields());
		}

		@Override
		public boolean isUnique() {
			return ie.getUnique();
		}

		@Override
		public boolean isSparse() {
			return ie.getSparse();
		}

		@Override
		public BigInteger getMinLength() {
			return BigInteger.valueOf(ie.getMinLength());
		}

		@Override
		public IndexKindType getType() {
			// TODO Replace with a switch, as bad-ass as this is. It's more bad, ass.
			// FIXME NEVER!
			final IndexKindType ikt = ie.getType() == IndexType.fulltext ? IndexKindType.FULLTEXT
					: ie.getType() == IndexType.persistent ? IndexKindType.PERSISTENT
							: ie.getType() == IndexType.geo ? IndexKindType.GEO
									: ie.getType() == IndexType.hash ? IndexKindType.HASH
											: ie.getType() == IndexType.skiplist ? IndexKindType.SKIPLIST : null;
			if ( ikt == null ) {
				throw new IllegalArgumentException("Could not convert: " + ie.getType());
			}
			return ikt;
		}

		@Override
		public boolean equals( final Object obj ) {
			boolean result = false;
			if ( obj != null && (obj instanceof IndexEntityWrapper) ) {
				// This seems logically the best answer to the indexes being
				// equal.
				// Can we actually have full text index on the same field with
				// two different minLengths?
				// Can we have to cap indexes? I'd have to say.
				final IndexEntityWrapper in = (IndexEntityWrapper) obj;
				result = this.getType() == in.getType() & this.getField().equals(in.getField());
			}
			return result;
		}

		@Override
		public int hashCode() {
			return this.ie.hashCode();
		}
	}
}
