package com.comp1008.happygui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

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

public class JSObject {
	public final int CAMERA_PICTURE_INTENT = 1;
	public final int GALLERY_IMAGE_INTENT = 2;
	
	public File cameraFile;
	
	private String callback;
	private WebView webView;
	private Activity activity;
	
	public JSObject(Activity activity, WebView webView) {
		this.activity = activity;
		this.webView = webView;
	}

	public void callJavascriptFunction(String function, Object... args) {
		log("Calling Javascript Function: " + function);
		if(function != "") {
			String url = "javascript:" + function + "(";
			for (int i = 0; i < args.length; i++) {
				if (i > 0)
					url += ",";
				url += toJavascriptString(args[i]); 
			}
			url += ");";
			log("Calling Javascript Function: " + url);
			webView.loadUrl(url);
		}
	}

	public String toJavascriptString(Object o) {
		String out;
		if(o instanceof String) {
			out = "'" + ((String)o).replaceAll("\"", "\\\"") + "'"; 
		} else if(o instanceof Object[]){
			out = "[";
			for (int i = 0; i < ((Object[])o).length; i++) {
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
	
	@JavascriptInterface
	public void log(String msg) {
		Log.d("Javascript", msg);
	}

	@JavascriptInterface
	public void setObject(String name, String json) {
		setObject(name, json, "");
	}
	
	@JavascriptInterface
	public void setObject(String name, String json, String callback) {
		String contents;
		try {
			contents = json; 
			System.out.println(contents);
		} catch (Exception e) {
			log("Could not translate JSON");
			callJavascriptFunction(callback, false);
			return;
		}
		
		
		File file = new File(activity.getDir("pages", Context.MODE_PRIVATE), name);

		try {
			FileWriter writer = new FileWriter(file);				
			writer.write(contents);
			writer.close();
			callJavascriptFunction(callback, true);
		} catch (IOException e) {
			log("Could not save file");
			callJavascriptFunction(callback, false);
		}
	}

	@JavascriptInterface
	public void getObjects(String callback) {
		File folder = activity.getDir("pages", Context.MODE_PRIVATE);
		File[] list = folder.listFiles();
		String[] names = new String[list.length];
		for (int i = 0; i < list.length; i++) {
			names[i] = list[i].getName();
		}
		callJavascriptFunction(callback, new Object[]{names});
	}
	
	@JavascriptInterface
	public void getObject(String name, String callback) {
		File file = new File(activity.getDir("pages", Context.MODE_PRIVATE), name);
		String text;
		try {
			text = new Scanner(file).useDelimiter("\\A").next();
			System.out.println(text);
		} catch (FileNotFoundException e) {
			log("File not found!");
			callJavascriptFunction(callback, false);
			return;
		}
	
		try {
			String json = text;
			System.out.println(json);
			callJavascriptFunction(callback, json);
		} catch (Exception e) {
			log("Could not translate");
			callJavascriptFunction(callback, false);
		}
	}
	
	@JavascriptInterface
	public void translateToHTML(String json, String callback) {
		try {
			String output = Translator.translate(json, Translator.FORMAT_JSON, Translator.FORMAT_HTML);
			callJavascriptFunction(callback, output);
		} catch (Exception e) {
			log("Could not translate");
			callJavascriptFunction(callback, false);
		}
	}
	
	@JavascriptInterface
	public void translateToTouchDevelop(String json, String callback) {
		try {
			String output = Translator.translate(json, Translator.FORMAT_JSON, Translator.FORMAT_TOUCHDEVELOP);
			callJavascriptFunction(callback, output);
		} catch (Exception e) {
			log("Could not translate");
			callJavascriptFunction(callback, false);
		}
	}

	@JavascriptInterface
	public void getPhoto(String callback) { // Take a photo and send the
											// image back to javascript
		this.callback = callback;
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		cameraFile = new File(Environment.getExternalStorageDirectory(),
				"HappyGUI_capture_" + timeStamp + ".png");

		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(cameraFile));
		activity.startActivityForResult(cameraIntent, CAMERA_PICTURE_INTENT);
	}
	
	@JavascriptInterface
	public void getGalleryImage(String callback) {
		this.callback = callback;
		log("getGalleryImage");
		Intent gallIntent=new Intent(Intent.ACTION_GET_CONTENT);
		gallIntent.setType("image/*"); 
		activity.startActivityForResult(gallIntent, GALLERY_IMAGE_INTENT);
	}

	public void addImage(File imageFile) { // Copies imageFile to local
											// directory, then passes the
											// new file URI to javascript
											// addImage
		Log.d("JSObject", "Adding image from:" + imageFile.toString());

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File newFile = new File(activity.getDir("images", Context.MODE_PRIVATE), "image_"
				+ timeStamp + ".png"); // replace filename with something
										// more descriptive later.
		FileChannel src;
		FileChannel dst;
		try {
			src = new FileInputStream(imageFile).getChannel();
			dst = new FileOutputStream(newFile).getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();
			
			Log.d("JSObject", "Image copied to:" + newFile.toString());
			callJavascriptFunction(callback, "file://" + newFile.toString());
		} catch (Exception e) {
			Log.d("addPicture", "Error copying picture.");
		}
	}
}