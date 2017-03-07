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

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.SortedSet;

import org.junit.Test;

/**
 * Makes sure that we can properly get the
 * 
 * @author J Patrick Davenport
 *
 */
public class MigrationsFinderTest {

	@Test
	public void shouldFindOneFile() {
		final MigrationsFinder finder = new MigrationsFinder();
		final SortedSet<Path> migrations = finder.migrations(	"/one",
																Action.MIGRATION);
		System.out.println(migrations);
		assertEquals(	"Size was one",
						1,
						migrations.size());
	}

	@Test
	public void shouldFindThreeSorted() {
		final MigrationsFinder finder = new MigrationsFinder();
		final SortedSet<Path> migrations = finder.migrations(	"/multiple",
																Action.MIGRATION);

		assertEquals(	"Should have 3",
						3,
						migrations.size());
		int counter = 1;
		for ( final Path path : migrations ) {
			assertTrue(path.endsWith(counter + ".xml"));
			counter++;
		}
	}
}
