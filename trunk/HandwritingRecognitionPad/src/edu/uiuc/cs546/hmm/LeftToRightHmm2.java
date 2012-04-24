package edu.uiuc.cs546.hmm;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;

public class LeftToRightHmm2 extends Hmm<ObservationInteger> {

	public LeftToRightHmm2(Hmm hmm) {

		super(hmm.nbStates(), new OpdfIntegerFactory(HmmCommons.HMM_SYMBOLS));

		int nbStates = hmm.nbStates();

		for (int i = 0; i < nbStates; i++) {
			for (int j = 0; j < nbStates; j++) {
				setAij(i, j, hmm.getAij(i, j));
			}
			setPi(i, hmm.getPi(i));
			setOpdf(i, hmm.getOpdf(i));

		}
	}

	public LeftToRightHmm2(int nbStates) {
		super(nbStates, new OpdfIntegerFactory(HmmCommons.HMM_SYMBOLS));

		// set Aij and Pi
		for (int i = 0; i < nbStates; i++) {

			for (int j = 0; j < nbStates; j++) {
				if (i == j || i == j - 1) {
					setAij(i, j, (double) 0.5);
				} else {
					setAij(i, j, 0);
				}
			}
			if (i == 0) {
				setPi(i, 1);
			} else {
				setPi(i, 0);
			}

		}
		setAij(nbStates - 1, nbStates - 1, 1);

	}
}
