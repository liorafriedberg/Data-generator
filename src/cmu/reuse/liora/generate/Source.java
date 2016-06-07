package cmu.reuse.liora.generate;

public enum Source {	
	//options for determining the synthetic data value
	PROBS, //prob distribution
	DEP_PROBS, //depends on prev value, prob distribution
	DEP_STATIC, //depends on prev value, pre-determined	
	RANDOM, //random generation
		RAND_UUID, //uuid
		RAND_LINE, //random line (column) from file
		RAND_NUMBER; //random number between 0 and 1 (can modify based on needs)
	
}
