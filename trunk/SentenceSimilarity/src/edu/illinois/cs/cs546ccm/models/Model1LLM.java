package edu.illinois.cs.cs546ccm.models;

import java.io.IOException;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.mrcs.comparators.LlmComparator;

public class Model1LLM extends Model {

	private LlmComparator llm;

	public Model1LLM() throws IOException {
		super("LLM");
		llm = new LlmComparator("config/alternativeLlmConfig.txt");
	}

	public double similarity(String source, String target) {
		int scale = 5;
		return scale * llm.compareStrings(source, target);
	}

	@Override
	public double similarity(TextAnnotation ta1, TextAnnotation ta2) {
		try {
			String source = ta1.getText();
			int line = Integer.parseInt(ta1.getId());
			// get cached score, not using reversed score
			double llmScore = getLLMScores(line)[0];
			int scale = 5;
			return scale * llmScore;// llm.compareStrings(source, target);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// use -1 to indicate something bad happens
			return -1;
		}
	}

	@Override
	public int confidence(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO how to determine the confidence using LLM ?
		return 100;
	}

	public static void main(String[] args) throws Exception {
		String source = "Three boys haha named shit the temp.";
		String target = "Three children were named in lawsuit.";
		Model1LLM m = new Model1LLM();
		double result = m.similarity(source, target);
		System.out.println(result);
	}
}
