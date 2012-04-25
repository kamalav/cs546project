package edu.uiuc.cs546.data.util;

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import edu.uiuc.cs546.data.Stroke;
import edu.uiuc.cs546.hmm.feature.FeatureQuantizer;
import edu.uiuc.cs546.hmm.feature.FeatureVector;

public class OberservationSequence {

	public static List<ObservationInteger> fromStrokes(List<Stroke> strokes) {

		List<ObservationInteger> sequence = new ArrayList<ObservationInteger>();

		List<FeatureVector> vectors = null;
		try {
			vectors = FeatureVector.generateVectorsFromStrokes(strokes);
			FeatureVector.normalizeVectors(vectors);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < vectors.size(); i++) {
			FeatureVector vector = vectors.get(i);
			int symbol = FeatureQuantizer.quantize(vector);
			sequence.add(i, new ObservationInteger(symbol));
		}

		return sequence;
	}

}
