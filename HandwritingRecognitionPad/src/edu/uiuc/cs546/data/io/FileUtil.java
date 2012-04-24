package edu.uiuc.cs546.data.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	static public String[] getLines(String file) {
		List<String> lines = new ArrayList<String>();
		try {
			FileInputStream fstream = new FileInputStream(file);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String line;

			while ((line = br.readLine()) != null) {
				lines.add(line);
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return lines.toArray(new String[] {});
	}

	static public String[] fileNamesInDirectory(String dir) {
		File folder = new File(dir);
		File[] files = folder.listFiles();
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				names.add(files[i].getName());
			}
		}

		return names.toArray(new String[] {});
	}

	static public String[] subDirectoryNames(String dir) {
		File folder = new File(dir);
		File[] files = folder.listFiles();
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				names.add(files[i].getName());
			}
		}

		return names.toArray(new String[] {});
	}

	public static void writeFile(String file, String string) {
		try {
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(string);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
