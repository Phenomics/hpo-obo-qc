package de.phenomics.qcsteps;

import java.util.Arrays;

import ontologizer.go.Ontology;
import ontologizer.go.Term;
import ontologizer.go.TermID;

public class ObsoleteTermsQC implements QcStep {

	private Ontology hpo;

	public ObsoleteTermsQC(Ontology hpo) {
		this.hpo = hpo;
	}

	@Override
	public void performQC() {
		System.out.println("Perform qc: " + getClass().getName());

		boolean foundWrongObsoleteClass = false;
		for (Term t : hpo.allObsoloteTerms()) {
			TermID[] altids = t.getAlternatives();
			if (altids != null && altids.length > 0) {
				System.out.println("found obsolete class " + t + " with alternative ID(s): " + Arrays.toString(altids));
			}
		}

		if (foundWrongObsoleteClass) {
			System.out.println(QcStep.errorMessage);
			System.exit(1);
		} else {
			System.out.println(QcStep.everythingOkMessage);
		}
	}

}
