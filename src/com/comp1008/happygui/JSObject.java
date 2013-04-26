package com.comp1008.happygui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

/* This class is used to handle communication between javascript and java.
   Methods annotated with @JavascriptInterface are callable from javascript within the WebView */

public class JSObject {
	public final int CAMERA_PICTURE_INTENT = 1;
	public final int GALLERY_IMAGE_INTENT = 2;
	
	public File cameraFile; // File where camera intent saves a photograph
	
	private String callback = ""; // Javascript function to call with any returned data. Emptry string means no callback.
	private WebView webView;
	
	public JSObject(WebView webView) {
		this.webView = webView;
	}
	
	
	
	

	// ----------- FUNCTIONS CALLABLE FROM JAVASCRIPT -----------------
	
	@JavascriptInterface
	public void log(String msg) {
		Log.d("JSObject", msg);
	}	
	
	
	// Save JSON to file "/pages/<name>"
	@JavascriptInterface
	public void setObject(String name, String json, String callback) {
		Log.d("setObject", json);
		this.callback = callback;
		
		String contents = json;
		/*try {
			contents = Translator.translate(json, Translator.FORMAT_JSON, Translator.FORMAT_XML);
		} catch (Exception e) {
			log("Could not translate JSON");
		2	callbackError();
			return;
		}*/
		
		try {
			FileHandler.saveData(name, contents);
			callJavascriptFunction(callback, true);
		} catch (IOException e) {
			Log.d("setObject", "Could not save file");
			callbackError();
		}
	}


	@JavascriptInterface
	public void setObject(String name, String json) {
		// no callback
		setObject(name, json, "");
	}
	
	
	
	// Call callback with a JSON-formatted directory listing of /pages 
	@JavascriptInterface
	public void getObjects(String callback) {
		String[] pages = FileHandler.getPageList();
		callJavascriptFunction(callback, new Object[]{pages});
	}
	
	
	// Call callback with the contents of file "/pages/<name>"
	@JavascriptInterface
	public void getObject(String name, String callback) {
		this.callback = callback;

		try {
			String text = FileHandler.getData(name); //Read the file as a string
			callJavascriptFunction(callback, text);
		} catch (FileNotFoundException e) {
			Log.d("getObject", "File not found!");
			callbackError();
			return;
		}
			
		/*
		try {
			String json = Translator.translate(text, Translator.FORMAT_XML, Translator.FORMAT_JSON);
			System.out.println(json);
			callJavascriptFunction(callback, json);
		} catch (TranslationException e) {
			log("Could not translate");
			callbackError();
		}*/
	}

	
	
	// Once an image has been removed from the editor, delete the local file.
	@JavascriptInterface
	public void delImage(String src, String callback) {
		Log.d("delImage", "delete image: " + src);
		this.callback = callback;
		
		if(FileHandler.deleteFile(src)) {
			callJavascriptFunction(callback, true);
		} else {
			Log.d("delImage", "Failed to delete image file: " + src);
			callbackError();
		}
	}
	
	@JavascriptInterface
	public void delImage(String src) {
		//no callback)
		delImage(src, "");
	}

	
	// Get the TouchDevelop translation for a particular page 
	@JavascriptInterface
	public void getTouchDevelop(String pageIDString, String callback) {
		int pageID = Integer.parseInt(pageIDString);
		Log.d("getTouchDevelop", "Getting TouchDevelop code for page " + pageID);
		File file = new File(webView.getContext().getDir("pages", Context.MODE_PRIVATE), "happy");
		// <file> contains all of the user's pages:
		
		try {
			Scanner s = new Scanner(file).useDelimiter("\\A");
			String text = s.next(); 										// Read in the whole file as a string
			JSONArray data = new JSONArray(text); 							// Convert the file into a JSONArray of pages
			translateToTouchDevelop(data.get(pageID).toString(), callback); // Translate item #<pageId> in the array
		} catch (FileNotFoundException e) {
			Log.d("getTouchDevelop","happy file does not exist!");
			callbackError();
			return;
		} catch (JSONException e) {
			Log.d("getTouchDevelop","Could not parse happy file:");
			callbackError();
			return;
		}	
	}

	
	/*@JavascriptInterface
	public void translateToHTML(String json, String callback) {
		try {
			String output = Translator.translate(json, Translator.FORMAT_JSON, Translator.FORMAT_HTML);
			callJavascriptFunction(callback, output);
		} catch (TranslationException e) {
			log(e.getMessage());
			callbackError();
		}
	}*/
	
	
	
