package com.comp1008.happygui;

public class TranslationException extends Exception {
	private int language;
	private String message;
	
	public TranslationException(int language, String message) {
		this.language = language;
		if(message == null) {
			this.message = "";
		} else {
			this.message = message;
		}
	}
	
	@Override
	public String getMessage() {
		switch(language) {
		case Translator.FORMAT_JSON:
			return "Could not parse JSON string: " + message;
			
		case Translator.FORMAT_XML:
			return "Could not parse XML string:" + message;
			
		default:
			return "";
		}
	}
}
