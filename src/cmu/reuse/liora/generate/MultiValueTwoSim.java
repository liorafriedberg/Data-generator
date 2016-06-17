package cmu.reuse.liora.generate;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * generates individual-level data for columns with dependencies including a multi-value column, and that are
 * multi-value themselves
 * @author liorafriedberg
 */
public class MultiValueTwoSim implements Simulator {

	/* (non-Javadoc)
	 * @see cmu.reuse.liora.generate.Simulator#simulate(cmu.reuse.liora.generate.Menu, cmu.reuse.liora.generate.Column, java.util.List, java.util.List)
	 */
	@Override
	public void simulate(Menu menu, Column column, List<Individual> people, List<Column> allColumns) {
		File file = menu.getFile(column);
		CSVReader reader = new CSVReader(file);
		List<Column> dependencies = menu.getDependencies(column, reader.header); //other deps
		Column mv = menu.getDepColumn(reader.header, column); //the multi dep
		Column vals = menu.getValueColumn(reader.header, column); //the value column
		Column prob = menu.getProbColumn(reader.header, column); //the prob column
		Map<Integer, Column> indexToDep = new HashMap<>();
		Set<Column> check = people.get(0).getValues().keySet();
		for (Column d : dependencies) { 
			int index = reader.getColumnIndex(d);
			indexToDep.put(index, d);
			if (!check.contains(d)) {
				throw new IllegalArgumentException("Chose a dependency not yet assigned");
			}
		}
		Map<Integer, Column> indexToP = new HashMap<>();
		indexToP.put(reader.getColumnIndex(vals), vals);
		indexToP.put(reader.getColumnIndex(prob), prob);
		Map<Map<Column, String>, Set<Map<Column, String>>> save = reader.findDepTwoValues(indexToDep, indexToP);
		for (Individual person : people) {		
			Map<Column, String> currentValues = person.getValues();
			String mvStr = currentValues.get(mv);
			Map<Column, String> currentDepVals = new HashMap<>();
			for (Column d : dependencies) {
				if (!d.equals(mv)) { //only one multi dep now
				currentDepVals.put(d, currentValues.get(d));
				}
			}	
			if (mvStr.length() > 0) {
			String[] parts = mvStr.split(",");
			
			for (int i = 0; i < parts.length; i++) { //for every disease						
				String twoVals = "";
				currentDepVals.put(mv, parts[i]);
				for (Column c : currentDepVals.keySet()) {
				}
				Set<Map<Column, String>> set = save.get(currentDepVals);
		// the result has prob to prob and val to val
				if (set != null) {
				for (Map<Column, String> map : set) {
					
				String probS = map.get(prob);
				String v = map.get(vals);
				double random = Math.random();
				if (random <= Double.parseDouble(probS))  {
					twoVals = twoVals + v + ",";
				}
				}
				}	
				person.mvTwo.put(parts[i], twoVals); //disease to all the prescriptions
			}
		}
		}				
		reader.close();
	}
}
