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
			System.out.println("");
			for (Column c : values.keySet()) {
				System.out.print(c.datatype + ": " + values.get(c) + " ");
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
			Simulator sim;
			if (source.equals(Source.PROBS)) { //done with probs
				sim = new ProbSim();
				sim.simulate(menu, column, people, allColumns);
			} 
			else if (source.equals(Source.DEP_PROBS_FILE)) {
				sim = new DepProbFileSim();
				sim.simulate(menu, column, people, allColumns);
			}
			else if (source.equals(Source.DEP_PROBS)) {
				sim = new DepProbSim();
				sim.simulate(menu, column, people, allColumns);
			}			
			else if (source.equals(Source.DEP_STATIC)) { 
				sim = new DepStaticSim();
				sim.simulate(menu, column, people, allColumns);
			} 
			else if (source.equals(Source.DEP_DATE)) {
				sim = new DepDateSim();
				sim.simulate(menu, column, people, allColumns);
			}
			else if (source.equals(Source.RANDOM)) {
				sim = new RandomSim();
				sim.simulate(menu, column, people, allColumns);
			}	
		
		}
		
		return people;
	}

}
