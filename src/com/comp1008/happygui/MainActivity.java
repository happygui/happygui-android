package com.comp1008.happygui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.comp1008.happygui.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private final int CAMERA_PICTURE_INTENT = 1;

	private WebView webView;
	private File cameraFile;
	public JSObject androidJS;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		webView = new WebView(this);
		webView.setWebViewClient(new MyWebViewClient(this));
		//setContentView(webView);
		try {
			loadEditor();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadEditor() throws IOException { // loads the editor into the
													// webview
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("file:///android_asset/html5/editor.html");
		androidJS = new JSObject(this, webView);
		webView.addJavascriptInterface(androidJS, "jsObject");
	}

	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		if (resultCode == RESULT_OK) {
			if (requestCode == CAMERA_PICTURE_INTENT) {
				Log.d("onActivityResult", "Activity Result OK!");
				androidJS.addImage(cameraFile);
			}
		}
	}

}
