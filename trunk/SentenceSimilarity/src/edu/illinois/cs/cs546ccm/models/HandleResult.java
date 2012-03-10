package edu.illinois.cs.cs546ccm.models;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class HandleResult {

	public static String score_to_label(double score) {
		int label;
		score=score*10+1;
		label=Math.round((float)score);
		return String.valueOf(label);
	}
	
	public static double label_to_score(String label)
	{
		double score;
		int s;
		s=Integer.parseInt(label)-1;
		score=(double)s/10.0;
		return score;
	}
	
	
	public static void printBadpair(String datasetname) {
		try {
			String SVMoutput="svm/STS.svm."+datasetname;
			String inputfile="input/STS.input."+datasetname+".txt";
			String gsfile="input/STS.gs."+datasetname+".txt";
			String feature="svm/"+datasetname+".txt";
			
			FileInputStream fstream1 = new FileInputStream(SVMoutput);
			DataInputStream in1 = new DataInputStream(fstream1);
			BufferedReader br_SVMoutput = new BufferedReader(new InputStreamReader(in1));
			
			FileInputStream fstream2 = new FileInputStream(inputfile);
			DataInputStream in2 = new DataInputStream(fstream2);
			BufferedReader br_inputfile = new BufferedReader(new InputStreamReader(in2));
			
			FileInputStream fstream3 = new FileInputStream(gsfile);
			DataInputStream in3 = new DataInputStream(fstream3);
			BufferedReader br_gsfile = new BufferedReader(new InputStreamReader(in3));
			
			FileInputStream fstream4 = new FileInputStream(feature);
			DataInputStream in4 = new DataInputStream(fstream4);
			BufferedReader br_feature = new BufferedReader(new InputStreamReader(in4));
			
			String line1;
			while ((line1 = br_SVMoutput.readLine()) != null) {
				String[] ss = line1.split(" ");
				double resultscore=label_to_score(ss[0]);
				String gs=br_gsfile.readLine();
				double gsdouble=Double.parseDouble(gs);
				
				String sentencepair=br_inputfile.readLine();
				
				String featureline=br_feature.readLine();
				
				//the difference between the gs and our score is larger than 2, then output this pair
				if(Math.abs(resultscore-gsdouble)>2) {
					System.out.println("Sentence pair:"+sentencepair);
					System.out.println("gs:"+gs+" "+"our score:"+ss[0]);
					System.out.println("our feature's score:"+featureline);
				}
			}
			in1.close();
			in2.close();
			in3.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
/*	public static void z_score_svm(String datasetname)
	{
		String original="svm/"+datasetname+".txt";
		
		try{
		
		FileInputStream fstream1 = new FileInputStream(original);
		DataInputStream in1 = new DataInputStream(fstream1);
		BufferedReader br_SVMoriginal = new BufferedReader(new InputStreamReader(in1));
		
		String line1;
		int count=1;
		line1 = br_SVMoriginal.readLine();
		String[] ss=line1.split(" ");
		int featurenum=ss.length-1;
		double totalsquare[]=new double[featurenum];
		double total[]=new double[featurenum];
		totalsquare[0]=0;
		total[0]=0;
		
		while ((line1 = br_SVMoriginal.readLine()) != null) {
			count++;
			String[] ss = line1.split(" ");
			for(int i=1; i<ss.length;i++){
				
			}
		}
		
		
	} catch (IOException e) {
		e.printStackTrace();
	}
	}*/
	
	public static void main(String[] args) throws IOException {
		printBadpair("Temp");
	
	}
}
