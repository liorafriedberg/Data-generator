package cmu.reuse.liora.generate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author liorafriedberg
 * Generates synthetic individual-level data from aggregate data
 */

public class Generator {
	
	//**note to reader: override menu calls in automenu, change back to menu for user interaction**
	
	
	/* todo:
	 * javadoc - check other tags - push
	 * tests (performance numbers, functionality)
	 * shorten to just do datatype, etc for ex: getprobcolumn in menu
	 */
	
	/**
	 * @param args
	 * calls generate
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
		Menu menu = new AutoMenu(); //AutoMenu to automate
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
		int numRows = menu.getNumRows(); //get number of rows user wants
		for (int i = 0; i < numRows; i++) {
			Individual person = new Individual();
			people.add(person);
		}
		
		for (Column column : columns) {
			Source source = menu.getSource(column); //ask user for how want to do it - in order
		
			if (source.equals(Source.PROBS)) { //done with probs
				File file = menu.getFile();
				Column probColumn = menu.getProbabilityColumn(allColumns);
				//value column is column, probColumn is frequency column
				int format = menu.getDataFormat(); //1 is percentage 2 is freq
				CSVReader reader = new CSVReader(file);
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
					
				} else { //rand_line
					File file = menu.getFile();
					CSVReader readerCount = new CSVReader(file);
					int count = readerCount.countLines();
					readerCount.close();					
					for (Individual person: people) {
						CSVReader reader = new CSVReader(file);
						person.setValue(column, reader.staticRead(column, count));
						reader.close();
					}
				}
				
			}
		
		
		}
		
		return people;
	}

}
