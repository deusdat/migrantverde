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
import java.util.LinkedList;
import java.util.List;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;
import com.arangodb.entity.IndexEntity;
import com.arangodb.entity.IndexType;
import com.arangodb.entity.IndexesEntity;
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
	public void migrate( final IndexOperationType migration, final ArangoDriver driver ) throws ArangoException {
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

	private void drop( final IndexOperationType migration, final ArangoDriver driver ) throws ArangoException {
		final String collName = migration.getName();
		final IndexesEntity indexes = driver.getIndexes(collName);
		for ( final IndexEntity ie : indexes.getIndexes() ) {
			final IndexEntityWrapper wrapper = new IndexEntityWrapper(ie);
			if ( migration.equals(wrapper) ) {
				driver.deleteIndex(ie.getId());
				break;
			}
		}
	}

	private void create( final IndexOperationType migration, final ArangoDriver driver ) throws ArangoException {
		final IndexKindType type = migration.getType();
		final String[] fields = migration.getField().toArray(new String[0]);
		final String collection = migration.getName();
		switch ( type ) {
			case FULLTEXT:
				driver.createFulltextIndex(	collection,
											migration.getMinLength().intValue(),
											fields);
				break;
			case PERSISTENT:
			case GEO:
			case HASH:
			case SKIPLIST:
				final IndexType indexType = to(type);
				driver.createIndex(	collection,
									indexType,
									migration.isUnique(),
									migration.isSparse(),
									fields);
				break;
			default:
				break;

		}
	}

	private IndexType to( final IndexKindType ikt ) {
		IndexType result;
		switch ( ikt ) {
			case PERSISTENT:
				result = IndexType.PERSISTENT;
				break;
			case FULLTEXT:
				result = IndexType.FULLTEXT;
				break;
			case GEO:
				result = IndexType.GEO;
				break;
			case HASH:
				result = IndexType.HASH;
				break;
			case SKIPLIST:
				result = IndexType.SKIPLIST;
				break;
			default:
				throw new IllegalArgumentException("Can't convert " + ikt);
		}
		return result;
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
			return ie.getFields() == null ? new LinkedList<String>() : ie.getFields();
		}

		@Override
		public boolean isUnique() {
			return ie.isUnique();
		}

		@Override
		public boolean isSparse() {
			return ie.isSparse();
		}

		@Override
		public BigInteger getMinLength() {
			return BigInteger.valueOf(ie.getMinLength());
		}

		@Override
		public BigInteger getSize() {
			return BigInteger.valueOf(ie.getSize());
		}

		@Override
		public IndexKindType getType() {
			// TODO Replace with a switch, as bad-ass as this is. It's more bad,
			// ass
			// FIXME NEVER!
			final IndexKindType ikt = ie.getType() == IndexType.FULLTEXT ? IndexKindType.FULLTEXT
					: ie.getType() == IndexType.PERSISTENT ? IndexKindType.PERSISTENT
							: ie.getType() == IndexType.GEO ? IndexKindType.GEO
									: ie.getType() == IndexType.HASH ? IndexKindType.HASH
											: ie.getType() == IndexType.SKIPLIST ? IndexKindType.SKIPLIST : null;
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
