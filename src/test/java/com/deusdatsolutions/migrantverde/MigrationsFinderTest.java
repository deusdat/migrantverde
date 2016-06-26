package com.deusdatsolutions.migrantverde;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.util.SortedSet;

import org.junit.Test;

public class MigrationsFinderTest {
	@Test
	public void shouldFindOneFile() {
		final MigrationsFinder finder = new MigrationsFinder();
		final SortedSet<Path> migrations = finder.migrations("/one");
		System.out.println(migrations);
		assertEquals("Size was one", 1, migrations.size());
	}

	@Test
	public void shouldFindThreeSorted() {
		final MigrationsFinder finder = new MigrationsFinder();
		final SortedSet<Path> migrations = finder.migrations("/multiple");

		assertEquals("Should have 3", 3, migrations.size());
		int counter = 1;
		for (final Path path : migrations) {
			assertTrue(path.endsWith(counter + ".xml"));
			counter++;
		}
	}
}
