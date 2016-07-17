package com.deusdatsolutions.migrantverde;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import com.deusdatsolutions.migrantverde.jaxb.MigrationType;

/**
 * Reads the configuration files. Enforces the XSD specified in the resources directory.
 * 
 * @author J Patrick Davenport
 *
 */
public class Deserializier {
	private final JAXBContext ctx;
	private final Unmarshaller jaxbUnmarshaller;

	public Deserializier() {
		super();
		try {
			this.ctx = JAXBContext.newInstance("com.deusdatsolutions.migrantverde.jaxb");
			this.jaxbUnmarshaller = ctx.createUnmarshaller();

			final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			final Schema migrationSchema = schemaFactory.newSchema(this.getClass().getResource("/migrations.xsd"));

			jaxbUnmarshaller.setSchema(migrationSchema);
			jaxbUnmarshaller.setEventHandler(new DefaultValidationEventHandler());
		} catch (final JAXBException | SAXException ex) {
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
