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

	public MigrationContext(	final String migrationVersion,
								final MigrationType migration ) {
		super();
		this.migrationVersion = migrationVersion;
		this.migration = migration;
	}
}
