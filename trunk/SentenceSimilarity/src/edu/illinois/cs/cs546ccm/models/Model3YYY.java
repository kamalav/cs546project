package edu.illinois.cs.cs546ccm.models;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import weka.classifiers.Classifier;
import weka.classifiers.trees.M5P;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.View;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class Model3YYY extends Model {

	Instances data;
	FastVector attributes;
	Classifier model;

	StringBuffer svmFeatureBuffer;
	Set<String> Stopwords = new HashSet<String>();

	public Model3YYY() {
		super("YYY");
		data = defineFeatures();
		resetSVMFeatureBuffer();
		try {
			FileInputStream fstream1 = new FileInputStream(
					"config/llmStopwords.txt");

			DataInputStream in1 = new DataInputStream(fstream1);
			BufferedReader br_stopwords = new BufferedReader(
					new InputStreamReader(in1));

			String line;
			while ((line = br_stopwords.readLine()) != null) {
				Stopwords.add(line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void resetSVMFeatureBuffer() {
		svmFeatureBuffer = new StringBuffer("");
	}

	private Instances defineFeatures() {
		// Declare the attribute vector
		attributes = new FastVector(59);

		// Ryan's attributes
		attributes.addElement(new Attribute("r1"));
		attributes.addElement(new Attribute("r2"));
		attributes.addElement(new Attribute("r3"));
		attributes.addElement(new Attribute("r4"));
		attributes.addElement(new Attribute("r5"));
		attributes.addElement(new Attribute("r6"));
		attributes.addElement(new Attribute("r7"));
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

		// LLM's features
		attributes.addElement(new Attribute("l1"));
		attributes.addElement(new Attribute("l2"));
		//

		// features about quantity comparison
		for (int i = 1; i <= 24; i++)
			attributes.addElement(new Attribute("n" + i));
		//

		// features about dependency parser
		for (int i = 1; i <= 10; i++)
			attributes.addElement(new Attribute("d" + i));
		//

		// features about SRL predicates
		for (int i = 1; i <= 4; i++)
			attributes.addElement(new Attribute("s" + i));
		// features about NP VP compare respectively for short sentences
		for (int i = 1; i <= 4; i++)
			attributes.addElement(new Attribute("k" + i));

		// LLM_WNsim's features
		attributes.addElement(new Attribute("w1"));
		attributes.addElement(new Attribute("w2"));

		// Gold-standard score (class value)
		// Code snippet for handling multi-class classification
		FastVector fvClassVal = new FastVector(51);
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 10; j++) {
				fvClassVal.addElement(i + "." + j);
			}
		}
		fvClassVal.addElement("5.0");
		//
		// attributes.addElement(new Attribute("gs_approx", fvClassVal));
		attributes.addElement(new Attribute("gs"));
		//

		Instances ret = new Instances("CCM-SemanticSimilarity", attributes,
				1000);
		ret.setClassIndex(ret.numAttributes() - 1);
		return ret;
	}

	@Override
	public double similarity(TextAnnotation ta1, TextAnnotation ta2) {
		Instance example = getInstance(ta1, ta2, 0.0, false);
		example.setDataset(data);
		double similarity;
		try {
			similarity = model.classifyInstance(example);
			if (similarity < 0)
				similarity = 0;
			if (similarity > 5)
				similarity = 5;

		} catch (Exception e) {
			System.err.println("Exception while trying to classify");
			similarity = -1;
		}
		return similarity;
	}

	private double[] score5(TextAnnotation ta1, TextAnnotation ta2,
			boolean isTrain) {
		// use cached raw LLM score as a feature
		int line = Integer.parseInt(ta1.getId()) / 2;
		if (isTrain)
			return getTrainLLMScores(line);
		return getTestLLMScores(line);
	}

	private double[] score_WNsim(TextAnnotation ta1, TextAnnotation ta2,
			boolean isTrain) {
		// use cached raw LLM score as a feature
		int line = Integer.parseInt(ta1.getId()) / 2;
		if (isTrain)
			return getTrainLLMScores_WNsim(line);
		return getTestLLMScores_WNsim(line);
	}

	private double[] score4(TextAnnotation ta1, TextAnnotation ta2,
			boolean isTrain) {
		// Zhijin's method

		// if both do not contain NER views, use averaged LLM scores instead
		if (!ta1.hasView(ViewNames.NER) || !ta2.hasView(ViewNames.NER)) {
			int line = Integer.parseInt(ta1.getId()) / 2;
			double[] llmPairScores;
			if (isTrain)
				llmPairScores = getTrainLLMScores(line);
			else
				llmPairScores = getTestLLMScores(line);
			int sizeDiff = ta1.hasView(ViewNames.NER) ? ta1
					.getView(ViewNames.NER).getConstituents().size() : ta2
					.hasView(ViewNames.NER) ? ta2.getView(ViewNames.NER)
					.getConstituents().size() : 0;
			double score = (llmPairScores[0] + llmPairScores[1]) / 2;
			return new double[] { score, sizeDiff };
		}

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
				String entity1 = c1.toString();
				String entity2 = c2.toString();
				double p = SimilarityUtils.namedEntitySimilarity(entity1,
						entity2);
				if (p > point)
					point = p;
				// System.out.println(entity1 + " vs " + entity2 + ": " +
				// point);
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

	private double[] score3(TextAnnotation ta1, TextAnnotation ta2) {
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

		scores[0] = score;
		return scores;
		// return new double[] {score};
	}

	private double score2_1(TextAnnotation ta1, TextAnnotation ta2)
			throws IOException {
		/* temporary change by zhijin */
		if (!ta1.hasView(ViewNames.SHALLOW_PARSE)
				|| !ta2.hasView(ViewNames.SHALLOW_PARSE)) {
			return 1;
		}
		
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

	private double[] score2(TextAnnotation ta1, TextAnnotation ta2) {
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

	private double[] score1(TextAnnotation ta1, TextAnnotation ta2) {
		double l1 = ta1.getTokens().length;
		double l2 = ta2.getTokens().length;
		double length_ratio = Math.min(l1 / l2, l2 / l1);
		return new double[] { length_ratio, wordsInCommon(ta1, ta2),
				wordsInCommon(ta2, ta1), wordSimilarity(ta1, ta2),
				wordSimilarity(ta2, ta1), srlSimilarity(ta1, ta2),
				srlSimilarity(ta2, ta1) };
	}

	private static double wordSimilarity(TextAnnotation ta2, TextAnnotation ta1) {
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
				double temp = SimilarityUtils.wordSimilairty(w1, w2);
				if (temp > point)
					point = temp;
			}
			score = score + point;
		}
		if (ws1.length > 0)
			return score / ws1.length;
		return score;
	}

	private static double srlSimilarity(TextAnnotation ta1, TextAnnotation ta2) {
		// for temporary testing, to be replaced
		if (!ta1.hasView(ViewNames.SRL) || !ta2.hasView(ViewNames.SRL)) {
			return 0.5;
		}

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
					score += point;
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
				double temp = SimilarityUtils.wordSimilairty(w1, w2);
				if (temp > point)
					point = temp;
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
		return 100;
	}

	public void train(String corpusID) {
		resetSVMFeatureBuffer();
		try {
			ArrayList<Double> gs_arr = getGSscores("input/STS.gs." + corpusID
					+ ".txt");
			int pairs = train_tas.length / 2;
			if (gs_arr.size() != pairs) {
				System.err
						.println("Corpus does not match gold-standard; aborting training");
				return;
			}
			for (int i = 0; i < pairs; i++) {
				TextAnnotation ta1 = this.train_tas[2 * i];
				TextAnnotation ta2 = this.train_tas[2 * i + 1];
				double gs = gs_arr.get(i);
				trainInstance(ta1, ta2, gs);
			}
			model = new M5P();
			// model = new LibSVM();
			model.buildClassifier(data);

			String svmFileName = "svm_final/" + corpusID + ".txt";
			saveSVMFeaturesToFile(svmFileName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("Training failed.");
			e.printStackTrace();
		}
	}

	private void trainInstance(TextAnnotation ta1, TextAnnotation ta2, double gs) {
		data.add(getInstance(ta1, ta2, gs, true));
	}

	private Instance getInstance(TextAnnotation ta1, TextAnnotation ta2,
			double gs, boolean isTrain) {
		double[] score1 = score1(ta1, ta2);
		double[] score2 = score2(ta1, ta2);
		double[] score3 = score3(ta1, ta2);
		double[] score4 = score4(ta1, ta2, isTrain);
		double[] score5 = score5(ta1, ta2, isTrain);

		// below are just added for model 3
		double[] score6 = score_number(ta1, ta2);
		double[] score7 = score_dependency(ta1, ta2);
		double[] score8 = score_predicateofSRL(ta1, ta2);
		double[] score9 = score_NP_VP(ta1, ta2);
		double[] score10 = score_WNsim(ta1, ta2, isTrain);

		return combineAttributes(score1, score2, score3, score4, score5,
				score6, score7, score8, score9, score10, gs, isTrain);
	}

	private Instance combineAttributes(double[] score1, double[] score2,
			double[] score3, double[] score4, double[] score5, double[] score6,
			double[] score7, double[] score8, double[] score9,
			double[] score10, double gs, boolean isTrain) {
		Instance inst = new Instance(data.numAttributes());
		inst.setDataset(data);

		StringBuffer sb = new StringBuffer("");
		// sb.append(HandleResult.score_to_label(gs));
		sb.append(String.valueOf(gs));

		int count = 0;
		int featureIndex = 1;

		for (int i = 0; i < score1.length; i++) {
			inst.setValue(count, score1[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score1[i]);
		}
		for (int i = 0; i < score2.length; i++) {
			inst.setValue(count, score2[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score2[i]);
		}
		for (int i = 0; i < score3.length; i++) {
			inst.setValue(count, score3[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score3[i]);
		}
		for (int i = 0; i < score4.length; i++) {
			inst.setValue(count, score4[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score4[i]);
		}

		for (int i = 0; i < score5.length; i++) {
			inst.setValue(count, score5[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score5[i]);
		}

		// below are just added for model 3
		for (int i = 0; i < score6.length; i++) {
			inst.setValue(count, score6[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score6[i]);
		}

		for (int i = 0; i < score7.length; i++) {
			inst.setValue(count, score7[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score7[i]);
		}

		for (int i = 0; i < score8.length; i++) {
			inst.setValue(count, score8[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score8[i]);
		}

		for (int i = 0; i < score9.length; i++) {
			inst.setValue(count, score9[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score9[i]);
		}

		for (int i = 0; i < score10.length; i++) {
			inst.setValue(count, score10[i]);
			count++;
			sb.append(" " + featureIndex++ + ":" + score10[i]);
		}

		// inst.setValue(count, parseGS(gs));
		inst.setValue(count, gs);
		// System.out.println(inst);

		svmFeatureBuffer.append(sb.toString() + "\n");
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
		resetSVMFeatureBuffer();
		super.computeAndSaveOutputToFile(fileName);

		// save feature vectors to file for (Guihua's) SVM training
		String svmFileName = "svm_folds/test_" + fileName.split("/")[1];
		saveSVMFeaturesToFile(svmFileName);
	}

	public void saveSVMFeaturesToFile(String fileName) {
		String fileContent = svmFeatureBuffer.toString();
		if (fileContent.isEmpty()) {
			System.err.println("SVM features buffer is empty!");
		} else {
			try {
				File file = new File(fileName);
				FileOutputStream fop = new FileOutputStream(file);
				fop.write(fileContent.getBytes());
				fop.flush();
				fop.close();
				System.out.println("SVM features are saved into file: "
						+ fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String getGSFileName(String fileName) {
		String corpusLabel = fileName.substring(fileName.indexOf('/') + 1,
				fileName.indexOf('_'));
		return "input/STS.gs." + corpusLabel + ".txt";
	}

	// guihua, calculate the number's feature
	private double[] score_number(TextAnnotation ta1, TextAnnotation ta2) {
		double score[] = new double[24];
		for (int i = 0; i < 24; i++)
			score[i] = 0;
		double score1[] = new double[12];
		for (int i = 0; i < 12; i++)
			score1[i] = 0;
		double score2[] = new double[12];
		for (int i = 0; i < 12; i++)
			score2[i] = 0;

		score1 = score_number1(ta1, ta2);
		score2 = score_number1(ta2, ta1);

		for (int i = 0; i < 24; i++) {
			if (i < 12) {
				score[i] = score1[i];
			} else {
				score[i] = score2[i - 12];
			}
		}
		return score;
	}

	private double[] score_number1(TextAnnotation ta1, TextAnnotation ta2) {
		double score[] = new double[12];
		for (int i = 0; i < 12; i++) {
			score[i] = 0;
		}
		try {
			View v1 = ta1.getView(ViewNames.DEPENDENCY);
			View v2 = ta2.getView(ViewNames.DEPENDENCY);

			List<Relation> r1 = v1.getRelations();
			List<Relation> r2 = v2.getRelations();
			int countN = 0;

			for (Relation e1 : r1) {
				String relation_name = e1.getRelationName();
				String e1source = e1.getSource().toString();
				String e1target = e1.getTarget().toString();

				boolean isNumber = false;
				String e1number = "", e1other = "";
				if (checkNumber(e1source)) {
					isNumber = true;
					e1number = getNumber(e1source);
					e1other = e1target;
				}
				if (checkNumber(e1target)) {
					isNumber = true;
					e1number = getNumber(e1target);
					e1other = e1source;
				}

				if (isNumber) {
					countN++;
					boolean same = false;
					for (Relation e2 : r2) {
						String relation_name2 = e2.getRelationName();
						String e2source = e2.getSource().toString();
						String e2target = e2.getTarget().toString();

						boolean isNumber2 = false;
						String e2number = "", e2other = "";
						if (checkNumber(e2source)) {
							isNumber2 = true;
							e2number = getNumber(e2source);
							e2other = e2target;
						}
						if (checkNumber(e2target)) {
							isNumber2 = true;
							e2number = getNumber(e2target);
							e2other = e2source;
						}

						if (isNumber2) {
							try {
								if (SimilarityUtils.wordSimilairty(e1other,
										e2other) > 0.9) {
									// (1)relation_name are same
									if (relation_name
											.equalsIgnoreCase(relation_name2)) {
										if (Math.abs(Double
												.parseDouble(e1number)
												- Double.parseDouble(e2number)) <= 1) {
											score[0] = score[0] + 1;
											same = true;
											break;
										} else {
											score[1] = score[1] + 1;
											break;
										}
									}
									// (2)relation_name are nothing
									if (Math.abs(Double.parseDouble(e1number)
											- Double.parseDouble(e2number)) <= 1) {
										score[2] = score[2] + 1;
										same = true;
										break;
									} else {
										score[3] = score[3] + 1;
										break;
									}
								}
								// just compare the number
								if (Math.abs(Double.parseDouble(e1number)
										- Double.parseDouble(e2number)) <= 1) {
									score[4] = score[4] + 1;
									same = true;
									break;
								}
							} catch (Exception e) {
								System.err.println(e1number + ' ' + e2number);
							}
						}
					}
					if (same == false)
						score[5] = score[5] + 1;
				}
			}
			if (countN > 0) {
				for (int i = 0; i < 6; i++) {
					score[i + 6] = score[i] / countN;
				}
			}
			return score;
		} catch (Exception e) {
			System.err.println("can't get dependency view");
			return score;
		}
	}

	private boolean checkNumber(String argument) {
		if (argument.matches("[^\n]*[0-9][^\n]*")) {
			if (argument.matches("[$,.0-9]*"))
				return true;
			if (argument.contains("-")) {
				String[] s = argument.split("-");
				if (s[0].matches("[0-9.]*") && !s[1].matches("[0-9.]*")) {
					return true;
				} else
					return false;
			} else {

				String a1 = argument.replaceFirst("US", "");
				if (a1.matches("[0-9.]*"))
					return true;
				else {
					a1 = argument.replaceFirst("NO.", "");
					if (a1.matches("[0-9]*"))
						return true;
					else {
						if (argument.matches("[0-9.]*[a-z.]*")) {
							return true;
						}
					}
				}
			}
			return false;

		}
		return false;
	}

	private String getNumber(String argument) {
		if (argument.matches("[^\n]*[0-9][^\n]*")) {
			if (argument.matches("[$,.0-9]*")) {
				String s = argument.replaceAll("[$]", "");
				s = s.replaceAll("[,]", "");
				return s;
			}
			if (argument.contains("-")) {
				String[] s = argument.split("-");
				if (s[0].matches("[0-9.]*") && !s[1].matches("[0-9.]*")) {
					return s[0];
				}
			} else {

				String a1 = argument.replaceFirst("US", "");
				if (a1.matches("[0-9.]*"))
					return a1;
				else {
					a1 = argument.replaceFirst("NO.", "");
					if (a1.matches("[0-9]*"))
						return a1;
					else {
						if (argument.matches("[0-9.]*[a-z.]*")) {
							return argument.replaceAll("[a-z]", "");
						}
					}
				}
			}
		}
		return "";
	}

	// compare dependency parser
	private double[] score_dependency(TextAnnotation ta1, TextAnnotation ta2) {
		double score[] = new double[10];
		double score1[] = new double[5];
		for (int i = 0; i < 5; i++)
			score1[i] = 0;
		double score2[] = new double[5];
		for (int i = 0; i < 5; i++)
			score2[i] = 0;

		score1 = score_dependency1(ta1, ta2);
		score2 = score_dependency1(ta2, ta1);

		for (int i = 0; i < 10; i++) {
			if (i < 5) {
				score[i] = score1[i];
			} else {
				score[i] = score2[i - 5];
			}
		}
		return score;
	}

	private double[] score_dependency1(TextAnnotation ta1, TextAnnotation ta2) {
		double[] score = new double[5];
		for (int i = 0; i < 5; i++) {
			score[i] = 0;
		}
		try {
			View v1 = ta1.getView(ViewNames.DEPENDENCY);
			View v2 = ta2.getView(ViewNames.DEPENDENCY);
			if (v1 == null || v2 == null)
				return score;
			List<Relation> r1 = v1.getRelations();
			List<Relation> r2 = v2.getRelations();
			int countN = 0;

			for (Relation e1 : r1) {
				boolean totalunmatch = false;
				boolean totalmatch = false;
				boolean halfmatch = false;
				String relation_name1 = e1.getRelationName();
				String e1source = e1.getSource().toString();
				String e1target = e1.getTarget().toString();

				double maxscore = 0;
				double minscore = 0;
				for (Relation e2 : r2) {
					String relation_name2 = e2.getRelationName();
					String e2source = e2.getSource().toString();
					String e2target = e2.getTarget().toString();

					if (relation_name1.equalsIgnoreCase(relation_name2)) {
						double temp1 = SimilarityUtils.wordSimilairty(e1source,
								e2source);
						double temp2 = SimilarityUtils.wordSimilairty(e1target,
								e2target);
						double tempmax = 0;
						double tempmin = 0;

						// get positive score (maximum)
						if (temp1 < 0)
							tempmax = tempmax + 0;
						else
							tempmax = tempmax + temp1;
						if (temp2 < 0)
							tempmax = tempmax + 0;
						else
							tempmax = tempmax + temp2;
						tempmax = tempmax / 2;
						if (tempmax > maxscore)
							maxscore = tempmax;

						// get negative score (minimum)
						if (temp1 > 0 && temp2 > 0)
							tempmin = 0;
						else {
							if (temp1 > 0.5)
								tempmin = tempmin - temp1;
							if (temp1 > 0 && temp1 <= 0.5)
								tempmin = tempmin + 0;
							if (temp1 <= 0)
								tempmin = tempmin + temp1;

							if (temp2 > 0.5)
								tempmin = tempmin - temp2;
							if (temp2 > 0 && temp1 <= 0.5)
								tempmin = tempmin + 0;
							if (temp2 <= 0)
								tempmin = tempmin + temp2;
						}
						if (tempmin < minscore)
							minscore = tempmin;

						// get the count of totally matching, half matching
						if (temp1 == 1 && temp2 == 1 && totalmatch == false) {
							score[2] = score[2] + 1;
							totalmatch = true;
							totalunmatch = true;
						} else {
							if ((temp1 == 1 || temp2 == 1)
									&& halfmatch == false) {
								score[3] = score[3] + 1;
								totalunmatch = true;
								halfmatch = true;
							}
						}
					}
				}

				if (totalunmatch == false)
					score[4] = score[4] + 1;
				score[0] = score[0] + maxscore;
				score[1] = score[1] + minscore;
			}

			if (r1.size() != 0) {
				score[0] = score[0] / r1.size();
				score[1] = score[1] / r1.size();
				score[2] = score[2] / r1.size();
				score[3] = score[3] / r1.size();
				score[4] = score[4] / r1.size();
			}
			return score;
		} catch (Exception e) {
			System.err.println("can't get dependency view");
			return score;
		}
	}

	// compare predicate of SRL (positive score and negatice score)
	private double[] score_predicateofSRL(TextAnnotation ta1, TextAnnotation ta2) {
		double[] score = new double[4];
		double score1[] = new double[2];
		for (int i = 0; i < 2; i++)
			score1[i] = 0;
		double score2[] = new double[2];
		for (int i = 0; i < 2; i++)
			score2[i] = 0;

		score1 = score_predicateofSRL1(ta1, ta2);
		score2 = score_predicateofSRL1(ta2, ta1);

		for (int i = 0; i < 4; i++) {
			if (i < 2) {
				score[i] = score1[i];
			} else {
				score[i] = score2[i - 2];
			}
		}
		return score;
	}

	private double[] score_predicateofSRL1(TextAnnotation ta1,
			TextAnnotation ta2) {
		double[] score = new double[2];
		for (int i = 0; i < 2; i++)
			score[i] = 0;

		if (!ta1.hasView(ViewNames.SRL) || !ta2.hasView(ViewNames.SRL)) {
			return score;
		}

		View v1 = ta1.getView(ViewNames.SRL);
		View v2 = ta2.getView(ViewNames.SRL);
		List<Relation> r1 = v1.getRelations();
		List<Relation> r2 = v2.getRelations();

		Set<String> Predicate1_set = new HashSet<String>();
		Set<String> Predicate2_set = new HashSet<String>();
		for (Relation e1 : r1) {
			Constituent s = e1.getSource();
			String p = s.toString();
			if (!Predicate1_set.contains(p))
				Predicate1_set.add(p);
		}
		for (Relation e2 : r2) {
			Constituent s = e2.getSource();
			String p = s.toString();
			if (!Predicate2_set.contains(p))
				Predicate2_set.add(p);
		}

		Iterator<String> iterator1 = Predicate1_set.iterator();
		while (iterator1.hasNext()) {
			String p1 = iterator1.next();
			// System.out.println(p);

			double max = 0, min = 0;
			Iterator<String> iterator2 = Predicate2_set.iterator();
			while (iterator2.hasNext()) {
				String p2 = iterator2.next();
				double temp = SimilarityUtils.wordSimilairty(p1, p2);
				if (temp > max)
					max = temp;
				if (temp < min)
					min = temp;
			}
			score[0] = score[0] + max;
			score[1] = score[1] + min;
		}

		if (Predicate1_set.size() != 0) {
			score[0] = score[0] / Predicate1_set.size();
			score[1] = score[1] / Predicate1_set.size();
		}

		return score;
	}

	// guihua, compare NP and VP fpr short sentences.
	private double[] score_NP_VP(TextAnnotation ta1, TextAnnotation ta2) {
		double[] score = new double[4];
		for (int i = 0; i < 4; i++)
			score[i] = 0;

		if (!ta1.hasView(ViewNames.PARSE_STANFORD)
				|| !ta2.hasView(ViewNames.PARSE_STANFORD)) {
			return score;
		}

		View v1 = ta1.getView(ViewNames.PARSE_STANFORD);
		View v2 = ta2.getView(ViewNames.PARSE_STANFORD);
		List<Constituent> r1 = v1.getConstituents();
		List<Constituent> r2 = v2.getConstituents();

		int count1 = 0, count2 = 0;
		String np1 = "";
		String vp1 = "";
		String np2 = "";
		String vp2 = "";
		for (Constituent e1 : r1) {
			if (e1.getLabel().equalsIgnoreCase("NP")) {
				count1++;
				if (count1 == 1)
					np1 = e1.toString();
			}
			if (e1.getLabel().endsWith("VP")) {
				count2++;
				if (count2 == 1)
					vp1 = e1.toString();
			}
		}
		count1 = 0;
		count2 = 0;
		for (Constituent e2 : r2) {
			if (e2.getLabel().equalsIgnoreCase("NP")) {
				count1++;
				if (count1 == 1)
					np2 = e2.toString();
			}
			if (e2.getLabel().endsWith("VP")) {
				count2++;
				if (count2 == 1)
					vp2 = e2.toString();
			}
		}

		String[] npwords1 = np1.split(" ");
		String[] npwords2 = np2.split(" ");
		String[] vpwords1 = vp1.split(" ");
		String[] vpwords2 = vp2.split(" ");

		if ((npwords1.length + vpwords1.length) < 15
				&& (npwords2.length + vpwords2.length) < 15) {
			// compare NP, VP respectively
			score[0] = (allStringCompare(npwords1, npwords2) + allStringCompare(
					npwords2, npwords1)) / 2;
			score[1] = (allStringCompare(vpwords1, vpwords2) + allStringCompare(
					vpwords2, vpwords1)) / 2;
			score[2] = LLM_comparator(npwords1, npwords2);
			score[3] = LLM_comparator(vpwords1, vpwords2);
		}

		return score;
	}

	private double allStringCompare(String[] s1, String[] s2) {
		double score = 0;
		for (String w1 : s1) {
			double point = 0;
			for (String w2 : s2) {
				double temp = SimilarityUtils.wordSimilairty(w1, w2);
				if (temp > point)
					point = temp;
			}
			score = score + point;
		}
		if (s1.length > 0)
			return score / s1.length;
		return score;
	}

	double LLM_comparator(String[] s1, String[] s2) {
		double scale = 1;
		double score1 = 0;
		double score2 = 0;

		int nstw1 = 0;
		int nstw2 = 0;

		for (int i = 0; i < s1.length; i++) {
			double max = 0;
			if (Stopwords.contains(s1[i])) {
				nstw1++;
			} else {

				for (int j = 0; j < s2.length; j++) {
					if (!Stopwords.contains(s2[j])) {
						String w1 = s1[i];
						String w2 = s2[j];
						double temp = SimilarityUtils.wordSimilairty(w1, w2);
						if (temp > max)
							max = temp;

					}
				}

			}
			score1 = score1 + max;
		}
		for (int i = 0; i < s2.length; i++) {
			double max = 0;
			if (Stopwords.contains(s2[i])) {
				nstw2++;
			} else {
				for (int j = 0; j < s1.length; j++) {
					if (!Stopwords.contains(s1[j])) {
						String w1 = s2[i];
						String w2 = s1[j];
						double temp = SimilarityUtils.wordSimilairty(w1, w2);
						if (temp > max)
							max = temp;
					}
				}
			}
			score2 = score2 + max;
		}

		double score = 0;

		int nlength1 = s1.length - nstw1;
		int nlength2 = s2.length - nstw2;

		if ((nlength1 != 0) && (nlength2 != 0))
			score = (score1 / nlength1 + score2 / nlength2) / 2;

		return scale * score;
	}

}
