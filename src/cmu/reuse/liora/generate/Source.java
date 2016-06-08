package cmu.reuse.liora.generate;

/**
 * @author liorafriedberg
 * options for determining the synthetic data value
 */
public enum Source {	
	PROBS, //prob distribution
	DEP_PROBS, //depends on prev value, prob distribution
	DEP_STATIC, //depends on prev value, pre-determined	
	RANDOM, //random generation
		RAND_UUID, //uuid
		RAND_LINE, //value from column and random line in file
		RAND_NUMBER; //random number between 0 and 1 (can modify based on needs)
	
}
