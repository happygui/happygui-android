package com.comp1008.happygui;

public class TranslationException extends Exception {
	private int language;
	
	public TranslationException(int language) {
		this.language = language;
	}
	
	@Override
	public String getMessage() {
		switch(language) {
		case Translator.FORMAT_JSON:
			return "Could not parse JSON string";
			
		case Translator.FORMAT_XML:
			return "Could not parse XML string";
			
		default:
			return "";
		}
	}
}
