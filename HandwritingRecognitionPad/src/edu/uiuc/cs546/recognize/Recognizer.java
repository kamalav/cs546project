package edu.uiuc.cs546.recognize;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import edu.uiuc.cs546.hmm.LeftToRightHmm2;

public class Recognizer {

	public static String recognize(Map<String, LeftToRightHmm2> hmms,
			List<ObservationInteger> testSequence, StringBuffer probs) {

		double maxProb = -1;
		String recognizedStr = null;

		for (Map.Entry<String, LeftToRightHmm2> entry : hmms.entrySet()) {
			String str = entry.getKey();
			LeftToRightHmm2 hmm = entry.getValue();

			double prob = hmm.probability(testSequence);

			probs.append("P(stroke|'" + str + "') = "
					+ (new DecimalFormat("0.0E0")).format(prob) + "\n");

			if (prob > maxProb) {
				maxProb = prob;
				recognizedStr = str;
			}
		}

		return recognizedStr;
	}
}
