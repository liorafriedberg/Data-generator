package cmu.reuse.liora.generate;

import java.io.File;
import java.util.List;

/**
 * generates individual-level data from a probability distribution
 * @author liorafriedberg
 */
public class ProbSim implements Simulator {

	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Simulator#simulate(cmu.reuse.liora.generate.Menu, cmu.reuse.liora.generate.Column, java.util.List, java.util.List)
	 */
	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> cols) {
		File file = menu.getFile(column);
		CSVReader reader = new CSVReader(file);
		System.out.println("Please enter the name of the column with the probabilities.");
		Column probColumn = menu.getProbColumn(reader.header, column);
		System.out.println("Please enter the name of the column with the possible values.");	
		Column valColumn = menu.getValueColumn(reader.header, column);
		//value column is column, probColumn is frequency column
		int format = menu.getDataFormat(column); //1 is percentage 2 is freq
		
		if (format == 1) {
			reader.parseProbs(valColumn, probColumn);
		}
		else { //== 2
			reader.parseFreqs(valColumn, probColumn);
		}		
		List<Double> cumus = reader.binaryHelper();
		for (Individual person : people) {
			person.setValue(column, reader.calculateBinary(cumus));
		}
		reader.close();
	}

}
