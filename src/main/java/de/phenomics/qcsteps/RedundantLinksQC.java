package de.phenomics.qcsteps;

import ontologizer.go.Ontology;
import ontologizer.go.Term;
import ontologizer.go.TermID;

public class RedundantLinksQC implements QcStep {

	private Ontology hpo;

	public RedundantLinksQC(Ontology hpo) {
		this.hpo = hpo;
	}

	@Override
	public void performQC() {

		System.out.println("Perform qc: " + getClass().getName());

		boolean foundRedundantLink = false;
		for (Term t : hpo) {
			TermID redundant = hpo.findARedundantISARelation(t);
			if (redundant != null) {
				foundRedundantLink = true;
				Term superClass = hpo.getTerm(redundant);
				System.out.println("Redundant subclass assertion found: ");
				System.out.println("   - " + t + " -> " + superClass);
				System.out.println();
			}
		}

		if (foundRedundantLink) {
			System.out.println(QcStep.errorMessage);
			System.exit(1);
		}
		else {
			System.out.println(QcStep.everythingOkMessage);
		}

	}
}
