package edu.uiuc.cs546.data.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import edu.uiuc.cs546.data.Datapoint;
import edu.uiuc.cs546.data.Stroke;
import edu.uiuc.cs546.data.io.FileUtil;

public class StrokeReader {

	static public List<Stroke> readStrokesFromFile(String dataFile)
			throws IOException {

		List<Stroke> strokes = new ArrayList<Stroke>();
		String[] lines = FileUtil.getLines(dataFile);

		for (int i = 0; i < lines.length; i++) {

			String line = lines[i];

			if (line.startsWith(".PEN_DOWN")) {

				List<Datapoint> dps = new ArrayList<Datapoint>();

				String dataStr;

				while (++i < lines.length) {
					if ((dataStr = lines[i]).startsWith(".PEN_UP"))
						break;

					Datapoint dp = null;
					Scanner s = new Scanner(dataStr);
					try {
						dp = new Datapoint(s.nextInt(), s.nextInt());
						// System.out.println(dp.x + " " + dp.y);
						dps.add(dp);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(dataFile);
					}

				}

				Stroke stroke = new Stroke(dps, false);
				strokes.add(stroke);
			}

		}

		// System.out.println("Number of strokes read: " + strokes.size());

		return strokes;
	}

}