package edu.illinois.cs.cs546ccm.models;

import java.io.IOException;

public class HandleResult {

	public static String score_to_label(double score) {
		int label;
		score=score*10;
		label=Math.round((float)score);
		return String.valueOf(label);
	}
	
	public static double label_to_score(String label)
	{
		double score;
		int s;
		s=Integer.parseInt(label);
		score=(double)s/10.0;
		return score;
	}
	
	public static void main(String[] args) throws IOException {
		String s=score_to_label(3.560);
		double s1=label_to_score("35");
		System.out.println(s);
		System.out.println(s1);
	
	}
}
