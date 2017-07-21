package phenomics.qcsteps;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashMultimap;

import ontologizer.go.Ontology;
import ontologizer.go.Term;
import ontologizer.go.TermID;

/**
 * Check the free-text fields in the ontology:<br>
 * - label<br>
 * - synonyms<br>
 * - textual definition<br>
 * 
 * Checks implemented:<br>
 * - a label or synonym only used once in complete ontology<br>
 * - double whitespaces<br>
 * - trim should not change anything<br>
 * - a synonym should not be the same as the label of the term<br>
 * - a synonym should not be listed twice for a term<br>
 * - a text-def should not be the same as the label of the term<br>
 * - the value in replaced_by should only be HP:1234567 (not a purl or HP_1234567) (this might change later)<br>
 * - an id can only occur once. the only exception is that it is ok to be listed as alt_id and as id of an obsolete term
 * 
 * @author Sebastian Koehler
 *
 */
public class FreeTextAnnotationsQC implements QcStep {

	public static final Pattern multiWs = Pattern.compile("\\s{2,}");
	public static final Pattern oboTermId = Pattern.compile("HP:\\d{7}");
	private Ontology hpo;

	public FreeTextAnnotationsQC(Ontology hpo) {
		this.hpo = hpo;
	}

	private static HashSet<String> checkOtherText(String termPrimaryLabel, String text) {

		HashSet<String> problems = checkLabel(text);

		// should not be the same as primary label
		if (text.equalsIgnoreCase(termPrimaryLabel)) {
			problems.add("'" + text + "' equals the terms primary label.");
		}

		return problems;
	}

	private static HashSet<String> checkLabel(String name) {

		HashSet<String> problems = new HashSet<>();

		Matcher m = multiWs.matcher(name);
		if (m.find()) {
			problems.add("Found multiple consecutive whitespaces in '" + name + "'");
		}

		if (!name.equals(name.trim())) {
			problems.add("Found leading or trailing whitespace in '" + name + "'");
		}

		return problems;
	}

	@Override
	public void performQC() {

		System.out.println("Perform qc: " + getClass().getName());

		/*
		 * Used to identify if label or synonym used twice in ontology
		 */
		HashMultimap<String, Term> label2terms = HashMultimap.create();

		/*
		 * Used to report other problems found in terms (e.g. multiple whitespaces)
		 */
		HashMultimap<Term, String> term2problem = HashMultimap.create();

		HashMultimap<String, Term> oboid2term = HashMultimap.create();

		for (Term t : hpo) {

			if (t.getReplacedBy() != null) {
				Matcher m = oboTermId.matcher(t.getReplacedBy());
				if (!m.matches()) {
					term2problem.put(t, "Invalid value for replaced by found. I expect an obo-style ID (i.e. " + oboTermId.pattern()
							+ "). The value found was: " + t.getReplacedBy());
				}
			}

			oboid2term.put(t.getIDAsString(), t);

			if (t.getAlternatives() != null) {
				for (TermID tid : t.getAlternatives()) {
					String altIdStr = tid.toString();
					Matcher m = oboTermId.matcher(altIdStr);
					if (!m.matches()) {
						term2problem.put(t, "Invalid value for alt_id found. I expect an obo-style ID (i.e. " + oboTermId.pattern()
								+ "). The value found was: " + altIdStr);
					}

					oboid2term.put(altIdStr, t);

				}
			}

			if (t.isObsolete())
				continue;

			label2terms.put(t.getName().toLowerCase(), t);

			HashSet<String> checkLabel = checkLabel(t.getName());
			if (checkLabel.size() > 0) {
				term2problem.putAll(t, checkLabel);
			}

			if (t.getSynonyms() != null) {
				String[] synonyms = t.getSynonyms();
				for (String syn : synonyms) {
					label2terms.put(syn.toLowerCase(), t);

					// only check if there is no definition
					if (t.getDefinition() == null) {
						// if no defintion, then we assume a synonym should be not too long!
						if (syn.length() > 90) {
							term2problem.put(t, "Synonym is more than 90 characters long. Looks wrong! " + syn);
						}
					}
					// HashSet<String> checkSyn = checkOtherText(t.getName(), syn);
					// if (checkSyn.size() > 0)
					// term2problem.putAll(t, checkSyn);
				}

				HashSet<String> synonymsHs = new HashSet<>(Arrays.asList(synonyms));
				if (synonyms.length != synonymsHs.size()) {
					term2problem.put(t, "List of synonyms contains duplicated entries");
				}

			}
			if (t.getDefinition() != null) {
				String def = t.getDefinition();
				HashSet<String> checkDef = checkOtherText(t.getName(), def);
				if (checkDef.size() > 0)
					term2problem.putAll(t, checkDef);
			}
		}

		for (String oboid : oboid2term.keySet()) {
			Set<Term> termsWithId = oboid2term.get(oboid);
			int countTerms = 0;
			Term firstTerm = null;
			for (Term t : termsWithId) {
				if (firstTerm == null)
					firstTerm = t;
				if (!t.isObsolete())
					++countTerms;
			}
			if (countTerms > 1) {
				term2problem.put(firstTerm,
						"Problem with duplicated ID " + oboid + ". This ID has been used for the the following terms: " + termsWithId);
			}
		}

		for (Term t : term2problem.keySet()) {
			System.out.println("Found problem in Term : " + t);
			for (String problem : term2problem.get(t)) {
				System.out.println(" - " + problem);
			}
		}

		boolean duplicationProblem = false;

		for (String s : label2terms.keySet()) {
			if (label2terms.get(s).size() > 1) {

				System.out.println("PROBLEM WITH MULTIPLE OCCURENCE OF LABEL: " + s);
				System.out.println("FOUND AS LABEL FOR TERMS: ");
				for (Term t : label2terms.get(s)) {
					System.out.println("  - " + t);
				}
				System.out.println();
				duplicationProblem = true;
			}
		}

		boolean formatProblem = term2problem.keySet().size() > 0;

		if (duplicationProblem || formatProblem) {
			System.out.println(QcStep.errorMessage);
			System.exit(1);
		}
		else {
			System.out.println(QcStep.everythingOkMessage);
		}

	}

}
