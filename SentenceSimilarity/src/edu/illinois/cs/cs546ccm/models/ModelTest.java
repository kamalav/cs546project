package edu.illinois.cs.cs546ccm.models;

import java.io.IOException;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.mmak4.corpus.Corpus;

public class ModelTest {

	/**
	 * this is the main entry for our whole program: it runs all models in this
	 * package against all the corpus in the 'input' folder, and generates
	 * corresponding formatted output files in the 'output' folder
	 */
	public static void main(String[] args) throws IOException {

		// add instances of models in this package to a model array
		Model[] models = { /* new Model1LLM(), */new Model2XXX() /*
																 * , new
																 * Model3YYY()
																 */};

		/*
		 * note: for temporary testing, don't put all the corpus in the
		 * following array, because it's really time-consuming to iterate all
		 * the sentences inside. Instead, you might want to test only one
		 * corpus, or just a new testing corpus containing just a few sentences
		 */

		// add instances of all corpus to a corpus array
		Corpus[] allCorpus = { new Corpus("input/STS.input.Temp.txt", "Temp"),
				new Corpus("input/STS.input.MSRvid.txt", "MSRvid"),
				new Corpus("input/STS.input.MSRpar.txt", "MSRpar"),
				new Corpus("input/STS.input.SMTeuroparl.txt", "SMTeuroparl") };

		// do the below lines only once, when no serialization file is saved
		// SerializationUtils.serializeAllCorpusTextAnnotations(allCorpus);

		// for each model, generate the output containing similarity and
		// confidence for every pair of sentences, and save it into file
		for (Corpus corpus : allCorpus) {

			// deserialize objects from file
			System.out.println("Deserializing text annotations for "
					+ corpus.getId() + "...");
			String fileName = "serialization/" + corpus.getId() + ".sel";
			TextAnnotation[] tas = SerializationUtils
					.deserializeTextAnnotations(fileName);

			// deserialize LLM scores from file
			fileName = "serialization/" + corpus.getId() + ".llm";
			double[] llmScores = SerializationUtils
					.deserializeLLMScores(fileName);

			for (Model model : models) {
				// set the read objects to the model
				model.setTextAnnotations(tas);

				// set the LLM scores to the model
				model.setLLMScores(llmScores);

				// compute result and save to file
				fileName = "output/" + corpus.getId() + "_teamCCM_model"
						+ model.getId() + ".txt";
				model.computeAndSaveOutputToFile(fileName);
			}
		}
	}
}
