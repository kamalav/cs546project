package edu.uiuc.cs546.preprocess;

import java.util.List;

import edu.uiuc.cs546.data.Datapoint;
import edu.uiuc.cs546.data.Stroke;

public class Smoother {
	/**
	 * x(t) = alpha*x(t-1) + beta*x(t) Y(t) = alpha*y(t-1) + beta*y(t)
	 * 
	 * @param strokes
	 * @return
	 */
	public static List<Stroke> noiseSmoothing(List<Stroke> strokes) {
		final double alpha = 0.1;
		final double beta = 0.9;
		for (Stroke s : strokes) {
			List<Datapoint> dps = s.getDatapoints();
			for (int i = 1; i < dps.size(); i++) {
				Datapoint dp = dps.get(i);
				Datapoint prevDp = dps.get(i - 1);
				dp.x = alpha * prevDp.x + beta * dp.x;
				dp.y = alpha * prevDp.y + beta * dp.y;
			}
		}

		return strokes;
	}
}
