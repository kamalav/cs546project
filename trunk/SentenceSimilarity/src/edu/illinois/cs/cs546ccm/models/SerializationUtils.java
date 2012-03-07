package edu.illinois.cs.cs546ccm.models;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
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
}
