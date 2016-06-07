package cmu.reuse.liora.generate;

import java.io.File;
import java.util.concurrent.ThreadLocalRandom;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;

public class CSVReader {
	Scanner sc;
	Map<String, Double> probabilities; //to store parsed probabilities
	Map<String, Map<Double, Double>> bounds; //to store values for the "dice roll"
	List<Column> header;
	
	public CSVReader(File file) {	
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println("invalid file");
		}
		probabilities = new HashMap<>();
		bounds = new HashMap<>();
		header = getColumns();
	}
	
	public void close() {
		sc.close();
	}
	
	public void parseFreqsDep(Map<Integer, String> indexToValue, Map<Integer, Column> indexToPotential) {		
		probabilities.clear();
		bounds.clear();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			boolean found = true;
			for (int index : indexToValue.keySet()) {
				if (!parts[index].equals(indexToValue.get(index))) { //look for line in file
					found = false;
					break;
				}
			}
			if (found) {
				double totalFreq = 0.0;
				for (Integer index : indexToPotential.keySet()) {
					String label = indexToPotential.get(index).label;
					double freq = Double.parseDouble(parts[index]);
					totalFreq = totalFreq + freq;
					probabilities.put(label, freq); //put in frequencies from row
				}
				for (String value : probabilities.keySet()) {
					double currFreq = probabilities.get(value);
					currFreq = currFreq / totalFreq; //divide freqs by total freq to get percentages
					probabilities.put(value, currFreq); 
				}							
				bound();
				break;
			}
		}
	}
	
	public void parseProbsDep(Map<Integer, String> indexToValue, Map<Integer, Column> indexToPotential) {
		probabilities.clear(); 
		bounds.clear();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			boolean found = true;
			for (int index : indexToValue.keySet()) {
				if (!parts[index].equals(indexToValue.get(index))) { //look for the line in file
					found = false;
					break;
				}
			}
			if (found) {
				for (Integer index : indexToPotential.keySet()) {
					String label = indexToPotential.get(index).label;
					double prob = Double.parseDouble(parts[index]); //probabilities directly in the row
					probabilities.put(label, prob);
				}
				bound();
				break;
			}
		}
	}
	
	public String findStaticValue(Map<Integer, String> indexToValue, int valIndex) {
		String value = null;		
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			boolean found = true;
			for (int index : indexToValue.keySet()) {
				if (!parts[index].equals(indexToValue.get(index))) { //look for corresponding line in file
					found = false;
					break;
				}
			}
			if (found) {
				value = parts[valIndex]; //take value directly from file
				break;
			}
		}
		return value;
	}
	
	public void parseProbs(Column values, Column probs) throws NumberFormatException {
		probabilities.clear();
		bounds.clear();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			String value = parts[getColumnIndex(values)];
			double prob = Double.parseDouble(parts[getColumnIndex(probs)]);
			probabilities.put(value, prob);
		}
		bound();		
	}
	
	public void parseFreqs(Column values, Column probs) throws NumberFormatException {
		probabilities.clear();
		bounds.clear(); 
		double totalFreq = 0.0;
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			String value = parts[getColumnIndex(values)];
			double freq = Double.parseDouble(parts[getColumnIndex(probs)]);
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
	
	public void bound() {
		double upperBound = 0.0;
		for (String s : probabilities.keySet()) {
			Map<Double, Double> secondaryMap = new HashMap<>();	
			double d = probabilities.get(s);
			secondaryMap.put(upperBound, upperBound + d); //range for random number to corresp to this value
			upperBound = upperBound + d; //update to prep for next range
			bounds.put(s, secondaryMap);
		}
	}		
	
	public List<Column> getColumns() {
		List<Column> columns = new ArrayList<>();
		String firstLine = sc.nextLine(); //new reader every time call this, so first line
		String[] columnStrings = firstLine.split(",");	
		for (int i = 0; i < columnStrings.length; i++) {
			Column c = new Column();
			c.datatype = columnStrings[i];
			columns.add(c);	
		}
		return columns;
	}
	
	public int countLines() { 
		int count = 0;
		while (sc.hasNextLine()) {
			sc.nextLine();
			count++;
		}
		return count;
	}
	
	public int getColumnIndex(Column column) {
		int index = 0;
		for (Column col : header) {
			if (col.datatype.equals(column.datatype)) {
				index = header.indexOf(col); //spot in list should correspond to this index
				break;
			}
		}
		return index;
	}
	
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
