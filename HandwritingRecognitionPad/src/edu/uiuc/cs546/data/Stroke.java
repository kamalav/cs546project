package edu.uiuc.cs546.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.graphics.Point;
import edu.uiuc.cs546.preprocess.DatapointSampler;

public class Stroke {
	protected List<Datapoint> datapoints;

	protected boolean invisible;

	public Stroke() {
		this.datapoints = new ArrayList<Datapoint>();
		this.invisible = false;
	}

	public void addDatapoint(Datapoint dp) {
		this.datapoints.add(dp);
	}

	public Stroke(List<Datapoint> dps, boolean invisible) {
		this.datapoints = dps;
		this.invisible = invisible;
	}

	public List<Datapoint> getDatapoints() {
		return datapoints;
	}

	public boolean isInvisible() {
		return invisible;
	}

	public String toString() {
		String str = "";
		if (invisible)
			str += "invisible storke: ";
		else
			str += "real storke: ";
		for (Datapoint dp : datapoints) {
			str += "(" + dp.x + ", " + dp.y + ") ";
		}
		return str;
	}

	static public List<Stroke> convertFromVectors(
			Vector<Vector<Point>> strokeVectors) {
		List<Stroke> strokes = new ArrayList<Stroke>();

		// two data points used for sampling
		Datapoint start = null;
		Datapoint end = null;

		for (int i = 0; i < strokeVectors.size(); i++) {
			Vector<Point> vector = strokeVectors.get(i);

			List<Datapoint> dps = new ArrayList<Datapoint>();
			for (Point point : vector) {
				// y coordinate is opposite in the CursiveTextPanel (the lower
				// the larger)
				Datapoint dp = new Datapoint(point.x, -point.y);
				dps.add(dp);
			}

			if (i != 0) {
				end = dps.get(0);
				List<Datapoint> inviDps = DatapointSampler.virtualSampling(
						start, end, 10);
				boolean invisible = true;
				strokes.add(new Stroke(inviDps, invisible));
			}

			start = dps.get(dps.size() - 1);

			// TODO: from the second vector, add invisible stroke between the
			// consective real vectors and do sampling for getting like 10
			// virtual datapoints
			strokes.add(new Stroke(dps, false));

		}

		return strokes;
	}

	static public List<Stroke> connectStrokes(List<Stroke> strokes) {
		List<Stroke> ss = new ArrayList<Stroke>();

		// two data points used for sampling
		Datapoint start = null;
		Datapoint end = null;

		for (int i = 0; i < strokes.size(); i++) {
			List<Datapoint> dps = new ArrayList<Datapoint>();
			for (Datapoint point : strokes.get(i).getDatapoints()) {
				Datapoint dp = new Datapoint(point.x, -point.y);
				dps.add(dp);
			}

			if (i != 0) {
				end = strokes.get(i).datapoints.get(0);
				List<Datapoint> inviDps = DatapointSampler.virtualSampling(
						start, end, 10);
				boolean invisible = true;
				ss.add(new Stroke(inviDps, invisible));
			}

			start = strokes.get(i).datapoints.get(strokes.get(i).datapoints
					.size() - 1);

			ss.add(new Stroke(dps, false));
		}

		return ss;
	}

	public static List<Stroke> makeYNegative(List<Stroke> strokes) {

		return strokes;
	}
}
