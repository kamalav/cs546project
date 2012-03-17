package edu.illinois.cs.cs546ccm.models;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

import org.apache.commons.lang.ArrayUtils;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.mmak4.corpus.Corpus;

public class ModelTestFolds {

	/**
	 * this is the main entry for our whole program: it runs all models in this
	 * package against all the corpus in the 'input' folder, and generates
	 * corresponding formatted output files in the 'output' folder
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// add instances of models in this package to a model array
		Model[] models = { new Model1LLM(), new Model2XXX(), new Model3YYY() };

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

		for (int i = 0; i < trainCorpus.length; i++) {
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
			fileName = "serialization_folds/" + train.getId() + ".llm2";
			double[] train_llmScores_WNsim = SerializationUtils
					.deserializeLLMScores(fileName);

			// deserialize test LLM scores from file
			fileName = "serialization_folds/" + test.getId() + ".llm";
			double[] test_llmScores = SerializationUtils
					.deserializeLLMScores(fileName);
			fileName = "serialization_folds/" + test.getId() + ".llm2";
			double[] test_llmScores_WNsim = SerializationUtils
					.deserializeLLMScores(fileName);

			for (Model model : models) {
				// set the read objects to the model
				model.setTrainAnnotations(train_tas);
				model.setTestAnnotations(test_tas);
				// set the LLM scores to the model
				model.setTrainLLMScores(train_llmScores);
				model.setTrainLLMScores_WNsim(train_llmScores_WNsim);

				model.setTestLLMScores(test_llmScores);
				model.setTestLLMScores_WNsim(test_llmScores_WNsim);

				// compute result and save to file
				String id = train.getId();
				String dataset = id.substring(0, id.indexOf("."));
				String fold = id.substring(id.indexOf(".") + 1,
						id.lastIndexOf("."));
				fileName = "output_folds/" + dataset + "_" + model.getId()
						+ "_CCM_" + fold + ".txt";
				// System.out.println(fileName);
				model.train(train.getId());
				model.computeAndSaveOutputToFile(fileName);
			}
		}

		combineResults();

		// serialize word similarity map

		SerializationUtils.serializeHashMap(
				SimilarityUtils.getWordSimilarityMap(),
				SimilarityUtils.SIMILARITY_MAP_FILE_NAME);
	}

	private static void combineResults() throws Exception {
		String[] corpora = new String[] { "MSRpar", "MSRvid", "SMTeuroparl" };
		String[] models = new String[] { "LLM", "XXX", "YYY" };

		for (int m = 0; m < models.length; m++) {
			for (int c = 0; c < corpora.length; c++) {
				String corpus = corpora[c];
				Scanner[] scs = new Scanner[5];
				for (int i = 0; i < 5; i++) {
					scs[i] = new Scanner(new File("output_folds/" + corpus
							+ "_" + models[m] + "_CCM_" + i + ".txt"));
				}

				PrintWriter pw = new PrintWriter(new File("output_folds/"
						+ corpus + "_" + models[m] + ".scores.txt"));
				boolean cond = true;
				int i = 0;
				while (cond) {
					if (scs[i].hasNextLine()) {
						String line = scs[i].nextLine();
						if (line.trim().isEmpty()) {
							cond = false;
						} else
							pw.println(line);
					} else {
						cond = false;
					}
					i = (i + 1) % 5;
				}
				pw.close();
				for (int j = 0; j < 5; j++)
					scs[j].close();
			}
		}
	}
}
