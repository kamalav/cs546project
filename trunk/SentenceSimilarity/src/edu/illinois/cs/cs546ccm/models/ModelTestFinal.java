package edu.illinois.cs.cs546ccm.models;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.mmak4.corpus.Corpus;

public class ModelTestFinal {

	/**
	 * this is the main entry for our whole program: it runs all models in this
	 * package against all the corpus in the 'input' folder, and generates
	 * corresponding formatted output files in the 'output' folder
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// add instances of models in this package to a model array
		Model[] models = { /* new Model1LLM(), new Model2XXX(), */new Model3YYY() };

		Corpus[] trainCorpus = {
				new Corpus("input/STS.input.MSRvid.txt", "MSRvid"),
				new Corpus("input/STS.input.MSRpar.txt", "MSRpar"),
				new Corpus("input/STS.input.SMTeuroparl.txt", "SMTeuroparl"),
				new Corpus("input/STS.input.MSRvid.txt", "MSRvid"),
				new Corpus("input/STS.input.SMTeuroparl.txt", "SMTeuroparl") };

		Corpus[] testCorpus = {
				new Corpus("input_final/STS.input.MSRvid.txt", "MSRvid"),
				new Corpus("input_final/STS.input.MSRpar.txt", "MSRpar"),
				new Corpus("input_final/STS.input.SMTeuroparl.txt",
						"SMTeuroparl"),
				new Corpus("input_final/STS.input.surprise.OnWN.txt",
						"surprise.OnWN"),
				new Corpus("input_final/STS.input.surprise.SMTnews.txt",
						"surprise.SMTnews") };

		for (int i = 0; i < trainCorpus.length; i++) {
			Corpus train = trainCorpus[i];
			Corpus test = testCorpus[i];

			// deserialize train objects from file
			System.out.println("Deserializing text annotations for "
					+ train.getId() + "...");
			String fileName = "serialization/" + train.getId() + ".sel";
			TextAnnotation[] train_tas = SerializationUtils
					.deserializeTextAnnotations(fileName);

			// deserialize test objects from file
			System.out.println("Deserializing text annotations for "
					+ test.getId() + "...");
			fileName = "serialization_final/" + test.getId() + ".sel";
			TextAnnotation[] test_tas = SerializationUtils
					.deserializeTextAnnotations(fileName);

			// deserialize train LLM scores from file
			fileName = "serialization/" + train.getId() + ".llm";
			double[] train_llmScores = SerializationUtils
					.deserializeLLMScores(fileName);
			fileName = "serialization/" + train.getId() + ".llm2";
			double[] train_llmScores_WNsim = SerializationUtils
					.deserializeLLMScores(fileName);

			// deserialize test LLM scores from file
			fileName = "serialization_final/" + test.getId() + ".llm";
			double[] test_llmScores = SerializationUtils
					.deserializeLLMScores(fileName);
			fileName = "serialization_final/" + test.getId() + ".llm2";
			double[] test_llmScores_WNsim = SerializationUtils
					.deserializeLLMScores(fileName);

			for (Model model : models) {
				// set the read objects to the model
				model.setTrainAnnotations(train_tas);
				model.setTestAnnotations(test_tas);

				// set the LLM scores
				model.setTrainLLMScores(train_llmScores);
				model.setTrainLLMScores_WNsim(train_llmScores_WNsim);
				model.setTestLLMScores(test_llmScores);
				model.setTestLLMScores_WNsim(test_llmScores_WNsim);

				// compute result and save to file fileName =
				fileName = "output_final/STS.output." + test.getId() + ".txt";
				model.train(train.getId());
				model.computeAndSaveOutputToFile(fileName);

				// serialize word similarity map

				SerializationUtils.serializeHashMap(
						SimilarityUtils.getWordSimilarityMap(),
						SimilarityUtils.SIMILARITY_MAP_FILE_NAME);
			}

		}

	}

}
