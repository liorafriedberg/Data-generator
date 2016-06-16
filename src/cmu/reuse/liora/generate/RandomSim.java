package cmu.reuse.liora.generate;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RandomSim implements Simulator {

	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> allColumns) {
		Source randSource = menu.getRandom(column);
		if (randSource.equals(Source.RAND_UUID)) {
			column.source = Source.RAND_UUID;
			for (Individual person : people) {
				person.setValue(column, UUID.randomUUID().toString());
			}					
		}
		else if (randSource.equals(Source.RAND_NUMBER)) {
			column.source = Source.RAND_NUMBER;
			for (Individual person : people) {
				person.setValue(column, "" + Math.random()); 
			}
			
		} 
		else if (randSource.equals(Source.RAND_OFFSET_NUM)) {
			column.source = Source.RAND_OFFSET_NUM;
			int count = people.size();	
			int offset = ThreadLocalRandom.current().nextInt(0, count + 1); 
			System.out.println("Please input starting value (ie '1' or '10000000')");
			long start = (long) menu.getOffset(column);
			long assign = start + offset; //first num to assign
			for (Individual person : people) {
				person.setValue(column, assign + ""); 
				assign++;
				if (assign >= count + start) assign = start; //continue at first value
			}
		} 
		else if (randSource.equals(Source.SEQUENTIAL_LINE)) {
			column.source = Source.SEQUENTIAL_LINE;
			File file = menu.getFile(column);				
			CSVReader reader = new CSVReader(file);
			System.out.println("Please enter the column name with the values for " + column.datatype);
			Column seqColumn = menu.getValueColumn(reader.header, column);
			int colIndex = reader.getColumnIndex(seqColumn);
			for (Individual person : people) {
				String[] parts = reader.sc.nextLine().split(",");
				person.setValue(column, parts[colIndex]); //read from column in next line
			}
		}
		else { //rand_lines
			column.source = Source.RAND_LINE;
			File file = menu.getFile(column);
			CSVReader readerCount = new CSVReader(file);
			int count = readerCount.countLines();
			System.out.println("Please enter the name of the column with the possible values.");	
			Column valColumn = menu.getValueColumn(readerCount.header, column);
			readerCount.close();	
			System.out.println("Please enter the upper bound of the number "
					+ "of values, starting"
					+ " at 1, to generate for " + column.datatype);
			int num = (int) menu.getBound(column); //will often be 1
			for (Individual person: people) {
				int times = ThreadLocalRandom.current().nextInt(1, num + 1);					
				String concat = "";
				for (int i = 0; i < times; i++) {
					CSVReader reader = new CSVReader(file);
					concat = concat + reader.staticRead(valColumn, count) + " "; //add next random value
					reader.close();
				}
				person.setValue(column, concat);						
			}
		}

	}

}
