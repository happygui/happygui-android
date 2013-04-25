package com.comp1008.happygui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

// Helper class for manipulating files on the device
public class FileHandler {
	private static Context context;
	
	public static void setContext(Context c) {
		context = c;
	}
	
	
	// Save data to pages/fileName 
	public static void saveData(String fileName, String data) throws IOException {
		String contents = data;

		File file = new File(context.getDir("pages", Context.MODE_PRIVATE), fileName);
		
		FileWriter writer = new FileWriter(file);				
		writer.write(contents);
		writer.close();
	}


	// Get a directory listing of /pages
	public static String[] getPageList() {
		File folder = context.getDir("pages", Context.MODE_PRIVATE);
		File[] list = folder.listFiles();
		String[] names = new String[list.length];
		for (int i = 0; i < list.length; i++) {
			names[i] = list[i].getName();
		}
		return names;
	}
	
	// Return the contents of pages/fileName
	public static String getData(String fileName) throws FileNotFoundException {
		File file = new File(context.getDir("pages", Context.MODE_PRIVATE), fileName);
		String data = new Scanner(file).useDelimiter("\\A").next();
		return data;
	}
	
	// Delete the contents of a file (and return whether the deletion was successful)
	public static boolean deleteFile(String src) {
		if(src.substring(0, 7).equals("file://")) {
			src = src.substring(7);
		}
		
		File file = new File(src.substring(7));
		return(file.delete());
	}
	
	
	// Copy an image to application's private directory, and return the file
	public static File copyImage(File imageFile) throws IOException {
		String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
		
		//Create a new file in "/images/image_<TIMESTAMP>.png" to hold the image
		File newFile = new File(context.getDir("images", Context.MODE_PRIVATE), "image_" + timeStamp + ".png");
		
		FileChannel src;
		FileChannel dst;
		
		// Copy the image to the new location
		src = new FileInputStream(imageFile).getChannel();
		dst = new FileOutputStream(newFile).getChannel();
		dst.transferFrom(src, 0, src.size());
		src.close();
		dst.close();

		return newFile;
	}
}
