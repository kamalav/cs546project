package edu.illinois.cs.cs546ccm.models;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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

	public static void main(String[] args) throws FileNotFoundException {
		Corpus[] allCorpus = {
				new Corpus("input/STS.input.MSRvid.txt", "MSRvid"),
				new Corpus("input/STS.input.MSRpar.txt", "MSRpar"),
				new Corpus("input/STS.input.SMTeuroparl.txt", "SMTeuroparl") };

		int folds = 5;
		for (Corpus corpus : allCorpus) {
			splitDatasetToFolds(corpus, folds);
		}
	}

}
