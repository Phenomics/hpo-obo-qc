package de.phenomics.qcsteps;

import java.util.Arrays;
import java.util.HashSet;

import ontologizer.ontology.Ontology;
import ontologizer.ontology.ParentTermID;
import ontologizer.ontology.Term;
import ontologizer.ontology.TermID;

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

			if (t.getParents().length > 0) {
				System.out.println("found obsolete class " + t + " with a parent class.");
				System.out.println("obsolete classes are not allowed to have superclasses! ");
				System.out.println("You HAVE to use the obsolescence plugin in Protege!!! ");
				foundErrorWithObsoleteClass = true;
			}

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
					for (String considerId : t.getConsider()) {
						Term consider = hpo.getTermIncludingAlternatives(considerId);
						if (consider == null || consider.isObsolete()) {
							System.out.println("Consider tag maps to obsolete class " + t + " -> " + t.getConsider());
							foundErrorWithObsoleteClass = true;
						}
					}
				}
				else {
					System.out.println("Obsolete class " + t + " does not have a replace_by or a consider tag!");
					foundErrorWithObsoleteClass = true;
				}
			}
		}

		HashSet<String> allObsIds = new HashSet<>();
		for (Term t : hpo.allObsoloteTerms())
			allObsIds.add(t.getIDAsString());

		for (Term t : hpo) {
			for (ParentTermID pid : t.getParents()) {
				Term p = hpo.getTerm(pid.termid);
				if (p == null && allObsIds.contains(pid.termid.toString())) {
					System.out.println("Class " + t + " is subclass of obsolete class " + pid.termid + " !");
					foundErrorWithObsoleteClass = true;
				}
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
