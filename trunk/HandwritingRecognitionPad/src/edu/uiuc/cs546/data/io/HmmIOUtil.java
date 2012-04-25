package edu.uiuc.cs546.data.io;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.content.Context;
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

	static public FeatureVector[] readSavedFeatureVectors(String fileContent) {
		String[] lines = fileContent.split("\n");
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

	public static String readRawTextFile(Context ctx, int resId) {
		InputStream inputStream = ctx.getResources().openRawResource(resId);

		InputStreamReader inputreader = new InputStreamReader(inputStream);
		BufferedReader buffreader = new BufferedReader(inputreader);
		String line;
		StringBuilder text = new StringBuilder();

		try {
			while ((line = buffreader.readLine()) != null) {
				text.append(line);
				text.append('\n');
			}
		} catch (IOException e) {
			return null;
		}
		return text.toString();
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
