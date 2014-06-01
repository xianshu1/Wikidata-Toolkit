package org.wikidata.wdtk.rdf.values;

import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikidata.wdtk.datamodel.interfaces.PropertyIdValue;
import org.wikidata.wdtk.rdf.PropertyTypes;
import org.wikidata.wdtk.rdf.OwlDeclarationBuffer;
import org.wikidata.wdtk.rdf.RdfWriter;

public abstract class AbstractValueConverter<V extends org.wikidata.wdtk.datamodel.interfaces.Value>
		implements ValueConverter<V> {

	final PropertyTypes propertyTypes;
	final RdfWriter rdfWriter;
	final OwlDeclarationBuffer rdfConversionBuffer;

	static final Logger logger = LoggerFactory.getLogger(ValueConverter.class);

	public AbstractValueConverter(RdfWriter rdfWriter,
			PropertyTypes propertyTypes, OwlDeclarationBuffer rdfConversionBuffer) {
		this.rdfWriter = rdfWriter;
		this.propertyTypes = propertyTypes;
		this.rdfConversionBuffer = rdfConversionBuffer;
	}

	@Override
	public void writeAuxiliaryTriples() throws RDFHandlerException {
		// default implementation: no auxiliary triples
	}

	/**
	 * Logs a message for a case where the value of a property does not fit to
	 * its declared datatype.
	 * 
	 * @param propertyIdValue
	 *            the property that was used
	 * @param datatype
	 *            the declared type of the property
	 * @param valueType
	 *            a string to denote the type of value
	 */
	protected void logIncompatibleValueError(PropertyIdValue propertyIdValue,
			String datatype, String valueType) {
		logger.warn("Property " + propertyIdValue.getId() + " has type \""
				+ datatype + "\" but a value of type " + valueType
				+ ". Data ignored.");
	}
}
