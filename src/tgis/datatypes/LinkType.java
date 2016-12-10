package tgis.datatypes;

/**
 * Enumeration of potential OxCal model containers for dates.
 */
public enum LinkType {
	SEQUENCE("Sequence"), PHASE("Phase"); 
	
	private String oxCalKeyword;
	
	LinkType(String keyword) {
		this.oxCalKeyword = keyword;
	}
	
	public String getOxCalKeyword() {
		return this.oxCalKeyword;
	}
}
