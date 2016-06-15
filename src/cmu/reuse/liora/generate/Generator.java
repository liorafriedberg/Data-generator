package cmu.reuse.liora.generate;

import java.io.File;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
			c.source = Source.SEQUENTIAL_LINE; //fine
			person.setValue(c, i + 1 + "");
			people.add(person);
		}
		
		for (Column column : columns) {
			Source source = menu.getSource(column); //ask user for how want to do it - in order
			Simulator sim;
			if (source.equals(Source.PROBS)) {
				column.source = Source.PROBS;
				sim = new ProbSim();
				sim.simulate(menu, column, people, allColumns);
			} 
			else if (source.equals(Source.DEP_PROBS_FILE)) {
				column.source = Source.DEP_PROBS_FILE;
				sim = new DepProbFileSim();
				sim.simulate(menu, column, people, allColumns);
			}
			else if (source.equals(Source.DEP_PROBS)) {
				column.source = Source.DEP_PROBS;
				sim = new DepProbSim();
				sim.simulate(menu, column, people, allColumns);
			}			
			else if (source.equals(Source.DEP_STATIC)) { 
				column.source = Source.DEP_STATIC;
				sim = new DepStaticSim();
				sim.simulate(menu, column, people, allColumns);
			} 
			else if (source.equals(Source.DEP_DATE)) {
				column.source = Source.DEP_DATE;
				sim = new DepDateSim();
				sim.simulate(menu, column, people, allColumns);
			}
			else if (source.equals(Source.RANDOM)) {
				column.source = Source.RANDOM;
				sim = new RandomSim();
				sim.simulate(menu, column, people, allColumns);
			} else if (source.equals(Source.MULTI_VALUE)) {
				column.source = Source.MULTI_VALUE; //can just keep for this one
				sim = new MultiValueSim();
				sim.simulate(menu, column, people, allColumns);
			}
		
		}
		
		return people;
	}
	
	//comment out for part working right now
	/**
	 * writes new tables related to current tables in db
	 * @param rows		rows generated
	 * @param c			connection to db
	 * @param tables	all new tables in db
	 */
	public static void addDbCols(List<Individual> rows, Connection c, String[] tables) {
		Menu menu = new Menu();
		PreparedStatement stmt = null;
		try {
			for (int i = 0; i < tables.length; i++) {
				String table = tables[i];
				boolean choice = menu.getChoice(table); //insurance
				if (choice) {
					//medical records
					String[] addTables = menu.getTables();
					for (int j = 0; j < addTables.length; j++) {	
						String newTable = addTables[j];
						List<Column> newTableCols = menu.getTableCols(newTable); //disease, prescription
						Column mv = menu.getColumn(newTableCols); //or could go through and find without ui
						String key = menu.getKey(newTable); //need?
						String mvType = "";
						String addCreate = "CREATE TABLE " + newTable + "( ";
						
						for (Column col : newTableCols) {
							
							String val = rows.get(0).getValues().get(col);
							String type = "";
							try {
								Integer.parseInt(val);
								type = "BIGINT";
							} catch(NumberFormatException e) {
								type = "VARCHAR";
							}
							if (!col.source.equals(Source.MULTI_VALUE)) {
								addCreate = addCreate + col.datatype + " " + type + " NOT NULL, ";
							}
							else {
								mvType = type;
							}
						}						
						
						addCreate = addCreate + mv.datatype + " " + mvType + ");"; //so matches
						//order below

				System.out.println("addCreate: " + addCreate);
				stmt = c.prepareStatement(addCreate);
				stmt.executeUpdate();			
				System.out.println("Please input the multivalue column to assign");
				//assume is only one mv ?? ****	if not then do with some kind of list
				
				for (Individual person : rows) {
					Map<Column, String> vals = person.getValues();					
					String[] parts = vals.get(mv).split(",");
					for (int k = 0; k < parts.length; k++) { //clean everything up
						String addInsert = "INSERT INTO " + newTable + " VALUES (";	
							for (Column colTwo : newTableCols) {
								if (!colTwo.source.equals(Source.MULTI_VALUE)) {
									addInsert = addInsert + vals.get(colTwo) + ", ";
								}
							}
							addInsert = addInsert + parts[k] + ");";
				
							System.out.println("addInsert: " + addInsert);
							stmt = c.prepareStatement(addInsert);
							stmt.executeUpdate();	
					}
					 					
				}
				System.out.println("Please input the value to join on"); // multiple to join on? **
				Column joinC = menu.getColumn(newTableCols);
			
			//is above order okay?? then just go through and assign prescription if need,
			//and can "join" (id from insurance_member_id?) ****
					
			//OR DIFF FROM ABOVE: -------------
			//insert into medicalrecords insurance_member_id (joiners) how many? 
					//select insurance_member_id etc
					//from insurance
					//where; 				
					//(or
					//SELECT insurance_member_id
				//	INTO medicalrecords
				//	FROM insurance;)
				
					//if do this order: how handle insert multiple times for later mult ds? 
					
					//then can join - so get id? 
					
					// select insurance.id
					// into medicalrecords
					//	from insurance
					// left join medicalrecords
					// on insurance.insurance_member_id=medicalrecords.insurance_member_id
			
			//get person id and insert disease - but how duplicate rows? one by one? 
			
			//update medicalrecords
			//set disease=first
			//where record ? 		-- how assign? 
			
			//set disease 2
			//where record ?
					
			//------------
					//are the cols only joiners and multivalue? **
				    //what table look like? ****
					
					//insurance_member_id   disease		prescription
					//a						cancer		
					//a						diabetes
					
					//how assign prescription? static from disease so already assigned? ****				
										
				}
			}
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
	
	/**
	 * writes new tables to break up total data in db
	 * @param rows		the data generated
	 * @param c			Connection to db
	 */
	public static void subTables(List<Individual> rows, Connection c) {
		Menu menu = new Menu();
		PreparedStatement stmt = null;
		try {
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
				subCreate = subCreate.substring(0, subCreate.length() - 2) + ");";
					//don't need primary or foreign keys in these tables

				stmt = c.prepareStatement(subCreate);
				stmt.executeUpdate();
					
				String subInsert = "INSERT INTO " + table + 
										" SELECT"; 
				for (Column col : cols) {
					subInsert = subInsert + " " + col.datatype + ",";
				}
				subInsert = subInsert.substring(0, subInsert.length() - 1); 
				subInsert = subInsert + " FROM TOTAL;";
					
				stmt = c.prepareStatement(subInsert);
				stmt.executeUpdate();				
			}
			stmt.close();
			addDbCols(rows, c, tables);
		}	catch(Exception e) {
				e.printStackTrace();
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
				System.exit(0);
			}				
	} 			

	//may extract as own class
	/**
	 * writes generated data to database
	 * @param rows		all rows generated
	 */
	public static void write(List<Individual> rows) {
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/microsim", "postgres", "microsim2016");
			c.setAutoCommit(false);
			
			String create = "CREATE TABLE TOTAL("; //but don't want to recreate now so comment out or drop in cl
			Map<Column, String> values = rows.get(0).getValues();
			for (Column col : values.keySet()) {
				if (!col.source.equals(Source.MULTI_VALUE)) {
					try {
						Integer.parseInt(values.get(col));
						create = create + col.datatype + " BIGINT " + "NOT NULL, ";
					} catch(NumberFormatException e) {
						create = create + col.datatype + " VARCHAR " + "NOT NULL, ";
					}
				}
			}
			create = create + " PRIMARY KEY (id));";
			System.out.println("create: " + create); //check
			stmt = c.prepareStatement(create);			
			stmt.executeUpdate();
			for (Individual i : rows) {
				String insert = "INSERT INTO TOTAL " +
								"VALUES (";
				
				Map<Column, String> iValues = i.getValues();
				for (Column col : iValues.keySet()) {
					if (!col.source.equals(Source.MULTI_VALUE)) {
						String s = iValues.get(col);
						try {
							Integer.parseInt(s);
							insert = insert + " " + s + ","; 
						} catch(NumberFormatException e) {
						insert = insert + " '" + s + "', "; //doing this each time because need to add '' if a string and no user input
						}
					}
				} 
				insert = insert.substring(0, insert.length() - 2); //cut off last , 
				insert = insert + ");"; 
				stmt = c.prepareStatement(insert);
				stmt.executeUpdate();			
			}
			stmt.close();
			subTables(rows, c);
		} catch(Exception e) {
			e.printStackTrace();
	         System.err.println(e.getClass().getName() + ": " + e.getMessage());
	         System.exit(0);
		}				
	}						
}
