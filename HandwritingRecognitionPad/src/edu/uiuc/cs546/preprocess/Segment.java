package edu.uiuc.cs546.preprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.uiuc.cs546.data.Datapoint;
import edu.uiuc.cs546.data.Stroke;

public class Segment {

	public static List<Double> overSegment(List<Stroke> strokes) {

		Map<Double, List<Datapoint>> dpsMap = createDatapointsMap(strokes);

		List<Double> segmentations = new ArrayList<Double>();

		Double prevX = null;
		List<Double> prevY = null;
		for (Map.Entry<Double, List<Datapoint>> entry : dpsMap.entrySet()) {
			double x = entry.getKey();
			List<Datapoint> dps = entry.getValue();
			System.out.println(x + ": " + dps.size());

			if (prevX == null) {
				prevX = x;
				prevY = new ArrayList<Double>();
				for (Datapoint dp : dps)
					prevY.add(dp.y);
				continue;
			}

			// delta x is too small, skip the point
			if (x - prevX < 5)
				continue;

			List<Double> y = new ArrayList<Double>();
			for (Datapoint dp : dps)
				y.add(dp.y);

			// add segment change in vertical density is noted
			if (prevY.size() - y.size() != 0) {
				segmentations.add(x);
			} else {
				for (int i = 0; i < y.size(); i++) {
					Double yy1 = y.get(i);
					double yy2 = prevY.get(i);
					if (yy1 - yy2 > 30 || yy2 - yy1 > 30) {
						segmentations.add(x);
						break;
					}
				}
			}

			// keep previous values
			prevX = x;
			prevY = y;
		}

		System.out.println("Segmentation x coordinates:");
		for (Double s : segmentations) {
			// System.out.println(s);
		}

		return segmentations;
	}

	private static Map<Double, List<Datapoint>> createDatapointsMap(
			List<Stroke> strokes) {
		Map<Double, List<Datapoint>> dpsMap = new TreeMap<Double, List<Datapoint>>();
		for (Stroke s : strokes) {
			List<Datapoint> dps = s.getDatapoints();
			for (Datapoint dp : dps) {
				if (dpsMap.containsKey(dp.x)) {
					dpsMap.get(dp.x).add(dp);
				} else {
					List<Datapoint> tempDps = new ArrayList<Datapoint>();
					tempDps.add(dp);
					dpsMap.put(dp.x, tempDps);
				}
			}
		}
		return dpsMap;
	}

	public static List<Double> eliminateSegsInLoops(List<Stroke> strokes,
			List<Double> segmentations) {
		Map<Double, List<Datapoint>> dpsMap = createDatapointsMap(strokes);
		boolean isLoopStart = true;
		Integer prevSize = null;

		List<Double> loopStartsX = new ArrayList<Double>();
		List<Double> loopEndsX = new ArrayList<Double>();

		for (Map.Entry<Double, List<Datapoint>> entry : dpsMap.entrySet()) {
			double x = entry.getKey();
			List<Datapoint> dps = entry.getValue();

			if (isLoopStart == true) {
				prevSize = dps.size();
				loopStartsX.add(x);
				isLoopStart = false;
				continue;
			}

			if (dps.size() == 1 && prevSize == 1) {
				continue;
			} else if (dps.size() == 1 && prevSize != 1) {
				// loop detected
				loopEndsX.add(x);
				isLoopStart = true;
			} else if (prevSize != 1) {
				if (dps.size() == 2) {
					double y1 = dps.get(0).y;
					double y2 = dps.get(1).y;
					if (y1 - y2 < 5 || y2 - y1 < 5) {
						loopEndsX.add(x);
						isLoopStart = true;
					}
				}
			}

			prevSize = dps.size();
		}

		int s = 0;
		List<Double> removes = new ArrayList<Double>();
		for (int i = 0; i < loopEndsX.size(); i++) {
			double start = loopStartsX.get(i);
			double end = loopEndsX.get(i);
			System.out.println("segmentation start and end: " + start + " "
					+ end);
			for (int j = s; j < segmentations.size(); j++) {
				Double x = segmentations.get(j);
				if (x > start && x < end) {
					removes.add(x);
				}
				s = j + 1;
			}
		}

		for (Double remove : removes)
			segmentations.remove(remove);

		return segmentations;
	}

}
