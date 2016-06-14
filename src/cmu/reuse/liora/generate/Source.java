package cmu.reuse.liora.generate;

/**
 * options for determining the synthetic data value
 * @author liorafriedberg
 */
public enum Source {	
	PROBS, //prob distribution
	DEP_PROBS_FILE, //file depends on prev value, prob distribution
	DEP_PROBS, //depends on prev value, prob distribution
	DEP_STATIC, //depends on prev value, pre-determined	
	DEP_DATE, //date dependent on range
	MULTI_VALUE, //takes on multiple values per individual
	RANDOM, //random generation
		RAND_UUID, //uuid
		RAND_LINE, //value from column and random line in file
		RAND_OFFSET_NUM, //sequential value from 1 to total, starting with random offset
		SEQUENTIAL_LINE, //sequential value from file, starting with random offset
		RAND_NUMBER; //random number between 0 and 1 (can modify based on needs)
	
}
