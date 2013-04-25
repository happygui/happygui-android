package com.comp1008.happygui;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Translator {
	public static final int FORMAT_JSON = 1;
	public static final int FORMAT_XML = 2;
	public static final int FORMAT_HTML = 3;
	public static final int FORMAT_TOUCHDEVELOP = 4;


	public static String translate(String input, int inputFormat, int outputFormat) throws TranslationException {

		if(inputFormat == FORMAT_JSON && outputFormat == FORMAT_TOUCHDEVELOP) {
			return jsonToTouchDevelop(input);
		}
		
		/*
		if(inputFormat == FORMAT_JSON && outputFormat == FORMAT_TOUCHDEVELOP) {
			String xml = translate(input, FORMAT_JSON, FORMAT_XML); // translate first JSON -> XML, then XML -> TouchDevelop
			Log.d("Translator", xml);
			try {
				return xmlToTouchDevelop(xml);
			} catch (XmlPullParserException e) {
				throw new TranslationException(FORMAT_XML);
			}
		}*/
		
		/*if(inputFormat == FORMAT_JSON && outputFormat == FORMAT_XML) {
			JSONObject data;
			try {
				data = new JSONObject(input);
				return XML.toString(data);
			} catch (JSONException e) {
				throw new TranslationException(FORMAT_JSON);
			}
		}

		if(inputFormat == FORMAT_XML && outputFormat == FORMAT_JSON) {
			JSONObject data;
			try {
				data = XML.toJSONObject(input);
			} catch (JSONException e) {
				throw new TranslationException(FORMAT_XML);
			}
			return data.toString();
		}*/

		/*if(inputFormat == FORMAT_XML && outputFormat == FORMAT_HTML) {
			return xmlToHtml(input);
		}*/

		/*if(inputFormat == FORMAT_XML && outputFormat == FORMAT_TOUCHDEVELOP) {
			try {
				return xmlToTouchDevelop(input);
			} catch (XmlPullParserException e) {
				throw new TranslationException(FORMAT_XML);
			}
		}*/

		return null;
	}


	
	
	
	
	
	public static String jsonToTouchDevelop(String input) throws TranslationException {
		JSONObject data;
		
		try {
			data = new JSONObject(input);
			int imageNum = 0; // the number of images on the page (for variable naming)
			
			String output = "action main()\n";
			if(data.has("backgroundColor")) {
				output += "wall -> set background(" + colorToTouchDevelop(data.get("backgroundColor").toString(), 1) + ")\n";
			}
			output += "var page := media -> create picture(480, 600)\n";
			
			JSONArray elements = data.getJSONArray("elements");
			for(int i=0; i<elements.length(); i++) {
				JSONObject element = elements.optJSONObject(i);
				if(element != null && element.has("type")) {
					String type = element.optString("type");
					if(type == null) {
						continue;
					} else if(type.equals("circle")) {
						// Generate TouchDevelop code for circle
						int x = Integer.parseInt(element.get("x").toString());
						int y = Integer.parseInt(element.get("y").toString());
						int r = Integer.parseInt(element.get("r").toString());
						
						output += "page -> fill ellipse (" + (x - r)
								+ ", " + (y - r)
								+ ", " + (2 * r)
								+ ", " + (2 * r)
								+ ", 0"
								+ ", " + colorToTouchDevelop(element.get("backgroundColor").toString(), 0.8)
								+ ")\n";
						
						output += "page -> draw ellipse (" + (x - r)
								+ ", " + (y - r)
								+ ", " + (2 * r)
								+ ", " + (2 * r)
								+ ", 0"
								+ ", " + colorToTouchDevelop(element.get("borderColor").toString(), 0.8)
								+ ", " + element.get("borderThickness").toString()
								+ ")\n";

					} else if(type.equals("rect")) {
						// Generate TouchDevelop code for rectangle
						output += "page -> fill rect (" + element.get("x").toString()
								+ ", " + element.get("y").toString()
								+ ", " + element.get("width").toString()
								+ ", " + element.get("height").toString()
								+ ", 0"
								+ ", " + colorToTouchDevelop(element.get("backgroundColor").toString(), 0.8)
								+ ")\n";
						output += "page -> draw rect (" + element.get("x").toString()
								+ ", " + element.get("y").toString()
								+ ", " + element.get("width").toString()
								+ ", " + element.get("height").toString()
								+ ", 0"
								+ ", " + colorToTouchDevelop(element.get("borderColor").toString(), 0.8)
								+ ", " + element.get("borderThickness").toString()
								+ ")\n";

					}  else if(type.equals("text")) {
						// Generate TouchDevelop code for text
						output += "page -> draw text (" + element.get("x").toString()
								+ ", " + element.get("y").toString()
								+ ", \"PUT TEXT HERE\""
								+ ", " + element.get("fontSize").toString()
								+ ", 0"
								+ ", " + colorToTouchDevelop(element.get("fontColor").toString(), 0.8)
								+ "))\n";
						
					} else if(type.equals("image")) {
						// Generate TouchDevelop code for image
						imageNum ++;
						output += "var image" + imageNum + " := web -> download picture (\"Image " + imageNum + " URL\")\n";
						output += "image" + imageNum + " -> resize(" + element.get("width").toString()
								+ ", " + element.get("height").toString()
								+ ")\n";
						output += "page -> blend (image" + imageNum
								+ ", " + element.get("x").toString()
								+ ", " + element.get("y").toString()
								+ ", 0"
								+ ", 1"
								+ ")\n";
						
					} else {
						output += "// " + type + " \n";
					}
				}
			}
			
			
			output += "page -> post to wall";
			
			return output;
			
		} catch (JSONException e) {
			throw new TranslationException(FORMAT_JSON, e.getMessage());
		}
	}
	
	
	// Translate a colour from format "rgb(255,255,255)" to "color -> from argb(alpha,1,1,1)"
	public static String colorToTouchDevelop(String col, double alpha) {
		int rLeft = col.indexOf("(") + 1;
		int gLeft = col.indexOf(",") + 1;
		int bLeft = col.indexOf(",", gLeft) + 1;
		int bRight = col.indexOf(")");
		
		double r = Double.parseDouble(col.substring(rLeft, gLeft - 1)) / 255;
		double g = Double.parseDouble(col.substring(gLeft, bLeft - 1)) / 255;
		double b = Double.parseDouble(col.substring(bLeft, bRight)) / 255;
		
		DecimalFormat formatter = new DecimalFormat("#.##"); // Use up to 2 decimal places for each channel

		if(alpha == 1) {
			return "color -> from rgb (" + formatter.format(r) + ", " + formatter.format(g) + ", " + formatter.format(b) + ")";
		} else {
			return "color -> from argb (" + formatter.format(alpha) + ", " + formatter.format(r) + ", " + formatter.format(g) + ", " + formatter.format(b) + ")";
		}
	}
	
	
	
	
	/*
	public static String xmlToTouchDevelop(String input) throws XmlPullParserException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(new StringReader(input));
		int imageNum = 0; // the number of images on the page (for variable naming)
		
		String output = "action main()";
		output += "var page := media -> create picture(480, 600)\n";

		while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
			switch(parser.getEventType()) {			
			case XmlPullParser.START_TAG:
				String type = parser.getAttributeValue(null, "type");
				if(type == null) {
					output += "// " + parser.getName() + " tag" + " \n";
					for(int i=0; i<parser.getAttributeCount(); i++) {
						output += "// - " + parser.getAttributeName(i) + ": " + parser.getAttributeValue(i);
					}
					break;
				}
				if(type.equals("circle")) {
					// Generate TouchDevelop code for circle
					output += "page -> fill ellipse (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "backgroundColor"), 0.8)
							+ ")\n";
					output += "page -> draw ellipse (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "borderColor"), 0.8)
							+ ", " + parser.getAttributeValue(null, "borderThickness")
							+ ")\n";

				} else if(type.equals("rect")) {
					// Generate TouchDevelop code for rectangle
					output += "page -> fill rect (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "width")
							+ ", " + parser.getAttributeValue(null, "height")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "backgroundColor"), 0.8)
							+ ")\n";
					output += "page -> draw rect (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "width")
							+ ", " + parser.getAttributeValue(null, "height")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "borderColor"), 0.8)
							+ ", " + parser.getAttributeValue(null, "borderThickness")
							+ ")\n";

				}  else if(type.equals("text")) {
					// Generate TouchDevelop code for text
					output += "page -> draw text (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", \"< PUT TEXT HERE >\""
							+ ", " + parser.getAttributeValue(null, "fontSize")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "fontColor"), 0.8)
							+ "))\n";
				} else if(type.equals("image")) {
					// Generate TouchDevelop code for image
					imageNum ++;
					output += "var image" + imageNum + " := web -> download picture (\"<URL>\")\n";
					output += "image" + imageNum + " -> resize(" + parser.getAttributeValue(null, "width")
							+ ", " + parser.getAttributeValue(null, "height")
							+ ")\n";
					output += "page -> blend (image" + imageNum
							+ ", " + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", 0"
							+ ", 0"
							+ ")\n";
				} else {
					output += "// " + type + " \n";
				}
				break;

			case XmlPullParser.END_TAG:
				break;
			}

			try {
				parser.next();
			} catch (IOException e) {
				break;
			}

		}

		output += "page -> post to wall";
		return output;
	}
*/
	
	/*
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
				} else if(parser.getName().equals("TextElement")) {
					// Start textbox!HTML generation
					output += "<div style=\"position: absolute; left:" + parser.getAttributeValue(null, "x")
							+ "; top:" + parser.getAttributeValue(null, "y")
							+ "; width:" + parser.getAttributeValue(null, "width")
							+ "; height:" + parser.getAttributeValue(null, "height")
							+ "; background-color:" + parser.getAttributeValue(null, "backgroundColor")
							+ ";\">"
							+ "<font color=\"" + parser.getAttributeValue(null, "fontColor") // Takes RGB values; needs a method to convert to hex first
							+ "\" size=\"" + parser.getAttributeValue(null, "fontSize")
							+ "\">" + parser.getAttributeValue(null, "textContent")
							+ "</font>"
							+ "</div>";	
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
	}*/










}
