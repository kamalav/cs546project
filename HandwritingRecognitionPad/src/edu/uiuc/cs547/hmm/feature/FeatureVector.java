package edu.uiuc.cs547.hmm.feature;

import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs546.data.Datapoint;
import edu.uiuc.cs546.data.Pair;
import edu.uiuc.cs546.data.Stroke;

;

public class FeatureVector {

	public static final int DIMENSIONS = 2;

	protected double penUp; // 0 - PenDown, 1 - PenUp

	protected double deltaX;

	protected double deltaY;

	protected double writingAngle;

	protected double deltaWritingAngle;

	protected double biggerX; // whether current X is bigger than max all the
								// preceding X

	/*
	 * the reason for using double for penDown and biggerX here is that we want
	 * to use k-means clustering later
	 */

	public FeatureVector() {
	}

	public FeatureVector(double penUp, double deltaX, double deltaY,
			double writingAngle, double deltaWritingAngle, double biggerX) {

		this.penUp = penUp;
		this.deltaX = deltaX * 0.003;
		this.deltaY = deltaY * 0.003;
		this.writingAngle = writingAngle * 2.0;
		this.deltaWritingAngle = deltaWritingAngle;
		this.biggerX = biggerX;

	}

	static public List<FeatureVector> generateVectorsFromStrokes(
			List<Stroke> strokes) throws Exception {
		List<FeatureVector> vectors = new ArrayList<FeatureVector>();

		ArrayList<Pair<Datapoint, Double>> pairs = new ArrayList<Pair<Datapoint, Double>>();

		for (Stroke stroke : strokes) {
			List<Datapoint> dps = stroke.getDatapoints();
			double invisible = stroke.isInvisible() ? 1 : 0;

			for (Datapoint dp : dps) {
				Pair<Datapoint, Double> pair = new Pair<Datapoint, Double>(dp,
						invisible);
				pairs.add(pair);
			}
		}

		int maxDpIndex = pairs.size() - 1;

		double previousWritingAngle = -1;

		double maxX = -1;

		if (maxDpIndex <= 2) {
			// throw new Exception(
			// "The number of data points must be greater than 3!");
			for (int i = 0; i <= maxDpIndex; i++) {
				vectors.add(new FeatureVector(0, 0, 0, 0, 0, 0));
			}

			return vectors;
		}

		for (int i = 0; i <= maxDpIndex; i++) {

			double penUp = pairs.get(i).getSecond();

			double deltaX;
			double deltaY;

			Datapoint current = pairs.get(i).getFirst();
			Datapoint twoBefore = i >= 2 ? pairs.get(i - 2).getFirst() : null;
			Datapoint twoAfter = i <= maxDpIndex - 2 ? pairs.get(i + 2)
					.getFirst() : null;

			if (i == 0 || i == 1) {
				deltaX = twoAfter.x - current.x;
				deltaY = twoAfter.y - current.y;
			} else if (i >= 2 && i <= maxDpIndex - 2) {
				deltaX = twoAfter.x - twoBefore.x;
				deltaY = twoAfter.y - twoBefore.y;
			} else {
				deltaX = current.x - twoBefore.x;
				deltaY = current.y - twoBefore.y;
			}

			double writingAngle;
			if (deltaX == 0) {
				deltaX += 0.00000000001;
				deltaY += 0.00000000001;
			}
			if (deltaX > 0) {
				writingAngle = Math.atan(deltaY / deltaX);
			} else if (deltaX < 0) {
				writingAngle = Math.atan(deltaY / deltaX) - Math.PI;
			} else {
				if (deltaY > 0)
					writingAngle = Math.PI;
				else
					writingAngle = -Math.PI;

			}

			double deltaWritingAngle;

			if (i == 0) {
				deltaWritingAngle = 0;
			} else {
				deltaWritingAngle = writingAngle - previousWritingAngle;
			}

			previousWritingAngle = writingAngle;

			double biggerX;

			if (current.x > maxX) {
				maxX = current.x;
				biggerX = 1;
			} else {
				biggerX = 0;
			}

			FeatureVector vector = new FeatureVector(penUp, deltaX, deltaY,
					writingAngle, deltaWritingAngle, biggerX);

			vectors.add(vector);
		}

		return vectors;
	}

	public double[] toArray() {
		return new double[] { penUp, deltaX, deltaY, writingAngle,
				deltaWritingAngle, biggerX };

	}

	public String toString() {
		return "[penUp: " + penUp + ", deltaX: " + deltaX + ", deltaY: "
				+ deltaY + ", writingAngle: " + writingAngle
				+ ", deltaWritingAngle: " + deltaWritingAngle + ", biggerX: "
				+ biggerX + "]";
	}

