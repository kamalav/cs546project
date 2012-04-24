package edu.uiuc.cs546.hmm.feature;

import static edu.uiuc.cs546.hmm.HmmCommons.TRAINING_DATA_SET_DIR;

import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs546.data.Pair;
import edu.uiuc.cs546.data.Stroke;
import edu.uiuc.cs546.data.io.FileUtil;
import edu.uiuc.cs546.data.io.HmmIOUtil;
import edu.uiuc.cs546.data.util.StringStrokesPairsBuilder;
import edu.uiuc.cs546.hmm.HmmCommons;

public class FeatureQuantizer2 {

	private static FeatureVector[] prototypeVectors = null;

	static public FeatureVector[] getPrototypeVectors() {
		if (prototypeVectors == null) {
			// use the following line if you want to train new models
			// HmmIOUtil.deleteSavedModels();

			String file = HmmCommons.TRAINED_VECTORS;

			if (HmmIOUtil.prototypeVectorsFileExist()) {
				prototypeVectors = HmmIOUtil.readSavedFeatureVectors(file);
			} else {
				// no vectors in memory
				prototypeVectors = trainPrototypeVectorsByKMeans(
						HmmCommons.HMM_SYMBOLS, 30);

				// write trained prototype vectors into a text file for quick
				// reading in next time
				try {
					StringBuilder sb = new StringBuilder();
					for (FeatureVector vector : prototypeVectors) {
						sb.append(vector.penUp + "\t" + vector.deltaX + "\t"
								+ vector.deltaY + "\t" + vector.writingAngle
								+ "\t" + vector.deltaWritingAngle + "\t"
								+ vector.biggerX + "\n");
					}
					FileUtil.writeFile(file, sb.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return prototypeVectors;
	}

	private static FeatureVector[] trainPrototypeVectorsByKMeans(int k,
			int iterations) {

		List<FeatureVector> trainingVectors = new ArrayList<FeatureVector>();

		List<String> refFiles = new ArrayList<String>();
		List<String> dataFiles = new ArrayList<String>();

		String[] dirs = FileUtil.subDirectoryNames(TRAINING_DATA_SET_DIR
				+ "/ref");

		for (String dir : dirs) {
			String[] files = FileUtil
					.fileNamesInDirectory(TRAINING_DATA_SET_DIR + "/ref/" + dir);
			for (String file : files) {
				refFiles.add(TRAINING_DATA_SET_DIR + "/ref/" + dir + "/" + file);
				dataFiles.add(TRAINING_DATA_SET_DIR + "/data/" + dir + "/"
						+ file);
			}
		}

		List<Pair<String, List<Stroke>>> pairs = StringStrokesPairsBuilder
				.generatePairs(refFiles, dataFiles);

		for (Pair<String, List<Stroke>> pair : pairs) {
			List<Stroke> strokes = pair.getSecond();
			try {
				trainingVectors.addAll(FeatureVector
						.generateVectorsFromStrokes(strokes));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		int count = trainingVectors.size();

		System.out.println("total training vectors to be clustered: " + count);

		// randomly generate k centroids;
		FeatureVector[] centroids = new FeatureVector[k];
		for (int i = 0; i < k; i++) {
			centroids[i] = trainingVectors.get((int) (Math.random() * count));
		}

		int[] clusters = new int[count];

		System.out.println("Running KMeans for " + iterations + " iterations");

		// do clustering
		for (int i = 0; i < iterations; i++) {

			// assign each vector to its nearest cluster
			for (int vector = 0; vector < count; vector++) {
				FeatureVector v = trainingVectors.get(vector);
				double minDistance = Double.MAX_VALUE;
				for (int j = 0; j < k; j++) {
					FeatureVector centroid = centroids[j];
					double distance = euclideanDistance(centroid, v);
					if (distance < minDistance) {
						minDistance = distance;
						clusters[vector] = j;
					}
				}
			}

			// compute new centers for each cluster
			FeatureVector[] newCentroids = new FeatureVector[k];
			for (int j = 0; j < k; j++) {
				newCentroids[j] = new FeatureVector(0, 0, 0, 0, 0, 0);
			}

			int[] elementCounts = new int[k];

			for (int vector = 0; vector < count; vector++) {
				FeatureVector v = trainingVectors.get(vector);
				int clusterId = clusters[vector];
				newCentroids[clusterId].penUp += v.penUp;
				newCentroids[clusterId].deltaX += v.deltaX;
				newCentroids[clusterId].deltaY += v.deltaY;
				newCentroids[clusterId].writingAngle += v.writingAngle;
				newCentroids[clusterId].deltaWritingAngle += v.deltaWritingAngle;
				newCentroids[clusterId].biggerX += v.biggerX;
				elementCounts[clusterId]++;
			}

			for (int j = 0; j < k; j++) {
				int c = elementCounts[j];
				// if (c == 0)
				// continue;
				newCentroids[j].penUp /= c;
				newCentroids[j].deltaX /= c;
				newCentroids[j].deltaY /= c;
				newCentroids[j].writingAngle /= c;
				newCentroids[j].deltaWritingAngle /= c;
				newCentroids[j].biggerX /= c;
				centroids[j] = newCentroids[j];
			}

			System.out.println(i + " iterations finished");
		}

		return centroids;
	}

	private static double euclideanDistance(FeatureVector v, FeatureVector u) {

		double distance = 0;

		distance += (v.penUp - u.penUp) * (v.penUp - u.penUp);
		distance += (v.deltaX - u.deltaX) * (v.deltaX - u.deltaX);
		distance += (v.deltaY - u.deltaY) * (v.deltaY - u.deltaY);
		distance += (v.writingAngle - u.writingAngle)
				* (v.writingAngle - u.writingAngle);
		distance += (v.deltaWritingAngle - u.deltaWritingAngle)
				* (v.deltaWritingAngle - u.deltaWritingAngle);
		distance += (v.biggerX - u.biggerX) * (v.biggerX - u.biggerX);

		distance = Math.sqrt(distance);
		return distance;
	}

	/**
	 * Returns the index of the nearest vector in VQ codebook compared to the
	 * given vector. Used for getting a discrete symbol's index
	 */
	static private int findNearestVectorIndex(FeatureVector vector,
			FeatureVector[] prototypeVectors) {

		int index = -1;
		double minDistance = Double.MAX_VALUE;

		for (int i = 0; i < prototypeVectors.length; i++) {
			double distance = euclideanDistance(vector, prototypeVectors[i]);
			if (distance < minDistance) {
				index = i;
				minDistance = distance;
			}
		}

		return index;
	}

	static public int quantize(FeatureVector vector) {

		// compare the vector with the codebook
		// select the nearest one's index

		int index = findNearestVectorIndex(vector, getPrototypeVectors());

		return index;
	}

}
