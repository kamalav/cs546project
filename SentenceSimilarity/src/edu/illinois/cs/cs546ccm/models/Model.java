package edu.illinois.cs.cs546ccm.models;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.illinois.cs.mmak4.corpus.Corpus;

public abstract class Model {

	/*
	 * abstract function for computing similarity score (0.0-5.0) between two
	 * sentences, indexed by line number. This function is to be implemented by
	 * each actual model, definitely using corpus.get_annotation_pair(line)
	 */
	abstract public double similarity(Corpus corpus, int line);

	/*
	 * abstract function for computing confidence score (0-100) between two
	 * sentences, indexed by line number. This function is to be implemented by
	 * each actual model, definitely using corpus.get_annotation_pair(line)
	 */
	abstract public int confidence(Corpus corpus, int line);

	/*
	 * generate a string representing the output file
	 */
	public String generateSimilarityAndConfidenceOutput(Corpus corpus) {
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < corpus.get_pairs_size(); i++) {
			double similarity = similarity(corpus, i);
			int confidence = confidence(corpus, i);
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
	public void computeAndSaveOutputToFile(Corpus corpus, String fileName)
			throws IOException {
		File file = new File(fileName);
		FileOutputStream fop = new FileOutputStream(file);
		String fileContent = generateSimilarityAndConfidenceOutput(corpus);
		fop.write(fileContent.getBytes());
		fop.flush();
		fop.close();
		System.out.println("results written into file: " + fileName);
	}

	/*
	 * a simple model name
	 */
	private String id;

	public Model(String modelId) {
		id = modelId;
	}

	public String getId() {
		return id;
	}

}