	/*
	 * normalize vectors based on mean and variance
	 */
	public static void normalizeVectors(List<FeatureVector> vectors) {
		int n = vectors.size();

		// compute means
		double meanWritingAngle = 0;
		double meanDeltaWritingAngle = 0;
		double meanDeltaX = 0;
		double meanDeltaY = 0;
		for (int i = 0; i < n; i++) {
			FeatureVector v = vectors.get(i);
			meanWritingAngle += v.writingAngle;
			meanDeltaWritingAngle += v.deltaWritingAngle;
			meanDeltaX += v.deltaX;
			meanDeltaY += v.deltaY;
		}
		meanWritingAngle /= n;
		meanDeltaWritingAngle /= n;
		meanDeltaX /= n;
		meanDeltaY /= n;

		// compute variances
		double varWritingAngle = 0;
		double varDeltaWritingAngle = 0;
		double varDeltaX = 0;
		double varDeltaY = 0;
		for (int i = 0; i < n; i++) {
			FeatureVector v = vectors.get(i);
			varWritingAngle += (v.writingAngle - meanWritingAngle)
					* (v.writingAngle - meanWritingAngle);
			varDeltaWritingAngle += (v.deltaWritingAngle - meanDeltaWritingAngle)
					* (v.deltaWritingAngle - meanDeltaWritingAngle);
			varDeltaX += (v.deltaX - meanDeltaX) * (v.deltaX - meanDeltaX);
			varDeltaY += (v.deltaY - meanDeltaY) * (v.deltaY - meanDeltaY);
		}
		varWritingAngle = Math.sqrt(varWritingAngle / n);
		varDeltaWritingAngle = Math.sqrt(varDeltaWritingAngle / n);
		varDeltaX = Math.sqrt(varDeltaX / n);
		varDeltaY = Math.sqrt(varDeltaY / n);

		// System.out.println("meanWritingAngle:" + meanWritingAngle
		// + ", meanDeltaWritingAngle:" + meanDeltaWritingAngle
		// + ", varWritingAngle:" + varWritingAngle
		// + ", varDeltaWritingAngle:" + varDeltaWritingAngle);
		// normalize
		for (int i = 0; i < n; i++) {
			FeatureVector v = vectors.get(i);

			v.writingAngle = (v.writingAngle - meanWritingAngle)
					/ varWritingAngle;
			v.deltaWritingAngle = (v.deltaWritingAngle - meanDeltaWritingAngle)
					/ varDeltaWritingAngle;
			// v.deltaX = (v.deltaX - meanDeltaX) / varDeltaX;
			// v.deltaY = (v.deltaY - meanDeltaY) / varDeltaY;
		}
	}

	/*
	 * normalize vectors based on mean and variance
	 */
	public static void normalizeVectors(FeatureVector[] vectors) {
		int n = vectors.length;

		// compute means
		double meanWritingAngle = 0;
		double meanDeltaWritingAngle = 0;
		double meanDeltaX = 0;
		double meanDeltaY = 0;
		int count = n;
		for (int i = 0; i < n; i++) {
			FeatureVector v = vectors[i];
			if (Double.isNaN(v.writingAngle)
					|| Double.isNaN(v.deltaWritingAngle)
					|| Double.isNaN(v.deltaX) || Double.isNaN(v.deltaY)) {
				count--;
				continue;
			}
			meanWritingAngle += v.writingAngle;
			meanDeltaWritingAngle += v.deltaWritingAngle;
			meanDeltaX += v.deltaX;
			meanDeltaY += v.deltaY;
		}
		meanWritingAngle /= count;
		meanDeltaWritingAngle /= count;
		meanDeltaX /= count;
		meanDeltaY /= count;

		// compute variances
		double varWritingAngle = 0;
		double varDeltaWritingAngle = 0;
		double varDeltaX = 0;
		double varDeltaY = 0;
		count = n;
		for (int i = 0; i < n; i++) {
			FeatureVector v = vectors[i];
			if (Double.isNaN(v.writingAngle)
					|| Double.isNaN(v.deltaWritingAngle)
					|| Double.isNaN(v.deltaX) || Double.isNaN(v.deltaY)) {
				count--;
				continue;
			}
			varWritingAngle += (v.writingAngle - meanWritingAngle)
					* (v.writingAngle - meanWritingAngle);
			varDeltaWritingAngle += (v.deltaWritingAngle - meanDeltaWritingAngle)
					* (v.deltaWritingAngle - meanDeltaWritingAngle);
			varDeltaX += (v.deltaX - meanDeltaX) * (v.deltaX - meanDeltaX);
			varDeltaY += (v.deltaY - meanDeltaY) * (v.deltaY - meanDeltaY);
		}
		varWritingAngle = Math.sqrt(varWritingAngle / count);
		varDeltaWritingAngle = Math.sqrt(varDeltaWritingAngle / count);
		varDeltaX = Math.sqrt(varDeltaX / count);
		varDeltaY = Math.sqrt(varDeltaY / count);

		// normalize
		for (int i = 0; i < n; i++) {
			FeatureVector v = vectors[i];
			if (Double.isNaN(v.writingAngle)
					|| Double.isNaN(v.deltaWritingAngle)
					|| Double.isNaN(v.deltaX) || Double.isNaN(v.deltaY)) {
				continue;
			}
			v.writingAngle = (v.writingAngle - meanWritingAngle)
					/ varWritingAngle;
			v.deltaWritingAngle = (v.deltaWritingAngle - meanDeltaWritingAngle)
					/ varDeltaWritingAngle;
			// v.deltaX = (v.deltaX - meanDeltaX) / varDeltaX;
			// v.deltaY = (v.deltaY - meanDeltaY) / varDeltaY;
		}
	}
}
