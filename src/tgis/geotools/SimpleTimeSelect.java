package tgis.geotools;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import tgis.datatypes.Date;

import com.esri.arcgis.carto.IFeatureLayer;
import com.esri.arcgis.carto.IFeatureSelection;
import com.esri.arcgis.carto.ILayer;
import com.esri.arcgis.datasourcesGDB.AccessWorkspaceFactory;
import com.esri.arcgis.datasourcesfile.DEFile;
import com.esri.arcgis.datasourcesfile.DEFileType;
import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IFeatureCursor;
import com.esri.arcgis.geodatabase.IGPMessages;
import com.esri.arcgis.geodatabase.IGPValue;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geodatabase.ITable;
import com.esri.arcgis.geodatabase.IWorkspaceFactory;
import com.esri.arcgis.geodatabase.QueryFilter;
import com.esri.arcgis.geodatabase.Table;
import com.esri.arcgis.geodatabase.Workspace;
import com.esri.arcgis.geodatabase.esriGPMessageSeverity;
import com.esri.arcgis.geoprocessing.BaseGeoprocessingTool;
import com.esri.arcgis.geoprocessing.GPDouble;
import com.esri.arcgis.geoprocessing.GPDoubleType;
import com.esri.arcgis.geoprocessing.GPLong;
import com.esri.arcgis.geoprocessing.GPLongType;
import com.esri.arcgis.geoprocessing.GPParameter;
import com.esri.arcgis.geoprocessing.GPString;
import com.esri.arcgis.geoprocessing.GPStringType;
import com.esri.arcgis.geoprocessing.IGPEnvironmentManager;
import com.esri.arcgis.geoprocessing.IGPParameter;
import com.esri.arcgis.geoprocessing.esriGPParameterDirection;
import com.esri.arcgis.geoprocessing.esriGPParameterType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.Array;
import com.esri.arcgis.system.IArray;
import com.esri.arcgis.system.IName;
import com.esri.arcgis.system.IPersistStream;
import com.esri.arcgis.system.ITrackCancel;
import com.esri.arcgis.system.MemoryBlobStream;
import com.esri.arcgis.system.ObjectStream;
import com.esri.arcgis.system.PropertySet;

@SuppressWarnings("serial")
public class SimpleTimeSelect extends BaseGeoprocessingTool {

