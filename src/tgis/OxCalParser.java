package tgis;

import java.io.File;
import java.io.FileReader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Module for converting an OxCal v4 raw javascript output into JSON objects.
 */
public class OxCalParser {
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js");
	private JSONParser parser = new JSONParser();

	private JSONArray ocd = null;
	private JSONObject model = null;

	/**
	 * Constructor taking an OxCal output file, each instance of the converter will convert one file.
	 * @param filename
	 */
	OxCalParser(File filename) {
		//Load OxCal Data
		try {
			String jsonLib = getJsonLib();
			FileReader input = new FileReader(filename);

			//Load the JSON2.js library into the js engine
			engine.eval(jsonLib);

			//define some globals (the js engine is more strict than a browser)
			engine.eval("var ocd = new Array(); var calib = new Array(); var model = new Object();");

			engine.eval(input);
			input.close();

			//Turn the objects we want into JSON format
			engine.eval("var json_ocd = JSON.stringify(ocd);");
			engine.eval("var json_model = JSON.stringify(model);");
		} catch (Exception e) {
			e.printStackTrace();		
		}
	}

	/**
	 * Extracts the OxCal data (ocd) from the project specified by this object.
	 * @return
	 */
	public JSONArray getOcd() {
		if (this.ocd == null) {
			String ocdString = (String) engine.get("json_ocd");
			JSONArray ocd = null;
			try {
				ocd = (JSONArray) parser.parse(ocdString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			this.ocd = ocd;
		}

		return this.ocd;
	}

	/**
	 * Extract the OxCal model from the project specified by this object.
	 * @return
	 */
	public JSONObject getModel() {
		if (this.model == null) {
			String modelString = (String) engine.get("json_model");
			JSONObject model = null;
			try {
				model = (JSONObject) parser.parse(modelString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			this.model = model;
		}

		return this.model;
	}

	/**
	 * Inject javascript to create child relationships in the hierarchical data (only represented as levels in the raw data).
	 */
	private void createChildRelationships() {		
		try {
			engine.eval( "ocd[0].children = new Array(); " +
					"for (var i = 1; i < ocd.length; i++) {" + "if (ocd[i]) {" +
					"ocd[i].children = new Array();" + "if(ocd[i].level == 0){" +
					"ocd[0].children.push(ocd[i]);" + "}" +
					"for (var j = i+1; j < ocd.length; j++) {" + "if (ocd[j]) {" +
					"if (ocd[j].level == ocd[i].level + 1) {" +
					"ocd[i].children.push(ocd[j])" + "}" +
					"else if (ocd[j].level <= ocd[i].level) {" + "break;" + "}" + "}"
					+ "}" + "}" + "}");
		} catch (ScriptException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Returns a string containing the JSON2.js library.
	 * @return
	 */
	private static String getJsonLib() {
		return "/*\r\n" + 
				"    json2.js\r\n" + 
				"    2012-10-08\r\n" + 
				"\r\n" + 
				"    Public Domain.\r\n" + 
				"\r\n" + 
				"    NO WARRANTY EXPRESSED OR IMPLIED. USE AT YOUR OWN RISK.\r\n" + 
				"\r\n" + 
				"    See http://www.JSON.org/js.html\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"    This code should be minified before deployment.\r\n" + 
				"    See http://javascript.crockford.com/jsmin.html\r\n" + 
				"\r\n" + 
				"    USE YOUR OWN COPY. IT IS EXTREMELY UNWISE TO LOAD CODE FROM SERVERS YOU DO\r\n" + 
				"    NOT CONTROL.\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"    This file creates a global JSON object containing two methods: stringify\r\n" + 
				"    and parse.\r\n" + 
				"\r\n" + 
				"        JSON.stringify(value, replacer, space)\r\n" + 
				"            value       any JavaScript value, usually an object or array.\r\n" + 
				"\r\n" + 
				"            replacer    an optional parameter that determines how object\r\n" + 
				"                        values are stringified for objects. It can be a\r\n" + 
				"                        function or an array of strings.\r\n" + 
				"\r\n" + 
				"            space       an optional parameter that specifies the indentation\r\n" + 
				"                        of nested structures. If it is omitted, the text will\r\n" + 
				"                        be packed without extra whitespace. If it is a number,\r\n" + 
				"                        it will specify the number of spaces to indent at each\r\n" + 
				"                        level. If it is a string (such as '\\t' or '&nbsp;'),\r\n" + 
				"                        it contains the characters used to indent at each level.\r\n" + 
				"\r\n" + 
				"            This method produces a JSON text from a JavaScript value.\r\n" + 
				"\r\n" + 
				"            When an object value is found, if the object contains a toJSON\r\n" + 
				"            method, its toJSON method will be called and the result will be\r\n" + 
				"            stringified. A toJSON method does not serialize: it returns the\r\n" + 
				"            value represented by the name/value pair that should be serialized,\r\n" + 
				"            or undefined if nothing should be serialized. The toJSON method\r\n" + 
				"            will be passed the key associated with the value, and this will be\r\n" + 
				"            bound to the value\r\n" + 
				"\r\n" + 
				"            For example, this would serialize Dates as ISO strings.\r\n" + 
				"\r\n" + 
				"                Date.prototype.toJSON = function (key) {\r\n" + 
				"                    function f(n) {\r\n" + 
				"                        // Format integers to have at least two digits.\r\n" + 
				"                        return n < 10 ? '0' + n : n;\r\n" + 
				"                    }\r\n" + 
				"\r\n" + 
				"                    return this.getUTCFullYear()   + '-' +\r\n" + 
				"                         f(this.getUTCMonth() + 1) + '-' +\r\n" + 
				"                         f(this.getUTCDate())      + 'T' +\r\n" + 
				"                         f(this.getUTCHours())     + ':' +\r\n" + 
				"                         f(this.getUTCMinutes())   + ':' +\r\n" + 
				"                         f(this.getUTCSeconds())   + 'Z';\r\n" + 
				"                };\r\n" + 
				"\r\n" + 
				"            You can provide an optional replacer method. It will be passed the\r\n" + 
				"            key and value of each member, with this bound to the containing\r\n" + 
				"            object. The value that is returned from your method will be\r\n" + 
				"            serialized. If your method returns undefined, then the member will\r\n" + 
				"            be excluded from the serialization.\r\n" + 
				"\r\n" + 
				"            If the replacer parameter is an array of strings, then it will be\r\n" + 
				"            used to select the members to be serialized. It filters the results\r\n" + 
				"            such that only members with keys listed in the replacer array are\r\n" + 
				"            stringified.\r\n" + 
				"\r\n" + 
				"            Values that do not have JSON representations, such as undefined or\r\n" + 
				"            functions, will not be serialized. Such values in objects will be\r\n" + 
				"            dropped; in arrays they will be replaced with null. You can use\r\n" + 
				"            a replacer function to replace those with JSON values.\r\n" + 
				"            JSON.stringify(undefined) returns undefined.\r\n" + 
				"\r\n" + 
				"            The optional space parameter produces a stringification of the\r\n" + 
				"            value that is filled with line breaks and indentation to make it\r\n" + 
				"            easier to read.\r\n" + 
				"\r\n" + 
				"            If the space parameter is a non-empty string, then that string will\r\n" + 
				"            be used for indentation. If the space parameter is a number, then\r\n" + 
				"            the indentation will be that many spaces.\r\n" + 
				"\r\n" + 
				"            Example:\r\n" + 
				"\r\n" + 
				"            text = JSON.stringify(['e', {pluribus: 'unum'}]);\r\n" + 
				"            // text is '[\"e\",{\"pluribus\":\"unum\"}]'\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"            text = JSON.stringify(['e', {pluribus: 'unum'}], null, '\\t');\r\n" + 
				"            // text is '[\\n\\t\"e\",\\n\\t{\\n\\t\\t\"pluribus\": \"unum\"\\n\\t}\\n]'\r\n" + 
				"\r\n" + 
				"            text = JSON.stringify([new Date()], function (key, value) {\r\n" + 
				"                return this[key] instanceof Date ?\r\n" + 
				"                    'Date(' + this[key] + ')' : value;\r\n" + 
				"            });\r\n" + 
				"            // text is '[\"Date(---current time---)\"]'\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"        JSON.parse(text, reviver)\r\n" + 
				"            This method parses a JSON text to produce an object or array.\r\n" + 
				"            It can throw a SyntaxError exception.\r\n" + 
				"\r\n" + 
				"            The optional reviver parameter is a function that can filter and\r\n" + 
				"            transform the results. It receives each of the keys and values,\r\n" + 
				"            and its return value is used instead of the original value.\r\n" + 
				"            If it returns what it received, then the structure is not modified.\r\n" + 
				"            If it returns undefined then the member is deleted.\r\n" + 
				"\r\n" + 
				"            Example:\r\n" + 
				"\r\n" + 
				"            // Parse the text. Values that look like ISO date strings will\r\n" + 
				"            // be converted to Date objects.\r\n" + 
				"\r\n" + 
				"            myData = JSON.parse(text, function (key, value) {\r\n" + 
				"                var a;\r\n" + 
				"                if (typeof value === 'string') {\r\n" + 
				"                    a =\r\n" + 
				"/^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2}(?:\\.\\d*)?)Z$/.exec(value);\r\n" + 
				"                    if (a) {\r\n" + 
				"                        return new Date(Date.UTC(+a[1], +a[2] - 1, +a[3], +a[4],\r\n" + 
				"                            +a[5], +a[6]));\r\n" + 
				"                    }\r\n" + 
				"                }\r\n" + 
				"                return value;\r\n" + 
				"            });\r\n" + 
				"\r\n" + 
				"            myData = JSON.parse('[\"Date(09/09/2001)\"]', function (key, value) {\r\n" + 
				"                var d;\r\n" + 
				"                if (typeof value === 'string' &&\r\n" + 
				"                        value.slice(0, 5) === 'Date(' &&\r\n" + 
				"                        value.slice(-1) === ')') {\r\n" + 
				"                    d = new Date(value.slice(5, -1));\r\n" + 
				"                    if (d) {\r\n" + 
				"                        return d;\r\n" + 
				"                    }\r\n" + 
				"                }\r\n" + 
				"                return value;\r\n" + 
				"            });\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"    This is a reference implementation. You are free to copy, modify, or\r\n" + 
				"    redistribute.\r\n" + 
				"*/\r\n" + 
				"\r\n" + 
				"/*jslint evil: true, regexp: true */\r\n" + 
				"\r\n" + 
				"/*members \"\", \"\\b\", \"\\t\", \"\\n\", \"\\f\", \"\\r\", \"\\\"\", JSON, \"\\\\\", apply,\r\n" + 
				"    call, charCodeAt, getUTCDate, getUTCFullYear, getUTCHours,\r\n" + 
				"    getUTCMinutes, getUTCMonth, getUTCSeconds, hasOwnProperty, join,\r\n" + 
				"    lastIndex, length, parse, prototype, push, replace, slice, stringify,\r\n" + 
				"    test, toJSON, toString, valueOf\r\n" + 
				"*/\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"// Create a JSON object only if one does not already exist. We create the\r\n" + 
				"// methods in a closure to avoid creating global variables.\r\n" + 
				"\r\n" + 
				"if (typeof JSON !== 'object') {\r\n" + 
				"    JSON = {};\r\n" + 
				"}\r\n" + 
				"\r\n" + 
				"(function () {\r\n" + 
				"    'use strict';\r\n" + 
				"\r\n" + 
				"    function f(n) {\r\n" + 
				"        // Format integers to have at least two digits.\r\n" + 
				"        return n < 10 ? '0' + n : n;\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    if (typeof Date.prototype.toJSON !== 'function') {\r\n" + 
				"\r\n" + 
				"        Date.prototype.toJSON = function (key) {\r\n" + 
				"\r\n" + 
				"            return isFinite(this.valueOf())\r\n" + 
				"                ? this.getUTCFullYear()     + '-' +\r\n" + 
				"                    f(this.getUTCMonth() + 1) + '-' +\r\n" + 
				"                    f(this.getUTCDate())      + 'T' +\r\n" + 
				"                    f(this.getUTCHours())     + ':' +\r\n" + 
				"                    f(this.getUTCMinutes())   + ':' +\r\n" + 
				"                    f(this.getUTCSeconds())   + 'Z'\r\n" + 
				"                : null;\r\n" + 
				"        };\r\n" + 
				"\r\n" + 
				"        String.prototype.toJSON      =\r\n" + 
				"            Number.prototype.toJSON  =\r\n" + 
				"            Boolean.prototype.toJSON = function (key) {\r\n" + 
				"                return this.valueOf();\r\n" + 
				"            };\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"    var cx = /[\\u0000\\u00ad\\u0600-\\u0604\\u070f\\u17b4\\u17b5\\u200c-\\u200f\\u2028-\\u202f\\u2060-\\u206f\\ufeff\\ufff0-\\uffff]/g,\r\n" + 
				"        escapable = /[\\\\\\\"\\x00-\\x1f\\x7f-\\x9f\\u00ad\\u0600-\\u0604\\u070f\\u17b4\\u17b5\\u200c-\\u200f\\u2028-\\u202f\\u2060-\\u206f\\ufeff\\ufff0-\\uffff]/g,\r\n" + 
				"        gap,\r\n" + 
				"        indent,\r\n" + 
				"        meta = {    // table of character substitutions\r\n" + 
				"            '\\b': '\\\\b',\r\n" + 
				"            '\\t': '\\\\t',\r\n" + 
				"            '\\n': '\\\\n',\r\n" + 
				"            '\\f': '\\\\f',\r\n" + 
				"            '\\r': '\\\\r',\r\n" + 
				"            '\"' : '\\\\\"',\r\n" + 
				"            '\\\\': '\\\\\\\\'\r\n" + 
				"        },\r\n" + 
				"        rep;\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"    function quote(string) {\r\n" + 
				"\r\n" + 
				"// If the string contains no control characters, no quote characters, and no\r\n" + 
				"// backslash characters, then we can safely slap some quotes around it.\r\n" + 
				"// Otherwise we must also replace the offending characters with safe escape\r\n" + 
				"// sequences.\r\n" + 
				"\r\n" + 
				"        escapable.lastIndex = 0;\r\n" + 
				"        return escapable.test(string) ? '\"' + string.replace(escapable, function (a) {\r\n" + 
				"            var c = meta[a];\r\n" + 
				"            return typeof c === 'string'\r\n" + 
				"                ? c\r\n" + 
				"                : '\\\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);\r\n" + 
				"        }) + '\"' : '\"' + string + '\"';\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"    function str(key, holder) {\r\n" + 
				"\r\n" + 
				"// Produce a string from holder[key].\r\n" + 
				"\r\n" + 
				"        var i,          // The loop counter.\r\n" + 
				"            k,          // The member key.\r\n" + 
				"            v,          // The member value.\r\n" + 
				"            length,\r\n" + 
				"            mind = gap,\r\n" + 
				"            partial,\r\n" + 
				"            value = holder[key];\r\n" + 
				"\r\n" + 
				"// If the value has a toJSON method, call it to obtain a replacement value.\r\n" + 
				"\r\n" + 
				"        if (value && typeof value === 'object' &&\r\n" + 
				"                typeof value.toJSON === 'function') {\r\n" + 
				"            value = value.toJSON(key);\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"// If we were called with a replacer function, then call the replacer to\r\n" + 
				"// obtain a replacement value.\r\n" + 
				"\r\n" + 
				"        if (typeof rep === 'function') {\r\n" + 
				"            value = rep.call(holder, key, value);\r\n" + 
				"        }\r\n" + 
				"\r\n" + 
				"// What happens next depends on the value's type.\r\n" + 
				"\r\n" + 
				"        switch (typeof value) {\r\n" + 
				"        case 'string':\r\n" + 
				"            return quote(value);\r\n" + 
				"\r\n" + 
				"        case 'number':\r\n" + 
				"\r\n" + 
				"// JSON numbers must be finite. Encode non-finite numbers as null.\r\n" + 
				"\r\n" + 
				"            return isFinite(value) ? String(value) : 'null';\r\n" + 
				"\r\n" + 
				"        case 'boolean':\r\n" + 
				"        case 'null':\r\n" + 
				"\r\n" + 
				"// If the value is a boolean or null, convert it to a string. Note:\r\n" + 
				"// typeof null does not produce 'null'. The case is included here in\r\n" + 
				"// the remote chance that this gets fixed someday.\r\n" + 
				"\r\n" + 
				"            return String(value);\r\n" + 
				"\r\n" + 
				"// If the type is 'object', we might be dealing with an object or an array or\r\n" + 
				"// null.\r\n" + 
				"\r\n" + 
				"        case 'object':\r\n" + 
				"\r\n" + 
				"// Due to a specification blunder in ECMAScript, typeof null is 'object',\r\n" + 
				"// so watch out for that case.\r\n" + 
				"\r\n" + 
				"            if (!value) {\r\n" + 
				"                return 'null';\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"// Make an array to hold the partial results of stringifying this object value.\r\n" + 
				"\r\n" + 
				"            gap += indent;\r\n" + 
				"            partial = [];\r\n" + 
				"\r\n" + 
				"// Is the value an array?\r\n" + 
				"\r\n" + 
				"            if (Object.prototype.toString.apply(value) === '[object Array]') {\r\n" + 
				"\r\n" + 
				"// The value is an array. Stringify every element. Use null as a placeholder\r\n" + 
				"// for non-JSON values.\r\n" + 
				"\r\n" + 
				"                length = value.length;\r\n" + 
				"                for (i = 0; i < length; i += 1) {\r\n" + 
				"                    partial[i] = str(i, value) || 'null';\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"// Join all of the elements together, separated with commas, and wrap them in\r\n" + 
				"// brackets.\r\n" + 
				"\r\n" + 
				"                v = partial.length === 0\r\n" + 
				"                    ? '[]'\r\n" + 
				"                    : gap\r\n" + 
				"                    ? '[\\n' + gap + partial.join(',\\n' + gap) + '\\n' + mind + ']'\r\n" + 
				"                    : '[' + partial.join(',') + ']';\r\n" + 
				"                gap = mind;\r\n" + 
				"                return v;\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"// If the replacer is an array, use it to select the members to be stringified.\r\n" + 
				"\r\n" + 
				"            if (rep && typeof rep === 'object') {\r\n" + 
				"                length = rep.length;\r\n" + 
				"                for (i = 0; i < length; i += 1) {\r\n" + 
				"                    if (typeof rep[i] === 'string') {\r\n" + 
				"                        k = rep[i];\r\n" + 
				"                        v = str(k, value);\r\n" + 
				"                        if (v) {\r\n" + 
				"                            partial.push(quote(k) + (gap ? ': ' : ':') + v);\r\n" + 
				"                        }\r\n" + 
				"                    }\r\n" + 
				"                }\r\n" + 
				"            } else {\r\n" + 
				"\r\n" + 
				"// Otherwise, iterate through all of the keys in the object.\r\n" + 
				"\r\n" + 
				"                for (k in value) {\r\n" + 
				"                    if (Object.prototype.hasOwnProperty.call(value, k)) {\r\n" + 
				"                        v = str(k, value);\r\n" + 
				"                        if (v) {\r\n" + 
				"                            partial.push(quote(k) + (gap ? ': ' : ':') + v);\r\n" + 
				"                        }\r\n" + 
				"                    }\r\n" + 
				"                }\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"// Join all of the member texts together, separated with commas,\r\n" + 
				"// and wrap them in braces.\r\n" + 
				"\r\n" + 
				"            v = partial.length === 0\r\n" + 
				"                ? '{}'\r\n" + 
				"                : gap\r\n" + 
				"                ? '{\\n' + gap + partial.join(',\\n' + gap) + '\\n' + mind + '}'\r\n" + 
				"                : '{' + partial.join(',') + '}';\r\n" + 
				"            gap = mind;\r\n" + 
				"            return v;\r\n" + 
				"        }\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"// If the JSON object does not yet have a stringify method, give it one.\r\n" + 
				"\r\n" + 
				"    if (typeof JSON.stringify !== 'function') {\r\n" + 
				"        JSON.stringify = function (value, replacer, space) {\r\n" + 
				"\r\n" + 
				"// The stringify method takes a value and an optional replacer, and an optional\r\n" + 
				"// space parameter, and returns a JSON text. The replacer can be a function\r\n" + 
				"// that can replace values, or an array of strings that will select the keys.\r\n" + 
				"// A default replacer method can be provided. Use of the space parameter can\r\n" + 
				"// produce text that is more easily readable.\r\n" + 
				"\r\n" + 
				"            var i;\r\n" + 
				"            gap = '';\r\n" + 
				"            indent = '';\r\n" + 
				"\r\n" + 
				"// If the space parameter is a number, make an indent string containing that\r\n" + 
				"// many spaces.\r\n" + 
				"\r\n" + 
				"            if (typeof space === 'number') {\r\n" + 
				"                for (i = 0; i < space; i += 1) {\r\n" + 
				"                    indent += ' ';\r\n" + 
				"                }\r\n" + 
				"\r\n" + 
				"// If the space parameter is a string, it will be used as the indent string.\r\n" + 
				"\r\n" + 
				"            } else if (typeof space === 'string') {\r\n" + 
				"                indent = space;\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"// If there is a replacer, it must be a function or an array.\r\n" + 
				"// Otherwise, throw an error.\r\n" + 
				"\r\n" + 
				"            rep = replacer;\r\n" + 
				"            if (replacer && typeof replacer !== 'function' &&\r\n" + 
				"                    (typeof replacer !== 'object' ||\r\n" + 
				"                    typeof replacer.length !== 'number')) {\r\n" + 
				"                throw new Error('JSON.stringify');\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"// Make a fake root object containing our value under the key of ''.\r\n" + 
				"// Return the result of stringifying the value.\r\n" + 
				"\r\n" + 
				"            return str('', {'': value});\r\n" + 
				"        };\r\n" + 
				"    }\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"// If the JSON object does not yet have a parse method, give it one.\r\n" + 
				"\r\n" + 
				"    if (typeof JSON.parse !== 'function') {\r\n" + 
				"        JSON.parse = function (text, reviver) {\r\n" + 
				"\r\n" + 
				"// The parse method takes a text and an optional reviver function, and returns\r\n" + 
				"// a JavaScript value if the text is a valid JSON text.\r\n" + 
				"\r\n" + 
				"            var j;\r\n" + 
				"\r\n" + 
				"            function walk(holder, key) {\r\n" + 
				"\r\n" + 
				"// The walk method is used to recursively walk the resulting structure so\r\n" + 
				"// that modifications can be made.\r\n" + 
				"\r\n" + 
				"                var k, v, value = holder[key];\r\n" + 
				"                if (value && typeof value === 'object') {\r\n" + 
				"                    for (k in value) {\r\n" + 
				"                        if (Object.prototype.hasOwnProperty.call(value, k)) {\r\n" + 
				"                            v = walk(value, k);\r\n" + 
				"                            if (v !== undefined) {\r\n" + 
				"                                value[k] = v;\r\n" + 
				"                            } else {\r\n" + 
				"                                delete value[k];\r\n" + 
				"                            }\r\n" + 
				"                        }\r\n" + 
				"                    }\r\n" + 
				"                }\r\n" + 
				"                return reviver.call(holder, key, value);\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"\r\n" + 
				"// Parsing happens in four stages. In the first stage, we replace certain\r\n" + 
				"// Unicode characters with escape sequences. JavaScript handles many characters\r\n" + 
				"// incorrectly, either silently deleting them, or treating them as line endings.\r\n" + 
				"\r\n" + 
				"            text = String(text);\r\n" + 
				"            cx.lastIndex = 0;\r\n" + 
				"            if (cx.test(text)) {\r\n" + 
				"                text = text.replace(cx, function (a) {\r\n" + 
				"                    return '\\\\u' +\r\n" + 
				"                        ('0000' + a.charCodeAt(0).toString(16)).slice(-4);\r\n" + 
				"                });\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"// In the second stage, we run the text against regular expressions that look\r\n" + 
				"// for non-JSON patterns. We are especially concerned with '()' and 'new'\r\n" + 
				"// because they can cause invocation, and '=' because it can cause mutation.\r\n" + 
				"// But just to be safe, we want to reject all unexpected forms.\r\n" + 
				"\r\n" + 
				"// We split the second stage into 4 regexp operations in order to work around\r\n" + 
				"// crippling inefficiencies in IE's and Safari's regexp engines. First we\r\n" + 
				"// replace the JSON backslash pairs with '@' (a non-JSON character). Second, we\r\n" + 
				"// replace all simple value tokens with ']' characters. Third, we delete all\r\n" + 
				"// open brackets that follow a colon or comma or that begin the text. Finally,\r\n" + 
				"// we look to see that the remaining characters are only whitespace or ']' or\r\n" + 
				"// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.\r\n" + 
				"\r\n" + 
				"            if (/^[\\],:{}\\s]*$/\r\n" + 
				"                    .test(text.replace(/\\\\(?:[\"\\\\\\/bfnrt]|u[0-9a-fA-F]{4})/g, '@')\r\n" + 
				"                        .replace(/\"[^\"\\\\\\n\\r]*\"|true|false|null|-?\\d+(?:\\.\\d*)?(?:[eE][+\\-]?\\d+)?/g, ']')\r\n" + 
				"                        .replace(/(?:^|:|,)(?:\\s*\\[)+/g, ''))) {\r\n" + 
				"\r\n" + 
				"// In the third stage we use the eval function to compile the text into a\r\n" + 
				"// JavaScript structure. The '{' operator is subject to a syntactic ambiguity\r\n" + 
				"// in JavaScript: it can begin a block or an object literal. We wrap the text\r\n" + 
				"// in parens to eliminate the ambiguity.\r\n" + 
				"\r\n" + 
				"                j = eval('(' + text + ')');\r\n" + 
				"\r\n" + 
				"// In the optional fourth stage, we recursively walk the new structure, passing\r\n" + 
				"// each name/value pair to a reviver function for possible transformation.\r\n" + 
				"\r\n" + 
				"                return typeof reviver === 'function'\r\n" + 
				"                    ? walk({'': j}, '')\r\n" + 
				"                    : j;\r\n" + 
				"            }\r\n" + 
				"\r\n" + 
				"// If the text is not JSON parseable, then a SyntaxError is thrown.\r\n" + 
				"\r\n" + 
				"            throw new SyntaxError('JSON.parse');\r\n" + 
				"        };\r\n" + 
				"    }\r\n" + 
				"}());\r\n" + 
				"";
	}
}
