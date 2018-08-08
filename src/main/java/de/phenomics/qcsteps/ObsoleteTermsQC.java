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
		for (Term t : hpo.allObsoloteTerms()) {

			TermID[] altids = t.getAlternatives();
			if (altids != null && altids.length > 0) {
				System.out.println("found obsolete class " + t + " with alternative ID(s): " + Arrays.toString(altids));
				foundErrorWithObsoleteClass = true;
			}
			Term alternative = hpo.getTermIncludingAlternatives(t.getIDAsString());
			if (alternative == null || alternative.isObsolete()) {
				// System.out.println("Class " + t + " does not have a proper replacement via
				// alt_ids!");
				if (t.getReplacedBy() != null) {
					Term repl = hpo.getTermIncludingAlternatives(t.getReplacedBy());
					if (repl == null || repl.isObsolete()) {
						System.out.println("Replaced by tag maps to obsolete class " + t + " -> " + t.getReplacedBy());
						foundErrorWithObsoleteClass = true;
					}
				}
				else if (t.getConsider() != null) {
					Term consider = hpo.getTermIncludingAlternatives(t.getConsider());
					if (consider == null || consider.isObsolete()) {
						System.out.println("Consider tag maps to obsolete class " + t + " -> " + t.getConsider());
						foundErrorWithObsoleteClass = true;
					}
				}
				else {
					System.out.println("Obsolete class " + t + " does not have a replace_by or a consider tag!");
					foundErrorWithObsoleteClass = true;
				}
				foundErrorWithObsoleteClass = true;
			}
		}
		if (foundErrorWithObsoleteClass) {
			System.out.println(QcStep.errorMessage);
			System.exit(1);
		}
		else {
			System.out.println(QcStep.everythingOkMessage);
		}
	}

}
