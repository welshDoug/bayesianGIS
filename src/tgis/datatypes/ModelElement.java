package tgis.datatypes;

/**
 * Abstract superclass for all types which make up an OxCal model.
 * The common label field, and a getter are defined, along with 
 * an abstract writeToOxCal method.
 */
public abstract class ModelElement {
	private String label;
	
	public ModelElement(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	/**
	 * Adds this model elements OxCal input object structure to the provided StringBuilder.
	 * @param stringBuilder
	 */
	public abstract void writeToOxCal(StringBuilder stringBuilder);
}
