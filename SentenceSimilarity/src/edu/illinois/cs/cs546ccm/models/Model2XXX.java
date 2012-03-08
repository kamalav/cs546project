package edu.illinois.cs.cs546ccm.models;

import java.io.IOException;
import java.util.List;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.View;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class Model2XXX extends Model {

	public Model2XXX() {
		super("XXX");
	}

	@Override
	public double similarity(TextAnnotation ta1, TextAnnotation ta2) {
		double score1 = score1(ta1, ta2);
		double[] score2 = score2(ta1, ta2);
		double score3 = score3(ta1, ta2);
		//double score4 = score4(ta1, ta2);
		return -1;
	}

	private double score4(TextAnnotation ta1, TextAnnotation ta2) {
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
		return score / cs1.size();
	}

	private double score3(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		// Cedar's method
		return 0;
	}
	
	private double score2_1(TextAnnotation ta1, TextAnnotation ta2) throws IOException{
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
			String chuncktype2=c2.getLabel();
			String chunckcontent2=c2.toString();
			double point = 0;
			for (Constituent c1 : cs1) {
				String chuncktype1=c1.getLabel();
				
				//only if they have the same type of chunk (NP, VP, and so on), they can compare, and get the max one
				if(chuncktype1.compareToIgnoreCase(chuncktype2)==0){
					String chunckcontent1=c1.toString();
					Model1LLM m = new Model1LLM();
					double result = m.similarity(chunckcontent1, chunckcontent2);
					if(result>point) point=result;
				}
			}
			score=score+point;
		}
		
		//Normalize by the Hypothesis's length
		score=score/cs2.size();
		return score;
	}

	private double[] score2(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		// Guihua's method
		double score[]=new double[2];
		
		//ta1 is Text, ta2 is Hypothesis
		try {
			score[0]=score2_1(ta1, ta2);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//ta2 is Text, ta1 is Hypothesis
		try {
			score[1]=score2_1(ta2, ta1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return score;
	}

	private double score1(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		// Ryan's method
		return 0;
	}

	@Override
	public int confidence(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO implement this method
		return -1;
	}

}
