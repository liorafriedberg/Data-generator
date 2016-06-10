package cmu.reuse.liora.generate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates synthetic individual-level data from aggregate data
 * @author liorafriedberg
 */

public class Generator {
	
	//**note to reader: override menu calls in automenu, change back to menu for user interaction**	
	
	/**
	 * starts generation
	 * @param args
	 */
	public static void main(String[] args) {
		long first = System.currentTimeMillis();
		List<Individual> rows = generate();
		long second = System.currentTimeMillis();
		for (Individual i : rows) { //to test
			Map<Column, String> values = i.getValues();
			for (Column c : values.keySet()) {
				System.out.println("datatype: " + c.datatype + " value: " + values.get(c));
			}
		}
		//now have rows to put in database!
		long finalTime = second - first;
		System.out.println("final time in millis: " + finalTime);
	}
	

	/**
	 * @return		the generated individual data
	 */
	public static List<Individual> generate() {
		Menu menu = new Menu(); //AutoMenu to automate
		List<File> files = menu.getAllFiles(); //get all relevant files from user		
		
		List<Column> columns = new ArrayList<>();
		for (File file : files) {
			CSVReader reader = new CSVReader(file);
			columns.addAll(reader.header);
			reader.close();
		}
		//columns now has all the possible columns
		List<Column> allColumns = columns; //allColumns has all possible columns
		columns = menu.getFinalColumns(columns); //columns has ones the user actually wants, in order		
		//arraylist will maintain order
		
		List<Individual> people = new ArrayList<>();
		System.out.println("Please enter the number of individuals to simulate.");
		long numRows = menu.getNum(); //get number of rows user wants
		for (int i = 0; i < numRows; i++) {
			Individual person = new Individual();
			Column c = new Column("id");
			person.setValue(c, i + 1 + "");
			people.add(person);
		}
		
		for (Column column : columns) {
			Source source = menu.getSource(column); //ask user for how want to do it - in order
		
			if (source.equals(Source.PROBS)) { //done with probs
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
			else if (source.equals(Source.DEP_PROBS_FILE)) {
				System.out.println("Please enter the datatype on which file choice depends");
				Column depColumn = menu.getColumn(allColumns);
				Map<String, File> valueToFile = menu.getFileDeps();
				Map<File, CSVReader> fileToReader = new HashMap<>();
				for (File f : valueToFile.values()) {
					CSVReader reader = new CSVReader(f);
					System.out.println("For file " + f.getName() + " please enter the following");
					System.out.println("Please enter the name of the column with the probabilities");
					Column probColumn = menu.getColumn(reader.header);
					int format = menu.getDataFormat(); //1 is percentage 2 is freq
					if (format == 1) {
						reader.parseProbs(column, probColumn);
					} else {
						reader.parseFreqs(column, probColumn);
					}
					fileToReader.put(f, reader);			
				}		
				for (Individual person : people) {
					String currValue = person.getValues().get(depColumn);
					File file = valueToFile.get(currValue); 
					CSVReader reader = fileToReader.get(file); 
					person.setValue(column, reader.calculate());
				}
				for (CSVReader r : fileToReader.values()) {
					r.close();
				}			
			}
			else if (source.equals(Source.DEP_PROBS)) {
				File file = menu.getFile();								
				CSVReader reader = new CSVReader(file);
				List<Column> dependencies = menu.getDependencies(column, reader.header);
				List<Column> potentialValues = menu.getPotentialValues(column, reader.header);				
				menu.getLabels(potentialValues); //these columns now have the label variable										
				//list of columns to choose distribution from
				
				Map<Integer, Column> indexToPotential = new HashMap<>();
				for (Column col : potentialValues) {
					int index = reader.getColumnIndex(col);
					indexToPotential.put(index, col);
				}				
				//index to potential has the potential values mapped with their indices
				reader.close();
				int format = menu.getDataFormat();
				
				for (Individual person : people) {
					CSVReader personReader = new CSVReader(file);
					Map<Integer, String> indexToValue = new HashMap<>(); //index of dep cols and what vals looking for
					Map<Column, String> currentValues = person.getValues();
					for (Column d : dependencies) {
						int index = personReader.getColumnIndex(d);
						indexToValue.put(index, currentValues.get(d));
					}									
					if (format == 1) {
						personReader.parseProbsDep(indexToValue, indexToPotential);
					} 
					else { //== 2
						personReader.parseFreqsDep(indexToValue, indexToPotential);
					}														
					person.setValue(column, personReader.calculate());
					personReader.close();
				}
			}
			
			else if (source.equals(Source.DEP_STATIC)) { //done static deps
				File file = menu.getFile();
				CSVReader reader = new CSVReader(file);
				List<Column> dependencies = menu.getDependencies(column, reader.header);			
				int columnIndex = reader.getColumnIndex(column); //index of column value want
				reader.close();
				for (Individual person : people) {
				CSVReader personReader = new CSVReader(file);
				Map<Integer, String> indexToValue = new HashMap<>(); //index of dep cols and what vals looking for
				Map<Column, String> currentValues = person.getValues();
				for (Column d : dependencies) {
					int index = personReader.getColumnIndex(d);
					indexToValue.put(index, currentValues.get(d));
				}
				String value = personReader.findStaticValue(indexToValue, columnIndex); //index to val at least here fix
				person.setValue(column, value);
				personReader.close();
				}				
			} else if (source.equals(Source.DOB)) {
				File file = menu.getFile();
				CSVReader reader = new CSVReader(file);
				System.out.println("Please enter the name of the column with the probabilities for age group");
				Column probColumn = menu.getColumn(reader.header);
				System.out.println("Please enter the name of the column with the values for age group");
				Column valColumn = menu.getColumn(reader.header);
				int format = menu.getDataFormat();
				if (format == 1) {
					reader.parseProbs(valColumn, probColumn);
				}
				else { //== 2
					reader.parseFreqs(valColumn, probColumn);
				}
				for (Individual person : people) {
					String range = reader.calculate();
					String[] ends = range.split("-");
					int start = Integer.parseInt(ends[0]);
					int end = Integer.parseInt(ends[1]);	
					int randomDay = ThreadLocalRandom.current().nextInt(1, 366);
					int randomAge = ThreadLocalRandom.current().nextInt(start, end + 1);	
					Calendar calendar = Calendar.getInstance();
					int year = Calendar.getInstance().get(Calendar.YEAR);
					randomAge = year - randomAge;
					calendar.set(Calendar.DAY_OF_YEAR, randomDay);
					calendar.set(Calendar.YEAR, randomAge);
					Date date = calendar.getTime();
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");				
					String dob = sdf.format(date);
					person.setValue(column, dob);
				}
				reader.close();								
			}
			else if (source.equals(Source.RANDOM)) { //done with random
				Source randSource = menu.getRandom();
				if (randSource.equals(Source.RAND_UUID)) {
					for (Individual person : people) {
						person.setValue(column, UUID.randomUUID().toString());
					}					
				}
				else if (randSource.equals(Source.RAND_NUMBER)) { //what would really use this for? but an opt.
					for (Individual person : people) {
						person.setValue(column, "" + Math.random()); 
					}
					
				} else if (randSource.equals(Source.RAND_OFFSET_NUM)) {
					int count = people.size();	
					int offset = ThreadLocalRandom.current().nextInt(0, count + 1); 
					System.out.println("Please input starting value (ie '1' or '10000000')");
					long start = (long) menu.getNum();
					long assign = start + offset;
					for (Individual person : people) {
						person.setValue(column, assign + ""); 
						assign++;
						if (assign >= count + start) assign = start;
					}
				} else if (randSource.equals(Source.SEQUENTIAL_LINE)) {
					File file = menu.getFile();				
					CSVReader reader = new CSVReader(file);
					System.out.println("Please enter the column name with the values for " + column.datatype);
					Column seqColumn = menu.getColumn(reader.header);
					int colIndex = reader.getColumnIndex(seqColumn);
					for (Individual person : people) {
						String[] parts = reader.sc.nextLine().split(",");
						person.setValue(column, parts[colIndex]);
					}
				}
				else { //rand_line/s
					File file = menu.getFile();
					CSVReader readerCount = new CSVReader(file);
					int count = readerCount.countLines();
					readerCount.close();	
					System.out.println("Please enter the upper bound of the number "
							+ "of values, starting"
							+ " at 1, to generate for " + column.datatype);
					int num = (int) menu.getNum();
					for (Individual person: people) {
						int times = ThreadLocalRandom.current().nextInt(1, num + 1);					
						String concat = "";
						for (int i = 0; i < times; i++) {
							CSVReader reader = new CSVReader(file);
							concat = concat + reader.staticRead(column, count) + " ";
							reader.close();
						}
						person.setValue(column, concat);						
					}
				}
				
			}
		
		
		}
		
		return people;
	}

}
