package de.charite.phenomics.qcsteps;

public interface QcStep {

	public static final String everythingOkMessage = "Everything went ok.";
	public static final String errorMessage = "Returning with error code 1 !";

	public void performQC();

}
