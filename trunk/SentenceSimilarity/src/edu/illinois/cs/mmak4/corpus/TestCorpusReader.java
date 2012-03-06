package edu.illinois.cs.mmak4.corpus;

import java.io.FileNotFoundException;

import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;

public class TestCorpusReader {

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws TException
	 * @throws AnnotationFailedException
	 * @throws ServiceUnavailableException
	 */
	public static void main(String[] args) throws FileNotFoundException,
			ServiceUnavailableException, AnnotationFailedException, TException {
		// TODO Auto-generated method stub
		Corpus sampleCorpus = new Corpus("STS.input.MSRpar.txt",
				"STS.input.MSRpar.txt");

		TextAnnotation ta = sampleCorpus.get_annotation_textID(6);
		// Print the tokenized text
		System.out.println(ta.getTokenizedText());

		showViews(ta);

		TextAnnotation[] ta2 = sampleCorpus.get_annotation_pair(3);

		showViews(ta2[0]);
		showViews(ta2[1]);

		System.out.println(ta.getCorpusId() + " " + ta.getId() + ":"
				+ ta.getTokenizedText());
		System.out.println(ta2[0].getCorpusId() + " " + ta2[0].getId() + ":"
				+ ta2[0].getTokenizedText());
		System.out.println(ta2[1].getCorpusId() + " " + ta2[1].getId() + ":"
				+ ta2[1].getTokenizedText());
		System.out.println(sampleCorpus.get_pairs_size());
		System.out.println(sampleCorpus.get_total_size());
	}

	public static void showViews(TextAnnotation ta) {
		System.out.println(ta.getTokenizedText());
		System.out.println(ta.getView(ViewNames.SENTENCE));
		System.out.println(ta.getView(ViewNames.POS));
		System.out.println(ta.getView(ViewNames.NER));
		System.out.println(ta.getView(ViewNames.SHALLOW_PARSE));
		System.out.println(ta.getView(ViewNames.SRL));
		System.out.println(ta.getView(ViewNames.NOM));
		// System.out.println(ta.getView(ViewNames.COREF));
		System.out.println(ta.getView(ViewNames.PARSE_STANFORD));
		System.out.println(ta.getView(ViewNames.PARSE_CHARNIAK));
		// System.out.println(ta.getView(ViewNames.DEPENDENCY_STANFORD));
		System.out.println(ta.getView(ViewNames.DEPENDENCY));
		// System.out.println(ta.getView(ViewNames.QUANTITIES));
		System.out.println(ta.getView(ViewNames.WIKIFIER));

	}
}
