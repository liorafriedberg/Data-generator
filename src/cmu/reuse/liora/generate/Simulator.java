package cmu.reuse.liora.generate;

import java.util.List;

/**
 * generic simulator for generating data from any source
 * @author liorafriedberg
 */
public interface Simulator {
	
	/**
	 * assigns generated values to all people for the current column
	 * @param menu			Menu for input
	 * @param column		Current column
	 * @param people		All people
	 * @param allColumns	All possible columns
	 */
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> allColumns);

}
