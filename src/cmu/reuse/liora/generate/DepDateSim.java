package cmu.reuse.liora.generate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates individual-level data of dates from year ranges
 * @author liorafriedberg
 */
public class DepDateSim implements Simulator {

	/* 
	 * (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Simulator#simulate(cmu.reuse.liora.generate.Menu, cmu.reuse.liora.generate.Column, java.util.List, java.util.List)
	 */
	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> cols) {

		File file = menu.getFile(column);
		CSVReader reader = new CSVReader(file);
		System.out.println("Please enter the name of the column with the probabilities for the "
				+ "dependency");
		Column probColumn = menu.getProbColumn(reader.header, column);
		System.out.println("Please enter the name of the column with the values for the dependency");
		Column valColumn = menu.getValueColumn(reader.header, column);
		int format = menu.getDataFormat(column);
		if (format == 1) {
			reader.parseProbs(valColumn, probColumn);
		}
		else { //== 2
			reader.parseFreqs(valColumn, probColumn);
		}
		for (Individual person : people) {
			String range = reader.calculate();
			String[] ends = range.split("-"); //get range
			int start = Integer.parseInt(ends[0]);
			int end = Integer.parseInt(ends[1]);	
			int randomDay = ThreadLocalRandom.current().nextInt(1, 366);
			int randomAge = ThreadLocalRandom.current().nextInt(start, end + 1);	
			Calendar calendar = Calendar.getInstance();
			int year = Calendar.getInstance().get(Calendar.YEAR);
			randomAge = year - randomAge; //get birth year
			calendar.set(Calendar.YEAR, randomAge);
			calendar.set(Calendar.DAY_OF_YEAR, randomDay);				
			Date date = calendar.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");				
			String dob = sdf.format(date);
			person.setValue(column, dob);
			Column ageRange = new Column("range");
			ageRange.source = Source.DEP_DATE; //fine
			Column age = new Column("age");
			age.source = Source.DEP_DATE; //fine
			person.setValue(ageRange, range); 
			person.setValue(age, "" + (year - randomAge));
		}
		reader.close();								
	

	}

}
