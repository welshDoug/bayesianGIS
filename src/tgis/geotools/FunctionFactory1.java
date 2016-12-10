package tgis.geotools;

import java.io.IOException;
import java.util.UUID;
import com.esri.arcgis.geodatabase.*;
import com.esri.arcgis.geoprocessing.*;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.interop.extn.ArcGISExtension;
import com.esri.arcgis.interop.extn.ArcGISCategories;
import com.esri.arcgis.system.EngineInitializer;
import com.esri.arcgis.system.IUID;
import com.esri.arcgis.system.UID;
import com.esri.arcgis.system.esriProductCode;

/**
 * Auto generated factory objection for the SimpleTimeSelect tool.
 */
@SuppressWarnings("serial")
@ArcGISExtension(categories = { ArcGISCategories.GPFunctionFactories })
public class FunctionFactory1 implements IGPFunctionFactory {

	private String functionFactoryAlias = "bdfunctionfactory";
	private String factoryName = "BDFunctionFactory";
	private String toolset = "Bayesian Date Tools";
	private String toolName = "SimpleTimeSelect";
	private String displayName = "Select by Time Period";
	private String description = "Tool Description ";
	/**
	 * Returns the appropriate GPFunction object based on specified tool name
	 */
	public IGPFunction getFunction(String name) throws IOException, AutomationException
	{
		if ( name.equalsIgnoreCase(toolName) )
			return new tgis.geotools.SimpleTimeSelect();
		return null;
	}
	/**
	 * Returns a GPFunctionName objects based on specified tool name
	 */
	public IGPName getFunctionName(String name) throws IOException, AutomationException
	{
		if ( name.equalsIgnoreCase(toolName) ) {
			GPFunctionName functionName = new GPFunctionName();
			functionName.setCategory(toolset);
			functionName.setDescription(description);
			functionName.setDisplayName(displayName);
			functionName.setName(toolName);
			functionName.setMinimumProduct(esriProductCode.esriProductCodeAdvanced);
			functionName.setFactoryByRef(this);
			return functionName;
		}
		return null;
	}
	/**
	 * Returns names of all gp tools created by this function factory
	 */
	public IEnumGPName getFunctionNames() throws IOException, AutomationException
	{
		EnumGPName nameArray = new EnumGPName();
		nameArray.add(getFunctionName(toolName));
		return nameArray;
	}
	/**
	 * Returns Alias of the function factory
	 */
	public String getAlias() throws IOException, AutomationException
	{
		return functionFactoryAlias;  // lower case of the FunctionFactory name.
	}
	/**
	 * Returns Class ID
	 */
	public IUID getCLSID() throws IOException, AutomationException
	{
		UID uid = new UID();
		uid.setValue("{" + UUID.nameUUIDFromBytes(this.getClass().getName().getBytes()) + "}");
		return uid;
	}
	/**
	 * Returns Function Environments
	 */
	public IEnumGPEnvironment getFunctionEnvironments() throws IOException, AutomationException
	{
		return null;
	}
	/**
	 * Returns name of the FunctionFactory
	 */
	public String getName() throws IOException, AutomationException
	{
		return factoryName;
	}

}
