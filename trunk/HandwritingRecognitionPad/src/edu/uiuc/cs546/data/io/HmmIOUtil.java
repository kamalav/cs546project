package edu.uiuc.cs546.data.io;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

import be.ac.ulg.montefiore.run.jahmm.io.HmmBinaryReader;
import edu.uiuc.cs546.hmm.HmmCommons;
import edu.uiuc.cs546.hmm.LeftToRightHmm2;
import edu.uiuc.cs546.hmm.feature.FeatureVector;

public class HmmIOUtil {

	static public HashMap<String, LeftToRightHmm2> readSavedHmms() {
		HashMap<String, LeftToRightHmm2> hmms = new HashMap<String, LeftToRightHmm2>();
		String[] fileNames = FileUtil
				.fileNamesInDirectory(HmmCommons.TRAINED_HMMS_DIR);
		for (String file : fileNames) {
			String str = file.substring(0, file.indexOf('.'));
			DataInputStream dis;
			try {
				dis = new DataInputStream(new FileInputStream(
						HmmCommons.TRAINED_HMMS_DIR + "/" + file));
				LeftToRightHmm2 hmm = new LeftToRightHmm2(
						HmmBinaryReader.read(dis));
				hmms.put(str, hmm);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return hmms;
	}

	static public FeatureVector[] readSavedFeatureVectors(String file) {
		String[] lines = FileUtil.getLines(file);
		FeatureVector[] vectors = new FeatureVector[lines.length];
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			String[] data = line.split("\t");
			double penUp = Double.parseDouble(data[0]);
			double deltaX = Double.parseDouble(data[1]);
			double deltaY = Double.parseDouble(data[2]);
			double writingAngle = Double.parseDouble(data[3]);
			double deltaWritingAngle = Double.parseDouble(data[4]);
			double biggerX = Double.parseDouble(data[5]);
			vectors[i] = new FeatureVector(penUp, deltaX, deltaY, writingAngle,
					deltaWritingAngle, biggerX);
		}
		return vectors;
	}

	/*
	 * test if there exist trained HMMs in binary files by simply checking if
	 * the directory for saving models is empty
	 */
	static public boolean hmmModelFilesExist() {
		File folder = new File(HmmCommons.TRAINED_HMMS_DIR);
		return folder.listFiles().length > 1;
	}

	static public boolean prototypeVectorsFileExist() {
		File file = new File(HmmCommons.TRAINED_VECTORS);
		return file.exists();
	}

	static public void deleteSavedModels() {
		File folder = new File(HmmCommons.TRAINED_HMMS_DIR);
		if (folder.exists()) {
			File[] files = folder.listFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
		File file = new File(HmmCommons.TRAINED_VECTORS);
		if (file.exists())
			file.delete();
	}
}
