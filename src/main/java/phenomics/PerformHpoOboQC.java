package phenomics;

import hpo.HPOutils;

import java.util.ArrayList;

import ontologizer.go.Ontology;
import phenomics.qcsteps.FreeTextAnnotationsQC;
import phenomics.qcsteps.QcStep;
import phenomics.qcsteps.RedundantLinksQC;

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

		// make the tests
		qcSteps.stream().forEach(qc -> qc.performQC());

	}

}
