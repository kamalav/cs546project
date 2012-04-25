package edu.uiuc.cs546.train;

import static edu.uiuc.cs546.hmm.HmmCommons.CHAR_HMM_STATES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.learn.BaumWelchLearner;
import edu.uiuc.cs546.data.Pair;
import edu.uiuc.cs546.data.Stroke;
import edu.uiuc.cs546.data.util.OberservationSequence;
import edu.uiuc.cs546.data.util.StringStrokesPairsBuilder;
import edu.uiuc.cs546.hmm.LeftToRightHmm2;
import edu.uiuc.cs546.preprocess.Smoother;

public class Trainer {

	static public HashMap<String, LeftToRightHmm2> trainHmms() {
		HashMap<String, LeftToRightHmm2> hmms = new HashMap<String, LeftToRightHmm2>();

		List<Pair<String, List<Stroke>>> pairs = StringStrokesPairsBuilder
				.generateTrainingPairs();

		// collect training sequences for char HMMs

		HashMap<String, List<List<ObservationInteger>>> sequencesMap = new HashMap<String, List<List<ObservationInteger>>>();

		for (Pair<String, List<Stroke>> pair : pairs) {

			String str = pair.getFirst();
			List<Stroke> strokes = pair.getSecond();

			List<List<ObservationInteger>> sequences;

			if (sequencesMap.containsKey(str)) {
				sequences = sequencesMap.get(str);
			} else {
				sequences = new ArrayList<List<ObservationInteger>>();
				sequencesMap.put(str, sequences);
			}

			strokes = Smoother.noiseSmoothing(strokes);

			List<ObservationInteger> sequence = OberservationSequence
					.fromStrokes(strokes);

			sequences.add(sequence);

		}

		for (Map.Entry<String, List<List<ObservationInteger>>> entry : sequencesMap
				.entrySet()) {

			String str = entry.getKey();
			List<List<ObservationInteger>> sequences = entry.getValue();

			LeftToRightHmm2 hmm = new LeftToRightHmm2(CHAR_HMM_STATES);

			BaumWelchLearner bwl = new BaumWelchLearner();
			Hmm<ObservationInteger> learntHmm = bwl.learn(hmm, sequences);

			hmms.put(str, new LeftToRightHmm2(learntHmm));

		}

		return hmms;
	}
}
