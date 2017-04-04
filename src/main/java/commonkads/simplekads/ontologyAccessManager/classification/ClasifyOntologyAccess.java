package commonkads.simplekads.ontologyAccessManager.classification;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.util.iterator.ExtendedIterator;

public class ClasifyOntologyAccess {
	private static String nameSpace = "";
	private static Model model = ModelFactory.createOntologyModel();
	private static OntModel ontModel = null;

	public static void printIndividuals(String ontClassName) {
		OntClass ontClass = ontModel.getOntClass(nameSpace + ontClassName);
		ExtendedIterator<? extends OntResource> individuals = ontClass
				.listInstances();
		while (individuals.hasNext()) {
			OntResource d = (OntResource) individuals.next();
			System.out.println(d.getURI());
		}
	}

	public static ExtendedIterator<? extends OntResource> getIndividuals(
			String ontClassName) {
		OntClass ontClass = ontModel.getOntClass(nameSpace + ontClassName);
		ExtendedIterator<? extends OntResource> individuals = ontClass
				.listInstances();
		return individuals;
	}

	public static List<String> getDiseases(String symptomName) {
		List<String> result = new ArrayList<String>();

		OntResource ind = ontModel.getOntResource(nameSpace + symptomName);
		Property property = ontModel.getProperty(nameSpace + "evidenceOf");
		StmtIterator it = ind.listProperties(property);
		while (it.hasNext()) {
			Statement s = (Statement) it.next();
			Resource resource = s.getObject().asResource();
			result.add(resource.getLocalName());
		}
		return result;
	}

	public static void test1() {
		OntClass symptoms = ontModel.getOntClass(nameSpace + "Symptom");
		OntProperty nome = ontModel.getOntProperty(nameSpace + "causeOf");
		for (ExtendedIterator<? extends OntResource> instances = symptoms
				.listInstances(); instances.hasNext();) {
			OntResource symptomInstance = instances.next();
			// find out the resources that link to the instance
			for (StmtIterator stmts = ontModel.listStatements(null, null,
					symptomInstance); stmts.hasNext();) {
				Individual ind = stmts.next().getSubject().as(Individual.class);
				// show the properties of this individual
				System.out.println("  " + ind.getURI());
				for (StmtIterator j = ind.listProperties(); j.hasNext();) {
					Statement s = j.next();
					if (s.getPredicate().getLocalName().equals("causeOf")) {
						System.out.print("    "
								+ s.getPredicate().getLocalName() + " -> ");
						if (s.getObject().isLiteral()) {
							System.out.println(s.getLiteral().getLexicalForm());
						} else {
							System.out.println(s.getObject());
						}
					}
				}
			}
		}
	}

	public static List<String> getSymptoms(String diseaseName) {
		List<String> result = new ArrayList<String>();
		OntResource disease = ontModel.getOntResource(nameSpace + diseaseName);
		Property property = ontModel.getProperty(nameSpace + "evidenceOf");
		// find out the resources that link to the instance
		for (StmtIterator stmts = ontModel.listStatements(null, property,
				disease); stmts.hasNext();) {
			Individual ind = stmts.next().getSubject().as(Individual.class);
			// show the properties of this individual
			result.add(ind.getLocalName());
		}
		return result;
	}

	public static Boolean loadOntology(String filename) {
		InputStream in;
		try {
			in = new FileInputStream(filename);
			model.read(in, null);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		nameSpace = model.getNsPrefixURI("");
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
		reasoner = reasoner.bindSchema(model);
		OntModelSpec ontModelSpec = OntModelSpec.OWL_DL_MEM;
		ontModelSpec.setReasoner(reasoner);
		ontModel = ModelFactory.createOntologyModel(ontModelSpec, ontModel);
		return true;
	}
}
