package edu.illinois.cs.cs546ccm.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import weka.classifiers.*;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.trees.M5P;
import weka.core.*;

import edu.illinois.cs.cogcomp.edison.sentences.Constituent;
import edu.illinois.cs.cogcomp.edison.sentences.Relation;
import edu.illinois.cs.cogcomp.edison.sentences.TextAnnotation;
import edu.illinois.cs.cogcomp.edison.sentences.View;
import edu.illinois.cs.cogcomp.edison.sentences.ViewNames;

public class Model2XXX extends Model {

    Instances data;
    FastVector attributes;
    Classifier model;
    

    public Model2XXX() {
        super("XXX");
        data = defineFeatures();
    }

    private Instances defineFeatures() {
        // Declare the attribute vector
        attributes = new FastVector(8);
        
        // Ryan's attributes
        attributes.addElement(new Attribute("r1"));
        attributes.addElement(new Attribute("r2"));
        attributes.addElement(new Attribute("r3"));
        attributes.addElement(new Attribute("r4"));
        attributes.addElement(new Attribute("r5"));
        attributes.addElement(new Attribute("r6"));
        //
        
        // Guihua's features
        attributes.addElement(new Attribute("g1"));
        attributes.addElement(new Attribute("g2"));
        //
        
        // Cedar's features
        attributes.addElement(new Attribute("c1"));
        //
        
        // Zhijin's features
        attributes.addElement(new Attribute("z1"));
        attributes.addElement(new Attribute("z2"));
        //
        
        // Gold-standard score (class value)
            // Code snippet for handling multi-class classification
            FastVector fvClassVal = new FastVector(21);
            for(int i=0; i<5; i++) {
                for(int j=0; j<10; j++) {
                    fvClassVal.addElement(i+"."+j);
                }
            }
            fvClassVal.addElement("5.0");
            attributes.addElement(new Attribute("gs_approx", fvClassVal));
            //
        //attributes.addElement(new Attribute("gs"));
        //
        
        Instances ret = new Instances("CCM-SemanticSimilarity", attributes, 1000);
        ret.setClassIndex(ret.numAttributes() - 1);
        return ret;
    }

    @Override
    public double similarity(TextAnnotation ta1, TextAnnotation ta2) {
        Instance example = getInstance(ta1, ta2, 0.0);
        example.setDataset(data);
        double similarity;
        try {
            double[] res = model.distributionForInstance(example);
            similarity = model.classifyInstance(example) / 10;
        }
        catch (Exception e) {
            System.out.println("Exception while trying to classify");
            similarity = -1;
        }
        return similarity;
    }

    private static double[] score4(TextAnnotation ta1, TextAnnotation ta2) {
        // TODO Auto-generated method stub
        // Zhijin's method
        View v1 = ta1.getView(ViewNames.NER);
        View v2 = ta2.getView(ViewNames.NER);
        // System.out.println(v1);
        // System.out.println(v2);

        List<Constituent> cs1;
        List<Constituent> cs2;
        // use cs2 to save the larger
        if (v1.getConstituents().size() < v2.getConstituents().size()) {
            cs1 = v1.getConstituents();
            cs2 = v2.getConstituents();
        } else {
            cs1 = v2.getConstituents();
            cs2 = v1.getConstituents();
        }

        double score = 0;
        for (Constituent c1 : cs1) {
            // System.out.println(c1.getLabel() + " " + c1.toString());
            double point = 0;
            for (Constituent c2 : cs2) {

                if (c1.toString().equals(c2.toString())) {// same words
                    point = 1;
                } else if (point == 0 && c1.getLabel().equals(c2.getLabel())) {
                    if (c1.toString().contains(c2.toString())
                            || c2.toString().contains(c1.toString())) {
                        // one NE is a substring of another
                        point = 1;
                    } else {
                        // a little score if different words but same NER types
                        point = 0.3;
                    }

                } else {
                    // NER types not matching
                }

            }
            score += point;
        }

        // if the numbers of NEs are different, penalize the score
        int sizeDiff = cs2.size() - cs1.size();
        for (int i = 1; i <= sizeDiff; i++) {
            double penalty = 1.0 / i;
            if (score - penalty > 0) {
                score -= penalty;
            } else {
                break;
            }
        }
        if(cs1.size() == 0)
            return new double[]{0, sizeDiff};
        return new double[]{score / cs1.size(), sizeDiff};
    }

