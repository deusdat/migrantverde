package com.deusdatsolutions.migrantverde;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.deusdatsolutions.migrantverde.jaxb.MigrationType;

public class Deserializier {
	private final JAXBContext ctx;
	private final Unmarshaller jaxbUnmarshaller;

	public Deserializier() {
		super();
		try {
			this.ctx = JAXBContext.newInstance("com.deusdatsolutions.migrantverde.jaxb");
			this.jaxbUnmarshaller = ctx.createUnmarshaller();
		} catch (final JAXBException ex) {
			throw new IllegalArgumentException("Can't create JAXB context", ex);
		}
	}

	@SuppressWarnings("unchecked")
	public MigrationType get(final Path xml) {
		try {
			final JAXBElement<MigrationType> unmarshal = (JAXBElement<MigrationType>) this.jaxbUnmarshaller
					.unmarshal(Files.newInputStream(xml, StandardOpenOption.READ));
			return unmarshal.getValue();
		} catch (final Exception ex) {
			throw new MigrationException("Couldn't process path " + xml, ex);
		}
	}

}
