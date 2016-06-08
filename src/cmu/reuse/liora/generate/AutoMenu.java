package cmu.reuse.liora.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liorafriedberg
 * extension of menu for automated user input to facilitate testing
 */
public class AutoMenu extends Menu {
	
	boolean firstFile;
	Column z = new Column();
	File zFile; 
	File pFile;
	
	public AutoMenu() {
		firstFile = true;
		z.datatype = "zip";
		zFile = new File("zip.csv");
		pFile = new File("population_data.csv");
	}

	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getAllFiles()
	 */
	@Override
	public List<File> getAllFiles() {
		List<File> list = new ArrayList<>();
		list.add(zFile); //files once for later
		list.add(pFile);
		return list;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getProbabilityColumn(java.util.List)
	 */
	@Override
	public Column getProbabilityColumn(List<Column> columns) {
		Column c = new Column(); //column/s once for later
		c.datatype = "population";
		 return c;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getFinalColumns(java.util.List)
	 */
	@Override
	public List<Column> getFinalColumns(List<Column> currentColumns) {
		List<Column> list = new ArrayList<>();
		Column b = new Column();
		Column c = new Column();		
		b.datatype = "city";
		c.datatype = "state";	
		list.add(z);
		list.add(b);
		list.add(c);
		return list;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getNumRows()
	 */
	@Override
	public int getNumRows() {
		return 10; //can change
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getSource(cmu.reuse.liora.generate.Column)
	 */
	@Override
	public Source getSource(Column column) { 
		if (column.datatype.equals("zip")) {
			return Source.PROBS;
		} else {
			return Source.DEP_STATIC;
		}
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getRandom()
	 */
	@Override
	public Source getRandom() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getFile()
	 */
	@Override
	public File getFile() {
		if (firstFile) {
			firstFile = false;
			return pFile;
		}
		else {
			return zFile;
		}
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getDataFormat()
	 */
	@Override
	public int getDataFormat() {
		return 2;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getDependencies(cmu.reuse.liora.generate.Column, java.util.List)
	 */
	@Override
	public List<Column> getDependencies(Column column, List<Column> columns) {
		List<Column> list = new ArrayList<>();	
		list.add(z);
		return list; 
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getPotentialValues(cmu.reuse.liora.generate.Column, java.util.List)
	 */
	@Override 
	public List<Column> getPotentialValues(Column column, List<Column> columns) {
		return null;
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
		return;
	}
}
