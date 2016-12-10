package tgis.datatypes;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Object to represent Bayesian modelled output from OxCal.
 */
public class Date /*implements IPersistVariant {
	private static final long serialVersionUID = 4393736002613411669L;*/ {

	private List<DateSegment> dates;
	private double mean;
	private double sigma;
	private double median;
	private double interval;
	
	public Date() {
		dates = new ArrayList<DateSegment>();
	}
	
	/**
	 * Serialises the object to XML
	 * @return
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public String toXml() throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		//root elements
		Document doc = docBuilder.newDocument();

		Element rootElement = doc.createElement("date");
		doc.appendChild(rootElement);
		
		Element mean = doc.createElement("mean");
		mean.setTextContent(Double.toString(this.mean));
		rootElement.appendChild(mean);
		
		Element sigma = doc.createElement("sigma");
		sigma.setTextContent(Double.toString(this.sigma));
		rootElement.appendChild(sigma);
		
		Element median = doc.createElement("median");
		median.setTextContent(Double.toString(this.median));
		rootElement.appendChild(median);
		
		Element interval = doc.createElement("interval");
		interval.setTextContent(Double.toString(this.interval));
		rootElement.appendChild(interval);
		
		Element values = doc.createElement("values");
		rootElement.appendChild(values);
		
		for (DateSegment d : dates) {
			Element entry = doc.createElement("entry");
			
			Element year = doc.createElement("year");
			year.setTextContent(Double.toString(d.getYear()));
			entry.appendChild(year);
			
			Element prob = doc.createElement("prob");
			prob.setTextContent(Double.toString(d.getProb()));
			entry.appendChild(prob);
			
			values.appendChild(entry);
		}
		
	  Transformer transformer = TransformerFactory.newInstance().newTransformer();
	  StreamResult result = new StreamResult(new StringWriter());
	  DOMSource source = new DOMSource(doc);
	  transformer.transform(source, result);
	  
	  return result.getWriter().toString();
	}
	
	/**
	 * Inflates from an XML serialised version
	 * @param xmlString
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public void populateFromXml(String xmlString) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
        
        Node root = doc.getChildNodes().item(0);
        if (root.getNodeName().equals("date")) {
        	NodeList childNodes = root.getChildNodes();
        	
        	for (int i = 0; i < childNodes.getLength(); i++) {
        		Node node = childNodes.item(i);
        		
        		String name = node.getNodeName();
        		
        		if (name.equals("mean")) {
        			this.mean = Double.parseDouble(node.getTextContent());
        		}
        		else if (name.equals("sigma")) {
        			this.sigma = Double.parseDouble(node.getTextContent());
        		}
        		else if (name.equals("median")) {
        			this.median = Double.parseDouble(node.getTextContent());
        		}
        		else if (name.equals("interval")) {
        			this.interval = Double.parseDouble(node.getTextContent());
        		}
        		else if (name.equals("values")) {
        			NodeList values = node.getChildNodes();
        			
        			for (int j = 0; j < values.getLength(); j++) {
        				Node entry = values.item(j);
        				
        				NodeList props = entry.getChildNodes();
        				
        				double prob = 0, year = 0;
        				
        				for (int k = 0; k < props.getLength(); k++) {
        					Node prop1 = props.item(k);
        					if (prop1.getNodeName().equals("prob")) {
        						prob = Double.parseDouble(prop1.getTextContent());
        					}
        					else if (prop1.getNodeName().equals("year")) {
        						year = Double.parseDouble(prop1.getTextContent());
        					}
        				}
        				dates.add(new DateSegment(year, prob));
        			}
        		}
        	}
        }
	}
	
	public void populateFromJsonString(String jsonObj) {
		JSONObject obj = (JSONObject) JSONValue.parse(jsonObj);
		populateFromJson(obj);
	}
	
	/**
	 * Build a probabilistic date from the supplied JSONObject of the likelihood.
	 * @param obj
	 */
	public void populateFromJson(JSONObject obj) {
		//obj/start (signed)
		double startDate = Double.parseDouble(String.valueOf(obj.get("start")));
		
		//obj/resolution
		long res = Long.parseLong(String.valueOf(obj.get("resolution")));
		
		//obj/probNorm
		double probNorm = Double.parseDouble(String.valueOf(obj.get("probNorm")));
		
		//dates - obj/prob[]
		Object[] dateProbs = ((JSONArray) obj.get("prob")).toArray();
		
		for (int i = 0; i < dateProbs.length; i++) {
			double year = startDate + (res * i);
			
			double prob = Double.parseDouble(String.valueOf(dateProbs[i])) * probNorm;
			//Round to 4 sig figs?
			
			dates.add(new DateSegment(year, prob));
		}
		
		this.median = Double.parseDouble(String.valueOf(obj.get("median")));
		this.mean = Double.parseDouble(String.valueOf(obj.get("mean")));
		this.sigma = Double.parseDouble(String.valueOf(obj.get("sigma")));
		this.interval = res;
	}
	
	/**
	 * Work out the probability of this Date falling between two dates.
	 * Assumes BC/BP -ve and AD +ve.
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public double probBetweenDates(double start, double end) {
		double overallProb = 0.0;
		double probTotal = 0.0;
		
		for (DateSegment date : dates) {
			double year = date.getYear();
			
			overallProb += date.getProb();
			
			if ((year >= start) && (year <= end)) {
				probTotal += date.getProb();
			}
			else if (((year + this.interval) >= start) && (year <= end)) {
				double prob = 0;
				DateSegment date2 = dates.get(dates.indexOf(date)+1);
				
				if (date2 == null) {
					//Insufficient Data to Calculate this
					continue;
				}
				prob = (date.getProb()*(date2.getYear()-start)+date2.getProb()*(start-year))/(date2.getYear()-year);
				probTotal += prob;
			}
			else if((year >= start) && ((year - this.interval) <= end)) {
				double prob = 0;
				DateSegment date2 = dates.get(dates.indexOf(date)-1);
				
				if (date2 == null) {
					//Insufficient Data to Calculate this
					continue;
				}
				prob = (date2.getProb()*(date.getYear()-end)+date.getProb()*(end-date2.getYear()))/(date.getYear()-date2.getYear());
				probTotal += prob;
			}
			
		}
		
		return probTotal/overallProb;
	}
	
	public double[] getYears() {
		double[] years = new double[this.dates.size()];
		
		for (int i = 0; i < this.dates.size(); i++) {
			years[i] = this.dates.get(i).getYear();
		}
		
		return years;
	}
	
	public double[] getProbs() {
		double[] probs = new double[this.dates.size()];
		
		for (int i = 0; i < this.dates.size(); i++) {
			probs[i] = this.dates.get(i).getProb();
		}
		
		return probs;
	}
	
	public List<DateSegment> getDates() {
		return dates;
	}

	public double getMean() {
		return mean;
	}

	public double getSigma() {
		return sigma;
	}

	public double getMedian() {
		return median;
	}

	/**
	 * Inner class to represent a single probability, or segment of a probabilistic date.
	 */
	class DateSegment {
		private double year;
		private double prob;
		
		DateSegment(double year, double prob) {
			this.year = year;
			this.prob = prob;
		}
		
		double getYear() {
			return this.year;
		}
		
		double getProb() {
			return this.prob;
		}
		
		public String toString() {
			return year + "," + prob;
		}
	}

/*	@Override
	public IUID getID() throws IOException, AutomationException {
		UID uid = new UID();
		String id = new String("{"+serialVersionUID+"}");
		uid.setValue(id);
		
		return uid;
	}

	@Override
	public void load(IVariantStream stream) throws IOException,
			AutomationException {
		mean = (Double) stream.read();
		sigma = (Double) stream.read();
		median = (Double) stream.read();
		interval = (Double) stream.read();
	}

	@Override
	public void save(IVariantStream stream) throws IOException,
			AutomationException {
		stream.write(mean);
		stream.write(sigma);
		stream.write(median);
		stream.write(interval);
	}*/
}
