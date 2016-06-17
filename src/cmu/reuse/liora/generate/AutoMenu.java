package cmu.reuse.liora.generate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * extension of menu for automated user input to facilitate testing
 * @author liorafriedberg
 */
public class AutoMenu extends Menu {
	Properties prop;
	FileInputStream in;
	public AutoMenu() throws IOException {		
		prop = new Properties();
		in = new FileInputStream("config.properties");
		prop.load(in);
	}

	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getAllFiles()
	 */
	@Override
	public List<File> getAllFiles() {
		String files = prop.getProperty("files");
		String[] fileParts = files.split(",");
		List<File> fList = new ArrayList<>();
		for (int i = 0; i < fileParts.length; i++) {
			File file = new File(fileParts[i]);
			if (!file.exists() || !file.canRead() || !file.isFile() || file.isDirectory()) {
				throw new IllegalArgumentException("Invalid file chosen in input");
			} else {
				fList.add(file);			
			}
		}
		return fList;
	}
	

	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getProbColumn(java.util.List, cmu.reuse.liora.generate.Column)
	 */
	@Override
	public Column getProbColumn(List<Column> columns, Column c) {
		String prob = prop.getProperty(c.datatype + "ProbColumn");
		Column probColumn = null;
		for (Column column : columns) {
			if (column.datatype.equals(prob)) {
				probColumn = column;
				break;
			}
		}
		if (probColumn == null) {
			throw new IllegalArgumentException("Invalid probability column in input");
		}
		return probColumn;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getValueColumn(java.util.List, cmu.reuse.liora.generate.Column)
	 */
	@Override
	public Column getValueColumn(List<Column> columns, Column c) {
		String val = prop.getProperty(c.datatype + "ValueColumn");
		Column valColumn = null;
		for (Column column : columns) {
			if (column.datatype.equals(val)) {
				valColumn = column;
				break;
			}
		}
		if (valColumn == null) {
			throw new IllegalArgumentException("Invalid value column in input");
		}
		return valColumn;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getDepColumn(java.util.List, cmu.reuse.liora.generate.Column)
	 */
	@Override
	public Column getDepColumn(List<Column> columns, Column c) {
		String dep = prop.getProperty(c.datatype + "DepColumn");
		Column depColumn = null;
		for (Column column : columns) {
			if (column.datatype.equals(dep)) {
				depColumn = column;
				break;
			}
		}
		if (depColumn == null) {
			throw new IllegalArgumentException("Invalid dependency column in input");
		}
		return depColumn;
	}
	
	public Column getMvColumn(List<Column> columns) { //need?
		String mv = prop.getProperty("MvColumn");
		Column mvColumn = null;
		for (Column column : columns) {
			if (column.datatype.equals(mv)) {
				mvColumn = column;
				break;
			}
		}
		if (mvColumn == null) {
			throw new IllegalArgumentException("Invalid mv2 column in input");
		}
		return mvColumn;
	}
	
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getFinalColumns(java.util.List)
	 */
	@Override
	public List<Column> getFinalColumns(List<Column> currentColumns) {
		String cols = prop.getProperty("finalColumns");
		List<Column> finalColumns = new ArrayList<>();
		String[] tempColArr = cols.split(",");
		for (int i = 0; i < tempColArr.length; i++) {
			String columnStr = tempColArr[i];
			Column column = new Column(columnStr);
					finalColumns.add(column);
			}
		return finalColumns;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getNumRows()
	 */
	@Override
	public long getNum() {
		String people = prop.getProperty("numPeople");
		try {
			long num = Long.parseLong(people);
			return num;
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Invalid number of individuals in input");
		}
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getOffset(cmu.reuse.liora.generate.Column)
	 */
	@Override
	public long getOffset(Column c) {
		String offset = prop.getProperty(c.datatype + "Offset");
		try {
			long num = Long.parseLong(offset);
			return num;
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Incorrect number for offset in input");
		}
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getBound(cmu.reuse.liora.generate.Column)
	 */
	@Override
	public int getBound(Column c) {
		String bound = prop.getProperty(c.datatype + "Bound");
		try {
			int num = Integer.parseInt(bound);
			return num;
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Invalid bound in input");
		}
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getSource(cmu.reuse.liora.generate.Column)
	 */
	@Override
	public Source getSource(Column column) {
		String sourceStr = prop.getProperty(column.datatype + "Source");
		Source source;
		if (sourceStr.equals("1")) {
			source = Source.PROBS;
		} else if (sourceStr.equals("2")) {
			source = Source.DEP_PROBS;
		} else if (sourceStr.equals("3")) {
			source = Source.DEP_STATIC;
		} else if (sourceStr.equals("4")) {
			source = Source.RANDOM;
		} else if (sourceStr.equals("5")) {
			source = Source.DEP_PROBS_FILE;
		} else if (sourceStr.equals("6")) {
			source = Source.DEP_DATE;
		} else if (sourceStr.equals("7")) {
			source = Source.MULTI_VALUE;
		} else if (sourceStr.equals("8")) {
			source = Source.MULTI_VALUE_2;
		} else {
			throw new IllegalArgumentException("Invalid source chosen in input");
		}
		return source;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getRandom()
	 */
	@Override
	public Source getRandom(Column c) {
		String random = prop.getProperty(c.datatype + "Random");
		Source source;
		if (random.equals("UUID")) {
			source = Source.RAND_UUID;
		} else if (random.equals("rand_value_from_list")) {
			source = Source.RAND_LINE;
		} else if (random.equals("number")) {
			source = Source.RAND_NUMBER;
		} else if (random.equals("offset_num")) {
			source = Source.RAND_OFFSET_NUM;
		} else if (random.equals("seq_value")) {
			source = Source.SEQUENTIAL_LINE;
		}  else {
			throw new IllegalArgumentException("Invalid random source in input");
		}
		return source;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getFile()
	 */
	@Override
	public File getFile(Column c) {
		String fileStr = prop.getProperty(c.datatype + "File");
		File file = new File(fileStr);
		if (!file.exists() || !file.canRead() || !file.isFile() 
				|| file.isDirectory()) {
			throw new IllegalArgumentException("Invalid file in input");
		}
		return file;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getDataFormat()
	 */
	@Override
	public int getDataFormat(Column c) {
		String format = prop.getProperty(c.datatype + "Format");
		try {
			int num = Integer.parseInt(format);
			if (num != 1 && num != 2) {
				throw new IllegalArgumentException("Incorrect data format input");
			}
			return num;
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Incorrect data format input");
		}
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getFileDeps()
	 */
	@Override
	public Map<String, File> getFileDeps(Column c) {
		String pairsStr = prop.getProperty(c.datatype + "Pairs");
		Map<String, File> valueToFile = new HashMap<>();
		String[] pairs = pairsStr.split(",");
		for (int i = 0; i < pairs.length; i++) {
			String pair = pairs[i];
			String[] split = pair.split(":");
			String value = split[0];
			File file = new File(split[1]);
			if (!file.exists() || !file.canRead() || !file.isFile() || file.isDirectory()) {
				throw new IllegalArgumentException("Invalid file in input");
			} else {
				valueToFile.put(value, file);
			}
		}	
		return valueToFile;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getDependencies(cmu.reuse.liora.generate.Column, java.util.List)
	 */
	@Override
	public List<Column> getDependencies(Column column, List<Column> columns) {
		String deps = prop.getProperty(column.datatype + "Deps");
		List<Column> range = new ArrayList<>();
		String[] parts = deps.split(",");
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
				throw new IllegalArgumentException("Invalid column in input");
			}
		}
		return range;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getPotentialValues(cmu.reuse.liora.generate.Column, java.util.List)
	 */
	@Override 
	public List<Column> getPotentialValues(Column column, List<Column> columns) {
		String vals = prop.getProperty(column.datatype + "Values");
		List<Column> range = new ArrayList<>();
		String[] parts = vals.split(",");
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
				throw new IllegalArgumentException("Invalid column in input");
			}
		}
		return range;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getColumnRange(cmu.reuse.liora.generate.Column, java.util.List)
	 */
	@Override
	public List<Column> getColumnRange(Column column, List<Column> columns) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getLabels(java.util.List)
	 */
	@Override
	public void getLabels(List<Column> potentialValues) {
		for (Column c : potentialValues) {
			String label = prop.getProperty(c.datatype + "Label");
			c.label = label;
		}
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getTables()
	 */
	@Override
	public String[] getTables() {
		String tables = prop.getProperty("tables");
		return tables.split(",");
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getMvTables(java.lang.String)
	 */
	@Override
	public String[] getMvTables(String table) {
		String mvTables = prop.getProperty(table + "MvTables");
		return mvTables.split(",");
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getTableCols(java.lang.String)
	 */
	@Override
	public List<Column> getTableCols(String table, List<Column> allColumns) { 
		String colsStr = prop.getProperty(table + "Cols");
		List<Column> cols = new ArrayList<>();
		String[] parts = colsStr.split(",");
		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			for (Column c : allColumns) {
				if (c.datatype.equals(part)) {
					cols.add(c);
					break;
				}
			}
		}
		return cols;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getProbability()
	 */
	@Override
	public double getProbability(String table) {
		String prob = prop.getProperty(table + "PresenceProb");
		try {
			double num = Double.parseDouble(prob);
			if (num < 0 || num > 1) {
				throw new IllegalArgumentException("Incorrect probability input");
			}
			return num;
		} catch(NumberFormatException e) {
			throw new IllegalArgumentException("Incorrect probability input");
		}
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#close()
	 */
	@Override
	public void close() throws IOException {
		in.close();
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getChoice(java.lang.String)
	 */
	@Override
	public boolean getChoice(String table) {
		String answer = prop.getProperty(table + "Choice");
		if (answer.equals("yes")) {
			return true;
		}
		else if (answer.equals("no")) {
			return false;
		} else {
			throw new IllegalArgumentException("Invalid choice in input");
		}
	}

}
