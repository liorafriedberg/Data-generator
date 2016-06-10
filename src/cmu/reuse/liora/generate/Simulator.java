package cmu.reuse.liora.generate;

import java.util.List;

public interface Simulator {
	
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> allColumns);

}
