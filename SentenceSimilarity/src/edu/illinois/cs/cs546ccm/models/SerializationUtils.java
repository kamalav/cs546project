package edu.illinois.cs.cs546ccm.models;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.mrcs.comparators.LlmComparator;
import edu.illinois.cs.mmak4.corpus.Corpus;

public class SerializationUtils {

	/*
	 * read TextAnnotation objects from the given file, used for associating the
	 * returned objects with models
	 */
	public static TextAnnotation[] deserializeTextAnnotations(String fileName) {
		TextAnnotation[] tas = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(fileName);
			in = new ObjectInputStream(fis);
			int count = in.readInt();
			tas = new TextAnnotation[count];
			for (int i = 0; i < count; i++) {
				TextAnnotation ta = (TextAnnotation) in.readObject();
				tas[i] = ta;
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		return tas;
	}

	/*
	 * serialize all the TextAnnotaion objects for each line in corpus to a file
	 */
	public static void serializeTextAnnotations(Corpus corpus, String fileName) {
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(fileName);
			out = new ObjectOutputStream(fos);
			int count = corpus.get_total_size();
			out.writeInt(count);
			int size = corpus.get_pairs_size();
			for (int i = 0; i < size; i++) {
				TextAnnotation[] tas = corpus.get_annotation_pair(i);
				out.writeObject(tas[0]);
				out.writeObject(tas[1]);
				System.out.println("Serializing line " + i + " of "
						+ corpus.getId());
			}
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void serializeAllCorpusTextAnnotations(Corpus[] allCorpus) {
		for (Corpus corpus : allCorpus) {
			String fileName = "serialization/" + corpus.getId() + ".sel";
			serializeTextAnnotations(corpus, fileName);
		}
	}

	public static double[] deserializeLLMScores(String fileName) {
		double[] scores = null;
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line = br.readLine();
			scores = new double[Integer.parseInt(line)];
			int i = 0;
			while ((line = br.readLine()) != null) {
				String[] ss = line.split("\t");
				double score1 = Double.parseDouble(ss[0]);
				double score2 = Double.parseDouble(ss[1]);
				scores[2 * i] = score1;
				scores[2 * i + 1] = score2;
				i++;
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return scores;
	}

	public static void serializeLLMScores(TextAnnotation[] tas, String fileName) {
		try {
			LlmComparator llm = new LlmComparator(
					"config/alternativeLlmConfig.txt");
			StringBuffer sb = new StringBuffer(tas.length + "\n");
			int pairs = tas.length / 2;
			for (int i = 0; i < pairs; i++) {
				String source = tas[2 * i].getText();
				String target = tas[2 * i + 1].getText();
				double score1 = llm.compareStrings(source, target);
				double score2 = llm.compareStrings(target, source);
				String line = score1 + "\t" + score2 + "\n";
				sb.append(line);
				System.out.println("Serializing line " + i + " for LLM scores");
			}
			File file = new File(fileName);
			FileOutputStream fop = new FileOutputStream(file);
			String fileContent = sb.toString();
			fop.write(fileContent.getBytes());
			fop.flush();
			fop.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
