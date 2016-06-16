package cmu.reuse.liora.generate;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Parses and reasons over CSV files
 * @author liorafriedberg
 */
public class CSVReader {
	Scanner sc;
	
	/**
	 * to store parsed probabilities
	 */
	Map<String, Double> probabilities; 
	/**
	 * to store values for the "dice roll"
	 */
	Map<String, Map<Double, Double>> bounds; 
	/**
	 * all columns parsed from header
	 */
	List<Column> header;
	/**
	 * columns mapped to their indices in file
	 */
	Map<Column, Integer> indices;
	
	/**
	 * @param file		CSV file with agg data
	 */
	public CSVReader(File file) {
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println("invalid file");
		}
		probabilities = new HashMap<>();
		bounds = new HashMap<>();
		indices = new HashMap<>();
		header = getColumns();	
	}
	
	/**
	 * closes reader
	 */
	public void close() {
		sc.close();
	}
	
	
	public Map<Map<Column, String>, Map<Column, String>> findDepValues(Map<Integer, Column> indexToDep, Map<Integer, Column> indexToP) {
		Map<Map<Column, String>, Map<Column, String>> saveRows = new HashMap<>();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			Map<Column, String> deps = new HashMap<>();
			for (int i : indexToDep.keySet()) {
				deps.put(indexToDep.get(i), parts[i]);
			}
			Map<Column, String> vals = new HashMap<>();
			for (int i : indexToP.keySet()) {
				vals.put(indexToP.get(i), parts[i]);
			}
			saveRows.put(deps, vals);
		}
		return saveRows;
	}
	
	/**
	 * parse frequencies into percentages from file based on row
	 * @param indexToValue		indices to values to match on
	 * @param indexToPotential	indices to columns for the distribution
	 */
	public void parseFreqsDep(Map<Column, String> pVals) {
		probabilities.clear();
		bounds.clear();

				double totalFreq = 0.0;
				for (Column c : pVals.keySet()) {
					String label = c.label;
					double freq = Double.parseDouble(pVals.get(c));
					totalFreq = totalFreq + freq;
					probabilities.put(label, freq); //put in frequencies from row
				}
				for (String value : probabilities.keySet()) {
					double currFreq = probabilities.get(value);
					currFreq = currFreq / totalFreq; //divide freqs by total freq to get percentages
					probabilities.put(value, currFreq); 
				}							
				bound();
			
		
	}
	
	/**
	 * parse probabilities from file based on row
	 * @param indexToValue		indices to values to match on
	 * @param indexToPotential		indices to columns for the distribution
	 */
	public void parseProbsDep(Map<Column, String> pVals) {
		probabilities.clear(); 
		bounds.clear();

				for (Column c : pVals.keySet()) {
					String label = c.label; //matching label
					double prob = Double.parseDouble(pVals.get(c)); //probabilities directly in the row
					probabilities.put(label, prob);
				}
			
				bound();

		
	}
	
	/**
	 * @param indices 			indices to values to match on 
	 * @param valIndex		index of the column with the value to return
	 * @return				map from column indices to their values to the possible value for each line
	 */
	public Map<Map<Column, String>, String> findStaticValue(Map<Integer, Column> indexToDep, int valIndex) {
		Map<Map<Column, String>, String> depToVal = new HashMap<>();	
		while (sc.hasNextLine()) {
			Map<Column, String> depVals = new HashMap<>();
			String line = sc.nextLine();
			String[] parts = line.split(",");
			for (Integer i : indexToDep.keySet()) {
				depVals.put(indexToDep.get(i), parts[i]);
			}
			depToVal.put(depVals, parts[valIndex]);
		}
		return depToVal;
	}
	
	/**
	 * parse probabilities from file
	 * @param values					column with the possible data values
	 * @param probs						column with the percentages for the values
	 * @throws NumberFormatException	On parsing Double from String
	 */
	public void parseProbs(Column values, Column probs) throws NumberFormatException {
		probabilities.clear();
		bounds.clear();
		int partsVal = getColumnIndex(values);
		int partsProb = getColumnIndex(probs);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			String value = parts[partsVal];
			double prob = Double.parseDouble(parts[partsProb]);
			probabilities.put(value, prob);
		}
		bound();		
	}
	
	/**
	 * parse frequencies into percentages from file
	 * @param values					column with the possible data values
	 * @param probs						column with the frequencies for the values
	 * @throws NumberFormatException	On parsing Double from String
	 */
	public void parseFreqs(Column values, Column probs) throws NumberFormatException {
		probabilities.clear();
		bounds.clear(); 
		double totalFreq = 0.0;
		int partsVal = getColumnIndex(values);
		int partsProb = getColumnIndex(probs);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			String value = parts[partsVal];
			double freq = Double.parseDouble(parts[partsProb]);
			probabilities.put(value, freq);
			totalFreq = totalFreq + freq;
		}
		for (String value : probabilities.keySet()) {
			double currFreq = probabilities.get(value);
			currFreq = currFreq / totalFreq; //divide frequencies by total frequency to get percentages
			probabilities.put(value, currFreq);
		}
		bound();		
	}
	
	/**
	 * @return	data value from dice roll based on the parsed probability distribution
	 */
	public String calculate() { //roll the dice - the simulation
		double random = Math.random();
		for (String s : bounds.keySet()) {
			Map<Double, Double> compare = bounds.get(s);
			for (Double lower : compare.keySet()) {
				Double higher = compare.get(lower); //range for this value				
				if (random > lower && random <= higher) { //depending on range random num falls into
					return s; //return corresp data value
				}
			}
		}		
		System.out.println("System calculation error");
		return null;
	}
	
	/**
	 * @return		all values for the datatype concatenated and comma separated
	 */
	public String multiCalculate() { //pretty sure works fine
		double random = Math.random();
		String concat = "";
		for (String s : probabilities.keySet()) {
			double limit = probabilities.get(s);
			if (random <= limit) {
				concat = concat + s + ",";
			}
		}
		if (concat.equals("")) {
			return concat; //or could return null
		}
		else {
			concat = concat.substring(0, concat.length() - 1);
			return concat;
		}
		
	}
	
	/**
	 * set ranges on the data values for the dice roll
	 */
	public void bound() {
		double upperBound = 0.0;
		String lastStr = null;
		for (String s : probabilities.keySet()) {
			Map<Double, Double> secondaryMap = new HashMap<>();	
			double d = probabilities.get(s);
			lastStr = s;
			secondaryMap.put(upperBound, upperBound + d); //range for random number to corresp to this value
			upperBound = upperBound + d; //update to prep for next range			
			bounds.put(s, secondaryMap);
		}
		Map<Double, Double> secondaryMap = bounds.get(lastStr);
		for (Double d : secondaryMap.keySet()) {
			secondaryMap.put(d, 1.0);
		}
	}
	
	/**
	 * also fills in a map of column to index
	 * @return	all columns in the file
	 */
	public List<Column> getColumns() {
		List<Column> columns = new ArrayList<>();
		String firstLine = sc.nextLine(); //new reader every time call this, so first line
		String[] columnStrings = firstLine.split(",");	
		for (int i = 0; i < columnStrings.length; i++) {
			Column c = new Column();
			c.datatype = columnStrings[i];
			columns.add(c);	
			indices.put(c, i);
		}
		return columns;
	}
	
	/**
	 * @return	number of lines in file
	 */
	public int countLines() { 
		int count = 0;
		while (sc.hasNextLine()) {
			sc.nextLine();
			count++;
		}
		return count;
	}
	
	/**
	 * @param column	
	 * @return		index of column
	 */
	public int getColumnIndex(Column column) { 
		return indices.get(column);
	}
	
	/**
	 * @param column	column of data looking for
	 * @param count		number of lines in file
	 * @return			random value in column
	 */
	public String staticRead(Column column, int count) {
		String value = null;
		int randomInt = ThreadLocalRandom.current().nextInt(2, count + 1); //random line number
		count = 0;
		while (sc.hasNextLine()) {
			value = sc.nextLine();
			count++;
			if (count == randomInt) { //go to random line
				break;
			}
		}	
		int index = getColumnIndex(column);
		String[] tempParts = value.split(",");
		value = tempParts[index]; //that column in that line
		return value;
	}

}
