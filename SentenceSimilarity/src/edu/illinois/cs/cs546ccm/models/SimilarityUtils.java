package edu.illinois.cs.cs546ccm.models;

import java.util.HashMap;

import edu.illinois.cs.cogcomp.mrcs.comparators.XmlRpcMetricClient;
import edu.illinois.cs.cogcomp.mrcs.dataStructures.MetricResponse;

public class SimilarityUtils {

	public static final String SIMILARITY_MAP_FILE_NAME = "serialization/wordSimilarity.map";

	static XmlRpcMetricClient wnSimClient;
	static HashMap<String, Double> wordSimilarityMap;

	static {
		String metricHost = "handy.cs.uiuc.edu";// "greedy.cs.uiuc.edu";
		int metricPort = 29023;// 9988;
		wnSimClient = new XmlRpcMetricClient("WNSim", metricHost, metricPort);
		wordSimilarityMap = SerializationUtils
				.deserializeHashMap(SIMILARITY_MAP_FILE_NAME);
	}

	public static HashMap<String, Double> getWordSimilarityMap() {
		return wordSimilarityMap;
	}

	public static double wordSimilairty(String word1, String word2) {
		word1 = word1.toLowerCase();
		word2 = word2.toLowerCase();
		String key = word1.compareTo(word2) < 0 ? word1 + " " + word2 : word2
				+ " " + word1;
		if (wordSimilarityMap.containsKey(key)) {
			return wordSimilarityMap.get(key);
		} else {
			MetricResponse response = wnSimClient.compareStrings(word1, word2);
			wordSimilarityMap.put(key, response.score);
			return response.score;
		}
	}

	public static void main(String[] args) {
		MetricResponse response = wnSimClient
				.compareStrings("slaughter", "not");
		System.out.println(response.score);
	}

}
