package cmu.reuse.liora.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
/**
 * interacts with user for input
 * @author liorafriedberg
 */
public class Menu {
	Scanner userInput;
	
	public Menu() {
		userInput = new Scanner(System.in);
	}
	
	/**
	 * @return		all files needed for simulation, assumed to be in the project-level directory
	 */
	public List<File> getAllFiles() {
		System.out.println("Please enter all filenames for files you would like to "
				+ "use, comma separated.");
		String[] fileParts;
		List<File> files = new ArrayList<>();
		boolean wait = true;
		String input = null;		
		while (wait) {
			boolean mistake = false;
			input = userInput.next();
			if (input != null) {
				fileParts = input.split(",");
				for (int i = 0; i < fileParts.length; i++) {
					File file = new File(fileParts[i]);
					if (!file.exists() || !file.canRead() || !file.isFile() || file.isDirectory()) {
						System.out.println("Invalid file entered. Please try again.");
						mistake = true;
					}
					else {
						files.add(file);					
					}
				}
				if (!mistake) {
					wait = false;	
				} else {
					files.clear();
				}
			}
		}
		return files;
	}
	
	/**
	 * @param columns		all columns from files
	 * @return				column with the probability distribution
	 */
	public Column getColumn(List<Column> columns) { 
		String input = null;
		boolean wait = true;
		Column probColumn = null;
		while (wait) {
			input = userInput.next();
			if (input != null) {
				for (Column column : columns) {
					if (column.datatype.equals(input)) {
						probColumn = column;
						wait = false;
						break;
					}
				}
				if (wait) { 
					System.out.println("Column name did not match any column in given file.");
				}
			}
		}
		return probColumn;
	}
	
	/**
	 * @param currentColumns		all columns from files
	 * @return						columns of the datatypes to generate, in order
	 */
	public List<Column> getFinalColumns(List<Column> currentColumns) {
		List<Column> finalColumns = new ArrayList<>();
		System.out.println("I will now print all columns. Please rewrite the list with all columns"
				+ " to generate, in the desired order, comma separated.");
		for (Column column : currentColumns) {
			System.out.println(column.datatype);
		}
			boolean wait = true;
			String input = null;
			while (wait) {
				input = userInput.next();
				if (input != null) {
					String[] tempColArr = input.split(",");
					boolean mistake = false;
					for (int i = 0; i < tempColArr.length; i++) {
						String columnStr = tempColArr[i];
						boolean found = false;
						for (Column column : currentColumns) {
							if (column.datatype.equals(columnStr)) {
								finalColumns.add(column);
								found = true;
								break;
							}
						}
						if (!found) {
							mistake = true;
							break;
						}
					}
					if (mistake) {
						System.out.println("A column name did not match. Please try again.");						
					finalColumns.clear();
					} else {
						wait = false;
					}
				}
			}		
		return finalColumns;
	}
	
	/**
	 * @return		number of individuals to simulate
	 */
	public long getNum() {
		boolean wait = true;
		long num = 0;
		while (wait) {
			num = userInput.nextLong();
			if (num > 0) {
				wait = false;
			}
		}
		return num;
	}
	
	
	/**
	 * @param column		
	 * @return			source for input column
	 */
	public Source getSource(Column column) { 
		Source source = null;
		System.out.println("For datatype " + column.datatype + ", please choose a source");
		System.out.println("For generation from probability distributions, press 1");
		System.out.println("For generation from probability distributions with previous value"
				+ " dependencies, press 2");
		System.out.println("For static generation with previous value dependencies, press 3");
		System.out.println("For random generation, press 4");
		System.out.println("For generation from probability distributions, with "
				+ "file choice dependencies on previous values, press 5");
		boolean wait = true;
		int input = 0;
		while (wait) {
			input = userInput.nextInt();
			if (input != 0) {
			wait = false;
				if (input == 1) {
					source = Source.PROBS;
				} else if (input == 2) {
					source = Source.DEP_PROBS;
				} else if (input == 3) {
					source = Source.DEP_STATIC;
				} else if (input == 4) {
					source = Source.RANDOM;
				} else if (input == 5) {
					source = Source.DEP_PROBS_FILE;
				} else {
					wait = true;
					System.out.println("invalid option chosen. Please enter"
						+ " 1, 2, 3, 4, or 5."); 
				}
			}
		}
		return source;
	}
	
