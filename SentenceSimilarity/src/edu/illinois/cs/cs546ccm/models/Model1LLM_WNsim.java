package edu.illinois.cs.cs546ccm.models;

import java.io.IOException;
import java.util.List;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.View;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class Model1LLM_WNsim extends Model {

	public Model1LLM_WNsim() {
		super("LLM_WNsim");
		// TODO Auto-generated constructor stub
	}

	@Override
	public double similarity(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		String [] s1 = ta1.getTokens();
		String [] s2=ta2.getTokens();
		
		double score=0;
		
		score=this.similarity(s1, s2);

		return score;
	}
	
	public double similarity(String[] s1, String[] s2) {
		//String[] s1=source.split("\\s+");
		//String[] s2=source.split("\\s+");

		double scale=5;
		double score1=0;
		double score2=0;
		for (int i=0;i<s1.length;i++)
		{
			 double max=0;
			for(int j=0;j<s2.length;j++)
			{
				String w1=s1[i];
				String w2=s2[j];
				double temp=SimilarityUtils.wordSimilairty(w1, w2);
                if (temp>max)
                	max=temp;
			}
			  score1=score1+max;
		}
		for (int i=0;i<s2.length;i++)
		{
			 double max=0;
			for(int j=0;j<s1.length;j++)
			{
				String w1=s2[i];
				String w2=s1[j];
				double temp=SimilarityUtils.wordSimilairty(w1, w2);
                if (temp>max)
                	max=temp;
			}
			  score2=score2+max;
		}
		
		double score=0;
		
		if((s1.length!=0)&&(s2.length!=0))
			score=(score1/s1.length+score2/s2.length)/2;		
		
		return scale * score;
	}

	@Override
	public int confidence(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		return 100;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String source = " A female applies something to he hair with her hands.";
		String target = "A cat is eating a slice of watermelon";
		Model1LLM_WNsim m = new Model1LLM_WNsim();
		String[] s1=source.split("\\s+");
		String[] s2=target.split("\\s+");
		double result = m.similarity(s1, s2);
		System.out.println(result);
	}

}
