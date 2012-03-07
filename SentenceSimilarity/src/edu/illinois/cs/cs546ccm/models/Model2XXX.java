package edu.illinois.cs.cs546ccm.models;

import edu.illinois.cs.mmak4.corpus.Corpus;

public class Model2XXX extends Model {

	public Model2XXX() {
		super("XXX");
	}

	@Override
	public double similarity(Corpus corpus, int line) {
		double score1 = score1();
        double score2 = score2();
        double score3 = score3();
        double score4 = score4();
		return -1;
	}

	private double score4() {
        // TODO Auto-generated method stub
	    // Zhijin's method
        return 0;
    }

    private double score3() {
        // TODO Auto-generated method stub
        // Cedar's method
        return 0;
    }

    private double score2() {
        // TODO Auto-generated method stub
        // Guihua's method
        return 0;
    }

    private double score1() {
        // TODO Auto-generated method stub
        // Ryan's method
        return 0;
    }

    @Override
	public int confidence(Corpus corpus, int line) {
		// TODO implement this method
		return -1;
	}

}
