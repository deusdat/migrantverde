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

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.junit.Test;

import com.deusdatsolutions.migrantverde.jaxb.MigrationType;
import com.deusdatsolutions.migrantverde.jaxb.MigrationType.Up;

/**
 * Tests the process of loading a migration and the process of replacing
 * variables.
 * 
 * Needs two environment varaibles
 * <ul>
 * <li>ONE: Two</li>
 * <li>SOMETHING: else</li>
 * </ul>
 * 
 * @author J Patrick Davenport
 *
 */
public class DeserializerTest {

	@Test( expected = MigrationException.class )
	public void emptyPath() {
		final Deserializier d = new Deserializier();
		d.get(null);
	}

	@Test
	public void shouldHandleTestMigration() throws URISyntaxException {
		final Deserializier d = new Deserializier();

		final URL resource = getClass().getClassLoader().getResource("one/20160625123905.xml");
		assertNotNull(	"Should have found the resource locally",
						resource);

		final MigrationType migrationType = d.get(Paths.get(resource.toURI()));
		assertNotNull(migrationType);

		final Up up = migrationType.getUp();
		assertNull(up.getCollection());
		assertNull(up.getDatabase());
		assertEquals(	"Should be a simple FOR after trimming",
						"FOR",
						up.getAql().trim());

		assertNull(	"Should not not have a down grade",
					migrationType.getDown());
	}

	@Test
	public void shouldReplaceProperty() throws URISyntaxException {
		final Deserializier d = new Deserializier();

		final URL resource = getClass().getClassLoader().getResource("replacement/20160625123905.xml");
		assertNotNull(	"Should have found the resource locally",
						resource);

		final MigrationType migrationType = d.get(Paths.get(resource.toURI()));

		final Up up = migrationType.getUp();
		assertNull(up.getCollection());
		assertNull(up.getDatabase());
		assertEquals(	"Should be a simple 'FOR Two IN else' after trimming",
						"FOR Two in else",
						up.getAql().trim());

		assertNull(	"Should not not have a down grade",
					migrationType.getDown());
	}
}
