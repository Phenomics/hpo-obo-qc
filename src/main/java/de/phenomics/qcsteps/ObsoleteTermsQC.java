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

		boolean foundErrorWithObsoleteClass = false;
		int i = 0;
		int j = 0;
		for (Term t : hpo.allObsoloteTerms()) {
			i++;
			TermID[] altids = t.getAlternatives();
			if (altids != null && altids.length > 0) {
				System.out.println("found obsolete class " + t + " with alternative ID(s): " + Arrays.toString(altids));
				foundErrorWithObsoleteClass = true;
			}
			Term alternative = hpo.getTermIncludingAlternatives(t.getIDAsString());
			if (alternative == null || alternative.isObsolete()) {
				System.out.println("Class " + t + " does not have a proper replacement via alt_ids!");
				foundErrorWithObsoleteClass = true;
				j++;
			}
		}
		System.out.println(i);
		System.out.println(j);
		if (foundErrorWithObsoleteClass) {
			System.out.println(QcStep.errorMessage);
			System.exit(1);
		}
		else {
			System.out.println(QcStep.everythingOkMessage);
		}
	}

}
