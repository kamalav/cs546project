package edu.illinois.cs.cs546ccm.models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;

public abstract class Model {

	/*
	 * an array of TextAnnotaion objects for all the sentences in a corpus,
	 * using the 2*n, 2*n+1 scheme for indexing as well
	 */
	protected TextAnnotation[] tas;

	public void setTextAnnotations(TextAnnotation[] tas) {
		this.tas = tas;
	}

	/*
	 * an array of LLM scores, which are read from the cached files
	 * llmScores[2*i] is the score for i-th pair, llmScores[2*i+1] is the score
	 * for i-th pair in reversed order
	 */
	protected double[] llmScores;

	public void setLLMScores(double[] llmScores) {
		this.llmScores = llmScores;
	}

	public double[] getLLMScores(int line) {
		return new double[] { this.llmScores[2 * line],
				this.llmScores[2 * line + 1] };
	}

	/*
	 * abstract function for computing similarity score (0.0-5.0) between two
	 * sentences, indexed by line number. This function is to be implemented by
	 * each actual model, definitely using corpus.get_annotation_pair(line)
	 */
	abstract public double similarity(TextAnnotation ta1, TextAnnotation ta2);

	/*
	 * abstract function for computing confidence score (0-100) between two
	 * sentences, indexed by line number. This function is to be implemented by
	 * each actual model, definitely using corpus.get_annotation_pair(line)
	 */
	abstract public int confidence(TextAnnotation ta1, TextAnnotation ta2);

	/*
	 * generate a string representing the output file
	 */
	public String generateSimilarityAndConfidenceOutput() {
		if (this.tas == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer("");
		int pairs = this.tas.length / 2;
		for (int i = 0; i < pairs; i++) {
			TextAnnotation ta1 = this.tas[2 * i];
			TextAnnotation ta2 = this.tas[2 * i + 1];
			double similarity = similarity(ta1, ta2);
			int confidence = confidence(ta1, ta2);
			String outputLine = similarity + "\t" + confidence + "\n";
			System.out.println("result computed: similarity=" + similarity
					+ "\tconfidence=" + confidence + "\t");
			sb.append(outputLine);
		}
		return sb.toString();

	}

	/*
	 * generate and save the output into a file
	 */
	public void computeAndSaveOutputToFile(String fileName) throws IOException {
		File file = new File(fileName);
		FileOutputStream fop = new FileOutputStream(file);
		String fileContent = generateSimilarityAndConfidenceOutput();
		if (fileContent != null) {
			fop.write(fileContent.getBytes());
			System.out.println("results written into file: " + fileName);
		} else {
			System.err
					.println("error happens when generating file: make sure you have called setTextAnnotations() before outputing");
		}
		fop.flush();
		fop.close();
	}

	/*
	 * a simple model name
	 */
	protected String id;

	public Model(String modelId) {
		id = modelId;
	}

	public String getId() {
		return id;
	}

}