	/**
	 * @return		source within the randomized source options
	 */
	public Source getRandom() {
		Source source = null;
		System.out.println("Please choose a form of randomization, by typing 'UUID', 'value_from_list', "
				+ "'offset', or 'number'");
		boolean wait = true;
		String input = null;
		while (wait) {
			input = userInput.next();
			if (input != null) {
				wait = false;
				if (input.equals("UUID")) {
					source = Source.RAND_UUID;
				} else if (input.equals("value_from_list")) {
					source = Source.RAND_LINE;
				} else if (input.equals("number")) {
					source = Source.RAND_NUMBER;
				} else if (input.equals("offset")) {
					source = Source.RAND_OFFSET;
				} else {
					wait = true;
					System.out.println("Please enter 'UUID', 'value from list', or 'number'");
				}
			}
		}
		return source;
	}
	
	/**
	 * @return		file from user
	 */
	public File getFile() {
		System.out.println("Please enter filename for data generation");
		boolean wait = true;
		String input = null;
		File file = null;
		while (wait) {
			input = userInput.next();
			if (input != null) {			
				file = new File(input);
				if (!file.exists() || !file.canRead() || !file.isFile() 
						|| file.isDirectory()) {
					System.out.println("Invalid file entered. Please try again.");
				}
				else wait = false;
			}
		}
		return file;
	}
	
	/**
	 * @return		percentage or frequency data format
	 */
	public int getDataFormat() {
		System.out.println("Please enter the data format. For percentages, press 1. For frequencies, press 2.");
		int input = 0;
		boolean wait = true;
		while (wait) {
			input = userInput.nextInt();
			if (input == 1 || input == 2) {
				wait = false;
			}
			else System.out.println("Invalid number entered. Please enter 1 or 2.");
		}
		return input;
	}
	
	/**
	 * @return		a map from value to file for dependent datatype generation
	 */
	public Map<String, File> getFileDeps() {
		System.out.println("Please enter all value-file pairs. Format value:file and comma"
				+ "separated");
		Map<String, File> valueToFile = new HashMap<>();
		String input = null;
		boolean wait = true;
		while (wait) {
			boolean mistake = false;
			input = userInput.next();
			if (input != null) {
				String[] pairs = input.split(",");
				for (int i = 0; i < pairs.length; i++) {
					String pair = pairs[i];
					String[] split = pair.split(":");
					String value = split[0];
					File file = new File(split[1]);
					if (!file.exists() || !file.canRead() || !file.isFile() || file.isDirectory()) {
						mistake = true;
						System.out.println("Invalid file entered. Please try again.");
					}
					else {
						valueToFile.put(value, file);						
					}
				}
				if (!mistake) {
					wait = false;
				} else {
					valueToFile.clear();
				}
			}
		}
		return valueToFile;
	}
	
	/**
	 * @param column		
	 * @param columns		all columns
	 * @return				the columns on which the input column's value depends
	 */
	public List<Column> getDependencies(Column column, List<Column> columns) {
		System.out.println("For datatype " + column.datatype + ", please list all other datatypes"
				+ " on which its generation depends, comma separated.");
		return getColumnRange(column, columns);
	}
	
	/**
	 * @param column
	 * @param columns		all columns
	 * @return				the columns with the potential values for a datatype and their distributions
	 */
	public List<Column> getPotentialValues(Column column, List<Column> columns) {
		System.out.println("For datatype " + column.datatype + ", please list the names of columns"
				+ " with the potential values and probability distributions, comma separated.");
		return getColumnRange(column, columns);
	}
	
	/**
	 * @param column		
	 * @param columns
	 * @return		list of columns from user input
	 */
	public List<Column> getColumnRange(Column column, List<Column> columns) {		
		List<Column> range = new ArrayList<>();
		String input = null;
		boolean wait = true;
		while (wait) {
			boolean mistake = false;			
			input = userInput.next();
			if (input != null) {
				String[] parts = input.split(",");
				for (int i = 0; i < parts.length; i++) {
					boolean found = false;
					String arrColumn = parts[i];
					for (Column col : columns) {
						if (col.datatype.equals(arrColumn)) {
							range.add(col);
							found = true;
							break;
						}
					}
					if (!found) {
						mistake = true;
					}
				}
				if (mistake) {
					System.out.println("Entered invalid column. Please try again.");
					range.clear();
				} else {
					wait = false;
				}
			}
		}
		return range; 
	}
	
	/**
	 * assigns labels to columns to replace the datatype name with a user-friendly name
	 * @param potentialValues		columns with the potential values for a datatype and their distributions
	 */
	public void getLabels(List<Column> potentialValues) {
		System.out.println("I will output all datatype values. Please input the friendly label for the column.");
		for (Column column : potentialValues) {
			System.out.println(column.datatype);
			boolean wait = true;
			String input = null;
			while (wait) {
				input = userInput.next();
				if (input != null) {
					column.label = input;
					wait = false;
				}
			}
		}
	}

}
