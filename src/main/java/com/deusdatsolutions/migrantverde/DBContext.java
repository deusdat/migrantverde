package com.deusdatsolutions.migrantverde;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDatabase;

public class DBContext {
	public final ArangoDB driver;
	public ArangoDatabase db;

	public DBContext(	ArangoDB driver,
						String db ) {
		super();
		this.driver = driver;
		this.db = driver.db(db);
	}
	
	public DBContext update(String dbName) {
		this.db = driver.db(dbName);
		return this;
	}

}
