/**
 * 
 */
package edu.illinois.cs.mmak4.corpus;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

import org.apache.thrift.TException;

import edu.illinois.cs.cogcomp.edison.data.curator.CuratorClient;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.thrift.base.AnnotationFailedException;
import edu.illinois.cs.cogcomp.thrift.base.ServiceUnavailableException;

/**
 * @author mmak4
 * 
 */
public class Corpus {
	private HashMap<Integer, String> corpus_text;
	private String corpus_id;
	private String curatorHost = "smeagol.cs.uiuc.edu";
	private int curatorPort = 5988;
	private CuratorClient client;

	public int get_pairs_size() {
		return corpus_text.size() / 2;
	}

	public int get_total_size() {
		return corpus_text.size();
	}

	private void addAllViews(CuratorClient client, TextAnnotation ta,
			boolean forceUpdate) throws ServiceUnavailableException,
			AnnotationFailedException, TException {
		try {
			// System.out.println("    Charniak Parser");
			client.addCharniakParse(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    Chunk");
			client.addChunkView(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    Coref");
			client.addCorefView(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    Easy First Dependency");
			client.addEasyFirstDependencyView(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    Named Entity");
			client.addNamedEntityView(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    OM NOM NOM");
			client.addNOMView(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    Numerical Quantities");
			client.addNumericalQuantitiesView(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    POS");
			client.addPOSView(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    SRL");
			client.addSRLView(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    Stanford Parser");
			client.addStanfordParse(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}

		try {
			// System.out.println("    Wikifier");
			client.addWikifierView(ta, forceUpdate);
		} catch (Exception e) {
			// Just go on to next
			// System.out.println("      Failed");
		}
	}

	public Corpus(String filename, String corpus_id)
			throws FileNotFoundException {
		super();
		this.client = new CuratorClient(curatorHost, curatorPort);

		// Open up the file and load the corpus text
		FileReader fpIn = new FileReader(filename);
		BufferedReader inF = new BufferedReader(fpIn);

		// We need to get a CorpusID, and as for the text ID, we will make use
		// of a simple 2n, 2n+1 scheme.
		this.corpus_id = new String(corpus_id);
		this.corpus_text = new HashMap<Integer, String>();

		for (int sample_count = 0;; sample_count++) {
			try {
				String sample = inF.readLine();

				// Split sample by '\t'
				String[] text_seq = sample.split("\t");

				// Talk to the service for each sequence of text. Text ID is as
				// stated above.
				for (int i = 0; i < text_seq.length; i++) {
					corpus_text.put(sample_count * 2 + i, new String(
							text_seq[i]));
				}
			} catch (Exception e) {
				// Cheater's way of getting EOF
				break;
			}
		}
	}

	public TextAnnotation[] get_annotation_pair(int line)
			throws ServiceUnavailableException, AnnotationFailedException,
			TException {
		TextAnnotation[] ta2 = new TextAnnotation[2];

		ta2[0] = get_annotation_textID(line * 2);
		ta2[1] = get_annotation_textID(line * 2 + 1);
		return ta2;
	}

	public TextAnnotation get_annotation_textID(int textID)
			throws ServiceUnavailableException, AnnotationFailedException,
			TException {
		boolean forceUpdate = false;
		Integer textID_int = new Integer(textID);
		TextAnnotation ta = client.getTextAnnotation(this.corpus_id,
				textID_int.toString(), corpus_text.get(textID), forceUpdate);
		addAllViews(client, ta, forceUpdate);

		return ta;
	}

	public String getId() {
		return this.corpus_id;
	}

}
