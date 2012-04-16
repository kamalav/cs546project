package edu.uiuc.cs546.preprocess;

import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs546.data.Datapoint;
import edu.uiuc.cs546.data.Stroke;

public class DatapointSampler {

	/*
	 * virtually sample a certain number of data points by taking data points in
	 * the straight line between start and end, inclusive
	 */
	static public List<Datapoint> virtualSampling(Datapoint start,
			Datapoint end, int number) {

		List<Datapoint> dps = new ArrayList<Datapoint>(number);

		int numSamples = number - 2;

		double deltaX = (end.x - start.x) / (numSamples + 1);
		double deltaY = (end.y - start.y) / (numSamples + 1);

		dps.add(start);

		for (int i = 1; i <= numSamples; i++)
			dps.add(new Datapoint(start.x + deltaX * i, start.y + deltaY * i));

		dps.add(end);

		return dps;
	}

	static public List<Stroke> inStrokeSampling(List<Stroke> strokes,
			int number, boolean resampleInvisibleStroke) {
		List<Stroke> ss = new ArrayList<Stroke>();
		for (Stroke s : strokes) {
			if (!resampleInvisibleStroke && s.isInvisible()) {
				ss.add(s);
			} else {
				List<Datapoint> dps = s.getDatapoints();
				List<Datapoint> newDps = new ArrayList<Datapoint>();
				for (int i = 0; i < dps.size(); i += number) {
					newDps.add(dps.get(i));
				}
				ss.add(new Stroke(newDps, s.isInvisible()));
			}
		}

		return ss;
	}
}
