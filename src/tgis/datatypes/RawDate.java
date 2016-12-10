package tgis.datatypes;

/**
 * Represents un-calibrated dates in the model.
 */
public class RawDate extends ModelElement {
	private long date;
	private int sd;
	
	public RawDate(String label, long date, int sd) {
		super(label);
		this.date = date;
		this.sd = sd;
	}
	
	public String toString() {
		return new String(getLabel() + " " + date + " " + sd);
	}
	
	@Override
	public void writeToOxCal(StringBuilder stringBuilder) {
		stringBuilder.append("R_Date(\""+this.getLabel()+"\", "+this.date+", "+this.sd+");");
		stringBuilder.append(System.getProperty("line.separator"));
	}
}