    private static double[] score3(TextAnnotation ta1, TextAnnotation ta2) {
        // TODO Auto-generated method stub
        // Cedar's method
        return new double[]{0};
    }

    private static double score2_1(TextAnnotation ta1, TextAnnotation ta2) throws IOException{
        // Guihua's method
        // ta1 is Text, and ta2 is Hypothesis
        View v1 = ta1.getView(ViewNames.SHALLOW_PARSE);
        View v2 = ta2.getView(ViewNames.SHALLOW_PARSE);
        // System.out.println(v1);
        // System.out.println(v2);

        List<Constituent> cs1;
        List<Constituent> cs2;
        cs1 = v1.getConstituents();
        cs2 = v2.getConstituents();
        
        double score = 0;
        for (Constituent c2 : cs2) {
            String chuncktype2=c2.getLabel();
            String chunckcontent2=c2.toString();
            double point = 0;
            for (Constituent c1 : cs1) {
                String chuncktype1=c1.getLabel();
                
                //only if they have the same type of chunk (NP, VP, and so on), they can compare, and get the max one
                if(chuncktype1.compareToIgnoreCase(chuncktype2)==0){
                    String chunckcontent1=c1.toString();
                    Model1LLM m = new Model1LLM();
                    double result = m.similarity(chunckcontent1, chunckcontent2);
                    if(result>point) point=result;
                }
            }
            score=score+point;
        }
        
        //Normalize by the Hypothesis's length
        score=score/cs2.size();
        return score;
    }

