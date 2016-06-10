package cmu.reuse.liora.generate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * extension of menu for automated user input to facilitate testing
 * @author liorafriedberg
 */
public class AutoMenu extends Menu {
	
	public AutoMenu() {		
	}

	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getAllFiles()
	 */
	@Override
	public List<File> getAllFiles() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getProbabilityColumn(java.util.List)
	 */
	@Override
	public Column getColumn(List<Column> columns) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getFinalColumns(java.util.List)
	 */
	@Override
	public List<Column> getFinalColumns(List<Column> currentColumns) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getNumRows()
	 */
	@Override
	public long getNum() {
		return 100000; //can change
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getSource(cmu.reuse.liora.generate.Column)
	 */
	@Override
	public Source getSource(Column column) { 
			return null;
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
		return null;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getDataFormat()
	 */
	@Override
	public int getDataFormat() {
		return 1;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getFileDeps()
	 */
	@Override
	public Map<String, File> getFileDeps() {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Menu#getDependencies(cmu.reuse.liora.generate.Column, java.util.List)
	 */
	@Override
	public List<Column> getDependencies(Column column, List<Column> columns) {
		return null; 
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
	}
}
