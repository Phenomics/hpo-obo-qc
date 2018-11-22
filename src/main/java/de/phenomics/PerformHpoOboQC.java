package de.phenomics;

import java.util.ArrayList;

import de.phenomics.qcsteps.AnnotationReferences;
import de.phenomics.qcsteps.FreeTextAnnotationsQC;
import de.phenomics.qcsteps.ObsoleteTermsQC;
import de.phenomics.qcsteps.QcStep;
import de.phenomics.qcsteps.RedundantLinksQC;
import hpo.HpoDataProvider;
import ontologizer.ontology.Ontology;

/**
 * Performs QC on hpo.obo, which is to provided as argument.
 * 
 * @author Sebastian Koehler
 */
public class PerformHpoOboQC {

	public static void main(String[] args) {

		// parse hp obo
		HpoDataProvider dataProvider = new HpoDataProvider();
		dataProvider.setOboFile(args[0]);
		dataProvider.parseOntology();
		Ontology hpo = dataProvider.getHpo();

		// define which steps to perform
		ArrayList<QcStep> qcSteps = new ArrayList<>();
		qcSteps.add(new FreeTextAnnotationsQC(hpo));
		qcSteps.add(new RedundantLinksQC(hpo));
		qcSteps.add(new AnnotationReferences(args[0]));
		qcSteps.add(new ObsoleteTermsQC(hpo));

		// make the tests
		qcSteps.stream().forEach(qc -> qc.performQC());

	}

}