    private static double[] score2(TextAnnotation ta1, TextAnnotation ta2) {
        // TODO Auto-generated method stub
        // Guihua's method
        double score[]=new double[2];
        
        //ta1 is Text, ta2 is Hypothesis
        try {
            score[0]=score2_1(ta1, ta2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //ta2 is Text, ta1 is Hypothesis
        try {
            score[1]=score2_1(ta2, ta1);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return score;
    }

    private static double[] score1(TextAnnotation ta1, TextAnnotation ta2) {
        return new double[]{ta1.getText().split(" ").length, ta2.getText().split(" ").length, 
                wordsInCommon(ta1, ta2), wordsInCommon(ta2, ta1),
                srlSimilarity(ta1, ta2), srlSimilarity(ta2, ta1)};
    }

    private static double srlSimilarity(TextAnnotation ta1, TextAnnotation ta2) {
     // TODO Auto-generated method stub
        // Ryan's method
        View v1 = ta1.getView(ViewNames.SRL);
        View v2 = ta2.getView(ViewNames.SRL);
        
        List<Constituent> cs1 = v1.getConstituents();
        List<Constituent> cs2 = v2.getConstituents();
        
        
        double score = 0;
        for (Constituent c1 : cs1) {
            double point = 0;
            for (Constituent c2 : cs2) {
                double temp = predMatch(c1, c2);
                if(temp > point)
                    point = temp;
            }
            score=score+point;
        }
        
        return score;
    }

    // Helper method for SRL similarity
    private static double predMatch(Constituent c1, Constituent c2) {
        if(c1.getLabel().equals("Predicate") && c2.getLabel().equals("Predicate")) {
            if(c1.getAttribute("predicate").equals(c2.getAttribute("predicate"))) {
                // these two nodes having matching predicates
                double score = 0;
                List<Relation> rs1 = c1.getOutgoingRelations();
                List<Relation> rs2 = c2.getOutgoingRelations();
                for(Relation r1 : rs1) {
                    double point = 0;
                    for(Relation r2 : rs2) {
                        if(r1.getRelationName().equals(r2.getRelationName())) {
                            // should be a similarity measure
                            double temp = constituentMatch(r1.getTarget(), r2.getTarget());
                            if(temp > point)
                                point = temp;
                        }
                    }
                }
                if(rs1.size() > 0)
                    return score / rs1.size();
                return 1;
            }
        }
        return 0;
    }

    // Right now, must match on all tokens
    private static double constituentMatch(Constituent c1, Constituent c2) {
        String t1 = "";
        for(int i=c1.getStartSpan(); i<c1.getEndSpan(); i++) {
            t1 += c1.getTextAnnotation().getToken(i)+" ";
        }
        if(t1.trim().equals(c1.getTextAnnotation().getText())) {
            System.out.println(t1.trim());
            System.out.println(c1.getTextAnnotation().getText());
        }
        String t2 = "";
        for(int i=c2.getStartSpan(); i<c2.getEndSpan(); i++) {
            t2 += c2.getTextAnnotation().getToken(i)+" ";
        }
        
        String[] ws1 = t1.split(" ");
        String[] ws2 = t2.split(" ");
        
        double score = 0;
        for (String w1 : ws1) {
            double point = 0;
            for (String w2 : ws2) {
                if(w1.equals(w2))
                    point = 1;
            }
            score=score+point;
        }
        if(ws1.length > 0)
            return score / ws1.length;
        return score;
    }

    private static double wordsInCommon(TextAnnotation ta1, TextAnnotation ta2) {
        View v1 = ta1.getView(ViewNames.SENTENCE);
        View v2 = ta2.getView(ViewNames.SENTENCE);
        
        String[] ws1 = v1.getConstituents().get(0).getTextAnnotation().getTokens();
        String[] ws2 = v2.getConstituents().get(0).getTextAnnotation().getTokens();
        
        double score = 0;
        for (String w1 : ws1) {
            double point = 0;
            for (String w2 : ws2) {
                if(w1.equals(w2))
                    point = 1;
            }
            score=score+point;
        }
        if(ws1.length > 0)
            return score / ws1.length;
        return score;
    }

    @Override
    public int confidence(TextAnnotation ta1, TextAnnotation ta2) {
        // TODO implement this method
        return -1;
    }
    
    public void train(String gsFile) {
        try {
            ArrayList<Double> gs_arr = getGSscores(gsFile);
            int pairs = tas.length / 2;
            if(gs_arr.size() != pairs) {
                System.out.println("Corpus does not match gold-standard; aborting training");
                return;
            }
            for(int i=0; i<pairs; i++) {
                TextAnnotation ta1 = this.tas[2 * i];
                TextAnnotation ta2 = this.tas[2 * i + 1];
                double gs = gs_arr.get(i);
                trainInstance(ta1, ta2, gs);
            }
            //model = new M5P();
            model = new LibSVM();
            model.buildClassifier(data);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("Training failed.");
            e.printStackTrace();
        }
    }
    
    private void trainInstance(TextAnnotation ta1, TextAnnotation ta2, double gs) {
        data.add(getInstance(ta1, ta2, gs));
    }

    private Instance getInstance(TextAnnotation ta1, TextAnnotation ta2, double gs) {
        double[] score1 = score1(ta1, ta2);
        double[] score2 = score2(ta1, ta2);
        double[] score3 = score3(ta1, ta2);
        double[] score4 = score4(ta1, ta2);
        
        return combineAttributes(score1, score2, score3, score4, gs);
    }

    private Instance combineAttributes(double[] score1, double[] score2, double[] score3,
            double[] score4, double gs) {
        Instance inst = new Instance(data.numAttributes());
        inst.setDataset(data);
        int count = 0;
        for(int i=0; i<score1.length; i++) {
            inst.setValue(count, score1[i]);
            count++;
        }
        for(int i=0; i<score2.length; i++) {
            inst.setValue(count, score2[i]);
            count++;
        }
        for(int i=0; i<score3.length; i++) {
            inst.setValue(count, score3[i]);
            count++;
        }
        for(int i=0; i<score4.length; i++) {
            inst.setValue(count, score4[i]);
            count++;
        }
        inst.setValue(count, parseGS(gs));
        return inst;
    }
    
    // Truncates the string representation of gs to the tenths place
    // E.g.: parseGS(2.355) = 2.3
    private static String parseGS(double gs) {
        String val = gs+"";
        for(int i=val.length(); i<3; i++)
            val += "0";
        return val.substring(0, 3);
    }

    private static ArrayList<Double> getGSscores(String gsFile) {
        try {
            ArrayList<Double> list = new ArrayList<Double>();
            Scanner sc = new Scanner(new File(gsFile));
            while(sc.hasNextLine()) {
                list.add(Double.parseDouble(sc.nextLine()));
            }
            sc.close();
            return list;
        }
        catch(FileNotFoundException e) {
            // we'll make a fuss further up the call stack
            return new ArrayList<Double>();
        }
    }
    
    @Override
    public void computeAndSaveOutputToFile(String fileName) throws IOException {
        String gsFile = getGSFileName(fileName);
        train(gsFile);
        super.computeAndSaveOutputToFile(fileName);
    }

    private static String getGSFileName(String fileName) {
        String corpusLabel = fileName.split("[/_]")[1];
        if(corpusLabel.contains("MSR"))
            return "input/STS.gs."+corpusLabel+".txt";
        else
            return "input/temp.gs.txt";
    }
}