	private String toolName = "SimpleTimeSelect";
	private String displayName = "Select by Time Period";
	private String metadataFileName = "tgis.geotools.SimpleTimeSelect.xml";
	public SimpleTimeSelect() {
	
	}
	/**
	 * Returns name of the tool
	 * This name appears when executing the tool at the command line or in scripting.
	 * This name should be unique to each toolbox and must not contain spaces. 
	 */
	public String getName() throws IOException, AutomationException
	{
		return toolName;
	}
	/**
	 * Returns Display Name of the tool, as seen in ArcToolbox.
	 */
	public String getDisplayName() throws IOException, AutomationException
	{
		return displayName;
	}
	/**
	 * Returns the full name of the tool
	 */
	public IName getFullName() throws IOException, AutomationException
	{
		return (IName) new tgis.geotools.FunctionFactory1().getFunctionName(toolName);
	}
	/**
	 * Returns an array of paramInfo
	 * This is the location where the parameters to the Function Tool are defined.
	 * This property returns an IArray of parameter objects (IGPParameter).
	 * These objects define the characteristics of the input and output parameters.
	 */
	public IArray getParameterInfo() throws IOException, AutomationException {
		IArray parameters = new Array();
	
		GPParameter parameter1 = new GPParameter();
		parameter1.setName("dbase");
		parameter1.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
		parameter1.setDisplayName("Select Features from");
		parameter1.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
		parameter1.setDataTypeByRef(new DEFileType());
		parameter1.setValueByRef(new DEFile());
		parameters.add(parameter1);
	
		GPParameter parameter2 = new GPParameter();
		parameter2.setName("startDate");
		parameter2.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
		parameter2.setDisplayName("From Date");
		parameter2.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
		parameter2.setDataTypeByRef(new GPLongType());
		parameter2.setValueByRef(new GPLong());
		parameters.add(parameter2);
	
		GPParameter parameter3 = new GPParameter();
		parameter3.setName("endDate");
		parameter3.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
		parameter3.setDisplayName("Until Date");
		parameter3.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
		parameter3.setDataTypeByRef(new GPLongType());
		parameter3.setValueByRef(new GPLong());
		parameters.add(parameter3);
	
		GPParameter parameter4 = new GPParameter();
		parameter4.setName("prob");
		parameter4.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
		parameter4.setDisplayName("With Probability Greater than");
		parameter4.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
		parameter4.setDataTypeByRef(new GPDoubleType());
		parameter4.setValueByRef(new GPDouble());
		parameters.add(parameter4);
	
		GPParameter parameter5 = new GPParameter();
		parameter5.setName("linkedShapefile");
		parameter5.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
		parameter5.setDisplayName("Shapefile containing linked features");
		parameter5.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
		parameter5.setDataTypeByRef(new GPStringType());
		parameter5.setValueByRef(new GPString());
		parameters.add(parameter5);
	
		GPParameter parameter6 = new GPParameter();
		parameter6.setName("result");
		parameter6.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
		parameter6.setDisplayName("Result");
		parameter6.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
		parameter6.setDataTypeByRef(new GPStringType());
		parameter6.setValueByRef(new GPString());
		parameters.add(parameter6);
	
		return parameters;
	}
	/**
	 * Called each time the user changes a parameter in the tool dialog or Command Line.
	 * This updates the output data of the tool, which extremely useful for building models.
	 * After returning from UpdateParameters(), the GP framework calls its internal validation
	 * routine to check that a given set of parameter values are of the appropriate number,
	 * DataType, and value.
	 */
	public void updateParameters(IArray paramvalues, IGPEnvironmentManager envMgr) 
	{
		System.out.println("updateParameters");
		try {
			//Read Parameter 1
			IGPParameter parameter1 = (IGPParameter) paramvalues.getElement(0);
			IGPValue parameter1Value = gpUtilities.unpackGPValue(parameter1);
	
			//Read Parameter 2
			IGPParameter parameter2 = (IGPParameter) paramvalues.getElement(1);
			IGPValue parameter2Value = gpUtilities.unpackGPValue(parameter2);
	
			//Read Parameter 3
			IGPParameter parameter3 = (IGPParameter) paramvalues.getElement(2);
			IGPValue parameter3Value = gpUtilities.unpackGPValue(parameter3);
	
			//Read Parameter 4
			IGPParameter parameter4 = (IGPParameter) paramvalues.getElement(3);
			IGPValue parameter4Value = gpUtilities.unpackGPValue(parameter4);
	
			//Read Parameter 5
			IGPParameter parameter5 = (IGPParameter) paramvalues.getElement(4);
			IGPValue parameter5Value = gpUtilities.unpackGPValue(parameter5);
	
			//Read Parameter 6
			IGPParameter parameter6 = (IGPParameter) paramvalues.getElement(5);
			IGPValue parameter6Value = gpUtilities.unpackGPValue(parameter6);
			
			parameter6Value.setAsText("Result");
			gpUtilities.packGPValue(parameter6Value, parameter6);
	
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		//TODO: add custom logic for updating parameters here. This could also contain logic to
		//assign value to your output parameter to facilitate chaining in Models
	}
	/**
	 * Called after returning from the internal validation routine. You can examine the messages 
	 * created from internal validation and change them if desired. 
	 */
	public void updateMessages(IArray paramvalues, IGPEnvironmentManager envMgr, IGPMessages gpMessages)
	{
		System.out.println("Updatemessages");
		try
		{
			if(gpMessages.getMaxSeverity() == esriGPMessageSeverity.esriGPMessageSeverityError)
			{
				for(int i = 0; i < gpMessages.getCount(); i++)
				{
					System.out.println(gpMessages.getMessage(i).getDescription());
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * Executes the tool 
	 */
	public void execute(IArray paramvalues, ITrackCancel trackcancel, IGPEnvironmentManager envMgr, IGPMessages messages) throws IOException, AutomationException
	{
		System.out.println("Execute");
		//Read Parameter 1
		IGPParameter dbaseParam = (IGPParameter) paramvalues.getElement(0);
		IGPValue dbaseParamValue = gpUtilities.unpackGPValue(dbaseParam);
	
		//Read Parameter 2
		IGPParameter startDateParam = (IGPParameter) paramvalues.getElement(1);
		IGPValue startDateParamValue = gpUtilities.unpackGPValue(startDateParam);
	
		//Read Parameter 3
		IGPParameter endDateParam = (IGPParameter) paramvalues.getElement(2);
		IGPValue endDateParamValue = gpUtilities.unpackGPValue(endDateParam);
	
		//Read Parameter 4
		IGPParameter probParam = (IGPParameter) paramvalues.getElement(3);
		IGPValue probParamValue = gpUtilities.unpackGPValue(probParam);
	
		//Read Parameter 5
		IGPParameter parameterLayerMame = (IGPParameter) paramvalues.getElement(4);
		IGPValue parameterLayerNameValue = gpUtilities.unpackGPValue(parameterLayerMame);
		
		//Read Parameter 6
		IGPParameter parameter6 = (IGPParameter) paramvalues.getElement(5);
		IGPValue parameter6Value = gpUtilities.unpackGPValue(parameter6);
		
		Table objects = openOxCalTable(dbaseParamValue.getAsText());
		
		int datesField = objects.findField("Dates");
		int nameField = objects.findField("Name");
		int typeField = objects.findField("Type");
		
		QueryFilter filter = new QueryFilter();
		ICursor c = objects.getRows(new int[]{2,3,4,5}, true);//objects.ITable_search(filter, true);
		IRow row;
				
		//Parse parameters
		Double startDate = Double.valueOf(startDateParamValue.getAsText());
		Double endDate = Double.valueOf(endDateParamValue.getAsText());
		Double prob = Double.valueOf(probParamValue.getAsText());
		//ArrayList<String> matchedOxCalIds = new ArrayList<String>();
		
		//Prepare selection
		//IMap map = gpUtilities.getMap();
		//ILayer layer = gpUtilities.findMapLayer(parameterLayerNameValue.getAsText());
		ILayer layer = gpUtilities.getMapLayers().next();
		
		IFeatureLayer featureLayer = (IFeatureLayer) layer;
		IFeatureSelection featureSelection = (IFeatureSelection) featureLayer;
		
		featureSelection.clear(); //NPE..? - break into smaller sub-methods
		
		//filter.addField("Name");
		while ((row = c.nextRow()) != null) {
			Object typeObj = row.getValue(typeField);
			String type = (String) typeObj;
			
			if (!type.equals("date")) {
				continue;
			}
			
			Object obj = row.getValue(datesField);
			try {
				MemoryBlobStream blobStream = (MemoryBlobStream) obj;
				ObjectStream objStream = new ObjectStream();
				
				objStream.setStreamByRef(blobStream);
				PropertySet propSet = new PropertySet();
				IPersistStream persistStream = (IPersistStream) propSet;
				
				persistStream.load(objStream);
				
				String dateXML = (String) propSet.getProperty("Date");
				
				Date date = new Date();
				date.populateFromXml(dateXML);
				
				double dateProb = date.probBetweenDates(startDate, endDate);
				
				if (dateProb >= prob) {
					//matchedOxCalIds.add((String) row.getValue(nameField));
					filter.setWhereClause("Name = '" + (String) row.getValue(nameField) +"'");
					IFeatureCursor cursor = featureLayer.search(filter, true);
					IFeature feature = cursor.nextFeature();
					featureSelection.add(feature);
				}
			}
			catch (ClassCastException e) {
				System.out.println("Simple Time Select Wizard Should be a date but isn't.");
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//gpUtilities.getActiveView().refresh();
		//ISelectionSet selectionSet = featureSelection.getSelectionSet();
		//map.setFeatureSelectionByRef(selectionSet);
	}
	
	private Table openOxCalTable(String fileName) throws IOException,
			UnknownHostException {
		IWorkspaceFactory workspaceFactory = new AccessWorkspaceFactory();
		Workspace geodbase = null;
		
		try {
			geodbase = new Workspace(workspaceFactory.openFromFile(fileName, 0));
		}
		catch (Exception e) {
			System.out.println("Simple Time Select Wizard Load Geodbase failed");
		}
		ITable table = null;
		try {
			table = geodbase.openTable("OxCal_Events");
		} catch (Exception e) {
			System.out.println("Simple Time Select Wizard Load Table Failed");
		}
		
		//TODO: Check the table has been linked to a shapefile
		Table objects = new Table(table);
		
		return objects;
	}
	
	/**
	 * Returns metadata file
	 */
	public String getMetadataFile() throws IOException, AutomationException
	{
		return metadataFileName;
	}
	/**
	 * Returns status of license
	 */
	public boolean isLicensed() throws IOException, AutomationException
	{
		return true;
	}

}
