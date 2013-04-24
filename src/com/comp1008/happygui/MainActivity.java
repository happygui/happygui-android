package com.comp1008.happygui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class MainActivity extends Activity {
	private WebView webView;
	private JSObject androidJS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash); //Display the splash screen while waiting for the editor to load
		createEditor();
	}

	
	//Loads the editor into a WebView
	private void createEditor()  {
		webView = new WebView(this);
		webView.setWebViewClient(new HappyGUIWebViewClient());
		webView.setWebChromeClient(new WebChromeClient() {
			public boolean onConsoleMessage(ConsoleMessage cm) {
				Log.d("Error in WebView!", cm.message() + " -- From line "
						+ cm.lineNumber() + " of "
						+ cm.sourceId() );
				return true;
			}
		});
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("file:///android_asset/html5/editor.html");
		androidJS = new JSObject(webView);
		webView.addJavascriptInterface(androidJS, "jsObject"); // Facilitates communication between Javascript and Java 
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (resultCode == RESULT_OK) {
			if (requestCode == androidJS.CAMERA_PICTURE_INTENT) {
				// User has taken a camera picture
				Log.d("Activity Result", "Got camera picture!");
				
				addImage(androidJS.cameraFile);
			} else if(requestCode == androidJS.GALLERY_IMAGE_INTENT) {
				// User has selected a picture from gallery
				Log.d("Activity Result", "Got gallery image: " + intent.getDataString());
				
				// Get the actual file path for the gallery image
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(intent.getData(), filePathColumn, null, null, null);
				cursor.moveToFirst();
				String picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
				cursor.close();

				addImage(new File(picturePath));
			}
		} else if(requestCode == androidJS.CAMERA_PICTURE_INTENT || requestCode == androidJS.GALLERY_IMAGE_INTENT) {
			// Retrieval of an image has failed, or been cancelled
			Log.d("Activity Result", "Camera/Gallery image selection failed.");
			androidJS.callbackError();
		}
	}


	/* Copy imageFile to the application's local storage, then send the URI to the editor. The image
	 * is copied so that it will be available to the application even if the user deletes the original.	 */
	private void addImage(File imageFile) {
		Log.d("addImage", "Adding image from:" + imageFile.toString());

		String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
		//Create a new file in "/images/image_<TIMESTAMP>.png" to hold the image
		File newFile = new File(webView.getContext().getDir("images", Context.MODE_PRIVATE), "image_" + timeStamp + ".png");
		
		
		FileChannel src;
		FileChannel dst;
		
		try {
			// Copy the image to the new location
			src = new FileInputStream(imageFile).getChannel();
			dst = new FileOutputStream(newFile).getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();

			Log.d("addImage", "Image copied to:" + newFile.toString());
			androidJS.gotImage("file://" + newFile.toString());
		} catch (Exception e) {
			Log.d("addImage", "Failed to copy image.");
			androidJS.callbackError();
		}
	}
}
