package edu.uiuc.cs546.data.util;

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import edu.uiuc.cs546.data.Stroke;
import edu.uiuc.cs547.hmm.feature.FeatureQuantizer2;
import edu.uiuc.cs547.hmm.feature.FeatureVector;

public class OberservationSequence2 {

	public static List<ObservationInteger> fromStrokes(List<Stroke> strokes) {
		// TODO: replace QuantizedFeature by DiscreteSymbols

		List<ObservationInteger> sequence = new ArrayList<ObservationInteger>();

		List<FeatureVector> vectors = null;
		try {
			vectors = FeatureVector.generateVectorsFromStrokes(strokes);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < vectors.size(); i++) {
			FeatureVector vector = vectors.get(i);
			int symbol = FeatureQuantizer2.quantize(vector);
			sequence.add(i, new ObservationInteger(symbol));
		}

		return sequence;
	}

}
