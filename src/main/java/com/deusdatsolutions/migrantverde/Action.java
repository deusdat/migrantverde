package com.deusdatsolutions.migrantverde;

/**
 * Represents the goal of the operation the library will commit.
 * 
 * @author J Patrick Davenport
 *
 */
public enum Action {
	/**
	 * An creative process for the database. It can be inserting records, manipulating attributes or document
	 * structures, or even deleting collections.
	 */
	MIGRATION,
	/**
	 * A destructive process. The twin of a migration.
	 */
	ROLLBACK
}
