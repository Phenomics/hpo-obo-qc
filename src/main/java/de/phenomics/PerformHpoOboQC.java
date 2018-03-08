package de.phenomics;

import java.util.ArrayList;

import de.phenomics.qcsteps.AnnotationReferences;
import de.phenomics.qcsteps.FreeTextAnnotationsQC;
import de.phenomics.qcsteps.ObsoleteTermsQC;
import de.phenomics.qcsteps.QcStep;
import de.phenomics.qcsteps.RedundantLinksQC;
import hpo.HPOutils;
import ontologizer.go.Ontology;

/**
 * Performs QC on hpo.obo, which is to provided as argument.
 * 
 * @author Sebastian Koehler
 */
public class PerformHpoOboQC {

	public static void main(String[] args) {

		// parse hp obo
		HPOutils.setOboFile(args[0]);
		HPOutils.parseOntology();
		Ontology hpo = HPOutils.hpo;

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
