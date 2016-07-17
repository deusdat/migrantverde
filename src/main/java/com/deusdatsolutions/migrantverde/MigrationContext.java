package com.deusdatsolutions.migrantverde;

import com.deusdatsolutions.migrantverde.jaxb.MigrationType;

/**
 * DTO for passing execution configuration.
 * 
 * @author J Patrick Davenport
 *
 */
public class MigrationContext {
	private final String migrationVersion;
	private final MigrationType migration;

	public String getMigrationVersion() {
		return migrationVersion;
	}

	public MigrationType getMigration() {
		return migration;
	}

	public MigrationContext(final String migrationVersion,
							final MigrationType migration) {
		super();
		this.migrationVersion = migrationVersion;
		this.migration = migration;
	}
}
