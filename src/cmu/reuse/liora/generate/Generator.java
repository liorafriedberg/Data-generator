package cmu.reuse.liora.generate;

import java.io.File;
import java.io.IOException;
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
	/**
	 * starts generation
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long first = System.currentTimeMillis();
		List<Column> allColumns = new ArrayList<>();
		List<Individual> rows = generate(allColumns);
		long second = System.currentTimeMillis();
		for (Individual i : rows) { //to test
			System.out.println("");
			Map<Column, String> values = i.getValues();
			for (Column c : values.keySet()) { //can we assume values.keySet same iteration per person every time?
				System.out.print(c.datatype + ": " + values.get(c) + " ");
			}
			Map<String, String> map = i.mvTwo; //del - and change menu back
			for (String s : map.keySet()) {
				System.out.print("disease: " + s + " ps: " + map.get(s));
			}
		}
		//now have rows to put in database!
		long finalTime = second - first;
		System.out.println("final time in millis: " + finalTime);
	write(rows, allColumns);
		
	}

	/**
	 * @return		the generated individual data
	 * @throws IOException 
	 */
	public static List<Individual> generate(List<Column> allColumns) throws IOException {		
		Menu menu = new AutoMenu(); //AutoMenu to automate
		List<File> files = menu.getAllFiles(); //get all relevant files from user		
		
		List<Column> columns = new ArrayList<>();
		for (File file : files) {
			CSVReader reader = new CSVReader(file);
			columns.addAll(reader.header);
			allColumns.addAll(reader.header);
			reader.close();
		}
		//columns now has all the possible columns
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
			} else if (source.equals(Source.MULTI_VALUE_2)) {
				column.source = Source.MULTI_VALUE_2;
				sim = new MultiValueTwoSim();
				sim.simulate(menu, column, people, allColumns);
			}
		
		}
		
		return people;
	}
	
	/**
	 * writes new tables related to current tables in db
	 * @param rows		rows generated
	 * @param c			connection to db
	 * @param tables	all new tables in db
	 * @throws IOException 
	 */
	public static void addDbCols(List<Individual> rows, Connection c, String[] tables, List<Column> allColumns) throws IOException {
		Menu menu = new AutoMenu();
		PreparedStatement stmt = null;
		try {
			for (int i = 0; i < tables.length; i++) {
				String table = tables[i];
				boolean choice = menu.getChoice(table); //insurance
				if (choice) {
					//medical records
					String[] addTables = menu.getMvTables(table);
					for (int j = 0; j < addTables.length; j++) {	
						String newTable = addTables[j];

						List<Column> newTableCols = menu.getTableCols(newTable, allColumns); //disease, prescription
						Column mv = null;	
						Column mv2 = null;
						for (Column col : newTableCols) {
							Source s = menu.getSource(col);
							if (s.equals(Source.MULTI_VALUE)) {
								mv = col;
							} else if (s.equals(Source.MULTI_VALUE_2)) {
								mv2 = col;
							}
						}
						String mvType = "";
						String mv2Type = "";
						String addCreate = "CREATE TABLE " + newTable + "( ";
						
						for (Column col : newTableCols) {
							Source s = menu.getSource(col);
							String val = rows.get(0).getValues().get(col);
							String type = "";
							try {
								Integer.parseInt(val);
								type = "BIGINT";
							} catch(NumberFormatException e) {
								type = "VARCHAR";
							}
							if (!s.equals(Source.MULTI_VALUE) && !s.equals(Source.MULTI_VALUE_2)) {
								addCreate = addCreate + col.datatype + " " + type + " NOT NULL, ";
							}
							else if (s.equals(Source.MULTI_VALUE)) {
								mvType = type;
							} else {
								mv2Type = type;
							}
						}						
						
						addCreate = addCreate + mv.datatype + " " + mvType + 
								", " + mv2.datatype + " " + mv2Type + ");"; //so matches
						//order below

				stmt = c.prepareStatement(addCreate);
				stmt.executeUpdate();			
				String all = "";
				String some = "";
				String none = "";
				for (Column colTwo : newTableCols) {
					Source s = menu.getSource(colTwo);
					if (!s.equals(Source.MULTI_VALUE) && !s.equals(Source.MULTI_VALUE_2)) {
						none = none + colTwo.datatype + ",";
					}
				}
				none = none.substring(0, none.length() - 1);
				some = none + "," + mv.datatype;
				all = some + "," + mv2.datatype;

				for (Individual person : rows) {
					Map<String, String> mvMap = person.mvTwo;
					Map<Column, String> vals = person.getValues();			
					if (mvMap.size() > 0) {
					for (String mvS : mvMap.keySet()) {
						String mv2S = mvMap.get(mvS);
						if (mv2S.length() > 0) {
						String[] parts = mv2S.split(",");
						for (int l = 0; l < parts.length; l++) {
							String addInsert = "INSERT INTO " + newTable + " (" + all + ") " + " VALUES (";	 
							for (Column colTwo : newTableCols) {
								Source s = menu.getSource(colTwo);
								if (!s.equals(Source.MULTI_VALUE) && !s.equals(Source.MULTI_VALUE_2)) {
									String check = vals.get(colTwo);
									try {
										Integer.parseInt(check);
										addInsert = addInsert + check + ", ";
									} catch(NumberFormatException e) {
									addInsert = addInsert + "'" + check + "', ";
									}
								}
							}
							try {
								Integer.parseInt(mvS);
								addInsert = addInsert + mvS + ", ";
							} catch(NumberFormatException e) {
								addInsert = addInsert + "'" + mvS + "', ";
							}
							try {
								Integer.parseInt(parts[l]);
								addInsert = addInsert + parts[l] + ");";
							} catch(NumberFormatException e) {
								addInsert = addInsert + "'" + parts[l] + "');";
							}
							stmt = c.prepareStatement(addInsert);
							stmt.executeUpdate();	
						}
						} else {
							String addInsert = "INSERT INTO " + newTable + " (" + some + ") " + " VALUES (";
							for (Column colTwo : newTableCols) {
								Source s = menu.getSource(colTwo);
								if (!s.equals(Source.MULTI_VALUE) && !s.equals(Source.MULTI_VALUE_2)) {
									String check = vals.get(colTwo);
									try {
										Integer.parseInt(check);
										addInsert = addInsert + check + ", ";
									} catch(NumberFormatException e) {
										addInsert = addInsert + "'" + check + "', ";
										}
								}
							}
							System.out.println("what is disease: " + mvS);
							try {
								Integer.parseInt(mvS);
								addInsert = addInsert + mvS + ");";
							} catch(NumberFormatException e) {
								addInsert = addInsert + "'" + mvS + "');";
							}						
							stmt = c.prepareStatement(addInsert);
							stmt.executeUpdate();
						}
					}
					}
					else {
						String addInsert = "INSERT INTO " + newTable + " (" + none + ") " + " VALUES (";
						for (Column colTwo : newTableCols) {
							Source s = menu.getSource(colTwo);
							if (!s.equals(Source.MULTI_VALUE) && !s.equals(Source.MULTI_VALUE_2)) {
								String check = vals.get(colTwo);
								try {
									Integer.parseInt(check);
									addInsert = addInsert + check + ", ";
								} catch(NumberFormatException e) {
									addInsert = addInsert + "'" + check + "', ";
									}
							}
						}
						addInsert = addInsert.substring(0, addInsert.length() - 2) + ");";
						stmt = c.prepareStatement(addInsert);
						stmt.executeUpdate();
					}
							
					}
					 					
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
	
	public static String writeIndividual(Individual i, String table, List<Column> cols) throws IOException {
		Menu menu = new AutoMenu();
		String insert = "INSERT INTO " + table + " VALUES( ";
		Map<Column, String> iVals = i.getValues();
		for (Column col : cols) {
			Source sc = menu.getSource(col);
			if (!sc.equals(Source.MULTI_VALUE) && !sc.equals(Source.MULTI_VALUE_2)) {
			String s = iVals.get(col);
			try {
				Integer.parseInt(s);
				insert = insert + " " + s + ",";
			} catch(NumberFormatException e) {
				insert = insert + " '" + s + "', ";
			}
			}
		}
		insert = insert.substring(0, insert.length() - 2) + ");";
		return insert;
	}
	
	/**
	 * writes new tables to break up total data in db
	 * @param rows		the data generated
	 * @param c			Connection to db
	 * @throws IOException 
	 */
	public static void subTables(List<Individual> rows, Connection c, List<Column> allColumns) throws IOException {
		Menu menu = new AutoMenu();
		PreparedStatement stmt = null;
		try {
			String[] tables = menu.getTables();
			for (int i = 0; i < tables.length; i++) {
				String table = tables[i];
				String subCreate = "CREATE TABLE " + table + "( ";
				List<Column> cols = menu.getTableCols(table, allColumns); //assume they'll say id
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
			
				double prob = menu.getProbability(table);
				for (Individual person : rows) {
					double random = Math.random();
						if (random <= prob) { //put in both
							String insert = writeIndividual(person, tables[i], cols);
							stmt = c.prepareStatement(insert);
							stmt.executeUpdate();
						} 
				}		
			}			
			stmt.close();
			addDbCols(rows, c, tables, allColumns);
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
	public static void write(List<Individual> rows, List<Column> allColumns) {
		Connection c = null;
		PreparedStatement stmt = null;
		try {
			Class.forName("org.postgresql.Driver");
			c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/microsim", "postgres", "microsim2016");
			c.setAutoCommit(false);
		
			String create = "CREATE TABLE TOTAL(";
			Map<Column, String> values = rows.get(0).getValues();
			for (Column col : values.keySet()) {
				Source s = col.source;
				if (!s.equals(Source.MULTI_VALUE) && !s.equals(Source.MULTI_VALUE_2)) {
					try {
						Integer.parseInt(values.get(col));
						create = create + col.datatype + " BIGINT " + "NOT NULL, ";
					} catch(NumberFormatException e) {
						create = create + col.datatype + " VARCHAR " + "NOT NULL, ";
					}
				}
			}
			create = create + " PRIMARY KEY(id));"; 
			stmt = c.prepareStatement(create);			
			stmt.executeUpdate();
			for (Individual i : rows) {
				String insert = "INSERT INTO TOTAL " +
								"VALUES (";
				
				Map<Column, String> iValues = i.getValues();
				for (Column col : iValues.keySet()) {
					Source sc = col.source;
					if (!sc.equals(Source.MULTI_VALUE) && !sc.equals(Source.MULTI_VALUE_2)) {
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
			subTables(rows, c, allColumns);
		} catch(Exception e) {
			e.printStackTrace();
	         System.err.println(e.getClass().getName() + ": " + e.getMessage());
	         System.exit(0);
		}				
	}						
}
