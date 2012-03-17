package edu.illinois.cs.cs546ccm.models;

import java.io.File;
import java.util.HashMap;

import edu.illinois.cs.cogcomp.entityComparison.core.EntityComparison;
import edu.illinois.cs.cogcomp.mrcs.comparators.XmlRpcMetricClient;
import edu.illinois.cs.cogcomp.mrcs.dataStructures.MetricResponse;

public class SimilarityUtils {

	public static final String SIMILARITY_MAP_FILE_NAME = "serialization/wordSimilarity.map";

	static XmlRpcMetricClient wnSimClient;
	static HashMap<String, Double> wordSimilarityMap;

	static EntityComparison entityComparator;

	static {
		String metricHost = "handy.cs.uiuc.edu";
		int metricPort = 29022;
		wnSimClient = new XmlRpcMetricClient("WNSim", metricHost, metricPort);

		if ((new File(SIMILARITY_MAP_FILE_NAME)).exists()) {
			wordSimilarityMap = SerializationUtils
					.deserializeHashMap(SIMILARITY_MAP_FILE_NAME);
		} else {
			wordSimilarityMap = new HashMap<String, Double>();
		}

		entityComparator = new EntityComparison();

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

	public static double namedEntitySimilarity(String entity1, String entity2) {
		entityComparator.compare(entity1, entity2);
		return entityComparator.getScore();
	}

	public static void main(String[] args) {
		MetricResponse response = wnSimClient.compareStrings("beauty", "lady");
		System.out.println(response.score);
		System.out.println(namedEntitySimilarity("United States", "American"));
	}

}
