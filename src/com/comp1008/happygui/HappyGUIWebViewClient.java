package com.comp1008.happygui;

import android.app.Activity;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class HappyGUIWebViewClient extends WebViewClient {
	private boolean loadedEditor = false;

	@Override
	public void onPageFinished(WebView view, String url) {
		// When the WebView has loaded, display it instead of the splash screen
		super.onPageFinished(view, url);
		if(!loadedEditor) {
			Log.d("MyWebViewClient", "Finished loading page: " + url);
			loadedEditor = true;
			((Activity)view.getContext()).setContentView(view);
		}
	}
}
