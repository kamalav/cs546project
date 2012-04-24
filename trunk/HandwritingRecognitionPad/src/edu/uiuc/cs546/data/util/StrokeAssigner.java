package edu.uiuc.cs546.data.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs546.data.Datapoint;
import edu.uiuc.cs546.data.Pair;
import edu.uiuc.cs546.data.Stroke;
import edu.uiuc.cs546.data.io.FileUtil;

public class StrokeAssigner {

	static public List<Pair<String, List<Stroke>>> assignAndFillInvisibleStrokes(
			String refFile, List<Stroke> strokes) throws IOException {

		List<Pair<String, List<Stroke>>> pairs = new ArrayList<Pair<String, List<Stroke>>>();

		String[] lines = FileUtil.getLines(refFile);

		for (String line : lines) {

			if (line.startsWith(".SEGMENT ")) {

				String[] data = line.split(" ");

				String[] strokeIndexes = data[2].split("-");

				String str = data[4].substring(1, data[4].length() - 1);

				List<Stroke> unitStrokes = new ArrayList<Stroke>();
				try {
					if (strokeIndexes.length == 2
							&& !strokeIndexes[0].equals(strokeIndexes[1])) {
						// multiple strokes for writing a recognition unit
						// two strokes for digit '5' for example

						int index1 = Integer.parseInt(strokeIndexes[0]);
						int index2 = Integer.parseInt(strokeIndexes[1]);

						// two data points used for sampling
						Datapoint start = null;
						Datapoint end = null;

						for (int index = index1; index <= index2; index++) {

							// fill in invisible strokes (PenUp for all
							// datapoints in it) by sampling, if the current
							// stroke is not the first one

							List<Datapoint> dps = strokes.get(index)
									.getDatapoints();

							if (index != index1) {
								end = dps.get(0);

								List<Datapoint> inviDps = DatapointSampler
										.virtualSampling(start, end, 10);
								boolean invisible = true;
								unitStrokes.add(new Stroke(inviDps, invisible));
							}

							unitStrokes.add(strokes.get(index));

							start = dps.get(dps.size() - 1);
						}
					} else {
						int index = Integer.parseInt(strokeIndexes[0]);
						unitStrokes.add(strokes.get(index));

					}
				} catch (NumberFormatException e) {
					System.out.println(refFile);
				}

				Pair<String, List<Stroke>> pair = new Pair<String, List<Stroke>>(
						str, unitStrokes);
				pairs.add(pair);
			}
		}
		// for (Pair<String, List<Stroke>> pair : pairs) {
		// System.out.println(pair.getFirst() + " " + pair.getSecond().size());
		// for (Stroke s : pair.getSecond())
		// System.out.println(s);
		// }
		return pairs;
	}
}
