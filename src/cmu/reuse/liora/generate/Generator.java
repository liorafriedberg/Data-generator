package cmu.reuse.liora.generate;

import java.io.File;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Generates synthetic individual-level data from aggregate data
 * @author liorafriedberg
 */

public class Generator {
	//**note to reader: override menu calls in automenu, change back to menu for user interaction**	
	
	/*
	 * then go through however many tables (user input - insurance) and assign diseases 
	 * and prescriptions (and anything else) based on a distribution (or etc)
	 * this data inserted into different table using the person's unique id since maybe multiple
	 * records
	 * -------------------------------------------------
	 * 
	 *  see if this is what comes out - then maybe clear from command line - DELETE FROM CUSTOMERS; and view this way too
	 * 	insurance_member_id		VARCHAR		NOT NULL,
	 * 	grocery_member_id		VARCHAR		NOT NULL
	 * 	plan_number			BIGINT,	nn
	 * 	dob				DATE, 	nn
	 * 	address		VARCHAR,	nn
	 * 	credit_card		INT,	nn
	 * 	ad_keywords		VARCHAR,	nn
	 * 	coupon_code		BIGINT,	nn
	 * 	firstname		VARCHAR,	nn
	 * 	lastname		VARCHAR,	nn
	 * 	gender			VARCHAR,	nn
	 * 	ethnicity		VARCHAR,	nn
	 * 	ssn				BIGINT,	nn
	 * 	zip				SMALLINT,	nn
	 * 	id			BIGINT		NOT NULL,
	 * 	city		VARCHAR,	nn
	 * 	state		VARCHAR,	nn
	 * PRIMARY KEY(id)
	 * );
	 * ---------------------------------------
	 */
	
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
			for (Column c : values.keySet()) { //can we assume values.keySet same iteration per person every time?
				System.out.print(c.datatype + ": " + values.get(c) + " ");
			}
		}
		//now have rows to put in database!
		long finalTime = second - first;
		System.out.println("final time in millis: " + finalTime);
		
		write(rows);
		
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
	
	public static void write(List<Individual> rows) {
		Menu menu = new Menu();
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/microsim", "postgres", "microsim2016");
			c.setAutoCommit(false);
			
			 Iterator<Column> vs = rows.get(0).getValues().keySet().iterator();
			String create = "CREATE TABLE TOTAL(";
			Map<Column, String> values = rows.get(0).getValues();
			for (Column col : values.keySet()) {
				try {
					Integer.parseInt(values.get(col));
					create = create + col.datatype + " BIGINT " + "NOT NULL, "; //since said would only be ints / strings. but date? so okay or other auto way?
				} catch(NumberFormatException e) {
					create = create + col.datatype + " VARCHAR " + "NOT NULL, ";
				}
			}
			create = create + " PRIMARY KEY (id));";	
			stmt = c.prepareStatement(create);			
			stmt.executeUpdate();
			for (Individual i : rows) {
				String insert = "INSERT INTO TOTAL " +
								"VALUES (";
				
				Map<Column, String> iValues = i.getValues();
				for (String s : iValues.values()) {
					try {
						Integer.parseInt(s);
					insert = insert + " " + s + ","; 
					} catch(NumberFormatException e) {
						insert = insert + " '" + s + "', "; //doing this each time because need to add '' if a string and no user input
					}
				} 
				insert = insert.substring(0, insert.length() - 2); //cut off last , 
				insert = insert + ");"; 
				stmt = c.prepareStatement(insert);
				stmt.executeUpdate();
			}
			
			
			
			String[] tables = menu.getTables();
			for (int i = 0; i < tables.length; i++) {
				String table = tables[i];
				String subCreate = "CREATE TABLE " + table + "( ";
				List<Column> cols = menu.getTableCols(table);
				Map<Column, String> sampleVals = rows.get(0).getValues();
				for (Column col : cols) {
					try {
						Integer.parseInt(sampleVals.get(col));
						subCreate = subCreate + col.datatype + " BIGINT NOT NULL, ";
					} catch (NumberFormatException e) {
						subCreate = subCreate + col.datatype + " VARCHAR NOT NULL, ";
					}
				}
				subCreate = subCreate + table + "ID BIGINT);"; //this is the new table row number MAKE NON NULL AGAIN? 
				//insert it hardcoded below but then how change primary key?
				//subCreate = subCreate + " PRIMARY KEY (" + table + "ID));"; //hardcoded a new primary key
				// idea: just instead create it and then insert just the primary key, and then the from other table
				//but just then have to make key non-null

				stmt = c.prepareStatement(subCreate);
				stmt.executeUpdate();
				
				String subInsert = "INSERT INTO " + table + 
									" SELECT"; 
				for (Column col : cols) {
					subInsert = subInsert + " " + col.datatype + ",";
				}
				subInsert = subInsert.substring(0, subInsert.length() - 1); // HOW SEE IF SHOWING UP? - am on the last statement though
				subInsert = subInsert + " FROM TOTAL;";
				System.out.println("subInsert: " + subInsert);
				
				stmt = c.prepareStatement(subInsert);
				stmt.executeUpdate();				
			}
			stmt.close();
			c.commit();
			c.close();
		} catch(Exception e) {
			e.printStackTrace();
	         System.err.println(e.getClass().getName() + ": " + e.getMessage());
	         System.exit(0);
		}			
	}

}
