package tgis;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import tgis.datatypes.Date;

import com.esri.arcgis.addins.desktop.Tool;
import com.esri.arcgis.datasourcesGDB.AccessWorkspaceFactory;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.geodatabase.Field;
import com.esri.arcgis.geodatabase.FieldChecker;
import com.esri.arcgis.geodatabase.IEnumFieldError;
import com.esri.arcgis.geodatabase.IField;
import com.esri.arcgis.geodatabase.IFieldChecker;
import com.esri.arcgis.geodatabase.IFields;
import com.esri.arcgis.geodatabase.IFieldsEdit;
import com.esri.arcgis.geodatabase.IObjectClassDescription;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.IWorkspaceFactory;
import com.esri.arcgis.geodatabase.IWorkspaceName;
import com.esri.arcgis.geodatabase.ObjectClassDescription;
import com.esri.arcgis.geodatabase.Table;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriFieldType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.IMemoryBlobStream;
import com.esri.arcgis.system.IName;
import com.esri.arcgis.system.IPersistStream;
import com.esri.arcgis.system.IPropertySet;
import com.esri.arcgis.system.MemoryBlobStream;
import com.esri.arcgis.system.PropertySet;
import com.esri.arcgis.system.UID;

/**
 * ArcGIS tool to build a GeoDatabase from an OxCal v4 raw output file.
 */
public class dateImporter extends Tool {

	private IApplication app;
	private JFileChooser chooser;

	/*private String nameField;
	private String opField;
	private String typeField;
	private String dateField;
	private String parentIDField;*/

	private static String storedSourceDir = "";
	private static String storedResultDir = "";

	@Override
	public void init(IApplication app) {
		this.app = app;
		chooser = new JFileChooser();
	}

	/**
	 * Called when the tool is activated by clicking it.
	 * 
	 * @exception java.io.IOException if there are interop problems.
	 * @exception com.esri.arcgis.interop.AutomationException if the component throws an ArcObjects exception.
	 */
	@Override
	public void activate() throws IOException, AutomationException {
		// Load .js OxCal output
		if (!storedSourceDir.equals("")) {
			chooser.setCurrentDirectory(new File(storedSourceDir));
		}
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal != JFileChooser.APPROVE_OPTION) {
			System.err.println("User cancelled file open dialog");
			return;
		}
		storedSourceDir = chooser.getSelectedFile().getParent();

		// Once we have the filename, open and parse it
		OxCalParser rawOxCal = new OxCalParser(chooser.getSelectedFile());
		JSONArray ocd = rawOxCal.getOcd();

		if (ocd == null) {
			System.err.println("Fatal Error: No OxCal Data");
			return;
		}

		if (!storedResultDir.equals("")) {
			chooser.setCurrentDirectory(new File(storedResultDir));
		}
		returnVal = chooser.showSaveDialog(null);
		if(returnVal != JFileChooser.APPROVE_OPTION) {
			System.err.println("User cancelled file save dialog");
			return;
		}
		storedResultDir = chooser.getSelectedFile().getParent();

		// Save the data to the geodatabase
		storeToGeodatabase(chooser.getSelectedFile(), ocd);

