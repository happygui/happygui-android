package com.comp1008.happygui;

import java.io.StringReader;

import org.json.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

public class Translator {
	public static final int FORMAT_JSON = 1;
	public static final int FORMAT_XML = 2;
	public static final int FORMAT_HTML = 3;
	public static final int FORMAT_TOUCHDEVELOP = 4;
	
	public static String translate(String input, int inputFormat, int outputFormat) throws Exception {
		
		if(inputFormat == FORMAT_JSON && outputFormat == FORMAT_XML) {
			JSONObject data = new JSONObject(input);
			return XML.toString(data);
		}
		
		if(inputFormat == FORMAT_XML && outputFormat == FORMAT_JSON) {
			JSONObject data = XML.toJSONObject(input);
			return data.toString();
		}
		
		if(inputFormat == FORMAT_XML && (outputFormat == FORMAT_HTML || outputFormat == FORMAT_TOUCHDEVELOP)) {
			String output = "";
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(input));
			
			while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch(parser.getEventType()) {
				case XmlPullParser.START_DOCUMENT:
					if(outputFormat == FORMAT_HTML)
						output += "<html>\n<body>\n";
					break;
			
				case XmlPullParser.START_TAG:
					if(outputFormat == FORMAT_HTML)
						output += "<!--" + parser.getName() + " tag-->\n";
					else if(outputFormat == FORMAT_TOUCHDEVELOP)
						output += "//" + parser.getName() + " tag\n";
					break;
				
				case XmlPullParser.END_TAG:
					break;
				}
				
				parser.next();

			}
			
			return output;
		}
		
		throw new Exception();
	}
}
