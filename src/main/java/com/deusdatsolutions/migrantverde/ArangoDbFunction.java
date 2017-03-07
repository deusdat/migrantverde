package com.deusdatsolutions.migrantverde;

import com.arangodb.ArangoDriver;
import com.arangodb.ArangoException;

/**
 * Common interface for applying arbitrary code to an ArangoDB.
 * 
 * @author J Patrick Davenport
 *
 */
public interface ArangoDbFunction {
	void apply( final ArangoDriver driver ) throws ArangoException;
}
