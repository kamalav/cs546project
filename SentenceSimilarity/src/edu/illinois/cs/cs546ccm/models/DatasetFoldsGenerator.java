package edu.illinois.cs.cs546ccm.models;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.mmak4.corpus.Corpus;

public class DatasetFoldsGenerator {

	/*
	 * for the given corpus, generate 4*folds files: train, test, train_gs,
	 * test_gs
	 */
	public static void splitDatasetToFolds(Corpus corpus, int folds) {
		String id = corpus.getId();

		File[] trainFiles = new File[folds];
		File[] testFiles = new File[folds];
		File[] trainGsFiles = new File[folds];
		File[] testGsFiles = new File[folds];

		for (int i = 0; i < folds; i++) {
			String fileName = "input_folds/STS.input." + id + "." + i
					+ ".train.txt";
			trainFiles[i] = new File(fileName);
			fileName = "input_folds/STS.input." + id + "." + i + ".test.txt";
			testFiles[i] = new File(fileName);

			fileName = "input_folds/STS.gs." + id + "." + i + ".train.txt";
			trainGsFiles[i] = new File(fileName);
			fileName = "input_folds/STS.gs." + id + "." + i + ".test.txt";
			testGsFiles[i] = new File(fileName);
		}

		try {
			// for getting the source lines
			String sourceFileName = "input/STS.input." + id + ".txt";
			FileInputStream fstream = new FileInputStream(sourceFileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			// assume the number of lines is less than 1000
			String[] lines = new String[1000];
			int l = 0;
			while ((line = br.readLine()) != null) {
				lines[l++] = line;
			}
			in.close();

			// for getting the gs source lines
			String sourceGsFileName = "input/STS.gs." + id + ".txt";
			fstream = new FileInputStream(sourceGsFileName);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			// assume the number of lines is less than 1000
			String[] gsLines = new String[1000];
			l = 0;
			while ((line = br.readLine()) != null) {
				gsLines[l++] = line;
			}
			in.close();

			for (int i = 0; i < folds; i++) {
				// for test
				String fileName = "train_test_sets/STS.input." + id + ".txt__"
						+ i + "/test.txt";
				fstream = new FileInputStream(fileName);
				in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in));
				l = 0;
				StringBuffer lineSb = new StringBuffer("");
				StringBuffer gsSb = new StringBuffer("");
				while ((line = br.readLine()) != null) {
					String[] ss = line.split("\t");
					int lineInSourceFile = Integer.parseInt(ss[1]);
					lineSb.append(lines[lineInSourceFile] + "\n");
					gsSb.append(gsLines[lineInSourceFile] + "\n");
				}
				in.close();
				FileOutputStream fop = new FileOutputStream(testFiles[i]);
				String fileContent = lineSb.toString();
				fop.write(fileContent.getBytes());
				fop.flush();
				fop.close();
				fop = new FileOutputStream(testGsFiles[i]);
				fileContent = gsSb.toString();
				fop.write(fileContent.getBytes());
				fop.flush();
				fop.close();

				// for train
				fileName = "train_test_sets/STS.input." + id + ".txt__" + i
						+ "/train.txt";
				fstream = new FileInputStream(fileName);
				in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in));
				l = 0;
				lineSb = new StringBuffer("");
				gsSb = new StringBuffer("");
				while ((line = br.readLine()) != null) {
					String[] ss = line.split("\t");
					int lineInSourceFile = Integer.parseInt(ss[1]);
					lineSb.append(lines[lineInSourceFile] + "\n");
					gsSb.append(gsLines[lineInSourceFile] + "\n");
				}
				in.close();
				fop = new FileOutputStream(trainFiles[i]);
				fileContent = lineSb.toString();
				fop.write(fileContent.getBytes());
				fop.flush();
				fop.close();
				fop = new FileOutputStream(trainGsFiles[i]);
				fileContent = gsSb.toString();
				fop.write(fileContent.getBytes());
				fop.flush();
				fop.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void splitLLMScoresToFolds(Corpus corpus, int folds) {
		String id = corpus.getId();

		File[] trainLLMFiles = new File[folds];
		File[] testLLMFiles = new File[folds];

		for (int i = 0; i < folds; i++) {
			String fileName = "serialization_folds/" + id + "." + i
					+ ".train.llm2";
			trainLLMFiles[i] = new File(fileName);
			fileName = "serialization_folds/" + id + "." + i + ".test.llm2";
			testLLMFiles[i] = new File(fileName);
		}

		try {
			// for getting the source scores
			String sourceFileName = "serialization/" + id + ".llm2";
			double[] scores = SerializationUtils
					.deserializeLLMScores(sourceFileName);

			for (int i = 0; i < folds; i++) {
				// for test
				String fileName = "train_test_sets/STS.input." + id + ".txt__"
						+ i + "/test.txt";
				FileInputStream fstream = new FileInputStream(fileName);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				int l = 0;
				String line;
				while ((line = br.readLine()) != null) {
					l++;
				}
				StringBuffer sb = new StringBuffer(new String(l * 2 + "\n"));
				fstream = new FileInputStream(fileName);
				in = new DataInputStream(fstream);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						in));
				while ((line = br2.readLine()) != null) {
					String[] ss = line.split("\t");
					int lineInSourceFile = Integer.parseInt(ss[1]);
					sb.append(scores[2 * lineInSourceFile] + "\t"
							+ scores[2 * lineInSourceFile + 1] + "\n");
				}
				in.close();
				FileOutputStream fop = new FileOutputStream(testLLMFiles[i]);
				String fileContent = sb.toString();
				fop.write(fileContent.getBytes());
				fop.flush();
				fop.close();

				// for train
				fileName = "train_test_sets/STS.input." + id + ".txt__" + i
						+ "/train.txt";
				fstream = new FileInputStream(fileName);
				in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in));
				l = 0;
				while ((line = br.readLine()) != null) {
					l++;
				}
				sb = new StringBuffer(new String(l * 2 + "\n"));
				fstream = new FileInputStream(fileName);
				in = new DataInputStream(fstream);
				br2 = new BufferedReader(new InputStreamReader(in));
				while ((line = br2.readLine()) != null) {
					String[] ss = line.split("\t");
					int lineInSourceFile = Integer.parseInt(ss[1]);
					sb.append(scores[2 * lineInSourceFile] + "\t"
							+ scores[2 * lineInSourceFile + 1] + "\n");
				}
				in.close();
				fop = new FileOutputStream(trainLLMFiles[i]);
				fileContent = sb.toString();
				fop.write(fileContent.getBytes());
				fop.flush();
				fop.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void splitTextAnnotationSerializationsToFolds(Corpus corpus,
			int folds, TextAnnotation[] tas) {
		String id = corpus.getId();

		File[] trainSerializationFiles = new File[folds];
		File[] testSerializationFiles = new File[folds];

		for (int i = 0; i < folds; i++) {
			String fileName = "serialization_folds/" + id + "." + i
					+ ".train.sel";
			trainSerializationFiles[i] = new File(fileName);
			fileName = "serialization_folds/" + id + "." + i + ".test.sel";
			testSerializationFiles[i] = new File(fileName);
		}

		try {
			for (int i = 0; i < folds; i++) {
				// for test
				String fileName = "train_test_sets/STS.input." + id + ".txt__"
						+ i + "/test.txt";
				FileInputStream fstream = new FileInputStream(fileName);
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(
						new InputStreamReader(in));
				int lines = 0;
				String line;
				while ((line = br.readLine()) != null) {
					lines++;
				}
				FileOutputStream fos = new FileOutputStream(
						testSerializationFiles[i]);
				ObjectOutputStream out = new ObjectOutputStream(fos);
				int count = lines * 2;
				out.writeInt(count);
				fstream = new FileInputStream(fileName);
				in = new DataInputStream(fstream);
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						in));
				while ((line = br2.readLine()) != null) {
					String[] ss = line.split("\t");
					int tasLineIndex = Integer.parseInt(ss[1]);
					out.writeObject(tas[2 * tasLineIndex]);
					out.writeObject(tas[2 * tasLineIndex + 1]);
				}
				out.close();

				// for train
				fileName = "train_test_sets/STS.input." + id + ".txt__" + i
						+ "/train.txt";
				fstream = new FileInputStream(fileName);
				in = new DataInputStream(fstream);
				br = new BufferedReader(new InputStreamReader(in));
				lines = 0;
				while ((line = br.readLine()) != null) {
					lines++;
				}
				fos = new FileOutputStream(trainSerializationFiles[i]);
				ObjectOutputStream out2 = new ObjectOutputStream(fos);
				count = lines * 2;
				out2.writeInt(count);
				fstream = new FileInputStream(fileName);
				in = new DataInputStream(fstream);
				br2 = new BufferedReader(new InputStreamReader(in));
				while ((line = br2.readLine()) != null) {
					String[] ss = line.split("\t");
					int tasLineIndex = Integer.parseInt(ss[1]);
					out2.writeObject(tas[2 * tasLineIndex]);
					out2.writeObject(tas[2 * tasLineIndex + 1]);
				}
				out2.close();
				// System.out.println(i);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws FileNotFoundException {
		Corpus[] allCorpus = {
				new Corpus("input/STS.input.MSRvid.txt", "MSRvid"),
				new Corpus("input/STS.input.MSRpar.txt", "MSRpar"),
				new Corpus("input/STS.input.SMTeuroparl.txt", "SMTeuroparl") };

		int folds = 5;
		for (Corpus corpus : allCorpus) {
			splitDatasetToFolds(corpus, folds);
			splitLLMScoresToFolds(corpus, folds);
			TextAnnotation[] tas = SerializationUtils
					.deserializeTextAnnotations("serialization/"
							+ corpus.getId() + ".sel");
			splitTextAnnotationSerializationsToFolds(corpus, folds, tas);
		}
	}
}
