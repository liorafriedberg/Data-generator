package cmu.reuse.liora.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AutoMenu extends Menu {
	
	boolean firstFile = true;

	@Override
	public List<File> getAllFiles() {
		List<File> list = new ArrayList<>();
		list.add(new File("zip.csv"));
		list.add(new File("population_data.csv"));
		return list;
	}
	
	@Override
	public Column getProbabilityColumn(List<Column> columns) {
		for (Column c : columns) {
			if (c.datatype.equals("population")) return c;
		}
		return null;
	}
	
	@Override
	public List<Column> getFinalColumns(List<Column> currentColumns) {
		List<Column> list = new ArrayList<>();
		boolean yet = false;
		for (Column c : currentColumns) {
			if (c.datatype.equals("city") ||
					c.datatype.equals("state")) {
				list.add(c);
			}
			if (!yet && c.datatype.equals("zip")) {
				list.add(c);
				yet = true;
			}
				
		}
		return list;
	}
	
	@Override
	public int getNumRows() {
		return 10;
	}
	
	@Override
	public Source getSource(Column column) { 
		if (column.datatype.equals("zip")) {
			return Source.PROBS;
		} else {
			return Source.DEP_STATIC;
		}
	}
	
	@Override
	public Source getRandom() {
		return null;
	}
	
	@Override
	public File getFile() {
		if (firstFile) {
			firstFile = false;
			return new File("population_data.csv");
		}
		else {
			return new File("zip.csv");
		}
	}
	
	@Override
	public int getDataFormat() {
		return 2;
	}
	
	@Override
	public List<Column> getDependencies(Column column, List<Column> columns) {
		List<Column> list = new ArrayList<>();
		for (Column c : columns) {
			if (c.datatype.equals("zip")) {
				list.add(c);
				break;
			}
		}
		return list;
	}
	
	@Override 
	public List<Column> getPotentialValues(Column column, List<Column> columns) {
		return null;
	}
	
	@Override
	public List<Column> getColumnRange(Column column, List<Column> columns) {	
		return null;
	}
	
	@Override
	public void getLabels(List<Column> potentialValues) {
		return;
	}
}