		//TODO: Load table into arcgis app 
	}

	/**
	 * Writes the provided OxCal Data (ocd) to the geodatabase specified in filename.
	 * 
	 * @param filename
	 * @param ocd
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private boolean storeToGeodatabase(File filename, JSONArray ocd)
			throws UnknownHostException, IOException {
		IWorkspaceFactory workspaceFactory = new AccessWorkspaceFactory();
		Workspace geodbase;

		try {
			geodbase = new Workspace(workspaceFactory.openFromFile(filename.getCanonicalPath(), app.getHWnd()));
		}
		catch (Exception e) {
			//Probably means file doesnt exist so create it
			IWorkspaceName geodbaseName = workspaceFactory.create(filename.getParent(),
					filename.getName(), null, app.getHWnd());
			geodbase = new Workspace(((IName) geodbaseName).open());
		}

		geodbase.startEditing(false);
		geodbase.startEditOperation();

		//Hard code the table for now, could make it user specified in the future
		ITable table;
		try {
			table = geodbase.openTable("OxCal_Events");
		} catch (Exception e) {
			//table doesnt exist so create it
			UID uid = new UID();
			uid.setValue("esriGeoDatabase.Object");

			IFields fields = createFields();

			table = geodbase.createTable("OxCal_Events",
					fields, uid, null, "");
		}
		Table objects = new Table(table);

		Stack<Integer> parentStack = new Stack<Integer>();
		int prevLevel = -1;
		int prevId = -1;
		
		//Now we have a database, and table, add the rows
		for (int i = 1; i < ocd.size(); i++) {
			Object tmp = ocd.get(i);
			if (tmp != null){
				Long lvlLong = (Long) ((JSONObject) tmp).get("level");
				int level = lvlLong.intValue();
				int parentId = -1;
				
				if (level == prevLevel) {
					parentId = parentStack.peek();
				}
				else if (level > prevLevel) {
					parentId = prevId;
					parentStack.push(prevId);
				}
				else if (level < prevLevel) {
					parentStack.pop();
					parentId = parentStack.peek();
				}
				
				int rowId = addRow(objects, (JSONObject) tmp, parentId);
				
				prevLevel = level;
				prevId = rowId;
			}
		}

		//Now close off the database
		geodbase.stopEditOperation();
		geodbase.stopEditing(true);

		//geodbase.release();

		return true;
	}

	/**
	 * Helper method to create the necessary field layout in the geodatabase.
	 * 
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private IFields createFields() throws UnknownHostException, IOException {
		IObjectClassDescription ocDesc = new ObjectClassDescription();

		IFields requiredFields = ocDesc.getRequiredFields();
		IFieldsEdit fieldsEdit = (IFieldsEdit) requiredFields;

		IField temp;
		temp = createField("Name", esriFieldType.esriFieldTypeString, false,
				"OxCal Model Field Name", new String(""), 20);
		//nameField = temp.getName();
		fieldsEdit.addField(temp);
		temp = createField("Operation", esriFieldType.esriFieldTypeString, false,
				"OxCal Model Operation", new String(""), 10);
		//opField = temp.getName();
		fieldsEdit.addField(temp);
		temp = createField("Type", esriFieldType.esriFieldTypeString, false,
				"OxCal Field Type", new String(""), 10);
		//typeField = temp.getName();
		fieldsEdit.addField(temp);
		temp = createField("Dates", esriFieldType.esriFieldTypeBlob, true,
				"Bayesian Calibrated Date", new Object(), 0);
		/*temp = createField("Dates", esriFieldType.esriFieldTypeXML, true,
				"Bayesian Calibrated Date", new XMLStream(), 0);*/
		//dateField = temp.getName();
		fieldsEdit.addField(temp);
		temp = createField("ParentID", esriFieldType.esriFieldTypeInteger, true,
				"ID For Parent Field Row", null, 0);
		//parentIDField = temp.getName();
		fieldsEdit.addField(temp);

		IFields objectFields = fieldsEdit;

		IFieldChecker fieldChecker = new FieldChecker();
		IEnumFieldError[] enumFieldError = null;
		IFields[] validatedFields = new IFields[1];
		fieldChecker.validate(objectFields, enumFieldError, validatedFields);

		return validatedFields[0];
	}

	/**
	 * Build up a field using the provided attributes.
	 * 
	 * @param name
	 * @param type
	 * @param isNullable
	 * @param aliasName
	 * @param defaultValue
	 * @param length
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private IField createField(String name, int type, boolean isNullable,
			String aliasName, Object defaultValue, int length)
					throws UnknownHostException, IOException {
		Field theField = new Field();

		theField.setName(name);
		theField.setType(type);
		theField.setIsNullable(isNullable);
		theField.setAliasName(aliasName);
		if (defaultValue != null) {
		theField.setDefaultValue(defaultValue);
		}
		theField.setEditable(true);

		if (type == esriFieldType.esriFieldTypeString) {
			theField.setLength(length);
		}

		return theField;
	}

	/**
	 * Add a row to the geodatabase to persist the json object provided
	 * @param tbl
	 * @param obj
	 * @param parentID
	 * @return
	 * @throws AutomationException
	 * @throws IOException
	 */
	private int addRow(Table tbl, JSONObject obj, int parentID) throws AutomationException, IOException {
		IRow newRow = tbl.createRow();
		IFields fields = newRow.getFields();

		int nameIndex = 1;//fields.findField(nameField);
		String nameVal = (String) obj.get("name");
		int opIndex = 2;//fields.findField(opField);
		String opVal = (String) obj.get("op");
		int typeIndex = 3;//fields.findField(typeField);
		String typeVal = (String) obj.get("type");
		int dateIndex = 4;//fields.findField(dateField);
		Date dateVal = buildDate(obj);
		int parentIDIndex = 5;//fields.findField(parentIDField);
		//String dateVal = obj.toJSONString();

		if (dateVal != null) { 
			String doc = null;
			try {
				doc = dateVal.toXml();
				//could encode the String as an object into ESRI XML
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}

			IPropertySet property = new PropertySet();
			property.setProperty("Date", doc);

			IMemoryBlobStream memBlobStream = new MemoryBlobStream();
			IPersistStream persist = (IPersistStream)property;
			persist.save(memBlobStream, 0);

			newRow.setValue(dateIndex, memBlobStream);
		}

		newRow.setValue(nameIndex, nameVal);
		newRow.setValue(opIndex, opVal);
		newRow.setValue(typeIndex, typeVal);
		newRow.setValue(parentIDIndex, parentID);

		newRow.store();
		
		return newRow.getOID(); //Should be row Key
	}

	/**
	 * Turn an OxCal posterior date object stored as JSON into a @Date.
	 * 
	 * @param obj
	 * @return
	 */
	private Date buildDate(JSONObject obj) {
		Date newDate = new Date();

		JSONObject posterior = (JSONObject) obj.get("posterior");
		if (posterior != null) {
			newDate.populateFromJson(posterior);
		}
		else {
			return null;
		}

		return newDate;
	}
}
