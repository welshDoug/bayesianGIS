package tgis.datatypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Node represent all model elements other than dates, 
 * including those which can have child elements.
 */
public class Node extends ModelElement {
	private List<ModelElement> children;
	private LinkType type;
	private String shapeID;
	
	public Node(String label, LinkType type) {
		super(label);
		this.type = type;
		children = new ArrayList<ModelElement>();
	}
	
	public List<ModelElement> getChildren() {
		return children;
	}
	
	/**
	 * Add a Radiocarbon date directly from the raw data.
	 * @param label - A label for the date (e.g. lab code)
	 * @param date - The Date value
	 * @param sd - Standard Deviation
	 */
	public void addDate(String label, long date, int sd) {
		children.add(new RawDate(label, date,sd));
	}
	
	public LinkType getType() {
		return type;
	}
	
	public String getShapeID() {
		return this.shapeID;
	}

	@Override
	public void writeToOxCal(StringBuilder stringBuilder) {
		//Add Boundary start
		stringBuilder.append(type.getOxCalKeyword()+"(\""+this.getLabel()+"\")");
		stringBuilder.append(System.getProperty("line.separator"));
		stringBuilder.append("{");
		stringBuilder.append(System.getProperty("line.separator"));
		for (ModelElement d : children) {
			d.writeToOxCal(stringBuilder);
		}
		stringBuilder.append("}");
		stringBuilder.append(System.getProperty("line.separator"));
		//Add Boundary end
	}
}