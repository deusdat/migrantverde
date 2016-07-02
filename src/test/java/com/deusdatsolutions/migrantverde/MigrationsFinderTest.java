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
		final SortedSet<Path> migrations = finder.migrations("/one", Action.MIGRATION);
		System.out.println(migrations);
		assertEquals("Size was one", 1, migrations.size());
	}

	@Test
	public void shouldFindThreeSorted() {
		final MigrationsFinder finder = new MigrationsFinder();
		final SortedSet<Path> migrations = finder.migrations("/multiple", Action.MIGRATION);

		assertEquals("Should have 3", 3, migrations.size());
		int counter = 1;
		for (final Path path : migrations) {
			assertTrue(path.endsWith(counter + ".xml"));
			counter++;
		}
	}
}
