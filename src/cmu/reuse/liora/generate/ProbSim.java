package cmu.reuse.liora.generate;

import java.io.File;
import java.util.List;

public class ProbSim implements Simulator {

	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> cols) {
		File file = menu.getFile();
		CSVReader reader = new CSVReader(file);
		System.out.println("Please enter the name of the column with the probabilities.");
		Column probColumn = menu.getColumn(reader.header);
		//value column is column, probColumn is frequency column
		int format = menu.getDataFormat(); //1 is percentage 2 is freq
		
		if (format == 1) {
			reader.parseProbs(column, probColumn);
		}
		else { //== 2
			reader.parseFreqs(column, probColumn);
		}				
		for (Individual person : people) {
			person.setValue(column, reader.calculate());
		}
		reader.close();
	}

}
