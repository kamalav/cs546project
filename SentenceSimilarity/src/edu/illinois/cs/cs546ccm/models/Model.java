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
	protected TextAnnotation[] train_tas;
    protected TextAnnotation[] test_tas;

	public void setTrainAnnotations(TextAnnotation[] tas) {
		this.train_tas = tas;
	}
	
	public void setTestAnnotations(TextAnnotation[] tas) {
        this.test_tas = tas;
    }

	/*
	 * an array of LLM scores, which are read from the cached files
	 * llmScores[2*i] is the score for i-th pair, llmScores[2*i+1] is the score
	 * for i-th pair in reversed order
	 */
	protected double[] train_llmScores;
    protected double[] test_llmScores;
    
    protected double[] train_llmScores_WNsim;
    protected double[] test_llmScores_WNsim;

	public void setTrainLLMScores(double[] llmScores) {
		this.train_llmScores = llmScores;
	}
	
	public void setTestLLMScores(double[] llmScores) {
        this.test_llmScores = llmScores;
    }
	
	public void setTrainLLMScores_WNsim(double[] llmScores) {
		this.train_llmScores_WNsim = llmScores;
	}
	
	public void setTestLLMScores_WNsim(double[] llmScores) {
        this.test_llmScores_WNsim = llmScores;
    }

	/*
	 * return two LLM scores (normal and reversed) of sentences in the line
	 */
	public double[] getTrainLLMScores(int line) {
		return new double[] { this.train_llmScores[2 * line],
				this.train_llmScores[2 * line + 1], 
				this.train_llmScores_WNsim[2 * line],
				this.train_llmScores_WNsim[2 * line + 1] };
	}
	
	public double[] getTestLLMScores(int line) {
        return new double[] { this.test_llmScores[2 * line],
                this.test_llmScores[2 * line + 1],this.test_llmScores_WNsim[2 * line],
                this.test_llmScores_WNsim[2 * line + 1]};
    }
	
	/*
	 * return two LLM_WNSim scores (normal and reversed) of sentences in the line
	 */
	
	public double[] getTrainLLMScores_WNsim(int line) {
		return new double[] { this.train_llmScores_WNsim[2 * line],
				this.train_llmScores_WNsim[2 * line + 1]};
	}
	
	public double[] getTestLLMScores_WNsim(int line) {
        return new double[] { this.test_llmScores_WNsim[2 * line],
                this.test_llmScores_WNsim[2 * line + 1] };
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
	 * For methods that need training, prepare the model
	 */
	public void train(String gsFile) {
	    
	}

	/*
	 * generate a string representing the output file
	 */
	public String generateSimilarityAndConfidenceOutput() {
		if (this.test_tas == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer("");
		int pairs = this.test_tas.length / 2;
		for (int i = 0; i < pairs; i++) {
			TextAnnotation ta1 = this.test_tas[2 * i];
			TextAnnotation ta2 = this.test_tas[2 * i + 1];
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
