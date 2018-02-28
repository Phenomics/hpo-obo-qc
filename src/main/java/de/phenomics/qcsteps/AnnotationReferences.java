package de.phenomics.qcsteps;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * @author Sebastian Köhler (dr.sebastian.koehler@gmail.com)
 *
 */
public class AnnotationReferences implements QcStep {

	private String obofile;
	private HashSet<String> errorLines;

	public AnnotationReferences(String oboFile) {
		this.obofile = oboFile;
		this.errorLines = new HashSet<String>();
	}

	@Override
	public void performQC() {

		System.out.println("Perform qc: " + getClass().getName());

		// read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get(this.obofile))) {

			stream.forEach(l -> checkLine(l));

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (errorLines.size() > 0) {
			System.out.println("found " + errorLines.size() + " lines with character { , which we have to avoid: ");
			for (String line : errorLines) {
				System.out.println(" - " + line);
			}
			System.out.println(QcStep.errorMessage);
			System.exit(1);
		}
		else {
			System.out.println(QcStep.everythingOkMessage);
		}

	}

	/**
	 * @param l
	 * @return
	 */
	private void checkLine(String l) {
		if (l.startsWith("def:") || l.startsWith("synonym:") || l.startsWith("name:")) {
			if (l.contains("{")) {
				errorLines.add(l);
			}
		}
	}

}