	// Capture a photo to import into the editor
	@JavascriptInterface
	public void getPhoto(String callback) {
		this.callback = callback;
		
		String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
		cameraFile = new File(Environment.getExternalStorageDirectory(), "HappyGUI_capture_" + timeStamp + ".png");
		// Initially saves the photo in external storage, as the user may want to keep it outside of the application.
		
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraFile));
		((Activity) webView.getContext()).startActivityForResult(cameraIntent, CAMERA_PICTURE_INTENT);
		// Start the image capture intent.
	}
	
	

	// Select a gallery image to import into the editor
	@JavascriptInterface
	public void getGalleryImage(String callback) {
		this.callback = callback;

		Intent gallIntent = new Intent(Intent.ACTION_GET_CONTENT);
		gallIntent.setType("image/*"); // only display image files
		
		((Activity)webView.getContext()).startActivityForResult(gallIntent, GALLERY_IMAGE_INTENT);
		// Start the gallery selection intent
	}
	
	
	
	
	
	
	
	
	
	
	
	// ----------- FUNCTIONS FOR SENDING DATA BACK TO JAVASCRIPT -----------------
	
	
	// Image/Camera request has succeeded. Send the location of the image back to javascript
	public void gotImage(String src) {
		callJavascriptFunction(callback, src);
	}
	
		
	// The request from javascript has failed. Run javascript:<callback>(false) 
	public void callbackError() {
		callJavascriptFunction(callback, false);
	}
	
	
	// Call a javascript function in the webview
	public void callJavascriptFunction(String function, Object... args) {
		Log.d("callJavascriptFunction","Calling: " + function);
	
		if(! function.equalsIgnoreCase("")) { // Only call if the function is not empty
			String url = "javascript:" + function + "(";
			for (int i = 0; i < args.length; i++) {
				if (i > 0)
					url += ",";
				url += toJavascriptString(args[i]); 
			}
			url += ");";
			Log.d("callJavascriptFunction","... with url: " + url);
			webView.loadUrl(url);
		}
	}

	
	// Convert an Object to a string so that it can be sent to javascript 
	private String toJavascriptString(Object o) {
		String out;
		if(o instanceof String) {
			out = "'" + ((String)o).replaceAll("\"", "\\\"") + "'"; // Escape any " marks in strings, and surround with 's 
		} else if(o instanceof Object[]){
			out = "[";
			for (int i = 0; i < ((Object[])o).length; i++) {	// Represent arrays in [1, 2, 3] format
				if (i > 0)
					out += ",";
				out += toJavascriptString(((Object[])o)[i]);
			}
			out += "]";
		} else {
			out = o.toString();
		}
		return out;
	}
	
	
	// Translate a JSON formatted string representing the page to TouchDevelop code, and send to callback. 
	public void translateToTouchDevelop(String json, String callback) {
		this.callback = callback;
		try {
			String output = Translator.translate(json, Translator.FORMAT_JSON, Translator.FORMAT_TOUCHDEVELOP);
			Log.d("translateToTouchDevelop", "Translated to touch develop:\n" + output);
			callJavascriptFunction(callback, output);
		} catch (TranslationException e) {
			Log.d("translateToTouchDevelop", e.getMessage());
			callbackError();
		}
	}

}