package edu.illinois.cs.cs546ccm.models;

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
		double score2 = score2(ta1, ta2);
		double score3 = score3(ta1, ta2);
		double score4 = score4(ta1, ta2);
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

	private double score2(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		// Guihua's method
		return 0;
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
