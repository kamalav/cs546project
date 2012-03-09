package edu.illinois.cs.cs546ccm.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.View;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class Model2XXX extends Model {

	Instances data;
	FastVector attributes;
	Classifier model;

	StringBuffer featuresBuffer;

	public Model2XXX() {
		super("XXX");
		data = defineFeatures();
		featuresBuffer = new StringBuffer("");
	}

	private Instances defineFeatures() {
		// Declare the attribute vector
		attributes = new FastVector(8);

		// Ryan's attributes
		attributes.addElement(new Attribute("r1"));
		attributes.addElement(new Attribute("r2"));
		attributes.addElement(new Attribute("r3"));
		attributes.addElement(new Attribute("r4"));
		attributes.addElement(new Attribute("r5"));
		attributes.addElement(new Attribute("r6"));
		//

		// Guihua's features
		attributes.addElement(new Attribute("g1"));
		attributes.addElement(new Attribute("g2"));
		//

		// Cedar's features
		attributes.addElement(new Attribute("c1"));
		//

		// Zhijin's features
		attributes.addElement(new Attribute("z1"));
		attributes.addElement(new Attribute("z2"));
		//

		// Gold-standard score (class value)
		// Code snippet for handling multi-class classification
		FastVector fvClassVal = new FastVector(21);
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 10; j++) {
				fvClassVal.addElement(i + "." + j);
			}
		}
		fvClassVal.addElement("5.0");
		attributes.addElement(new Attribute("gs_approx", fvClassVal));
		//
		// attributes.addElement(new Attribute("gs"));
		//

		Instances ret = new Instances("CCM-SemanticSimilarity", attributes,
				1000);
		ret.setClassIndex(ret.numAttributes() - 1);
		return ret;
	}

	@Override
	public double similarity(TextAnnotation ta1, TextAnnotation ta2) {
		Instance example = getInstance(ta1, ta2, 0.0);
		example.setDataset(data);
		double similarity;
		try {
			double[] res = model.distributionForInstance(example);
			similarity = model.classifyInstance(example) / 10;
		} catch (Exception e) {
			System.out.println("Exception while trying to classify");
			similarity = -1;
		}
		return similarity;
	}

	private double[] score5(TextAnnotation ta1, TextAnnotation ta2) {
		// use cached raw LLM score as a feature
		int index1 = Integer.parseInt(ta1.getId());
		int index2 = Integer.parseInt(ta2.getId());
		double llmScore1 = llmScores[index1];
		double llmScore2 = llmScores[index2];
		System.out.println(llmScore1 + " " + llmScore2);
		return new double[] { llmScore1, llmScore2 };
	}

	private static double[] score4(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		// Zhijin's method
		View v1 = ta1.getView(ViewNames.NER);
		View v2 = ta2.getView(ViewNames.NER);
		// System.out.println(v1);
		// System.out.println(v2);

		List<Constituent> cs1;
		List<Constituent> cs2;
		// use cs2 to save the larger
		if (v1.getConstituents().size() < v2.getConstituents().size()) {
			cs1 = v1.getConstituents();
			cs2 = v2.getConstituents();
		} else {
			cs1 = v2.getConstituents();
			cs2 = v1.getConstituents();
		}

		double score = 0;
		for (Constituent c1 : cs1) {
			// System.out.println(c1.getLabel() + " " + c1.toString());
			double point = 0;
			for (Constituent c2 : cs2) {

				if (c1.toString().equals(c2.toString())) {// same words
					point = 1;
				} else if (point == 0 && c1.getLabel().equals(c2.getLabel())) {
					if (c1.toString().contains(c2.toString())
							|| c2.toString().contains(c1.toString())) {
						// one NE is a substring of another
						point = 1;
					} else {
						// a little score if different words but same NER types
						point = 0.3;
					}

				} else {
					// NER types not matching
				}

			}
			score += point;
		}

		// if the numbers of NEs are different, penalize the score
		int sizeDiff = cs2.size() - cs1.size();
		for (int i = 1; i <= sizeDiff; i++) {
			double penalty = 1.0 / i;
			if (score - penalty > 0) {
				score -= penalty;
			} else {
				break;
			}
		}
		if (cs1.size() == 0)
			return new double[] { 0, sizeDiff };
		return new double[] { score / cs1.size(), sizeDiff };
	}

	private static double[] score3(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		// Cedar's method

		// LLM as the comparator

		// Model1LLM m = new Model1LLM();

		// double result = m.similarity(chunckcontent1, chunckcontent2);

		// Initialize the score of the pair of sentences to be 0.
		double score = 0;
		double scores[] = new double[1];
		scores[0] = score;

		// The threshold for word comparator. For constituentMatch function.
		double threshold = 0.9;
		View v1 = ta1.getView(ViewNames.POS);
		View v2 = ta2.getView(ViewNames.POS);

		List<Constituent> cs1;
		;
		List<Constituent> cs2;

		// User sentence of the shorter length as the source sentence,i.e. cs1
		if (v1.getConstituents().size() < v2.getConstituents().size()) {
			cs1 = v1.getConstituents();
			cs2 = v2.getConstituents();
		} else {
			cs1 = v2.getConstituents();
			cs2 = v1.getConstituents();
		}

		/*
		 * Define the counts of each POS tags
		 */
		int cNNP = 0;
		int cNNPS = 0;
		int cNN = 0;
		int cNNS = 0;

		int cVB = 0;
		int cVBD = 0;
		int cVBG = 0;
		int cVBN = 0;
		int cVBP = 0;
		int cVBZ = 0;

		int cNouns = 0; // count of nouns
		int cVerbs = 0; // count of verbs

		int cNNP2 = 0;
		int cNNPS2 = 0;
		int cNN2 = 0;
		int cNNS2 = 0;

		int cVB2 = 0;
		int cVBD2 = 0;
		int cVBG2 = 0;
		int cVBN2 = 0;
		int cVBP2 = 0;
		int cVBZ2 = 0;

		int cNouns2 = 0; // count of nouns
		int cVerbs2 = 0; // count of verbs

		/*
		 * Define the set of constituents for each POS tags. Here we only
		 * consider noun-related and verb-related POS tags, since the words with
		 * these tags are most informative in the sentence.
		 */

		// noun-related POS tags
		// sentence1
		Set<Constituent> NNP_Setc = new HashSet<Constituent>();
		Set<Constituent> NNPS_Setc = new HashSet<Constituent>();
		Set<Constituent> NN_Setc = new HashSet<Constituent>();
		Set<Constituent> NNS_Setc = new HashSet<Constituent>();

		// sentence2
		Set<Constituent> NNP_Set2c = new HashSet<Constituent>();
		Set<Constituent> NNPS_Set2c = new HashSet<Constituent>();
		Set<Constituent> NN_Set2c = new HashSet<Constituent>();
		Set<Constituent> NNS_Set2c = new HashSet<Constituent>();

		// the set all nouns
		Set<Constituent> Nouns_Setc = new HashSet<Constituent>();
		Set<Constituent> Nouns_Set2c = new HashSet<Constituent>();

		// verb-related POS tags
		// Question: Do the types of verbs really matter???
		Set<Constituent> VB_Setc = new HashSet<Constituent>();
		Set<Constituent> VBD_Setc = new HashSet<Constituent>();
		Set<Constituent> VBG_Setc = new HashSet<Constituent>();
		Set<Constituent> VBN_Setc = new HashSet<Constituent>();
		Set<Constituent> VBP_Setc = new HashSet<Constituent>();
		Set<Constituent> VBZ_Setc = new HashSet<Constituent>();

		Set<Constituent> VB_Set2c = new HashSet<Constituent>();
		Set<Constituent> VBD_Set2c = new HashSet<Constituent>();
		Set<Constituent> VBG_Set2c = new HashSet<Constituent>();
		Set<Constituent> VBN_Set2c = new HashSet<Constituent>();
		Set<Constituent> VBP_Set2c = new HashSet<Constituent>();
		Set<Constituent> VBZ_Set2c = new HashSet<Constituent>();

		// the set of all verbs
		Set<Constituent> Verbs_Setc = new HashSet<Constituent>();
		Set<Constituent> Verbs_Set2c = new HashSet<Constituent>();

		for (Constituent c1 : cs1) {
			String tag = c1.getLabel();
			// String word=c1.toString();
			if (tag.contentEquals("NNP")) {
				cNNP++;
				NNP_Setc.add(c1);
			} else if (tag.contentEquals("NNPS")) {
				cNNPS++;
				NNPS_Setc.add(c1);
			} else if (tag.contentEquals("NN")) {
				cNN++;
				NN_Setc.add(c1);
			} else if (tag.contentEquals("NNS")) {
				cNNS++;
				NNS_Setc.add(c1);
			} else if (tag.contentEquals("VB")) {
				cVB++;
				VB_Setc.add(c1);
			} else if (tag.contentEquals("VBD")) {
				cVBD++;
				VBD_Setc.add(c1);
			} else if (tag.contentEquals("VBG")) {
				cVBG++;
				VBG_Setc.add(c1);
			} else if (tag.contentEquals("VBN")) {
				cVBN++;
				VBN_Setc.add(c1);
			} else if (tag.contentEquals("VBP")) {
				cVBP++;
				VBP_Setc.add(c1);
			} else if (tag.contentEquals("VBZ")) {
				cVBZ++;
				VBZ_Setc.add(c1);
			}

			if (tag.startsWith("N"))
				Nouns_Setc.add(c1);
			else if (tag.startsWith("V"))
				Verbs_Setc.add(c1);

		}

		for (Constituent c2 : cs2) {
			String tag = c2.getLabel();
			// String word=c2.toString();

			if (tag.contentEquals("NNP")) {
				cNNP2++;
				NNP_Set2c.add(c2);
			} else if (tag.contentEquals("NNPS")) {
				cNNPS2++;
				NNPS_Set2c.add(c2);
			} else if (tag.contentEquals("NN")) {
				cNN2++;
				NN_Set2c.add(c2);
			} else if (tag.contentEquals("NNS")) {
				cNNS2++;
				NNS_Set2c.add(c2);
			} else if (tag.contentEquals("VB")) {
				cVB2++;
				VB_Set2c.add(c2);
			} else if (tag.contentEquals("VBD")) {
				cVBD2++;
				VBD_Set2c.add(c2);
			} else if (tag.contentEquals("VBG")) {
				cVBG2++;
				VBG_Set2c.add(c2);
			} else if (tag.contentEquals("VBN")) {
				cVBN2++;
				VBN_Set2c.add(c2);
			} else if (tag.contentEquals("VBP")) {
				cVBP2++;
				VBP_Set2c.add(c2);
			} else if (tag.contentEquals("VBZ")) {
				cVBZ2++;
				VBZ_Set2c.add(c2);
			}

			if (tag.startsWith("N"))
				Nouns_Set2c.add(c2);
			else if (tag.startsWith("V"))
				Verbs_Set2c.add(c2);

		}

		/*
		 * Heuristic #1: If at least one alignment of two proper nouns between
		 * the two sentences can be found, then they will be likely to share the
		 * same topics. We can conclude the pair of sentences has at least score
		 * 0.2/1.
		 * 
		 * Implementation:
		 */

		if (NNP_Setc.size() < NNP_Set2c.size()) {
			loop_outer: for (Constituent c : NNP_Setc) {
				for (Constituent c2 : NNP_Set2c) {
					if (constituentMatch(c, c2) > threshold) {
						score = 0.2;
						break loop_outer;
					}
				}
			}
		} else {
			loop_outer: for (Constituent c : NNP_Set2c) {
				for (Constituent c2 : NNP_Setc) {
					if (constituentMatch(c, c2) > threshold) {
						score = 0.2;
						break loop_outer;
					}
				}
			}
		}

		System.out.println("Hello");

		int n_algn = 0;

		/*
		 * Find the number of alignments of nouns between the two sentences Loop
		 * through each noun in the sentence with less number of nouns.
		 */
		if (Nouns_Setc.size() < Nouns_Set2c.size()) {

			for (Constituent c : Nouns_Setc) {
				for (Constituent c2 : Nouns_Set2c) {
					// if (w.contentEquals(w2))
					if (constituentMatch(c, c2) > threshold) {
						// System.out.println(w+" "+w2);
						n_algn++;
						break;
					}
				}
			}

		} else {
			for (Constituent c : Nouns_Set2c) {
				for (Constituent c2 : Nouns_Setc) {
					// if (w.contentEquals(w2))
					if (constituentMatch(c, c2) > threshold) {
						// System.out.println(w+" "+w2);
						n_algn++;
						break;
					}
				}
			}

		}

		/*
		 * Find the number of alignments of verbs between the two sentences
		 * Start with Looping through each verb in the sentence with less number
		 * of verbs.
		 */
		int v_algn = 0;
		if (Verbs_Setc.size() < Verbs_Set2c.size()) {
			for (Constituent c : Verbs_Setc) {
				for (Constituent c2 : Verbs_Set2c) {
					// if (w.contentEquals(w2))
					if (constituentMatch(c, c2) > threshold) {
						// System.out.println(w+"::::::::::::::::::::::"+w2);
						v_algn++;
						break;
					}
				}
			}
		} else {
			for (Constituent c : Verbs_Setc) {
				for (Constituent c2 : Verbs_Set2c) {
					// if (w.contentEquals(w2))
					if (constituentMatch(c, c2) > threshold) {
						// System.out.println(c.toString()+" "+c2.toString());
						v_algn++;
						break;
					}
				}
			}

		}

		if (n_algn + v_algn == 0)
			return scores;

		/*
		 * Heuristic #2: After the heuristic#1 is confirmed, if at least one
		 * alignment of two verbs between the two sentences can be found, then
		 * they will be likely to share some details. Hence the pair can be
		 * given score 2 in GS, and 0.4/1.0 here.
		 */
		if (score == 0.2) {
			if (v_algn > 0)
				score = 0.4;
		}
		// System.out.println("The number of matched verbs is:"+v_algn);

		/*
		 * Heuristic #3: The more alignments of words with POS tags in the same
		 * category, the higher is the score.
		 * 
		 * Implementation: Nouns are considered as one group, so does verbs.
		 * 
		 * Remarks:
		 */

		int nsem_tag = 0;
		int nsem_tag2 = 0;

		nsem_tag = Nouns_Setc.size() + Verbs_Setc.size();
		nsem_tag2 = Nouns_Set2c.size() + Verbs_Set2c.size();

		double value = 0;

		if (nsem_tag > nsem_tag2) {
			value = (double) (v_algn + n_algn) / nsem_tag;
		} else {
			value = (double) (v_algn + n_algn) / nsem_tag2;
		}

		if (NNP_Setc.isEmpty() && NNP_Set2c.isEmpty()) {
			score = value;
		} else {
			score = score + value * 0.6;
		}

		System.out.println("The score for this pair of sentence is " + score);

		return scores;
		// return new double[] {score};
	}

	private static double score2_1(TextAnnotation ta1, TextAnnotation ta2)
			throws IOException {
		// Guihua's method
		// ta1 is Text, and ta2 is Hypothesis
		View v1 = ta1.getView(ViewNames.SHALLOW_PARSE);
		View v2 = ta2.getView(ViewNames.SHALLOW_PARSE);
		// System.out.println(v1);
		// System.out.println(v2);

		List<Constituent> cs1;
		List<Constituent> cs2;
		cs1 = v1.getConstituents();
		cs2 = v2.getConstituents();

		double score = 0;
		for (Constituent c2 : cs2) {
			String chuncktype2 = c2.getLabel();
			String chunckcontent2 = c2.toString();
			double point = 0;
			for (Constituent c1 : cs1) {
				String chuncktype1 = c1.getLabel();

				// only if they have the same type of chunk (NP, VP, and so on),
				// they can compare, and get the max one
				if (chuncktype1.compareToIgnoreCase(chuncktype2) == 0) {
					double result = constituentMatch(c1, c2);
					/*
					 * String chunckcontent1=c1.toString(); Model1LLM m = new
					 * Model1LLM(); double result = m.similarity(chunckcontent1,
					 * chunckcontent2);
					 */
					if (result > point)
						point = result;
				}
			}
			score = score + point;
		}

		// Normalize by the Hypothesis's length
		if (cs2.size() != 0)
			score = score / cs2.size();
		return score;
	}

	private static double[] score2(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		// Guihua's method
		double score[] = new double[2];

		// ta1 is Text, ta2 is Hypothesis
		try {
			score[0] = score2_1(ta1, ta2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// ta2 is Text, ta1 is Hypothesis
		try {
			score[1] = score2_1(ta2, ta1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return score;
	}

	private static double[] score1(TextAnnotation ta1, TextAnnotation ta2) {
		return new double[] { ta1.getText().split(" ").length,
				ta2.getText().split(" ").length, wordsInCommon(ta1, ta2),
				wordsInCommon(ta2, ta1), srlSimilarity(ta1, ta2),
				srlSimilarity(ta2, ta1) };
	}

	private static double srlSimilarity(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		// Ryan's method
		View v1 = ta1.getView(ViewNames.SRL);
		View v2 = ta2.getView(ViewNames.SRL);

		List<Constituent> cs1 = v1.getConstituents();
		List<Constituent> cs2 = v2.getConstituents();

		double score = 0;
		for (Constituent c1 : cs1) {
			double point = 0;
			for (Constituent c2 : cs2) {
				double temp = predMatch(c1, c2);
				if (temp > point)
					point = temp;
			}
			score = score + point;
		}

		return score;
	}

	// Helper method for SRL similarity
	private static double predMatch(Constituent c1, Constituent c2) {
		if (c1.getLabel().equals("Predicate")
				&& c2.getLabel().equals("Predicate")) {
			if (c1.getAttribute("predicate").equals(
					c2.getAttribute("predicate"))) {
				// these two nodes having matching predicates
				double score = 0;
				List<Relation> rs1 = c1.getOutgoingRelations();
				List<Relation> rs2 = c2.getOutgoingRelations();
				for (Relation r1 : rs1) {
					double point = 0;
					for (Relation r2 : rs2) {
						if (r1.getRelationName().equals(r2.getRelationName())) {
							// should be a similarity measure
							double temp = constituentMatch(r1.getTarget(),
									r2.getTarget());
							if (temp > point)
								point = temp;
						}
					}
				}
				if (rs1.size() > 0)
					return score / rs1.size();
				return 1;
			}
		}
		return 0;
	}

	// Right now, must match on all tokens
	private static double constituentMatch(Constituent c1, Constituent c2) {
		String t1 = "";
		for (int i = c1.getStartSpan(); i < c1.getEndSpan(); i++) {
			t1 += c1.getTextAnnotation().getToken(i) + " ";
		}
		if (t1.trim().equals(c1.getTextAnnotation().getText())) {
			System.out.println(t1.trim());
			System.out.println(c1.getTextAnnotation().getText());
		}
		String t2 = "";
		for (int i = c2.getStartSpan(); i < c2.getEndSpan(); i++) {
			t2 += c2.getTextAnnotation().getToken(i) + " ";
		}

		String[] ws1 = t1.split(" ");
		String[] ws2 = t2.split(" ");

		double score = 0;
		for (String w1 : ws1) {
			double point = 0;
			for (String w2 : ws2) {
				if (w1.equals(w2))
					point = 1;
			}
			score = score + point;
		}
		if (ws1.length > 0)
			return score / ws1.length;
		return score;
	}

	private static double wordsInCommon(TextAnnotation ta1, TextAnnotation ta2) {
		View v1 = ta1.getView(ViewNames.SENTENCE);
		View v2 = ta2.getView(ViewNames.SENTENCE);

		String[] ws1 = v1.getConstituents().get(0).getTextAnnotation()
				.getTokens();
		String[] ws2 = v2.getConstituents().get(0).getTextAnnotation()
				.getTokens();

		double score = 0;
		for (String w1 : ws1) {
			double point = 0;
			for (String w2 : ws2) {
				if (w1.equals(w2))
					point = 1;
			}
			score = score + point;
		}
		if (ws1.length > 0)
			return score / ws1.length;
		return score;
	}

	@Override
	public int confidence(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO implement this method
		return -1;
	}

	public void train(String gsFile) {
		try {
			ArrayList<Double> gs_arr = getGSscores(gsFile);
			int pairs = tas.length / 2;
			if (gs_arr.size() != pairs) {
				System.out
						.println("Corpus does not match gold-standard; aborting training");
				return;
			}
			for (int i = 0; i < pairs; i++) {
				TextAnnotation ta1 = this.tas[2 * i];
				TextAnnotation ta2 = this.tas[2 * i + 1];
				double gs = gs_arr.get(i);
				trainInstance(ta1, ta2, gs);
			}
			// model = new M5P();
			model = new LibSVM();
			model.buildClassifier(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Training failed.");
			e.printStackTrace();
		}
	}

	private void trainInstance(TextAnnotation ta1, TextAnnotation ta2, double gs) {
		data.add(getInstance(ta1, ta2, gs));
	}

	private Instance getInstance(TextAnnotation ta1, TextAnnotation ta2,
			double gs) {
		double[] score1 = score1(ta1, ta2);
		double[] score2 = score2(ta1, ta2);
		double[] score3 = score3(ta1, ta2);
		double[] score4 = score4(ta1, ta2);
		double[] score5 = score5(ta1, ta2);

		return combineAttributes(score1, score2, score3, score4, score5, gs);
	}

	private Instance combineAttributes(double[] score1, double[] score2,
			double[] score3, double[] score4, double[] score5, double gs) {
		Instance inst = new Instance(data.numAttributes());
		inst.setDataset(data);

		StringBuffer sb = new StringBuffer("");
		sb.append(HandleResult.score_to_label(gs));

		int count = 0;
		for (int i = 0; i < score1.length; i++) {
			inst.setValue(count, score1[i]);
			count++;
			sb.append(" " + count + ":" + score1[i]);
		}
		for (int i = 0; i < score2.length; i++) {
			inst.setValue(count, score2[i]);
			count++;
			sb.append(" " + count + ":" + score2[i]);
		}
		for (int i = 0; i < score3.length; i++) {
			inst.setValue(count, score3[i]);
			count++;
			sb.append(" " + count + ":" + score3[i]);
		}
		for (int i = 0; i < score4.length; i++) {
			inst.setValue(count, score4[i]);
			count++;
			sb.append(" " + count + ":" + score4[i]);
		}
		for (int i = 0; i < score5.length; i++) {
			inst.setValue(count, score5[i]);
			count++;
			sb.append(" " + count + ":" + score5[i]);
		}

		inst.setValue(count, parseGS(gs));
		// System.out.println(inst);

		featuresBuffer.append(sb.toString() + "\n");
		return inst;
	}

	// Truncates the string representation of gs to the tenths place
	// E.g.: parseGS(2.355) = 2.3
	private static String parseGS(double gs) {
		String val = gs + "";
		for (int i = val.length(); i < 3; i++)
			val += "0";
		return val.substring(0, 3);
	}

	private static ArrayList<Double> getGSscores(String gsFile) {
		try {
			ArrayList<Double> list = new ArrayList<Double>();
			Scanner sc = new Scanner(new File(gsFile));
			while (sc.hasNextLine()) {
				list.add(Double.parseDouble(sc.nextLine()));
			}
			sc.close();
			return list;
		} catch (FileNotFoundException e) {
			// we'll make a fuss further up the call stack
			return new ArrayList<Double>();
		}
	}

	@Override
	public void computeAndSaveOutputToFile(String fileName) throws IOException {
		String gsFile = getGSFileName(fileName);
		train(gsFile);
		super.computeAndSaveOutputToFile(fileName);
	}

	private static String getGSFileName(String fileName) {
		String corpusLabel = fileName.split("[/_]")[1];
		if (corpusLabel.contains("MSR"))
			return "input/STS.gs." + corpusLabel + ".txt";
		else
			return "input/STS.gs.Temp.txt";
	}
}
