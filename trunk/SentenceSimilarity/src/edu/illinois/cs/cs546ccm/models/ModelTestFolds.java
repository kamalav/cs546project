package edu.illinois.cs.cs546ccm.models;

import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.mmak4.corpus.Corpus;

public class ModelTestFolds {

	/**
	 * this is the main entry for our whole program: it runs all models in this
	 * package against all the corpus in the 'input' folder, and generates
	 * corresponding formatted output files in the 'output' folder
	 */
	public static void main(String[] args) throws IOException {

		// add instances of models in this package to a model array
		Model[] models = { new Model1LLM(), new Model2XXX() /*
															 * , new Model3YYY()
															 */};

		String[] corpusNames = { "MSRvid", "MSRpar", "SMTeuroparl" };
		int folds = 5;

		Corpus[] trainCorpus = new Corpus[folds * corpusNames.length];
		Corpus[] testCorpus = new Corpus[folds * corpusNames.length];
		// add instances of all corpus to a corpus array
		for (int i = 0; i < corpusNames.length; i++) {
			String name = corpusNames[i];
			for (int j = 0; j < folds; j++) {
				int index = folds * i + j;
				trainCorpus[index] = new Corpus("input_folds/STS.input." + name
						+ "." + j + ".train.txt", name + "." + j + ".train");
				testCorpus[index] = new Corpus("input_folds/STS.input." + name
						+ "." + j + ".test.txt", name + "." + j + ".test");
			}
		}

		// for each model, generate local LLM caches
		Corpus[] allCorpus = (Corpus[]) ArrayUtils.addAll(trainCorpus,
				testCorpus);

		// do the below lines only once, when no serialization file is saved
		// SerializationUtils.serializeAllCorpusFoldsTextAnnotations(allCorpus);

		for(int i=0; i<trainCorpus.length; i++) {
		    Corpus train = trainCorpus[i];
		    Corpus test = testCorpus[i];
		    
			// deserialize train objects from file
			System.out.println("Deserializing text annotations for "
					+ train.getId() + "...");
			String fileName = "serialization_folds/" + train.getId() + ".sel";
			TextAnnotation[] train_tas = SerializationUtils
					.deserializeTextAnnotations(fileName);
			
			// deserialize test objects from file
            System.out.println("Deserializing text annotations for "
                    + test.getId() + "...");
            fileName = "serialization_folds/" + test.getId() + ".sel";
            TextAnnotation[] test_tas = SerializationUtils
                    .deserializeTextAnnotations(fileName);

			// deserialize train LLM scores from file
			fileName = "serialization_folds/" + train.getId() + ".llm";
			double[] train_llmScores = SerializationUtils
					.deserializeLLMScores(fileName);
			
			// deserialize test LLM scores from file
            fileName = "serialization_folds/" + test.getId() + ".llm";
            double[] test_llmScores = SerializationUtils
                    .deserializeLLMScores(fileName);

			for (Model model : models) {
				// set the read objects to the model
				model.setTrainAnnotations(train_tas);
                model.setTestAnnotations(test_tas);
				// set the LLM scores to the model
				model.setTrainLLMScores(train_llmScores);
                model.setTestLLMScores(test_llmScores);
				// compute result and save to file
                String id = train.getId();
				String dataset = id.substring(0, id.indexOf("."));
				String fold = id.substring(id.indexOf(".") + 1,
						id.lastIndexOf("."));
				fileName = "output_folds/" + dataset + "_" + model.getId()
						+ "_CCM_" + fold + ".txt";
				System.out.println(fileName);
				model.train("input_folds/STS.gs."+train.getId()+".txt");
				model.computeAndSaveOutputToFile(fileName);
			}
		}

		// serialize word similarity map

		SerializationUtils.serializeHashMap(
				SimilarityUtils.getWordSimilarityMap(),
				SimilarityUtils.SIMILARITY_MAP_FILE_NAME);
	}
}
