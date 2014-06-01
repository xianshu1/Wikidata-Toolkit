package org.wikidata.wdtk.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.rio.RDFHandlerException;
import org.wikidata.wdtk.datamodel.interfaces.Reference;
import org.wikidata.wdtk.datamodel.interfaces.Snak;
import org.wikidata.wdtk.datamodel.interfaces.SnakGroup;

/**
 * This class supports the conversion of references to RDF. It buffers
 * references to avoid duplicates and to allow reference triples to be
 * serialized separately (for more efficient encodings in syntaxes like Turtle
 * or RDF/XML).
 * 
 * @author Markus Kroetzsch
 * 
 */
public class ReferenceRdfConverter {

	final RdfWriter rdfWriter;
	final SnakRdfConverter snakRdfConverter;

	final List<Reference> referenceQueue;
	final List<Resource> referenceSubjectQueue;
	final HashSet<Resource> declaredReferences;

	/**
	 * Constructor.
	 * 
	 * @param rdfWriter
	 *            object to use for constructing URI objects
	 * @param snakRdfConverter
	 *            object to use for writing snaks
	 */
	public ReferenceRdfConverter(RdfWriter rdfWriter,
			SnakRdfConverter snakRdfConverter) {
		this.rdfWriter = rdfWriter;
		this.snakRdfConverter = snakRdfConverter;

		this.referenceQueue = new ArrayList<Reference>();
		this.referenceSubjectQueue = new ArrayList<Resource>();
		this.declaredReferences = new HashSet<Resource>();
	}

	/**
	 * Adds the given reference to the list of references that should still be
	 * serialized, and returns the RDF resource that will be used as a subject.
	 * 
	 * @param reference
	 *            the reference to be serialized
	 * @return RDF resource that represents this reference
	 */
	public Resource addReference(Reference reference) {
		String referenceUri = Vocabulary.getReferenceUri(reference);
		Resource resource = this.rdfWriter.getUri(referenceUri);

		this.referenceQueue.add(reference);
		this.referenceSubjectQueue.add(resource);

		return resource;
	}

	/**
	 * Writes references that have been added recently. Auxiliary triples that
	 * are generated for serializing snaks in references will be written right
	 * afterwards. This will also trigger any other auxiliary triples to be
	 * written that the snak converter object may have buffered.
	 * 
	 * @throws RDFHandlerException
	 *             if there was a problem writing the restrictions
	 */
	public void writeReferences() throws RDFHandlerException {
		Iterator<Reference> referenceIterator = this.referenceQueue.iterator();
		for (Resource resource : this.referenceSubjectQueue) {
			if (!this.declaredReferences.add(resource)) {
				continue;
			}
			Reference reference = referenceIterator.next();
			writeReference(reference, resource);
		}
		this.referenceSubjectQueue.clear();
		this.referenceQueue.clear();

		this.snakRdfConverter.writeAuxiliaryTriples();
	}

	void writeReference(Reference reference, Resource resource)
			throws RDFHandlerException {

		this.rdfWriter.writeTripleValueObject(resource, RdfWriter.RDF_TYPE,
				RdfWriter.WB_REFERENCE);
		for (SnakGroup snakGroup : reference.getSnakGroups()) {
			this.snakRdfConverter.setSnakContext(resource,
					PropertyContext.REFERENCE);
			for (Snak snak : snakGroup.getSnaks()) {
				snak.accept(this.snakRdfConverter);
			}
		}
	}
}
