package edu.uiuc.cs546.data.util;

import static edu.uiuc.cs546.hmm.HmmCommons.TRAINING_DATA_SET_DIR;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs546.data.Pair;
import edu.uiuc.cs546.data.Stroke;
import edu.uiuc.cs546.data.io.FileUtil;

public class StringStrokesPairsBuilder {

	static public List<Pair<String, List<Stroke>>> generateTrainingPairs() {

		// list of data definition files
		List<String> refFiles = new ArrayList<String>();

		// list of files containing actual stroke data points
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

		return generatePairs(refFiles, dataFiles);
	}

	static public List<Pair<String, List<Stroke>>> generatePairs(
			List<String> refFiles, List<String> dataFiles) {

		List<Pair<String, List<Stroke>>> allPairs = new ArrayList<Pair<String, List<Stroke>>>();

		try {

			for (int i = 0; i < refFiles.size(); i++) {
				String refFile = refFiles.get(i);
				String dataFile = dataFiles.get(i);

				List<Stroke> strokes = StrokeReader
						.readStrokesFromFile(dataFile);

				List<Pair<String, List<Stroke>>> pairs = StrokeAssigner
						.assignAndFillInvisibleStrokes(refFile, strokes);

				allPairs.addAll(pairs);

			}
			return allPairs;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
}
