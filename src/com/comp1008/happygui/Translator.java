package com.comp1008.happygui;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

// Helper class for translating between formats / languages
public class Translator {
	public static final int FORMAT_JSON = 1;
	public static final int FORMAT_XML = 2;
	public static final int FORMAT_HTML = 3;
	public static final int FORMAT_TOUCHDEVELOP = 4;

	
	private static final String STYLE_KEYWORD = "pink_txt";
	private static final String STYLE_STRING = "blue_txt";
	private static final String STYLE_NUMBER = "dgray_txt";
	private static final String STYLE_COMMENT = "green_txt";
	
	private static final String START_LINE = "<span class=\"code_line\">";
	private static final String END_LINE = "</span><br/>";
	
	

	public static String translate(String input, int inputFormat, int outputFormat) throws TranslationException {
		
		if(inputFormat == FORMAT_JSON && outputFormat == FORMAT_TOUCHDEVELOP) {
			return jsonToTouchDevelop(input);
		}
		
		/*
		if(inputFormat == FORMAT_JSON && outputFormat == FORMAT_TOUCHDEVELOP) {
			String xml = translate(input, FORMAT_JSON, FORMAT_XML); // translate first JSON &rarr; XML, then XML &rarr; TouchDevelop
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


	
	
	
	
	//convert json-formatted description of a page to touchdevelop code
	public static String jsonToTouchDevelop(String input) throws TranslationException {
		JSONObject data;
		
		try {
			data = new JSONObject(input);
			int imageNum = 0; // the number of images on the page (for variable naming)
			
			String output = START_LINE + stylise(STYLE_COMMENT, "// " + data.get("name").toString()) + END_LINE; // Include the name of the page as a comment
			
			output += START_LINE + stylise(STYLE_KEYWORD, "action") + " main()" + END_LINE;
			
			if(data.has("backgroundColor")) {
				output += START_LINE + "wall &rarr; set background(" + colorToTouchDevelop(data.get("backgroundColor").toString(), 1) + ")" + END_LINE;
			}
			output += START_LINE + stylise(STYLE_KEYWORD, "var")
					+ " page := media &rarr; create picture(" + stylise(STYLE_NUMBER, 480) + ", " + stylise(STYLE_NUMBER, 600)
					+ ")" + END_LINE;
			
			JSONArray elements = data.getJSONArray("elements");
			// Loop through list of elements in the page
			for(int i=0; i<elements.length(); i++) {
				JSONObject element = elements.optJSONObject(i);
				if(element != null && element.has("type")) {
					
					String type = element.optString("type");	// The element type
					if(type == null) {
						continue;
					} else if(type.equals("circle")) {
						// Generate TouchDevelop code for circle
						int x = Integer.parseInt(element.get("x").toString());
						int y = Integer.parseInt(element.get("y").toString());
						int r = Integer.parseInt(element.get("r").toString()); //circle radius
						
						// first draw fill
						output += START_LINE + "page &rarr; fill ellipse ("
								+ stylise(STYLE_NUMBER, (x - r))		// left
								+ ", " + stylise(STYLE_NUMBER, (y - r)) // top
								+ ", " + stylise(STYLE_NUMBER, (2 * r))	// width
								+ ", " + stylise(STYLE_NUMBER, (2 * r))	// height
								+ ", " + stylise(STYLE_NUMBER, 0)		// angle
								+ ", " + colorToTouchDevelop(element.get("backgroundColor").toString(), 0.8) // fill colour
								+ ")" + END_LINE;
						
						// ...then draw border
						output += START_LINE + "page &rarr; draw ellipse ("
								+ stylise(STYLE_NUMBER, (x - r))		// left
								+ ", " + stylise(STYLE_NUMBER, (y - r))	// top
								+ ", " + stylise(STYLE_NUMBER, (2 * r))	// width
								+ ", " + stylise(STYLE_NUMBER, (2 * r))	// height
								+ ", " + stylise(STYLE_NUMBER, 0)		// angle
								+ ", " + colorToTouchDevelop(element.get("borderColor").toString(), 0.8)	// border colour
								+ ", " + stylise(STYLE_NUMBER, element.get("borderThickness").toString())	// border thickness
								+ ")" + END_LINE;

					} else if(type.equals("rect")) {
						// Generate TouchDevelop code for rectangle
						
						// first draw fill
						output += START_LINE + "page &rarr; fill rect ("
								+ stylise(STYLE_NUMBER, element.get("x").toString())							// left
								+ ", " + stylise(STYLE_NUMBER, element.get("y").toString())						// top
								+ ", " + stylise(STYLE_NUMBER, element.get("width").toString())					// width
								+ ", " + stylise(STYLE_NUMBER, element.get("height").toString())				// height
								+ ", " + stylise(STYLE_NUMBER, 0)												// angle
								+ ", " + colorToTouchDevelop(element.get("backgroundColor").toString(), 0.8)	//fill colour
								+ ")" + END_LINE;
						
						// ... then draw border
						output += START_LINE + "page &rarr; draw rect ("
								+ stylise(STYLE_NUMBER, element.get("x").toString())							// left
								+ ", " + stylise(STYLE_NUMBER, element.get("y").toString())						// top
								+ ", " + stylise(STYLE_NUMBER, element.get("width").toString())					// width
								+ ", " + stylise(STYLE_NUMBER, element.get("height").toString())				// height
								+ ", " + stylise(STYLE_NUMBER, 0)												// angle
								+ ", " + colorToTouchDevelop(element.get("borderColor").toString(), 0.8)		// border colour
								+ ", " + stylise(STYLE_NUMBER, element.get("borderThickness").toString())		// border thickness
								+ ")" + END_LINE;

					}  else if(type.equals("text")) {
						// Generate TouchDevelop code for text
						
						output += START_LINE + "page &rarr; draw text ("
								+ stylise(STYLE_NUMBER, element.get("x").toString())							// left
								+ ", " + stylise(STYLE_NUMBER, element.get("y").toString())						// top
								+ ", " + stylise(STYLE_STRING, "\"" + element.get("text").toString() + "\"")	// text
								+ ", " + stylise(STYLE_NUMBER, element.get("fontSize").toString())				// font size
								+ ", " + stylise(STYLE_NUMBER, 0)												// angle
								+ ", " + colorToTouchDevelop(element.get("fontColor").toString(), 0.8)			// colour
								+ ")" + END_LINE;
						
					} else if(type.equals("image")) {
						// Generate TouchDevelop code for image
						
						imageNum ++;
						output += START_LINE + stylise(STYLE_KEYWORD, "var") + " image" + imageNum
								+ " := web &rarr; download picture ("
								+ stylise(STYLE_STRING, "\"Image " + imageNum + " URL\"")
								+ ")" + END_LINE;																// get picture
						
						output += START_LINE + "image" + imageNum + " &rarr; resize("
								+ stylise(STYLE_NUMBER, element.get("width").toString())
								+ ", " + stylise(STYLE_NUMBER, element.get("height").toString())				//resize to correct proportions
								+ ")" + END_LINE;
						
						output += START_LINE + "page &rarr; blend ("											// add image to 'page'
								+ "image" + imageNum															
								+ ", " + stylise(STYLE_NUMBER, element.get("x").toString())						// left
								+ ", " + stylise(STYLE_NUMBER, element.get("y").toString())						// top
								+ ", " + stylise(STYLE_NUMBER, 0)												// angle
								+ ", " + stylise(STYLE_NUMBER, 1)												// opacity
								+ ")" + END_LINE;
						
					} else {
						output += START_LINE + stylise(STYLE_COMMENT, "// " + type) + END_LINE;					// element of unknown type
					}
				}
			}
			
			
			output += START_LINE + "page &rarr; post to wall" + END_LINE;
			return output;
			
		} catch (JSONException e) {
			throw new TranslationException(FORMAT_JSON, e.getMessage());
		}
	}
	
	
	// Translate a colour from format "rgb(255,255,255)" to "color &rarr; from argb(alpha,1,1,1)"
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
			return "color &rarr; from rgb (" + stylise(STYLE_NUMBER, formatter.format(r)) + ", " + stylise(STYLE_NUMBER, formatter.format(g)) + ", " + stylise(STYLE_NUMBER, formatter.format(b)) + ")";
		} else {
			return "color &rarr; from argb (" + stylise(STYLE_NUMBER, formatter.format(alpha)) + ", " + stylise(STYLE_NUMBER, formatter.format(r)) + ", " + stylise(STYLE_NUMBER, formatter.format(g)) + ", " + stylise(STYLE_NUMBER, formatter.format(b)) + ")";
		}
	}
	
	
	// Return the html for a particular string, coloured by the type it represents
	public static String stylise(String style, String text) {
		return "<span class=\"" + style + "\">" + text + "</span>";
	}
	
	public static String stylise(String style, Integer num) {
		return stylise(style, num.toString());
	}
	
	
	/*
	public static String xmlToTouchDevelop(String input) throws XmlPullParserException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(new StringReader(input));
		int imageNum = 0; // the number of images on the page (for variable naming)
		
		String output = "action main()";
		output += "var page := media &rarr; create picture(480, 600)<br/>";

		while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
			switch(parser.getEventType()) {			
			case XmlPullParser.START_TAG:
				String type = parser.getAttributeValue(null, "type");
				if(type == null) {
					output += "// " + parser.getName() + " tag" + " <br/>";
					for(int i=0; i<parser.getAttributeCount(); i++) {
						output += "// - " + parser.getAttributeName(i) + ": " + parser.getAttributeValue(i);
					}
					break;
				}
				if(type.equals("circle")) {
					// Generate TouchDevelop code for circle
					output += "page &rarr; fill ellipse (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "backgroundColor"), 0.8)
							+ ")<br/>";
					output += "page &rarr; draw ellipse (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", " + parser.getAttributeValue(null, "radius")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "borderColor"), 0.8)
							+ ", " + parser.getAttributeValue(null, "borderThickness")
							+ ")<br/>";

				} else if(type.equals("rect")) {
					// Generate TouchDevelop code for rectangle
					output += "page &rarr; fill rect (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "width")
							+ ", " + parser.getAttributeValue(null, "height")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "backgroundColor"), 0.8)
							+ ")<br/>";
					output += "page &rarr; draw rect (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", " + parser.getAttributeValue(null, "width")
							+ ", " + parser.getAttributeValue(null, "height")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "borderColor"), 0.8)
							+ ", " + parser.getAttributeValue(null, "borderThickness")
							+ ")<br/>";

				}  else if(type.equals("text")) {
					// Generate TouchDevelop code for text
					output += "page &rarr; draw text (" + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", \"< PUT TEXT HERE >\""
							+ ", " + parser.getAttributeValue(null, "fontSize")
							+ ", 0"
							+ ", " + colorToTouchDevelop(parser.getAttributeValue(null, "fontColor"), 0.8)
							+ "))<br/>";
				} else if(type.equals("image")) {
					// Generate TouchDevelop code for image
					imageNum ++;
					output += "var image" + imageNum + " := web &rarr; download picture (\"<URL>\")<br/>";
					output += "image" + imageNum + " &rarr; resize(" + parser.getAttributeValue(null, "width")
							+ ", " + parser.getAttributeValue(null, "height")
							+ ")<br/>";
					output += "page &rarr; blend (image" + imageNum
							+ ", " + parser.getAttributeValue(null, "x")
							+ ", " + parser.getAttributeValue(null, "y")
							+ ", 0"
							+ ", 0"
							+ ")<br/>";
				} else {
					output += "// " + type + " <br/>";
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

		output += "page &rarr; post to wall";
		return output;
	}
*/
	
	/*
	public static String xmlToHtml(String input) throws XmlPullParserException, IOException {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(new StringReader(input));

		String output = "<html><br/><body><br/>";

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
					output += "<!--" + parser.getName() + " tag -&rarr;<br/>";
				}
				break;

			case XmlPullParser.END_TAG:
				break;
			}

			parser.next();

		}

		output += "</body><br/></html>";
		return output;
	}*/










}
