package com.comp1008.happygui;

import java.io.IOException;
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
		
		
		if(inputFormat == FORMAT_XML && outputFormat == FORMAT_HTML) {
			return xmlToHtml(input);
		}
		
		if(inputFormat == FORMAT_XML && outputFormat == FORMAT_TOUCHDEVELOP) {
			return xmlToTouchDevelop(input);
		}
		
		return null;
	}
	
	public static String xmlToHtml(String input) throws XmlPullParserException, IOException {
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(input));
			
			String output = "<html>\n<body>\n";
			
			while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch(parser.getEventType()) {			
				case XmlPullParser.START_TAG:
					if(parser.getName().equals("CircleElement")) {
						// Start circle!HTML generation
						output += "<circle cx=\"" + parser.getAttributeValue(null, "x")
								+ "\" cy=\"" + parser.getAttributeValue(null, "y")
								+ "\" r=\"" + parser.getAttributeValue(null, "radius")
								+ "\" fill=\"" + parser.getAttributeValue(null, "backgroundColor")
								+ "\" stroke=\"" + parser.getAttributeValue(null, "borderColor")
								+ "\" stroke-width=\"" + parser.getAttributeValue(null, "borderThickness")
								+ "\" />";
					} else if(parser.getName().equals("RectElement")) {
						// Start rectangle!HTML generation
						output += "<rect x=\"" + parser.getAttributeValue(null, "x")
								+ "\" y=\"" + parser.getAttributeValue(null, "y")
								+ "\" width=\"" + parser.getAttributeValue(null, "width")
								+ "\" height=\"" + parser.getAttributeValue(null, "height")
								+ "\" fill=\"" + parser.getAttributeValue(null, "backgroundColor")
								+ "\" stroke=\"" + parser.getAttributeValue(null, "borderColor")
								+ "\" stroke-width=\"" + parser.getAttributeValue(null, "borderThickness")
								+ "\" />";	
					} else {
						output += "<!--" + parser.getName() + " tag -->\n";
					}
					break;
				
				case XmlPullParser.END_TAG:
					break;
				}
				
				parser.next();

			}

			output += "</body>\n</html>";
			return output;
	}
	
	
	
	
	
	
	
	
	
	public static String xmlToTouchDevelop(String input) throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(new StringReader(input));
		
		String output = "var page := media -> create picture(123,456)\n";
		
		while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
			switch(parser.getEventType()) {			
			case XmlPullParser.START_TAG:
				if(parser.getName().equals("CircleElement")) {
					// Start circle!touchdevelop generation
					output += "page -> fill elipse(" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", 0"
							+ ", colors -> from argb (BLARGLIEHDVFJN)"
							+ ")\n";
					output += "page -> draw elipse(" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", 0"
							+ ", colors -> from argb (BLARGLIEHDVFJN)"
							+ ", " + parser.getAttributeValue(null, "borderThickness")
							+ ")\n";
				
				} else if(parser.getName().equals("RectElement")) {
					// Start rectangle!touchdevelop generation
					output += "page -> fill rect(" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "width")
							+ ", " + parser.getAttributeValue(null, "height")
							+ ", 0"
							+ ", colors -> from argb (BLARGLIEHDVFJN)"
							+ ")\n";
					output += "page -> draw rect(" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "width")
							+ ", " + parser.getAttributeValue(null, "height")
							+ ", 0"
							+ ", colors -> from argb (BLARGLIEHDVFJN)"
							+ ", " + parser.getAttributeValue(null, "borderThickness")
							+ ")\n";
					
				} else {
					output += "// " + parser.getName() + " tag ";
				}
				break;
			
			case XmlPullParser.END_TAG:
				break;
			}
			
			parser.next();

		}

		output += "page -> post to wall";
		return output;
}
}
