package edu.illinois.cs.cs546ccm.models;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.View;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;
import edu.illinois.cs.cogcomp.mrcs.comparators.LlmComparator;
import edu.illinois.cs.mmak4.corpus.Corpus;

public class Model1LLM_WNsim extends Model {
	Set<String> Stopwords=new HashSet<String>();

	public Model1LLM_WNsim() {
		super("LLM_WNsim");
		// TODO Auto-generated constructor stub
		try {
			FileInputStream fstream1 = new FileInputStream("config/llmStopwords.txt");
	
		
		DataInputStream in1 = new DataInputStream(fstream1);
		BufferedReader br_stopwords = new BufferedReader(new InputStreamReader(in1));
		
		String line;
        while ((line=br_stopwords.readLine())!=null)
        {
        	Stopwords.add(line);           	
        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

	}

	@Override
	public double similarity(TextAnnotation ta1, TextAnnotation ta2) {
		// TODO Auto-generated method stub
		String [] s1 = ta1.getTokens();
		String [] s2=ta2.getTokens();
		
		double score=0;
		
		


		
		score=this.w_similarity(s1, s2);

		return score;
	}
	
	public double w_similarity(String[] s1, String[] s2) {
		//String[] s1=source.split("\\s+");
		//String[] s2=source.split("\\s+");

		double scale=1;
		double score1=0;
		double score2=0;
		
		int nstw1=0;
		int nstw2=0;
		
		for (int i=0;i<s1.length;i++)
		{
			 double max=0;
			 if (Stopwords.contains(s1[i]))
			 { 
				 nstw1++;  
			 }
			 else{
				 
			for(int j=0;j<s2.length;j++)
			{   
              if(!Stopwords.contains(s2[j]))
              {
				String w1=s1[i];
				String w2=s2[j];
				double temp=SimilarityUtils.wordSimilairty(w1, w2);
                if (temp>max)
                	max=temp;
			
              }
             }
			 
			 }
			  score1=score1+max;
		}
		for (int i=0;i<s2.length;i++)
		{
			double max=0;
			if(Stopwords.contains(s2[i]))
			{
				nstw2++;
			}
			else 
			{
			for(int j=0;j<s1.length;j++)
			{
				if(!Stopwords.contains(s1[j]))
				{
				String w1=s2[i];
				String w2=s1[j];
				double temp=SimilarityUtils.wordSimilairty(w1, w2);
                if (temp>max)
                	max=temp;
				}			
			}
			}
			  score2=score2+max;
		}
		
		double score=0;
		
	    int nlength1=s1.length-nstw1;
	    int nlength2=s2.length-nstw2;
		
		if((nlength1!=0)&&(nlength2!=0))
			score=(score1/nlength1+score2/nlength2)/2;		
		
		return scale * score;
	}
	
	public void serializeLLM_WNSim_Scores(TextAnnotation[] tas, String fileName) {
		try {
			StringBuffer sb = new StringBuffer(tas.length + "\n");
			int pairs = tas.length / 2;
			for (int i = 0; i < pairs; i++) {
				TextAnnotation source = tas[2 * i];
				TextAnnotation target = tas[2 * i + 1];
				double score1 = this.similarity(source, target);
				double score2 = this.similarity(target, source);
				String line = score1 + "\t" + score2 + "\n";
				sb.append(line);
				System.out.println("Serializing line " + i + " for LLM_WNSim scores");
			}
			File file = new File(fileName);
			FileOutputStream fop = new FileOutputStream(file);
			String fileContent = sb.toString();
			fop.write(fileContent.getBytes());
			fop.flush();
			fop.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
		String source = "A female applies something to he hair with her hands.";
		String target = "A cat is eating a slice of watermelon";
		Model1LLM_WNsim m = new Model1LLM_WNsim();
//		String[] s1=source.split("\\s+");
//		String[] s2=target.split("\\s+");
//		
//		FileInputStream fstream1 = new FileInputStream("config/llmStopwords.txt");
//		
//		DataInputStream in1 = new DataInputStream(fstream1);
//		BufferedReader br_stopwords = new BufferedReader(new InputStreamReader(in1));
//		
//		Set<String> Stopwords=new HashSet<String>();
//		String line;
//        while ((line=br_stopwords.readLine())!=null)
//        {
//        	Stopwords.add(line);           	
//        }
//		
//        for (String w :Stopwords)
//        {
//        	System.out.println(w);
//        }
//        
//  
//        
//		
//		double result = m.w_similarity(s1, s2);
//		System.out.println(result);
		
		Corpus[] allCorpuses = {
				new Corpus("input_final/STS.input.MSRvid.txt", "MSRvid"),
				new Corpus("input_final/STS.input.MSRpar.txt", "MSRpar"),
				new Corpus("input_final/STS.input.SMTeuroparl.txt",
	                    "SMTeuroparl"),
				new Corpus("input_final/STS.input.surprise.OnWN.txt",
						"surprise.OnWN"),
				new Corpus("input_final/STS.input.surprise.SMTnews.txt",
						"surprise.SMTnews") 
				};
		
		for (Corpus corpus : allCorpuses) {
			String fn_Corpus = "serialization_final/" + corpus.getId() + ".sel";
			String fn_llm2 = "serialization_final/" + corpus.getId() + ".llm2";
			TextAnnotation[] tas = SerializationUtils
			.deserializeTextAnnotations(fn_Corpus);
			m.serializeLLM_WNSim_Scores(tas,fn_llm2);
			System.out.println("Serialization is done for "+corpus.getId() +".llm2");
		}

		
		
		
		
		
		
	}

}
