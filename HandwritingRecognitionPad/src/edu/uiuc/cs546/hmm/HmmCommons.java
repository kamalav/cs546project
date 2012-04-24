package edu.uiuc.cs546.hmm;

import java.io.File;

public class HmmCommons {
	// directory of trained models
	static public final String TRAINING_SEQUENCE_DIR = System
			.getProperty("user.dir") + "/sequences";

	// number of states for each hmm to be trained
	static public final int CHAR_HMM_STATES = 7;

	// directory of trained models
	static public final String TRAINED_MODELS_DIR = System
			.getProperty("user.dir") + "/models";

	// directory of trained HMMs in form of binaries
	static public final String TRAINED_HMMS_DIR = TRAINED_MODELS_DIR + "/hmms";

	// directory of trained HMMs in form of binaries
	static public final String TRAINED_VECTORS = TRAINED_MODELS_DIR
			+ "/vectors/prototypeVectors.txt";

	// main directory of training data
	static public final String TRAINING_DATA_DIR = System
			.getProperty("user.dir") + "/training";

	static public final String TEST_DATA_DIR = System.getProperty("user.dir")
			+ "/testing";

	// which data set to use under the main directory
	static public final String TRAINING_DATA_SET = "digit-best";

	// full data set directory
	static public final String TRAINING_DATA_SET_DIR = TRAINING_DATA_DIR + "/"
			+ TRAINING_DATA_SET;

	// which data set to test
	static public final String TEST_DATA_SET = "digit-best-shalf";

	// testing data set directory
	static public final String TEST_DATA_SET_DIR = TEST_DATA_DIR + "/"
			+ TEST_DATA_SET;

	// unipen directory, for testing use
	static public final String UNIPEN_DIR = (new File("I:/unipen")).exists() ? "I:/unipen"
			: (new File("/home/zli12/Unipen/train_r01_v07")).exists() ? "/home/zli12/Unipen/train_r01_v07"
					: "/Users/zhijin/Documents/unipen";

	// "/Users/zhijin/Documents/unipen"

	// temporarily for Trainer2, number of observation symbols
	static public final int HMM_SYMBOLS = 110;
}
